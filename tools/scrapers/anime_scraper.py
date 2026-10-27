import re
from typing import Optional
from bs4 import BeautifulSoup
from client import SafeHttpClient, UpstreamNotFoundError
from scrapers.quote_scraper import parse_pagination


def extract_id_and_slug(url: str, pattern_prefix: str = "anime/view") -> tuple[Optional[int], str]:
    """Extract numeric ID and slug from URL."""
    match = re.search(rf"/{pattern_prefix}/(\d+)(?:/([^/?#]+))?", url)
    if match:
        return int(match.group(1)), match.group(2) or ""
    return None, ""


def parse_feed_anime_row(row) -> Optional[dict]:
    """Parse an anime row item from feed/genre/popular."""
    title_el = row.select_one(".anime-title a")
    if not title_el:
        return None

    title = title_el.get_text(strip=True)
    href = title_el.get("href", "")
    anime_id, slug = extract_id_and_slug(href, "anime/view")

    img_el = row.select_one(".anime-img img")
    image_url = ""
    if img_el:
        image_url = img_el.get("data-src") or img_el.get("src") or ""

    synopsis_el = row.select_one(".sinopsis-anime")
    synopsis = synopsis_el.get_text(strip=True) if synopsis_el else ""

    # Parse metadata columns (col-md-3 table)
    meta_info = {}
    for tr in row.select(".col-md-3 tr, table.table tr"):
        tds = [td.get_text(strip=True) for td in tr.find_all(["th", "td"])]
        if len(tds) == 2:
            meta_info[tds[0].lower()] = tds[1]
        elif len(tds) >= 4:
            meta_info[tds[0].lower()] = tds[1]
            meta_info[tds[2].lower()] = tds[3]

    return {
        "id": anime_id,
        "slug": slug,
        "title": title,
        "url": href,
        "image": image_url,
        "synopsis": synopsis,
        "type": meta_info.get("tipe", ""),
        "episodes": meta_info.get("eps", ""),
        "season": meta_info.get("musim", ""),
        "score": meta_info.get("skor", ""),
    }


def parse_season_anime_card(card) -> Optional[dict]:
    """Parse anime card in season view (.col-anime)."""
    title_el = card.select_one(".col-anime-title a")
    if not title_el:
        return None

    title = title_el.get_text(strip=True)
    href = title_el.get("href", "")
    anime_id, slug = extract_id_and_slug(href, "anime/view")

    img_el = card.select_one(".col-anime-cover img, img")
    image_url = ""
    if img_el:
        image_url = img_el.get("data-src") or img_el.get("src") or ""

    studio_el = card.select_one(".col-anime-studio a")
    studio = studio_el.get_text(strip=True) if studio_el else ""

    eps_el = card.select_one(".col-anime-eps")
    episodes = eps_el.get_text(strip=True) if eps_el else ""

    rating_el = card.select_one(".col-anime-rating a")
    rating = rating_el.get_text(strip=True) if rating_el else ""

    return {
        "id": anime_id,
        "slug": slug,
        "title": title,
        "url": href,
        "image": image_url,
        "studio": studio,
        "episodes": episodes,
        "rating": rating,
    }


class AnimeScraper:
    """Scraper for OtakOtaku Anime Database module."""

    def __init__(self, client: SafeHttpClient):
        self.client = client

    async def get_anime_feed(self, page: int = 1) -> dict:
        """Fetch latest anime releases feed."""
        target_path = f"/anime/feed/{page}" if page > 1 else "/anime/feed"
        html = await self.client.fetch_html(target_path)
        soup = BeautifulSoup(html, "html.parser")

        anime_list = []
        for row in soup.select(".row"):
            if row.select_one(".anime-title"):
                item = parse_feed_anime_row(row)
                if item:
                    anime_list.append(item)

        pagination = parse_pagination(soup)
        pagination["current_page"] = page

        return {
            "page": page,
            "total_items": len(anime_list),
            "pagination": pagination,
            "anime": anime_list,
        }

    async def get_anime_detail(self, anime_id: int, slug: str = "") -> dict:
        """Fetch full anime details (synopsis, metadata, characters, OP/ED, trailers)."""
        target_path = f"/anime/view/{anime_id}/{slug}".rstrip("/")
        html = await self.client.fetch_html(target_path)
        soup = BeautifulSoup(html, "html.parser")

        # Cover image
        cover_el = soup.select_one(".cover-content img, .main-content img")
        cover_url = ""
        if cover_el:
            cover_url = cover_el.get("src") or cover_el.get("data-src") or ""

        # Parse Metadata Table
        metadata = {}
        for tr in soup.select(".otaku-info table tr"):
            tds = [td.get_text(strip=True) for td in tr.find_all(["th", "td"])]
            if len(tds) >= 2:
                key = tds[0].lower()
                metadata[key] = tds[1]

        title = metadata.get("judul", "")
        if not title:
            # Fallback title
            h1 = soup.select_one("h1.headline, h1")
            title = h1.get_text(strip=True) if h1 else f"Anime {anime_id}"

        # Parse genres as structured list
        genre_list = []
        genre_links = soup.select('.otaku-info a[href*="/anime/genre/"]')
        for g in genre_links:
            g_href = g.get("href", "")
            g_slug = g_href.split("/anime/genre/")[-1].strip("/")
            genre_list.append({
                "name": g.get_text(strip=True),
                "slug": g_slug,
                "url": g_href,
            })

        # Parse Studio & Producers
        studios = []
        for s in soup.select('.otaku-info a[href*="/anime/producer/"]'):
            s_href = s.get("href", "")
            studios.append({
                "name": s.get_text(strip=True),
                "url": s_href,
            })

        # Synopsis & Section Content
        synopsis = ""
        trailers = []
        characters = []
        opening_songs = []
        ending_songs = []
        related_anime = []

        for h4 in soup.find_all("h4", class_="content-header"):
            header_text = h4.get_text(strip=True).lower()
            sibling = h4.find_next_sibling()
            if not sibling:
                continue

            if "sinopsis" in header_text:
                synopsis = sibling.get_text(separator="\n", strip=True)
            elif "trailer" in header_text or "pv" in header_text:
                for a in sibling.select("a[href], iframe[src]"):
                    t_url = a.get("href") or a.get("src") or ""
                    t_name = a.get_text(strip=True) or "Trailer"
                    if t_url and not t_url.startswith("#"):
                        trailers.append({"title": t_name, "url": t_url})
            elif "anime terkait" in header_text:
                curr = sibling
                while curr and curr.name != "h4":
                    if curr.find(class_="anime-related"):
                        type_span = curr.find("span", class_="anime-related")
                        current_type = type_span.get_text(strip=True) if type_span else "Anime"
                        for a in curr.find_all("a", class_="anime-related"):
                            if "active" in a.get("class", []):
                                continue
                            href = a.get("href", "")
                            t_title = a.get_text(strip=True)
                            parts = href.split("/anime/view/")[-1].strip("/").split("/")
                            if parts and parts[0].isdigit():
                                related_anime.append({
                                    "id": int(parts[0]),
                                    "title": t_title,
                                    "type": current_type,
                                })
                    curr = curr.find_next_sibling()
            elif "karakter" in header_text:
                for c_box in sibling.select(".char-info"):
                    c_link = c_box.select_one(".char-name a")
                    if not c_link:
                        continue
                    c_name = c_link.get_text(strip=True)
                    c_href = c_link.get("href", "")
                    c_id, c_slug = extract_id_and_slug(c_href, "character/view")

                    c_img_el = c_box.select_one(".char-img img")
                    c_img = c_img_el.get("data-src") or c_img_el.get("src") or "" if c_img_el else ""

                    c_role_el = c_box.select_one(".char-jenis-karakter")
                    c_role = c_role_el.get_text(strip=True) if c_role_el else ""

                    # Seiyu
                    seiyu_link = c_box.select_one(".char-seiyuu-list a")
                    seiyu_info = None
                    if seiyu_link:
                        s_href = seiyu_link.get("href", "")
                        s_id, s_slug = extract_id_and_slug(s_href, "people/view")
                        seiyu_info = {
                            "id": s_id,
                            "slug": s_slug,
                            "name": seiyu_link.get_text(strip=True),
                            "url": s_href,
                        }

                    characters.append({
                        "id": c_id,
                        "slug": c_slug,
                        "name": c_name,
                        "role": c_role,
                        "image": c_img,
                        "url": c_href,
                        "seiyu": seiyu_info,
                    })
            elif "pembuka" in header_text or "(op)" in header_text:
                op_text = sibling.get_text(separator="\n", strip=True)
                if op_text and "belum tersedia" not in op_text.lower():
                    opening_songs = [s.strip() for s in op_text.split("\n") if s.strip()]
            elif "penutup" in header_text or "(ed)" in header_text:
                ed_text = sibling.get_text(separator="\n", strip=True)
                if ed_text and "belum tersedia" not in ed_text.lower():
                    ending_songs = [s.strip() for s in ed_text.split("\n") if s.strip()]

        return {
            "id": anime_id,
            "slug": slug,
            "title": title,
            "japanese_title": metadata.get("jepang", ""),
            "english_title": metadata.get("inggris", ""),
            "alternate_titles": metadata.get("lainnya", ""),
            "type": metadata.get("tipe", ""),
            "status": metadata.get("status", ""),
            "season": metadata.get("musim perdana", ""),
            "aired": metadata.get("tayang", ""),
            "total_episodes": metadata.get("total episode", ""),
            "duration": metadata.get("durasi", ""),
            "rating": metadata.get("rating", ""),
            "score": metadata.get("skor", ""),
            "cover": cover_url,
            "genres": genre_list,
            "studios": studios,
            "synopsis": synopsis,
            "characters": characters,
            "theme_songs": {
                "opening": opening_songs,
                "ending": ending_songs,
            },
            "trailers": trailers,
            "related_anime": related_anime,
        }

    async def get_anime_popular(self) -> dict:
        """Fetch currently popular anime list."""
        html = await self.client.fetch_html("/anime/terpopuler")
        soup = BeautifulSoup(html, "html.parser")

        anime_list = []
        for row in soup.select(".row"):
            if row.select_one(".anime-title"):
                item = parse_feed_anime_row(row)
                if item:
                    anime_list.append(item)

        return {
            "total_items": len(anime_list),
            "anime": anime_list,
        }

    async def get_anime_season(self, season_slug: str) -> dict:
        """Fetch anime list for a specific season (e.g. summer-2024, fall-2026, later)."""
        target_path = f"/anime/season/{season_slug}"
        html = await self.client.fetch_html(target_path)
        soup = BeautifulSoup(html, "html.parser")

        anime_list = []
        cards = soup.select(".col-anime")
        for card in cards:
            item = parse_season_anime_card(card)
            if item:
                anime_list.append(item)

        # If col-anime wasn't present, check feed style rows
        if not anime_list:
            for row in soup.select(".row"):
                if row.select_one(".anime-title"):
                    item = parse_feed_anime_row(row)
                    if item:
                        anime_list.append(item)

        return {
            "season": season_slug,
            "total_items": len(anime_list),
            "anime": anime_list,
        }

    async def get_anime_genres(self) -> list[dict]:
        """Fetch master list of all available anime genres from navigation."""
        html = await self.client.fetch_html("/anime/genre/action")
        soup = BeautifulSoup(html, "html.parser")

        genres = []
        for a in soup.select("#menu_genre a"):
            name = a.get_text(strip=True)
            href = a.get("href", "")
            slug = href.split("/anime/genre/")[-1].strip("/")
            if slug:
                genres.append({"name": name, "slug": slug, "url": href})
        return genres

    async def get_anime_by_genre(self, genre_slug: str, page: int = 1) -> dict:
        """Fetch anime list filtered by genre with pagination."""
        target_path = f"/anime/genre/{genre_slug}/{page}" if page > 1 else f"/anime/genre/{genre_slug}"
        html = await self.client.fetch_html(target_path)
        soup = BeautifulSoup(html, "html.parser")

        anime_list = []
        for row in soup.select(".row"):
            if row.select_one(".anime-title"):
                item = parse_feed_anime_row(row)
                if item:
                    anime_list.append(item)

        pagination = parse_pagination(soup)
        pagination["current_page"] = page

        return {
            "genre": genre_slug,
            "page": page,
            "total_items": len(anime_list),
            "pagination": pagination,
            "anime": anime_list,
        }
