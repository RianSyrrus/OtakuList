import unittest
from fastapi.testclient import TestClient
from api import app


class TestApiEndpoints(unittest.TestCase):

    def setUp(self):
        # Using context manager triggers the lifespan (starts scraper and closes on exit)
        self.client = TestClient(app)
        self.client.__enter__()

    def tearDown(self):
        self.client.__exit__(None, None, None)

    def test_root_endpoint(self):
        response = self.client.get("/")
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertEqual(data["name"], "OtakOtaku Scraping API")
        self.assertIn("endpoints", data)

    def test_quote_feed_endpoint(self):
        response = self.client.get("/api/quote/feed?page=1")
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertEqual(data["status"], "success")
        self.assertIn("quotes", data["data"])

    def test_anime_feed_endpoint(self):
        response = self.client.get("/api/anime/feed?page=1")
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertEqual(data["status"], "success")
        self.assertIn("anime", data["data"])

    def test_search_endpoint(self):
        response = self.client.get("/api/search?q=naruto")
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertEqual(data["status"], "success")
        self.assertIn("anime", data["data"])

    def test_cache_clear_endpoint(self):
        response = self.client.post("/api/cache/clear")
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertEqual(data["status"], "success")


if __name__ == "__main__":
    unittest.main()
