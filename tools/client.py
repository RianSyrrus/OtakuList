import httpx
from urllib.parse import urljoin, urlparse
from typing import Optional


class UpstreamError(Exception):
    """Base exception for upstream requests."""
    pass


class UpstreamNotFoundError(UpstreamError):
    """Upstream resource returned 404."""
    pass


class UpstreamTimeoutError(UpstreamError):
    """Upstream request timed out."""
    pass


class SafeHttpClient:
    """Hardened HTTP client with SSRF protection and realistic browser headers."""

    BASE_URL = "https://otakotaku.com"
    ALLOWED_HOSTS = {"otakotaku.com", "www.otakotaku.com"}

    DEFAULT_HEADERS = {
        "User-Agent": (
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            "AppleWebKit/537.36 (KHTML, like Gecko) "
            "Chrome/124.0.0.0 Safari/537.36"
        ),
        "Accept": (
            "text/html,application/xhtml+xml,application/xml;q=0.9,"
            "image/avif,image/webp,image/apng,*/*;q=0.8"
        ),
        "Accept-Language": "id-ID,id;q=0.9,en-US;q=0.8,en;q=0.7",
        "DNT": "1",
        "Sec-Ch-Ua": '"Chromium";v="124", "Google Chrome";v="124", "Not-A.Brand";v="99"',
        "Sec-Ch-Ua-Mobile": "?0",
        "Sec-Ch-Ua-Platform": '"Windows"',
        "Sec-Fetch-Dest": "document",
        "Sec-Fetch-Mode": "navigate",
        "Sec-Fetch-Site": "none",
        "Sec-Fetch-User": "?1",
        "Upgrade-Insecure-Requests": "1",
    }

    def __init__(self, timeout: float = 15.0, proxy: Optional[str] = None):
        self.timeout = httpx.Timeout(timeout, connect=5.0, read=12.0)
        self.proxy = proxy
        self._client: Optional[httpx.AsyncClient] = None

    async def get_client(self) -> httpx.AsyncClient:
        if self._client is None or self._client.is_closed:
            self._client = httpx.AsyncClient(
                headers=self.DEFAULT_HEADERS,
                timeout=self.timeout,
                follow_redirects=True,
                proxy=self.proxy,
                limits=httpx.Limits(max_keepalive_connections=10, max_connections=20),
            )
        return self._client

    async def close(self) -> None:
        if self._client and not self._client.is_closed:
            await self._client.aclose()
            self._client = None

    def validate_and_normalize_url(self, target: str) -> str:
        """Validate SSRF protection and ensure destination is strictly otakotaku.com."""
        if not target.startswith("http://") and not target.startswith("https://"):
            if not target.startswith("/"):
                target = "/" + target
            return urljoin(self.BASE_URL, target)

        parsed = urlparse(target)
        if parsed.hostname not in self.ALLOWED_HOSTS:
            raise ValueError(f"SSRF Violation: Host '{parsed.hostname}' is not permitted.")
        return target

    async def fetch_html(self, target: str, params: Optional[dict] = None) -> str:
        """Fetch page HTML with error classification."""
        url = self.validate_and_normalize_url(target)
        client = await self.get_client()

        try:
            response = await client.get(url, params=params)
            if response.status_code == 404:
                raise UpstreamNotFoundError(f"Content not found at {url}")
            response.raise_for_status()
            return response.text
        except httpx.TimeoutException as exc:
            raise UpstreamTimeoutError(f"Request timed out for {url}: {exc}") from exc
        except httpx.HTTPStatusError as exc:
            if exc.response.status_code == 404:
                raise UpstreamNotFoundError(f"Content not found at {url}") from exc
            raise UpstreamError(f"HTTP error {exc.response.status_code} from upstream") from exc
        except httpx.RequestError as exc:
            raise UpstreamError(f"Connection error to {url}: {exc}") from exc
