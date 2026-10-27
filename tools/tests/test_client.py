import asyncio
import unittest
from cache import TTLCache
from client import SafeHttpClient, UpstreamNotFoundError


class TestClientAndCache(unittest.IsolatedAsyncioTestCase):

    def test_ttl_cache_operations(self):
        cache = TTLCache(default_ttl=1)
        cache.set("foo", "bar")
        self.assertEqual(cache.get("foo"), "bar")

        cache.delete("foo")
        self.assertIsNone(cache.get("foo"))

    async def test_ssrf_protection(self):
        client = SafeHttpClient()
        with self.assertRaises(ValueError):
            client.validate_and_normalize_url("https://malicious-site.com/evil")

        valid_url = client.validate_and_normalize_url("/anime/feed")
        self.assertEqual(valid_url, "https://otakotaku.com/anime/feed")
        await client.close()

    async def test_live_fetch(self):
        client = SafeHttpClient()
        html = await client.fetch_html("/quote/feed")
        self.assertTrue(len(html) > 1000)
        self.assertIn("otakotaku", html.lower())
        await client.close()


if __name__ == "__main__":
    unittest.main()
