import pytest as pytest
from app import create_app


@pytest.fixture()
def app():
    app = create_app()
    yield app


@pytest.fixture()
def client(app):
    return app.test_client()


def test_root(client):
    response = client.get("/")
    assert b"Hello, World!" in response.data


def test_failing_root(client):
    response = client.get("/")
    assert b"Hello World!" not in response.data

