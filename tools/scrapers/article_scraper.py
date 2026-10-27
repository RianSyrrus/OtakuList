import re
from typing import Optional
from bs4 import BeautifulSoup
from client import SafeHttpClient, UpstreamNotFoundError
from scrapers.quote_scraper import parse_pagination
from scrapers.anime_scraper import extract_id_and_slug


def parse_post_card(card) -> Optional[dict]:
    """Parse article item from .post-list."""
    title_el = card.select_one(".post-info h4 a")
    if not title_el:
        return None

    title = title_el.get_text(strip=True)
    href = title_el.get("href", "")
    post_id, slug = extract_id_and_slug(href, "post/view")

    img_el = card.select_one(".post-img img, img")
    image_url = ""
    if img_el:
        image_url = img_el.get("data-src") or img_el.get("src") or ""
        if "spin-black" in image_url or image_url.endswith(".svg"):
            image_url = ""

    author_el = card.select_one(".post-info .meta")
    author = author_el.get_text(strip=True) if author_el else ""

    snippet_el = card.select_one(".post-info .post-content")
    snippet = snippet_el.get_text(strip=True) if snippet_el else ""

    return {
        "id": post_id,
        "slug": slug,
        "title": title,
        "url": href,
        "image": image_url,
        "author": author,
        "snippet": snippet,
    }


class ArticleScraper:
    """Scraper for OtakOtaku Articles / News module."""

    def __init__(self, client: SafeHttpClient):
        self.client = client

    async def get_article_feed(self, page: int = 1) -> dict:
        """Fetch latest article feed."""
        target_path = f"/post/feed/{page}" if page > 1 else "/post/feed"
        html = await self.client.fetch_html(target_path)
        soup = BeautifulSoup(html, "html.parser")

        articles = []
        for card in soup.select(".post-list"):
            item = parse_post_card(card)
            if item:
                articles.append(item)

        pagination = parse_pagination(soup)
        pagination["current_page"] = page

        return {
            "page": page,
            "total_items": len(articles),
            "pagination": pagination,
            "articles": articles,
        }

    async def get_article_by_category(self, category_slug: str, page: int = 1) -> dict:
        """Fetch articles by category (e.g. anime, manga, seiyu)."""
        target_path = f"/post/category/{category_slug}/{page}" if page > 1 else f"/post/category/{category_slug}"
        html = await self.client.fetch_html(target_path)
        soup = BeautifulSoup(html, "html.parser")

        articles = []
        for card in soup.select(".post-list"):
            item = parse_post_card(card)
            if item:
                articles.append(item)

        pagination = parse_pagination(soup)
        pagination["current_page"] = page

        return {
            "category": category_slug,
            "page": page,
            "total_items": len(articles),
            "pagination": pagination,
            "articles": articles,
        }

    async def get_article_detail(self, post_id: int, slug: str = "") -> dict:
        """Fetch full article content and metadata."""
        target_path = f"/post/view/{post_id}/{slug}".rstrip("/")
        html = await self.client.fetch_html(target_path)
        soup = BeautifulSoup(html, "html.parser")

        h1 = soup.select_one("h1.headline, h1")
        if not h1:
            raise UpstreamNotFoundError(f"Article {post_id} not found")
        title = h1.get_text(strip=True)

        # Meta (author, date)
        author = ""
        date_published = ""
        meta_el = soup.select_one(".post-meta, .meta")
        if meta_el:
            author = meta_el.get_text(strip=True)

        date_el = soup.select_one(".post-date, .date, time")
        if date_el:
            date_published = date_el.get_text(strip=True)

        # Main image
        cover_el = soup.select_one(".post-cover img, .post-content img, .main-content img")
        cover_url = ""
        if cover_el:
            cover_url = cover_el.get("src") or cover_el.get("data-src") or ""
            if "spin-black" in cover_url or cover_url.endswith(".svg"):
                cover_url = ""

        # Content extraction
        content_el = soup.select_one(".post-content, .entry-content, .content-detail")
        content_paragraphs = []
        if content_el:
            for p in content_el.find_all("p"):
                txt = p.get_text(strip=True)
                if txt:
                    content_paragraphs.append(txt)

        # Tags
        tags = []
        for tag_a in soup.select('.post-tags a, a[href*="/tag/"]'):
            tag_name = tag_a.get_text(strip=True)
            if tag_name:
                tags.append(tag_name)

        return {
            "id": post_id,
            "slug": slug,
            "title": title,
            "author": author,
            "date": date_published,
            "cover": cover_url,
            "paragraphs": content_paragraphs,
            "content": "\n\n".join(content_paragraphs),
            "tags": tags,
        }
