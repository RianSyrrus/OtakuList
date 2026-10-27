import logging
from contextlib import asynccontextmanager
from typing import Optional
from fastapi import FastAPI, Query, Path, Request, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from client import (
    UpstreamError,
    UpstreamNotFoundError,
    UpstreamTimeoutError,
)
from scraper import OtakOtakuScraper

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("OtakOtakuAPI")

scraper: Optional[OtakOtakuScraper] = None


@asynccontextmanager
async def lifespan(app: FastAPI):
    global scraper
    scraper = OtakOtakuScraper(cache_ttl=300)
    logger.info("OtakOtakuScraper initialized.")
    yield
    if scraper:
        await scraper.close()
        logger.info("OtakOtakuScraper closed.")


app = FastAPI(
    title="OtakOtaku API",
    description="REST API Scraper untuk OtakOtaku (otakotaku.com) - Quotes Anime, Database, Musim, Artikel Berita & Karakter.",
    version="1.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# Exception Handlers
@app.exception_handler(UpstreamNotFoundError)
async def not_found_handler(request: Request, exc: UpstreamNotFoundError):
    return JSONResponse(
        status_code=status.HTTP_404_NOT_FOUND,
        content={"status": "error", "message": str(exc)},
    )


@app.exception_handler(UpstreamTimeoutError)
async def timeout_handler(request: Request, exc: UpstreamTimeoutError):
    return JSONResponse(
        status_code=status.HTTP_504_GATEWAY_TIMEOUT,
        content={"status": "error", "message": "Upstream request timed out."},
    )


@app.exception_handler(UpstreamError)
async def upstream_error_handler(request: Request, exc: UpstreamError):
    return JSONResponse(
        status_code=status.HTTP_502_BAD_GATEWAY,
        content={"status": "error", "message": f"Upstream error: {str(exc)}"},
    )


@app.exception_handler(ValueError)
async def value_error_handler(request: Request, exc: ValueError):
    return JSONResponse(
        status_code=status.HTTP_400_BAD_REQUEST,
        content={"status": "error", "message": str(exc)},
    )


@app.exception_handler(Exception)
async def generic_error_handler(request: Request, exc: Exception):
    logger.error("Unhandled error: %s", exc, exc_info=True)
    return JSONResponse(
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        content={"status": "error", "message": "Internal server error occurred."},
    )


# --- Root ---
@app.get("/", tags=["System"])
async def root():
    return {
        "name": "OtakOtaku Scraping API",
        "version": "1.0.0",
        "target": "https://otakotaku.com",
        "docs_url": "/docs",
        "endpoints": {
            "quotes": ["/api/quote/feed", "/api/quote/detail/{id}/{slug}", "/api/quote/search"],
            "anime": [
                "/api/anime/feed",
                "/api/anime/detail/{id}/{slug}",
                "/api/anime/popular",
                "/api/anime/season/{season_slug}",
                "/api/anime/genres",
                "/api/anime/genre/{genre_slug}",
            ],
            "articles": [
                "/api/article/feed",
                "/api/article/category/{category_slug}",
                "/api/article/detail/{id}/{slug}",
            ],
            "character": ["/api/character/detail/{id}/{slug}"],
            "search": ["/api/search"],
        },
    }


# --- Quotes Endpoints ---
@app.get("/api/quote/feed", tags=["Quotes"])
async def quote_feed(page: int = Query(1, ge=1), refresh: bool = Query(False)):
    data, cached = await scraper.get_quote_feed(page=page, refresh=refresh)
    return {"status": "success", "cached": cached, "data": data}


@app.get("/api/quote/detail/{quote_id}", tags=["Quotes"])
async def quote_detail_id(
    quote_id: int = Path(..., ge=1),
    refresh: bool = Query(False),
):
    data, cached = await scraper.get_quote_detail(quote_id=quote_id, slug="", refresh=refresh)
    return {"status": "success", "cached": cached, "data": data}


@app.get("/api/quote/detail/{quote_id}/{slug}", tags=["Quotes"])
async def quote_detail_slug(
    quote_id: int = Path(..., ge=1),
    slug: str = Path(...),
    refresh: bool = Query(False),
):
    data, cached = await scraper.get_quote_detail(quote_id=quote_id, slug=slug, refresh=refresh)
    return {"status": "success", "cached": cached, "data": data}


@app.get("/api/quote/search", tags=["Quotes"])
async def quote_search(
    q: str = Query(..., min_length=1),
    page: int = Query(1, ge=1),
    refresh: bool = Query(False),
):
    data, cached = await scraper.search_quotes(query=q, page=page, refresh=refresh)
    return {"status": "success", "cached": cached, "data": data}


# --- Anime Endpoints ---
@app.get("/api/anime/feed", tags=["Anime"])
async def anime_feed(page: int = Query(1, ge=1), refresh: bool = Query(False)):
    data, cached = await scraper.get_anime_feed(page=page, refresh=refresh)
    return {"status": "success", "cached": cached, "data": data}


@app.get("/api/anime/detail/{anime_id}", tags=["Anime"])
async def anime_detail_id(
    anime_id: int = Path(..., ge=1),
    refresh: bool = Query(False),
):
    data, cached = await scraper.get_anime_detail(anime_id=anime_id, slug="", refresh=refresh)
    return {"status": "success", "cached": cached, "data": data}


@app.get("/api/anime/detail/{anime_id}/{slug}", tags=["Anime"])
async def anime_detail_slug(
    anime_id: int = Path(..., ge=1),
    slug: str = Path(...),
    refresh: bool = Query(False),
):
    data, cached = await scraper.get_anime_detail(anime_id=anime_id, slug=slug, refresh=refresh)
    return {"status": "success", "cached": cached, "data": data}


@app.get("/api/anime/popular", tags=["Anime"])
async def anime_popular(refresh: bool = Query(False)):
    data, cached = await scraper.get_anime_popular(refresh=refresh)
    return {"status": "success", "cached": cached, "data": data}


@app.get("/api/anime/season/{season_slug}", tags=["Anime"])
async def anime_season(
    season_slug: str = Path(..., pattern=r"^[a-zA-Z0-9_-]+$"),
    refresh: bool = Query(False),
):
    data, cached = await scraper.get_anime_season(season_slug=season_slug, refresh=refresh)
    return {"status": "success", "cached": cached, "data": data}


@app.get("/api/anime/genres", tags=["Anime"])
async def anime_genres(refresh: bool = Query(False)):
    data, cached = await scraper.get_anime_genres(refresh=refresh)
    return {"status": "success", "cached": cached, "data": data}


@app.get("/api/anime/genre/{genre_slug}", tags=["Anime"])
async def anime_by_genre(
    genre_slug: str = Path(..., pattern=r"^[a-zA-Z0-9_-]+$"),
    page: int = Query(1, ge=1),
    refresh: bool = Query(False),
):
    data, cached = await scraper.get_anime_by_genre(genre_slug=genre_slug, page=page, refresh=refresh)
    return {"status": "success", "cached": cached, "data": data}


# --- Articles Endpoints ---
@app.get("/api/article/feed", tags=["Articles"])
async def article_feed(page: int = Query(1, ge=1), refresh: bool = Query(False)):
    data, cached = await scraper.get_article_feed(page=page, refresh=refresh)
    return {"status": "success", "cached": cached, "data": data}


@app.get("/api/article/category/{category_slug}", tags=["Articles"])
async def article_by_category(
    category_slug: str = Path(..., pattern=r"^[a-zA-Z0-9_-]+$"),
    page: int = Query(1, ge=1),
    refresh: bool = Query(False),
):
    data, cached = await scraper.get_article_by_category(
        category_slug=category_slug, page=page, refresh=refresh
    )
    return {"status": "success", "cached": cached, "data": data}


@app.get("/api/article/detail/{post_id}", tags=["Articles"])
async def article_detail_id(
    post_id: int = Path(..., ge=1),
    refresh: bool = Query(False),
):
    data, cached = await scraper.get_article_detail(post_id=post_id, slug="", refresh=refresh)
    return {"status": "success", "cached": cached, "data": data}


@app.get("/api/article/detail/{post_id}/{slug}", tags=["Articles"])
async def article_detail_slug(
    post_id: int = Path(..., ge=1),
    slug: str = Path(...),
    refresh: bool = Query(False),
):
    data, cached = await scraper.get_article_detail(post_id=post_id, slug=slug, refresh=refresh)
    return {"status": "success", "cached": cached, "data": data}


# --- Character Endpoints ---
@app.get("/api/character/detail/{char_id}", tags=["Character"])
async def character_detail_id(
    char_id: int = Path(..., ge=1),
    refresh: bool = Query(False),
):
    data, cached = await scraper.get_character_detail(char_id=char_id, slug="", refresh=refresh)
    return {"status": "success", "cached": cached, "data": data}


@app.get("/api/character/detail/{char_id}/{slug}", tags=["Character"])
async def character_detail_slug(
    char_id: int = Path(..., ge=1),
    slug: str = Path(...),
    refresh: bool = Query(False),
):
    data, cached = await scraper.get_character_detail(char_id=char_id, slug=slug, refresh=refresh)
    return {"status": "success", "cached": cached, "data": data}


# --- Global Search ---
@app.get("/api/search", tags=["Search"])
async def search_global(
    q: str = Query(..., min_length=1),
    refresh: bool = Query(False),
):
    data, cached = await scraper.search_global(query=q, refresh=refresh)
    return {"status": "success", "cached": cached, "data": data}


# --- Cache Utility ---
@app.post("/api/cache/clear", tags=["System"])
async def clear_cache():
    if scraper and scraper.cache:
        scraper.cache.clear()
    return {"status": "success", "message": "In-memory cache cleared successfully."}
