from bs4 import BeautifulSoup
from client import SafeHttpClient, UpstreamNotFoundError
from scrapers.anime_scraper import extract_id_and_slug


class CharacterScraper:
    """Scraper for OtakOtaku Character module."""

    def __init__(self, client: SafeHttpClient):
        self.client = client

    async def get_character_detail(self, char_id: int, slug: str = "") -> dict:
        """Fetch character biography, voice actors, appearances, and quotes."""
        target_path = f"/character/view/{char_id}/{slug}".rstrip("/")
        html = await self.client.fetch_html(target_path)
        soup = BeautifulSoup(html, "html.parser")

        # Metadata table
        metadata = {}
        for tr in soup.select(".otaku-info table tr, table tr"):
            tds = [td.get_text(strip=True) for td in tr.find_all(["th", "td"])]
            if len(tds) >= 2:
                metadata[tds[0].lower()] = tds[1]

        name = metadata.get("nama", "")
        if not name:
            h3 = soup.select_one("h3, h1")
            name = h3.get_text(strip=True) if h3 else f"Character {char_id}"

        # Image
        img_el = soup.select_one(".cover-content img, .main-content img, .char-img img")
        image_url = ""
        if img_el:
            image_url = img_el.get("src") or img_el.get("data-src") or ""
            if "spin-black" in image_url or image_url.endswith(".svg"):
                image_url = ""

        # Sections
        description = ""
        seiyuu_list = []
        anime_appearances = []

        for h4 in soup.find_all("h4", class_="content-header"):
            title_text = h4.get_text(strip=True).lower()
            sibling = h4.find_next_sibling()
            if not sibling:
                continue

            if "deskripsi" in title_text:
                desc_raw = sibling.get_text(separator="\n", strip=True)
                # Remove edit button text if any
                desc_lines = [line.strip() for line in desc_raw.split("\n") if line.strip() and "edit" != line.lower()]
                description = "\n\n".join(desc_lines)
            elif "pengisi suara" in title_text or "seiyu" in title_text:
                for a in sibling.select('a[href*="/people/view/"]'):
                    s_href = a.get("href", "")
                    s_name = a.get_text(strip=True)
                    s_id, s_slug = extract_id_and_slug(s_href, "people/view")
                    if s_name:
                        seiyuu_list.append({
                            "id": s_id,
                            "slug": s_slug,
                            "name": s_name,
                            "url": s_href,
                        })
            elif "daftar anime" in title_text:
                for row in sibling.select(".anime-char-list, .row, div"):
                    a_link = row.select_one('a[href*="/anime/view/"]')
                    if not a_link:
                        continue
                    a_href = a_link.get("href", "")
                    a_title = a_link.get_text(strip=True)
                    a_id, a_slug = extract_id_and_slug(a_href, "anime/view")
                    if a_title and not any(app["id"] == a_id for app in anime_appearances):
                        anime_appearances.append({
                            "id": a_id,
                            "slug": a_slug,
                            "title": a_title,
                            "url": a_href,
                        })

        return {
            "id": char_id,
            "slug": slug,
            "name": name,
            "japanese_name": metadata.get("jepang", ""),
            "alias": metadata.get("alias", ""),
            "gender": metadata.get("jenis kelamin", ""),
            "birthdate": metadata.get("tanggal lahir", ""),
            "image": image_url,
            "description": description,
            "seiyu": seiyuu_list,
            "anime_appearances": anime_appearances,
        }
