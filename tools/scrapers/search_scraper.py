from bs4 import BeautifulSoup
from client import SafeHttpClient
from scrapers.anime_scraper import extract_id_and_slug


class SearchScraper:
    """Scraper for OtakOtaku Global Search."""

    def __init__(self, client: SafeHttpClient):
        self.client = client

    async def search_global(self, query: str) -> dict:
        """Perform comprehensive search across all OtakOtaku categories."""
        html = await self.client.fetch_html("/search", params={"q": query})
        soup = BeautifulSoup(html, "html.parser")

        results = {
            "query": query,
            "anime": [],
            "characters": [],
            "articles": [],
            "quotes": [],
            "people": [],
            "anisong": [],
        }

        detail = soup.select_one(".col-md-8 .content-detail")
        if not detail:
            return results

        current_category = ""
        for child in detail.children:
            if not child.name:
                continue

            # Check if this element is a category header
            if "haiiro" in child.get("class", []):
                header_text = child.get_text(strip=True).lower()
                if "anime" in header_text and "anisong" not in header_text:
                    current_category = "anime"
                elif "karakter" in header_text:
                    current_category = "characters"
                elif "seiyu" in header_text or "staf" in header_text or "artis" in header_text:
                    current_category = "people"
                elif "artikel" in header_text:
                    current_category = "articles"
                elif "quote" in header_text:
                    current_category = "quotes"
                elif "anisong" in header_text:
                    current_category = "anisong"
                else:
                    current_category = ""
                continue

            # Parse items based on current category
            if current_category == "anime":
                for a in child.select('a[href*="/anime/view/"]'):
                    href = a.get("href", "")
                    title = a.get_text(strip=True)
                    a_id, a_slug = extract_id_and_slug(href, "anime/view")
                    img_el = a.find("img")
                    img = img_el.get("data-src") or img_el.get("src") or "" if img_el else ""
                    if title and not any(item["id"] == a_id for item in results["anime"]):
                        results["anime"].append({
                            "id": a_id,
                            "slug": a_slug,
                            "title": title,
                            "url": href,
                            "image": img,
                        })

            elif current_category == "characters":
                for a in child.select('a[href*="/character/view/"]'):
                    href = a.get("href", "")
                    name = a.get_text(strip=True)
                    c_id, c_slug = extract_id_and_slug(href, "character/view")
                    img_el = a.find("img")
                    img = img_el.get("data-src") or img_el.get("src") or "" if img_el else ""
                    if name and not any(item["id"] == c_id for item in results["characters"]):
                        results["characters"].append({
                            "id": c_id,
                            "slug": c_slug,
                            "name": name,
                            "url": href,
                            "image": img,
                        })

            elif current_category == "people":
                for a in child.select('a[href*="/people/view/"]'):
                    href = a.get("href", "")
                    name = a.get_text(strip=True)
                    p_id, p_slug = extract_id_and_slug(href, "people/view")
                    img_el = a.find("img")
                    img = img_el.get("data-src") or img_el.get("src") or "" if img_el else ""
                    if name and not any(item["id"] == p_id for item in results["people"]):
                        results["people"].append({
                            "id": p_id,
                            "slug": p_slug,
                            "name": name,
                            "url": href,
                            "image": img,
                        })

            elif current_category == "articles":
                for a in child.select('a[href*="/post/view/"]'):
                    href = a.get("href", "")
                    title = a.get_text(strip=True)
                    p_id, p_slug = extract_id_and_slug(href, "post/view")
                    img_el = a.find("img")
                    img = img_el.get("data-src") or img_el.get("src") or "" if img_el else ""
                    if title and not any(item["id"] == p_id for item in results["articles"]):
                        results["articles"].append({
                            "id": p_id,
                            "slug": p_slug,
                            "title": title,
                            "url": href,
                            "image": img,
                        })

            elif current_category == "quotes":
                if child.name == "a" and "/quote/view/" in child.get("href", ""):
                    href = child.get("href", "")
                    q_id, q_slug = extract_id_and_slug(href, "quote/view")
                    raw_text = child.get_text(separator=" | ", strip=True)
                    if not any(item["id"] == q_id for item in results["quotes"]):
                        results["quotes"].append({
                            "id": q_id,
                            "slug": q_slug,
                            "url": href,
                            "text": raw_text,
                        })

            elif current_category == "anisong":
                song_a = child.select_one('a[href*="/music/view/"]')
                if song_a:
                    href = song_a.get("href", "")
                    s_id, s_slug = extract_id_and_slug(href, "music/view")
                    title = song_a.get_text(strip=True)
                    results["anisong"].append({
                        "id": s_id,
                        "slug": s_slug,
                        "title": title,
                        "url": href,
                    })

        return results
