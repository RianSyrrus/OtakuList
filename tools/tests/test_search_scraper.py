import unittest
from scraper import OtakOtakuScraper


class TestSearchAndFacade(unittest.IsolatedAsyncioTestCase):

    async def asyncSetUp(self):
        self.scraper = OtakOtakuScraper(cache_ttl=60)

    async def asyncTearDown(self):
        await self.scraper.close()

    async def test_search_global(self):
        result, cached = await self.scraper.search_global("naruto")
        self.assertFalse(cached)
        self.assertEqual(result["query"], "naruto")
        self.assertTrue(len(result["anime"]) > 0 or len(result["characters"]) > 0)

        # Test caching
        result2, cached2 = await self.scraper.search_global("naruto")
        self.assertTrue(cached2)
        self.assertEqual(result, result2)

        # Test force refresh bypass
        result3, cached3 = await self.scraper.search_global("naruto", refresh=True)
        self.assertFalse(cached3)


if __name__ == "__main__":
    unittest.main()
