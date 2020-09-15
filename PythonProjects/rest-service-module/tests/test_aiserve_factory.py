"""Test suite for the factory module."""
# pylint: disable=redefined-outer-name

import logging

import backapp
import flask
import flask_api.status  # type: ignore
import flask_restful  # type: ignore
import pytest  # type: ignore
import requests
import ssltest

import aiserve
import aiserve.async_request_handler
import aiserve.configuration
import aiserve.dictpreserver
import aiserve.factory


class AsyncResourceBase(
    flask_restful.Resource
):  # pylint: disable=too-few-public-methods
    """Base class to be inherited by the Flask resources."""

    def __init__(self, processor, async_request_handler):
        self.processor = processor
        self.async_request_handler = async_request_handler


class DummyResource(AsyncResourceBase):
    """Dummy Flask resource."""

    @staticmethod
    def post():
        """Implement http post functionality."""
        return flask.Response(
            "This is aiserve, no custom configuration was specified, "
            "therefore no further functionality is implemented. "
            "Please run "
            "aiserve.factory.create_app "
            "with a custom configuration_path. \n "
            "Examples on the proper use of aiserve can be found "
            "under the following path: \n"
            "https://bitbucket.org/ai4bd/aisee-service/src/develop"
            "/examples/",
            status=flask_api.status.HTTP_200_OK,
        )


@pytest.fixture
def make_resource_configuration(dummy_processor_instance):
    """Fixture function to create endpoint keyword resources."""
    resource_cls_kwargs = aiserve.configuration.ResourceClsKwargsAsync(
        processor=dummy_processor_instance,
        async_request_handler=aiserve.async_request_handler.AsyncRequestHandler(
            processor=dummy_processor_instance,
            preserver=aiserve.dictpreserver.DictPreserver(),
            batch_size=1,
        ),
    )
    resource_configuration = [
        aiserve.configuration.ResourceConfiguration(
            endpoints={"/": DummyResource}, resource_cls_kwargs=resource_cls_kwargs
        )
    ]
    return resource_configuration


@pytest.fixture
def configuration(make_resource_configuration):
    """Create standard configuration."""
    resource_configuration = make_resource_configuration
    return aiserve.configuration.ServiceConfiguration(
        LOGGING_LEVEL=logging.DEBUG,
        SECURITY_USE_SSO=False,
        SECURITY_SSO_SERVICE=None,
        RESOURCE_CONFIGURATION=resource_configuration,
        SERVICE_INFO="Aiserve - no info specified",
    )


@pytest.fixture
def test_configuration_sso(make_resource_configuration):
    """Create configuration using sso."""
    resource_configuration = make_resource_configuration
    return aiserve.configuration.ServiceConfiguration(
        LOGGING_LEVEL=logging.DEBUG,
        SECURITY_USE_SSO=True,
        SECURITY_SSO_SERVICE="http://127.0.0.1:5001/",
        RESOURCE_CONFIGURATION=resource_configuration,
        SERVICE_INFO="Aiserve - no info specified",
    )


@pytest.mark.parametrize(
    "headers, expected",
    [
        (None, flask_api.status.HTTP_401_UNAUTHORIZED),
        ({"Authorization": "TRUE"}, flask_api.status.HTTP_200_OK),
    ],
)
def test_ssltest(headers: dict, expected: flask_api.status):
    """
    Test the functionality of ssl service.

    Two different cases are tested:
        No header send
        Correct header send
    Parameters
    ----------
    headers
        Authentication header sent to the service.
    expected
        Expected response code.
    """
    # Test if correct status code is returned.
    with ssltest.MockSsl():
        response = requests.get("http://127.0.0.1:5000/", headers=headers)
        assert response.status_code == expected


def test_create_app(configuration):
    """Test the nominal case of app creation without additional parameters."""
    app = aiserve.factory.create_app(configuration)
    assert isinstance(app, flask.Flask)

    with app.test_client() as client:
        assert client.post("/").status_code == flask_api.status.HTTP_200_OK


@pytest.mark.parametrize(
    "headers, expected",
    [
        (None, flask_api.status.HTTP_401_UNAUTHORIZED),
        ({"Authorization": "FALSE"}, flask_api.status.HTTP_401_UNAUTHORIZED),
        ({"Authorization": "TRUE"}, flask_api.status.HTTP_200_OK),
    ],
)
def test_create_app_sso(
    headers: dict, expected: flask_api.status, test_configuration_sso
):
    """
    Test if sso works properly.

    Three different cases are tested:
        No header send
        Wrong token send
        Correct token send

    Parameters
    ----------
    headers
        Authentication header sent to the service.
    expected
        Expected response code.
    """
    app = aiserve.factory.create_app(test_configuration_sso)

    with backapp.BackApp(app), ssltest.MockSsl(5001):
        # Test if correct status code is returned.
        assert (
            requests.post("http://127.0.0.1:5000/", headers=headers).status_code
            == expected
        )
