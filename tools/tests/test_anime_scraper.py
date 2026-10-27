import unittest
from client import SafeHttpClient
from scrapers.anime_scraper import AnimeScraper


class TestAnimeScraper(unittest.IsolatedAsyncioTestCase):

    async def asyncSetUp(self):
        self.client = SafeHttpClient()
        self.scraper = AnimeScraper(self.client)

    async def asyncTearDown(self):
        await self.client.close()

    async def test_anime_feed(self):
        result = await self.scraper.get_anime_feed(page=1)
        self.assertIn("anime", result)
        self.assertGreater(len(result["anime"]), 0)
        first = result["anime"][0]
        self.assertIsNotNone(first["id"])
        self.assertTrue(len(first["title"]) > 0)

    async def test_anime_detail(self):
        # Roshidere (ID: 2645)
        result = await self.scraper.get_anime_detail(2645, "tokidoki-bosotto-russia-go-de-dereru-tonari-no-alya-san")
        self.assertEqual(result["id"], 2645)
        self.assertIn("Alya", result["title"])
        self.assertIn("Comedy", [g["name"] for g in result["genres"]])
        self.assertTrue(len(result["characters"]) > 0)
        self.assertEqual(result["characters"][0]["name"], "Masachika Kuze")
        self.assertEqual(result["characters"][0]["seiyu"]["name"], "Kouhei Amasaki")

    async def test_anime_season(self):
        result = await self.scraper.get_anime_season("summer-2024")
        self.assertIn("anime", result)
        self.assertGreater(len(result["anime"]), 0)

    async def test_anime_genres(self):
        genres = await self.scraper.get_anime_genres()
        self.assertGreater(len(genres), 20)
        action = next((g for g in genres if g["slug"] == "action"), None)
        self.assertIsNotNone(action)

    async def test_anime_by_genre(self):
        result = await self.scraper.get_anime_by_genre("action", page=1)
        self.assertIn("anime", result)
        self.assertGreater(len(result["anime"]), 0)


if __name__ == "__main__":
    unittest.main()
