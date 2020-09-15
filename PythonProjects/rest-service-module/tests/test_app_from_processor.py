"""Create apps from dummy processor.

These series of tests define a Mock processor and create an app serving it.
They check that the right methods of the processor are called when requests are
sent to the app.
"""
# pylint: disable=redefined-outer-name

import logging
import time

import pytest
import werkzeug

import aiserve.async_request_handler
import aiserve.configuration
import aiserve.dictpreserver
import aiserve.factory
import aiserve.process
import aiserve.resources

ENDPOINT = "/test_endpoint"
POST_ENDPOINT = "/post"
GET_ENDPOINT = "/get"

JSON_SERIALIZABLE = 42

HTTP_INTERNAL_ERROR = 500
HTTP_CLIENT_ERROR = 400

HTTP_OK = 200


@pytest.fixture
def processor(mocker):
    """Mock a processor."""
    processor = mocker.Mock()
    processor.return_value = [JSON_SERIALIZABLE]
    processor.postprocess.return_value = JSON_SERIALIZABLE
    return processor


@pytest.fixture
def create_configuration_for_sync_processor(processor):
    """Create a configuration for sync processor."""
    resource_configuration = aiserve.configuration.ResourceConfiguration(
        endpoints={ENDPOINT: aiserve.resources.RequestPostingResourceSync},
        resource_cls_kwargs=aiserve.configuration.ResourceClsKwargsSync(
            processor=processor
        ),
    )
    configuration = aiserve.configuration.ServiceConfiguration(
        LOGGING_LEVEL=logging.DEBUG,
        SECURITY_USE_SSO=False,
        SECURITY_SSO_SERVICE=None,
        RESOURCE_CONFIGURATION=[resource_configuration],
        SERVICE_INFO="Aiserve - no info specified",
    )
    return configuration


@pytest.fixture
def create_configuration_for_async_processor(processor):
    """Create a configuration for async processor."""
    preserver = aiserve.dictpreserver.DictPreserver()
    resource_configuration = aiserve.configuration.ResourceConfiguration(
        endpoints={
            POST_ENDPOINT: aiserve.resources.RequestPostingResource,
            GET_ENDPOINT: aiserve.resources.ResultRetrievalResource,
        },
        resource_cls_kwargs=aiserve.configuration.ResourceClsKwargsAsync(
            processor=processor,
            async_request_handler=aiserve.async_request_handler.AsyncRequestHandler(
                processor=processor, preserver=preserver, batch_size=1
            ),
        ),
    )

    configuration = aiserve.configuration.ServiceConfiguration(
        LOGGING_LEVEL=logging.DEBUG,
        SECURITY_USE_SSO=False,
        SECURITY_SSO_SERVICE=None,
        RESOURCE_CONFIGURATION=[resource_configuration],
        SERVICE_INFO="Aiserve - no info specified",
    )
    return configuration


def test_sync_api(create_configuration_for_sync_processor):
    """Test sync API from mock processor."""
    configuration = create_configuration_for_sync_processor

    resource_cls_kwargs = configuration.RESOURCE_CONFIGURATION[0].resource_cls_kwargs
    processor = resource_cls_kwargs.processor

    app = aiserve.factory.create_app(configuration)
    app.config["TESTING"] = True

    with app.test_client() as client:
        response = client.post(ENDPOINT)

    assert response.status_code == HTTP_OK

    processor.preprocess.assert_called()
    processor.assert_called_with(input_preprocessed=[processor.preprocess.return_value])
    processor.postprocess.assert_called()

    assert response.json == processor.postprocess.return_value


_REQUEST_ID_KEY = "request-id"


def test_async_api(create_configuration_for_async_processor):
    """Test async API from mock processor."""
    configuration = create_configuration_for_async_processor

    processor = configuration.RESOURCE_CONFIGURATION[0].resource_cls_kwargs.processor

    app = aiserve.factory.create_app(configuration)
    app.config["TESTING"] = True

    with app.test_client() as client:
        post_response = client.post(POST_ENDPOINT)

        assert post_response.status_code == HTTP_OK
        assert _REQUEST_ID_KEY in post_response.json

        sleep_time = 0.1

        time.sleep(sleep_time)

        request_id = post_response.json[_REQUEST_ID_KEY]
        get_response = client.get(f"{GET_ENDPOINT}?{_REQUEST_ID_KEY}={request_id}")

    assert get_response.status_code == HTTP_OK

    processor.preprocess.assert_called()
    processor.assert_called()
    processor.postprocess.assert_called()

    assert get_response.json == processor.postprocess.return_value


def test_sync_api_returns_500_on_raise(create_configuration_for_sync_processor):
    """Check api response when processor raises an error."""
    configuration = create_configuration_for_sync_processor

    processor = configuration.RESOURCE_CONFIGURATION[0].resource_cls_kwargs.processor

    processor.side_effect = Exception()
    with pytest.raises(Exception):
        processor()

    app = aiserve.factory.create_app(configuration)
    app.config["TESTING"] = True

    with app.test_client() as client:
        response = client.post(ENDPOINT)

    assert response.status_code == HTTP_INTERNAL_ERROR


def test_async_api_returns_500_on_raise(create_configuration_for_async_processor):
    """Check api response when processor raises a 400 error."""
    configuration = create_configuration_for_async_processor

    processor = configuration.RESOURCE_CONFIGURATION[0].resource_cls_kwargs.processor

    processor.side_effect = Exception()

    app = aiserve.factory.create_app(configuration)
    app.config["TESTING"] = True

    with app.test_client() as client:
        post_response = client.post(POST_ENDPOINT)

        assert post_response.status_code == HTTP_OK
        assert _REQUEST_ID_KEY in post_response.json

        sleep_time = 0.1

        time.sleep(sleep_time)

        request_id = post_response.json[_REQUEST_ID_KEY]
        get_response = client.get(f"{GET_ENDPOINT}?{_REQUEST_ID_KEY}={request_id}")

    assert get_response.status_code == HTTP_INTERNAL_ERROR


def test_async_api_raises_werkzeug_exception_on_raise(
    create_configuration_for_async_processor,
):
    """Check the exception raised when a werkzeug exception is raised by processor."""
    configuration = create_configuration_for_async_processor

    processor = configuration.RESOURCE_CONFIGURATION[0].resource_cls_kwargs.processor

    processor.side_effect = werkzeug.exceptions.BadRequest("You sent a bad request")

    app = aiserve.factory.create_app(configuration)
    app.config["TESTING"] = True

    with app.test_client() as client:
        post_response = client.post(POST_ENDPOINT)

        assert post_response.status_code == HTTP_OK
        assert _REQUEST_ID_KEY in post_response.json

        sleep_time = 0.1

        time.sleep(sleep_time)

        request_id = post_response.json[_REQUEST_ID_KEY]
        get_response = client.get(f"{GET_ENDPOINT}?{_REQUEST_ID_KEY}={request_id}")

    assert get_response.status_code == HTTP_CLIENT_ERROR
