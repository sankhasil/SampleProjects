"""Implements a flask app factory that works with subclasses of BaseProcessor."""

import functools
from typing import Any, Callable, Dict, Union

import flask
import flask_api.status  # type: ignore
import flask_restful  # type: ignore
import requests
import werkzeug.exceptions
import werkzeug.middleware.proxy_fix

import aiserve.async_request_handler
import aiserve.configuration
import aiserve.inforesource
import aiserve.logger
import aiserve.preserver
import aiserve.process
import aiserve.resources

LOGGER = aiserve.logger.get_logger()

_INFO_ENDPOINT_NAME = "/info"


def _get_flask_app_with_configuration(
    configuration: aiserve.configuration.ServiceConfiguration,
) -> flask.Flask:
    """Create a Flask app and load the configuration."""
    app = flask.Flask(__name__)
    # Load configuration into flask app.
    app.config.from_object(configuration)
    LOGGER.debug(f"After adding the configuration to app.config {app.config}")
    return app


def _create_a_flask_restful_api_with_decorators(app) -> flask_restful.Api:
    """Add the relevant decorators to flask app."""
    api_decorators = []

    if app.config[aiserve.configuration.SECURITY_USE_SSO]:
        service_path = app.config[aiserve.configuration.SECURITY_SSO_SERVICE]
        api_decorators.append(build_security_check_func(service_path))

    api = flask_restful.Api(app, decorators=api_decorators)

    return api


def _add_flask_resources_to_api(
    api: flask_restful.Api,
    resource_class_kwargs: Dict[str, Any],
    endpoints: Dict[
        str,
        Union[
            aiserve.resources.ResultRetrievalResource,
            aiserve.resources.RequestPostingResourceSync,
            aiserve.resources.RequestPostingResource,
        ],
    ],
) -> flask_restful.Api:
    """Loop through resources and add them to Api."""
    for endpoint, flask_resource in endpoints.items():
        api.add_resource(
            flask_resource,
            endpoint,
            endpoint=endpoint,
            resource_class_kwargs=resource_class_kwargs,
        )
    return api


def _add_info_resource_to_api(
    api: flask_restful.Api, configuration: aiserve.configuration.ServiceConfiguration
) -> flask_restful.Api:
    """Add the info resource to the api."""
    api.add_resource(
        aiserve.inforesource.create_info_resource(configuration),
        _INFO_ENDPOINT_NAME,
        endpoint=_INFO_ENDPOINT_NAME,
    )
    return api


def _add_prosifix_to_app(app: flask.Flask) -> flask.Flask:
    """Add prosifix to app for gunicorn."""
    app.wsgi_app = werkzeug.middleware.proxy_fix.ProxyFix(  # type: ignore
        app.wsgi_app
    )
    return app


def create_app(
    configuration: aiserve.configuration.ServiceConfiguration, use_proxifix: bool = True
) -> flask.Flask:
    """
    Create a flask.Flask (app) from a ServiceConfiguration.

    The service configuration provides a endpoint->resource mapping, logging
    configuration and sso configuration.

    Parameters
    ----------
    configuration
        Contains the service configuration.
    use_proxifix
        Whether the proxifix for gunicorn should be used.

    Returns
    -------
        flask.Flask (app) with all the resources added to the specified endpoints.
    """
    # pylint: disable=no-member

    app = _get_flask_app_with_configuration(configuration)

    # Create api from app and add api level decorator for sso authentication if it is
    # activated in the configuration.
    api = _create_a_flask_restful_api_with_decorators(app)

    # Set the logging level as configured in the flask app configuration.
    app.logger.setLevel(app.config[aiserve.configuration.LOGGING_LEVEL])

    # Add all specified resources to the specified endpoints.
    resources_configuration = app.config[aiserve.configuration.RESOURCE_CONFIGURATION]

    for resource in resources_configuration:
        LOGGER.debug(f"Adding {resource} to api")
        LOGGER.debug(
            f"The value of the resource_cls_kwargs dict "
            f"is {resource.resource_cls_kwargs.asdict()}"
        )
        _add_flask_resources_to_api(
            api=api,
            resource_class_kwargs=resource.resource_cls_kwargs.asdict(),
            endpoints=resource.endpoints,
        )

    _add_info_resource_to_api(api=api, configuration=configuration)

    # This introduces a proxy fix to for compatibility with gunicorn.
    if use_proxifix:
        _add_prosifix_to_app(app)

    return app


def build_security_check_func(path_to_sso: str) -> Callable:
    """
    Implement a Decorator factory.

    Create a security decorator given the path to a sso service.

    Parameters
    ----------
    path_to_sso
        https to a sso service endpoint.

    Returns
    -------
    callable
        A decorator that can be used to add security on api and resource level.
    """

    def security_decorator(function_to_be_wrapped: Callable) -> Callable:
        """
        Decorate a function adding sso authentication to it.

        Parameters
        ----------
        function_to_be_wrapped
            A routed function that will be decorated to include security.

        Returns
        -------
        callable
            The decorated function.

        """

        @functools.wraps(function_to_be_wrapped)
        def wrapper(*args, **kwargs):
            """
            Wrap a function adding authentication to it.

            Returns
            -------
            str
                The return value of the decorated function if authentication was
                successful, an error code if not.
            """
            # Check the sso token from the request using the sso service.
            request_token = flask.request.headers.get("Authorization")
            sso_response = requests.get(
                path_to_sso, headers={"Authorization": request_token}
            )

            # If token gets authorization advance tu decorated function, else return
            # HTTP error.
            if sso_response.status_code == flask_api.status.HTTP_200_OK:
                return function_to_be_wrapped(*args, **kwargs)

            raise werkzeug.exceptions.Unauthorized()

        return wrapper

    return security_decorator
