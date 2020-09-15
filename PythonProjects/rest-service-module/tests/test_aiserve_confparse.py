"""Test suite for the configuration parsers."""
# pylint: disable=redefined-outer-name
import logging

import dummyprocessor
import pytest  # type: ignore

import aiserve.confparse
import aiserve.dictpreserver

# Define environment variable names and values for testing.
RESOURCE_PATH_ENV = "RESOURCE_PATH"
RESOURCE_PATH_STR = "/resource/path"

POST_ENDPOINT_NAME_ENV = "POST_ENDPOINT_NAME"
GET_ENDPOINT_NAME_ENV = "GET_ENDPOINT_NAME"
POST_ENDPOINT_NAME_STR = "/post-endpoint"
GET_ENDPOINT_NAME_STR = "/get-endpoint"

LOGGING_LEVEL_ENV = "LOGGING_LEVEL"
LOGGING_LEVEL_STR = "ERROR"
LOGGING_LEVEL_INT = logging.ERROR

EXECUTION_MODE_NAME_ENV = "EXECUTION_MODE"
EXECUTION_MODE_SYNC_STR = "SYNC"
EXECUTION_MODE_ASYNC_STR = "ASYNC"

SSO_SERVICE_ENV = "SECURITY_SSO_SERVICE"
SSO_ENDPOINT_STR = "/endpoint/url"

INFO_ENV = "INFO_ENV"
INFO_STR = "Test info"


@pytest.fixture
def simple_environment(monkeypatch):
    """Set up environment variables."""
    monkeypatch.setenv(RESOURCE_PATH_ENV, RESOURCE_PATH_STR)
    monkeypatch.setenv(POST_ENDPOINT_NAME_ENV, POST_ENDPOINT_NAME_STR)
    monkeypatch.setenv(GET_ENDPOINT_NAME_ENV, GET_ENDPOINT_NAME_STR)
    monkeypatch.setenv(LOGGING_LEVEL_ENV, LOGGING_LEVEL_STR)
    return monkeypatch


@pytest.fixture
def simple_sync_environment(monkeypatch):
    """Set up the env vars for sync mode."""
    monkeypatch.setenv(RESOURCE_PATH_ENV, RESOURCE_PATH_STR)
    monkeypatch.setenv(POST_ENDPOINT_NAME_ENV, POST_ENDPOINT_NAME_STR)
    monkeypatch.setenv(LOGGING_LEVEL_ENV, LOGGING_LEVEL_STR)
    monkeypatch.setenv(EXECUTION_MODE_NAME_ENV, EXECUTION_MODE_SYNC_STR)
    return monkeypatch


@pytest.fixture
def processor_parameter():
    """Create parameters to initialize a dummy resource."""
    return aiserve.confparse.ResourceParameters(
        model_path_url=RESOURCE_PATH_STR,
        post_endpoint=POST_ENDPOINT_NAME_STR,
        get_endpoint=GET_ENDPOINT_NAME_STR,
        processor_class=dummyprocessor.DummyProcessor,
        processor_batch_size=1,
        preserver_class=aiserve.dictpreserver.DictPreserver,
        execution_mode=EXECUTION_MODE_ASYNC_STR,
    )


@pytest.fixture
def processor_parameter_for_sync():
    """Create parameters to initialize a dummy resource."""
    return aiserve.confparse.ResourceParameters(
        model_path_url=RESOURCE_PATH_STR,
        post_endpoint=POST_ENDPOINT_NAME_STR,
        get_endpoint=None,
        processor_class=dummyprocessor.DummyProcessor,
        processor_batch_size=1,
        preserver_class=aiserve.dictpreserver.DictPreserver,
        execution_mode=EXECUTION_MODE_SYNC_STR,
    )


def test_configuration_for_app(simple_environment, processor_parameter):
    """Test if configuration parser works using environment variables."""
    # pylint: disable=unused-argument

    # Parse configuration using environment variables.

    configuration = aiserve.confparse.configuration_for_app(
        (processor_parameter,), LOGGING_LEVEL_INT
    )

    # Test if return type is correct.
    assert isinstance(configuration, aiserve.configuration.ServiceConfiguration)
    # Test if logging level and endpoint is correctly set.
    assert (
        configuration.__getattribute__(aiserve.configuration.LOGGING_LEVEL)
        == LOGGING_LEVEL_INT
    )

    resource_configuration = configuration.__getattribute__(
        aiserve.configuration.RESOURCE_CONFIGURATION
    )[0]

    first_endpoint_resource = resource_configuration.endpoints

    assert POST_ENDPOINT_NAME_STR in first_endpoint_resource
    assert GET_ENDPOINT_NAME_STR in first_endpoint_resource

    assert resource_configuration.resource_cls_kwargs.processor is not None

    assert resource_configuration.resource_cls_kwargs.async_request_handler is not None


def test_configuration_for_app_for_sync(
    simple_sync_environment, processor_parameter_for_sync
):
    """Test if configuration parser works using environment variables."""
    # pylint: disable=unused-argument

    # Parse configuration using environment variables.

    configuration = aiserve.confparse.configuration_for_app(
        (processor_parameter_for_sync,), LOGGING_LEVEL_INT
    )

    # Test if return type is correct.
    assert isinstance(configuration, aiserve.configuration.ServiceConfiguration)
    # Test if logging level and endpoint is correctly set.
    assert (
        configuration.__getattribute__(aiserve.configuration.LOGGING_LEVEL)
        == LOGGING_LEVEL_INT
    )

    resource_configuration = configuration.__getattribute__(
        aiserve.configuration.RESOURCE_CONFIGURATION
    )[0]

    first_endpoint_resource = resource_configuration.endpoints

    assert POST_ENDPOINT_NAME_STR in first_endpoint_resource
    assert GET_ENDPOINT_NAME_STR not in first_endpoint_resource

    assert resource_configuration.resource_cls_kwargs.processor is not None


def test_configuration_for_app_sso(processor_parameter):
    """
    Check if setting a sso verification endpoint works.

    This could be done using parametrized tests, i chose not to asi don't like the
    idea of passing in the default
    parameter in the first test and this makes the failing part more clear in case of
    an error.
    """
    configuration = aiserve.confparse.configuration_for_app(
        (processor_parameter,), LOGGING_LEVEL_INT, SSO_ENDPOINT_STR
    )

    # Check if sso configuration was correctly set.
    assert (
        configuration.__getattribute__(aiserve.configuration.SECURITY_USE_SSO) is True
    )
    assert (
        configuration.__getattribute__(aiserve.configuration.SECURITY_SSO_SERVICE)
        == SSO_ENDPOINT_STR
    )


def test_configuration_for_app_info(processor_parameter):
    """Check if setting an info works."""
    configuration = aiserve.confparse.configuration_for_app(
        (processor_parameter,), LOGGING_LEVEL_INT, service_info=INFO_STR
    )

    # Check if info string is correctly set.
    assert (
        configuration.__getattribute__(aiserve.configuration.SERVICE_INFO) == INFO_STR
    )
