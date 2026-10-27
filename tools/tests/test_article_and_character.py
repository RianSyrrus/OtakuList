import unittest
from client import SafeHttpClient
from scrapers.article_scraper import ArticleScraper
from scrapers.character_scraper import CharacterScraper


class TestArticleAndCharacterScraper(unittest.IsolatedAsyncioTestCase):

    async def asyncSetUp(self):
        self.client = SafeHttpClient()
        self.article_scraper = ArticleScraper(self.client)
        self.char_scraper = CharacterScraper(self.client)

    async def asyncTearDown(self):
        await self.client.close()

    async def test_article_feed(self):
        result = await self.article_scraper.get_article_feed(page=1)
        self.assertIn("articles", result)
        self.assertGreater(len(result["articles"]), 0)
        first = result["articles"][0]
        self.assertIsNotNone(first["id"])
        self.assertTrue(len(first["title"]) > 0)

    async def test_article_detail(self):
        # Article 2431
        result = await self.article_scraper.get_article_detail(2431, "pasangan-seiyu-mikako-komatsu-dan-tomoaki-maeno-umumkan-kelahiran-anak-ke-2-mereka")
        self.assertEqual(result["id"], 2431)
        self.assertIn("Mikako Komatsu", result["title"])
        self.assertTrue(len(result["content"]) > 0)

    async def test_character_detail(self):
        # Masachika Kuze (ID: 28115)
        result = await self.char_scraper.get_character_detail(28115, "masachika-kuze")
        self.assertEqual(result["id"], 28115)
        self.assertEqual(result["name"], "Masachika Kuze")
        self.assertEqual(result["gender"], "Laki-laki")
        self.assertTrue(len(result["seiyu"]) > 0)
        self.assertEqual(result["seiyu"][0]["name"], "Kouhei Amasaki")


if __name__ == "__main__":
    unittest.main()
