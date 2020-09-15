"""Test creation of an application."""
# pylint: disable=redefined-outer-name
import json
import time

import backapp
import dummyprocessor
import flask
import flask_api  # type: ignore
import pytest  # type: ignore
import requests

import aiserve.appcreate

ENVVAR_CONTEND = {
    "model_path_url_env": "/dummy/path",
    "post_endpoint_env": "/post",
    "get_endpoint_env": "/get",
    "service_info_env": "info",
    "processor_batch_size_env": "10",
    "logging_env": "INFO",
    "sso_url_env": "/sso/url",
}

ENVVAR_CONTEND_SYNC = {
    "model_path_url_env": "/dummy/path",
    "post_endpoint_env": "/post",
    "service_info_env": "info",
    "logging_env": "INFO",
    "sso_url_env": "/sso/url",
    "execution_mode": "SYNC",
}


@pytest.fixture
def environment_async(monkeypatch):
    """Mock up an environment for config parsing via envvars."""
    for env, value in ENVVAR_CONTEND.items():
        monkeypatch.setenv(env, value)

    return monkeypatch


@pytest.fixture
def environment_sync(monkeypatch):
    """Mock up an environment for config parsing via envvars."""
    for env, value in ENVVAR_CONTEND_SYNC.items():
        monkeypatch.setenv(env, value)

    return monkeypatch


_SLEEP_TIME = 0.01


def test_app_envvar_single_processor(environment_async):
    """
    Check if minimal configuration creates a flask.Flask.

    Parameters
    ----------
    environment
        Mocks up an environment for config parsing via envvars.

    """
    # pylint: disable=unused-argument
    app = aiserve.appcreate.app_envvar_single_processor(
        model_path_url_env="model_path_url_env",
        post_endpoint_env="post_endpoint_env",
        get_endpoint_env="get_endpoint_env",
        processor_class=dummyprocessor.DummyProcessor,
    )

    assert isinstance(app, flask.Flask)

    with app.test_client() as client:
        # Check if post endpoint works.
        post_response = client.post("/post")
        assert post_response.status_code == flask_api.status.HTTP_200_OK

        # Extract id for request.
        response_dict = json.loads(post_response.data)
        result_id = response_dict["request-id"]

        # Check if get endpoint works after post.
        time.sleep(_SLEEP_TIME)
        get_response = client.get(f"/get?request-id={result_id}")
        assert get_response.status_code == flask_api.status.HTTP_200_OK


def test_app_envvar_single_processor_for_sync(environment_sync):
    """
    Check if minimal configuration creates a flask.Flask.

    Parameters
    ----------
    environment_sync
        Mocks up an environment for config parsing for sync.

    """
    # pylint: disable=unused-argument
    app = aiserve.appcreate.app_envvar_single_processor(
        model_path_url_env="model_path_url_env",
        post_endpoint_env="post_endpoint_env",
        processor_class=dummyprocessor.DummyProcessor,
        execution_mode_env="execution_mode",
    )

    assert isinstance(app, flask.Flask)

    with backapp.BackApp(app):
        # Check if post endpoint works.
        post_response = requests.post("http://127.0.0.1:5000/post")
        assert post_response.status_code == flask_api.status.HTTP_200_OK
