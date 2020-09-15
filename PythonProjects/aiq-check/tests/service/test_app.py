"""Test for the configuration interface of the aiqcheck service app."""

# pylint: disable=redefined-outer-name, unused-import, import-outside-toplevel
POST_ENDPOINT = "/post"
GET_ENDPOINT = "/get"
CLASSIFICATION_INFO = "Info"
LOGGING_LEVEL = "DEBUG"
EXECUTION_MODE = "SYNC"


def test_app_with_endpoint(monkeypatch, mocker):
    """
    Test if app creation works.

    If this tests fails the probability is high that the public service interface was
    changed. Please be aware if there have to be any changes in this test to make it
    tun correctly the changes in public interface have to be communicated
    accordingly. This means adapting the documentation and alerting all the users of
    this service. Also a major version step is needed.

    """

    monkeypatch.setenv("AIQ_MODEL_PATH", "")
    monkeypatch.setenv("AIQ_POST_ENDPOINT", POST_ENDPOINT)
    monkeypatch.setenv("AIQ_GET_ENDPOINT", GET_ENDPOINT)
    monkeypatch.setenv("AIQ_INFO", CLASSIFICATION_INFO)
    monkeypatch.setenv("AIQ_LOGGING_LEVEL", LOGGING_LEVEL)
    monkeypatch.setenv("AIQ_EXECUTION_MODE", EXECUTION_MODE)

    mocked_pre_process = mocker.patch(
        "aiqcheck.service.processor.AiqProcessor.preprocess"
    )
    mocked_pre_process.return_value = [{"input": "mock input"}]

    from aiqcheck.detector.qr import DetectedCodeWithPosition

    mocked_process = mocker.patch("aiqcheck.service.processor.AiqProcessor.process")
    mocked_process.return_value = [
        [
            DetectedCodeWithPosition(
                code_type="mock_format",
                content="Mock content.",
                confidence=0.0,
                position=None,
            )
        ]
    ]

    from aiqcheck.service.app import APP as _TEST_APP

    with _TEST_APP.test_client() as client:
        response = client.post("/post", data="Test message")

    print(f"test result {response.get_json()}")
    assert response.status_code == 200
    assert response.get_json()[0]["content"] == "Mock content."
