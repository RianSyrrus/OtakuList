import re
from typing import Optional
from bs4 import BeautifulSoup
from client import SafeHttpClient, UpstreamNotFoundError


def parse_pagination(soup: BeautifulSoup) -> dict:
    """Parse standard OtakOtaku pagination bar."""
    pagination_ul = soup.select_one("ul.pagination")
    if not pagination_ul:
        return {"current_page": 1, "has_next": False, "last_page": 1}

    active_li = pagination_ul.select_one("li.active")
    current_page = int(active_li.get_text(strip=True)) if active_li and active_li.get_text(strip=True).isdigit() else 1

    last_page = current_page
    for a in pagination_ul.select("a[href]"):
        href = a.get("href", "")
        match = re.search(r"/(\d+)(?:\?.*)?$", href)
        if match:
            pg = int(match.group(1))
            if pg > last_page:
                last_page = pg

    has_next = current_page < last_page
    return {
        "current_page": current_page,
        "has_next": has_next,
        "last_page": last_page,
    }


def parse_quote_cards(soup: BeautifulSoup) -> list[dict]:
    """Extract list of quote items from container."""
    quotes = []
    items = soup.select(".kotodama-list")

    for item in items:
        link_el = item.select_one("a.kuroi") or item.select_one("a")
        if not link_el:
            continue

        href = link_el.get("href", "")
        # format: https://otakotaku.com/quote/view/{id}/{slug}
        id_slug_match = re.search(r"/quote/view/(\d+)(?:/([^/?#]+))?", href)
        quote_id = int(id_slug_match.group(1)) if id_slug_match else None
        slug = id_slug_match.group(2) if (id_slug_match and id_slug_match.group(2)) else ""

        # Character info
        char_name_el = item.select_one(".char-name")
        char_name = char_name_el.get_text(strip=True) if char_name_el else ""

        char_img_el = item.select_one(".char-img img")
        char_img = ""
        if char_img_el:
            char_img = char_img_el.get("data-src") or char_img_el.get("src") or ""

        # Anime info
        anime_title_el = item.select_one(".anime-title")
        anime_title = anime_title_el.get_text(strip=True) if anime_title_el else ""

        episode_el = item.select_one(".meta")
        episode = episode_el.get_text(strip=True) if episode_el else ""

        # Quote text
        quote_el = item.select_one(".quote")
        quote_text = quote_el.get_text(strip=True) if quote_el else ""

        quotes.append({
            "id": quote_id,
            "slug": slug,
            "url": href,
            "quote": quote_text,
            "character": {
                "name": char_name,
                "image": char_img,
            },
            "anime": {
                "title": anime_title,
                "episode": episode,
            }
        })

    return quotes


class QuoteScraper:
    """Scraper for OtakOtaku Quotes module."""

    def __init__(self, client: SafeHttpClient):
        self.client = client

    async def get_quote_feed(self, page: int = 1) -> dict:
        """Fetch latest quotes feed with pagination."""
        target_path = f"/quote/feed/{page}" if page > 1 else "/quote/feed"
        html = await self.client.fetch_html(target_path)
        soup = BeautifulSoup(html, "html.parser")

        quotes = parse_quote_cards(soup)
        pagination = parse_pagination(soup)
        pagination["current_page"] = page

        return {
            "page": page,
            "total_items": len(quotes),
            "pagination": pagination,
            "quotes": quotes,
        }

    async def get_quote_detail(self, quote_id: int, slug: str = "") -> dict:
        """Fetch full quote detail."""
        target_path = f"/quote/view/{quote_id}/{slug}".rstrip("/")
        html = await self.client.fetch_html(target_path)
        soup = BeautifulSoup(html, "html.parser")

        main = soup.select_one(".main-content, .col-md-8")
        if not main:
            raise UpstreamNotFoundError(f"Quote {quote_id} not found")

        # Quote text
        quote_el = main.find("blockquote") or main.select_one(".quote, .kotodama-quote")
        quote_text = quote_el.get_text(strip=True) if quote_el else ""
        if not quote_text:
            raise UpstreamNotFoundError(f"Quote text not found for ID {quote_id}")

        # Image
        img_el = main.select_one(".char-img img, img.img-responsive, .char-top img")
        image_url = ""
        if img_el:
            image_url = img_el.get("data-src") or img_el.get("src") or ""

        # Character Link & ID
        char_info = {"id": None, "name": "", "slug": "", "url": "", "image": image_url}
        for char_link in main.select('a[href*="/character/view/"]'):
            char_href = char_link.get("href", "")
            char_text = char_link.get_text(strip=True)
            if not char_info["url"]:
                char_info["url"] = char_href
                c_match = re.search(r"/character/view/(\d+)(?:/([^/?#]+))?", char_href)
                if c_match:
                    char_info["id"] = int(c_match.group(1))
                    char_info["slug"] = c_match.group(2) or ""
            if char_text:
                char_info["name"] = char_text
                break

        # Anime Link & ID
        anime_info = {"id": None, "title": "", "slug": "", "url": ""}
        for anime_link in main.select('a[href*="/anime/view/"]'):
            anime_href = anime_link.get("href", "")
            anime_text = anime_link.get_text(strip=True)
            if not anime_info["url"]:
                anime_info["url"] = anime_href
                a_match = re.search(r"/anime/view/(\d+)(?:/([^/?#]+))?", anime_href)
                if a_match:
                    anime_info["id"] = int(a_match.group(1))
                    anime_info["slug"] = a_match.group(2) or ""
            if anime_text:
                anime_info["title"] = anime_text
                break

        # Episode info
        episode = ""
        meta_el = main.select_one(".meta")
        if meta_el:
            episode = meta_el.get_text(strip=True)

        return {
            "id": quote_id,
            "slug": slug,
            "quote": quote_text,
            "character": char_info,
            "anime": anime_info,
            "episode": episode,
            "image": image_url,
        }

    async def search_quotes(self, query: str, page: int = 1) -> dict:
        """Search quotes by keyword or character."""
        target_path = f"/quote/search/{page}" if page > 1 else "/quote/search"
        html = await self.client.fetch_html(target_path, params={"q": query})
        soup = BeautifulSoup(html, "html.parser")

        quotes = parse_quote_cards(soup)
        pagination = parse_pagination(soup)
        pagination["current_page"] = page

        return {
            "query": query,
            "page": page,
            "total_items": len(quotes),
            "pagination": pagination,
            "quotes": quotes,
        }
