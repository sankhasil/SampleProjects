import os

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
def test_check_QR_code():
    """Just a simple name for the overall feature test."""
    pass


@pytest.fixture(autouse=True)
def mock_settings_env_vars(mocker):
    """Setting environment """
    set_env_dict = {
        "AIQ_MODEL_PATH": "",
        "AIQ_POST_ENDPOINT": POST_ENDPOINT,
        "AIQ_GET_ENDPOINT": GET_ENDPOINT,
        "AIQ_INFO": CLASSIFICATION_INFO,
        "AIQ_LOGGING_LEVEL": LOGGING_LEVEL,
        "AIQ_EXECUTION_MODE": EXECUTION_MODE,
    }
    mocker.patch.dict(os.environ, set_env_dict)


@pytest_bdd.given("<image> to detect", target_fixture="setup_env")
def setup_env(image):
    """Check if image exist."""
    assert image != " ", "Image should be given"


@pytest_bdd.when("<image> is sent")
def extract_QR(image, context, shared_datadir):
    from aiqcheck.service.app import APP as _TEST_APP

    data_file = shared_datadir / image
    file_open = open(data_file, "rb")
    input_image = file_open.read()
    with _TEST_APP.test_client() as client:
        response = client.post("/post", data=input_image)
        print(response.get_json())
        context["code_type"] = response.get_json()[0]["code_type"]
        context["content"] = response.get_json()[0]["content"]


@pytest_bdd.then("<type> and <content> are returned")
def check_response_code(context, type, content):
    assert context["code_type"] == type, "The type doesn't match expected"
    assert context["content"] == content, "The content doesn't match expected"
