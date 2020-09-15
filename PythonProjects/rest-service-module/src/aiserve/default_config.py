"""
Default configuration.

Used for the creation of a flask.Flask via RestAppFactory.create_app

This configuration is not meant to be actually used, it's only used to show the basic
structure of the needed
configuration and to alert the user of how to use aiserve if no other configuration
is specified.
"""

import logging

import flask
import flask_api  # type: ignore

LOGGING = {"log_level": logging.DEBUG}

# When use_sso is set to true sso authentication is activated.
SECURITY = {"use_sso": False, "sso_service_path": "https://sso.ai4bd.org/verify"}


def dummy_resource():
    """Inform the user on how to use aiserve and define a custom config.py."""
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


# Here resources with their target endpoint are defined.
RESOURCES = {"/": dummy_resource}
