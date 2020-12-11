import pytest
import pytest_bdd

POST_ENDPOINT = "/post"
GET_ENDPOINT = "/get"
CLASSIFICATION_INFO = "Info"
LOGGING_LEVEL = "DEBUG"
EXECUTION_MODE = "SYNC"


@pytest.fixture
def context():
    """Context fixture used to pass info from when to then in feature tests."""
    return {}


@pytest_bdd.scenario(
    "qr.feature", "When QR/Bar code needs to be detected from an image"
)
def test_login():
    """Just a simple name for the overall feature test."""
    pass


@pytest_bdd.given("image to detect", target_fixture="api")
def api(monkeypatch):
    """Return the api to query."""

    monkeypatch.setenv("AIQ_MODEL_PATH", "")
    monkeypatch.setenv("AIQ_POST_ENDPOINT", POST_ENDPOINT)
    monkeypatch.setenv("AIQ_GET_ENDPOINT", GET_ENDPOINT)
    monkeypatch.setenv("AIQ_INFO", CLASSIFICATION_INFO)
    monkeypatch.setenv("AIQ_LOGGING_LEVEL", LOGGING_LEVEL)
    monkeypatch.setenv("AIQ_EXECUTION_MODE", EXECUTION_MODE)


@pytest_bdd.when("<image> is sent")
def extractQR(image, context, shared_datadir):
    """Return the api to query."""
    from aiqcheck.service.app import APP as _TEST_APP

    data_file = shared_datadir / image
    # print("image",data_file)
    fileOpen = open(data_file, "rb")
    input_image = fileOpen.read()
    with _TEST_APP.test_client() as client:
        response = client.post("/post", data=input_image)
        print("response=", response.get_json())
        context["code_type"] = response.get_json()[0]["code_type"]


@pytest_bdd.then("<type> are returned")
def check_response_code(context, type, image):
    assert context["code_type"] == type, "The type doesn't match expected"
