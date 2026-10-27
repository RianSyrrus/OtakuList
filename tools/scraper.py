from typing import Any, Optional, Tuple
from cache import TTLCache
from client import SafeHttpClient
from scrapers.quote_scraper import QuoteScraper
from scrapers.anime_scraper import AnimeScraper
from scrapers.article_scraper import ArticleScraper
from scrapers.character_scraper import CharacterScraper
from scrapers.search_scraper import SearchScraper


class OtakOtakuScraper:
    """Master Scraper Facade integrating all modules with caching."""

    def __init__(
        self,
        client: Optional[SafeHttpClient] = None,
        cache: Optional[TTLCache] = None,
        cache_ttl: int = 300,
    ):
        self.client = client or SafeHttpClient()
        self.cache = cache or TTLCache(default_ttl=cache_ttl)
        self.cache_ttl = cache_ttl

        self.quote_scraper = QuoteScraper(self.client)
        self.anime_scraper = AnimeScraper(self.client)
        self.article_scraper = ArticleScraper(self.client)
        self.char_scraper = CharacterScraper(self.client)
        self.search_scraper = SearchScraper(self.client)

    async def close(self) -> None:
        await self.client.close()

    async def _cached_call(self, cache_key: str, fetch_func, refresh: bool = False) -> Tuple[Any, bool]:
        if not refresh:
            cached_val = self.cache.get(cache_key)
            if cached_val is not None:
                return cached_val, True

        fresh_val = await fetch_func()
        self.cache.set(cache_key, fresh_val, ttl=self.cache_ttl)
        return fresh_val, False

    # --- Quotes ---
    async def get_quote_feed(self, page: int = 1, refresh: bool = False) -> Tuple[dict, bool]:
        return await self._cached_call(f"quote:feed:{page}", lambda: self.quote_scraper.get_quote_feed(page), refresh)

    async def get_quote_detail(self, quote_id: int, slug: str = "", refresh: bool = False) -> Tuple[dict, bool]:
        return await self._cached_call(f"quote:detail:{quote_id}", lambda: self.quote_scraper.get_quote_detail(quote_id, slug), refresh)

    async def search_quotes(self, query: str, page: int = 1, refresh: bool = False) -> Tuple[dict, bool]:
        return await self._cached_call(f"quote:search:{query}:{page}", lambda: self.quote_scraper.search_quotes(query, page), refresh)

    # --- Anime ---
    async def get_anime_feed(self, page: int = 1, refresh: bool = False) -> Tuple[dict, bool]:
        return await self._cached_call(f"anime:feed:{page}", lambda: self.anime_scraper.get_anime_feed(page), refresh)

    async def get_anime_detail(self, anime_id: int, slug: str = "", refresh: bool = False) -> Tuple[dict, bool]:
        return await self._cached_call(f"anime:detail:{anime_id}", lambda: self.anime_scraper.get_anime_detail(anime_id, slug), refresh)

    async def get_anime_popular(self, refresh: bool = False) -> Tuple[dict, bool]:
        return await self._cached_call("anime:popular", lambda: self.anime_scraper.get_anime_popular(), refresh)

    async def get_anime_season(self, season_slug: str, refresh: bool = False) -> Tuple[dict, bool]:
        return await self._cached_call(f"anime:season:{season_slug}", lambda: self.anime_scraper.get_anime_season(season_slug), refresh)

    async def get_anime_genres(self, refresh: bool = False) -> Tuple[list[dict], bool]:
        return await self._cached_call("anime:genres", lambda: self.anime_scraper.get_anime_genres(), refresh)

    async def get_anime_by_genre(self, genre_slug: str, page: int = 1, refresh: bool = False) -> Tuple[dict, bool]:
        return await self._cached_call(f"anime:genre:{genre_slug}:{page}", lambda: self.anime_scraper.get_anime_by_genre(genre_slug, page), refresh)

    # --- Articles ---
    async def get_article_feed(self, page: int = 1, refresh: bool = False) -> Tuple[dict, bool]:
        return await self._cached_call(f"article:feed:{page}", lambda: self.article_scraper.get_article_feed(page), refresh)

    async def get_article_by_category(self, category_slug: str, page: int = 1, refresh: bool = False) -> Tuple[dict, bool]:
        return await self._cached_call(f"article:cat:{category_slug}:{page}", lambda: self.article_scraper.get_article_by_category(category_slug, page), refresh)

    async def get_article_detail(self, post_id: int, slug: str = "", refresh: bool = False) -> Tuple[dict, bool]:
        return await self._cached_call(f"article:detail:{post_id}", lambda: self.article_scraper.get_article_detail(post_id, slug), refresh)

    # --- Character ---
    async def get_character_detail(self, char_id: int, slug: str = "", refresh: bool = False) -> Tuple[dict, bool]:
        return await self._cached_call(f"char:detail:{char_id}", lambda: self.char_scraper.get_character_detail(char_id, slug), refresh)

    # --- Global Search ---
    async def search_global(self, query: str, refresh: bool = False) -> Tuple[dict, bool]:
        return await self._cached_call(f"search:global:{query}", lambda: self.search_scraper.search_global(query), refresh)
