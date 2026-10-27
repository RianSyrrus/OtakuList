import unittest
from client import SafeHttpClient
from scrapers.quote_scraper import QuoteScraper


class TestQuoteScraper(unittest.IsolatedAsyncioTestCase):

    async def asyncSetUp(self):
        self.client = SafeHttpClient()
        self.scraper = QuoteScraper(self.client)

    async def asyncTearDown(self):
        await self.client.close()

    async def test_quote_feed(self):
        result = await self.scraper.get_quote_feed(page=1)
        self.assertIn("quotes", result)
        self.assertGreater(len(result["quotes"]), 0)
        first = result["quotes"][0]
        self.assertIsNotNone(first["id"])
        self.assertTrue(len(first["quote"]) > 0)
        self.assertTrue(len(first["character"]["name"]) > 0)

    async def test_quote_detail(self):
        # Naluri melindungi anak-anak (ID: 2435)
        result = await self.scraper.get_quote_detail(2435, "naluri-melindungi-anak-anak")
        self.assertEqual(result["id"], 2435)
        self.assertIn("naluri", result["quote"].lower())
        self.assertEqual(result["character"]["name"], "Genau")
        self.assertIn("Frieren", result["anime"]["title"])

    async def test_search_quotes(self):
        result = await self.scraper.search_quotes("naruto", page=1)
        self.assertIn("quotes", result)
        self.assertGreater(len(result["quotes"]), 0)


if __name__ == "__main__":
    unittest.main()
