"""
Module that implements a context manager.

It can be used to spin up a ssl service for testing.
"""

import argparse
import subprocess
import time

import flask
import flask_api.status  # type: ignore
import flask_restful  # type: ignore

# This is needed to give the Mock time to properly setup, setting this to low results
# in failed tests.
SLEEP_TIME = 0.5
DEFAULT_PORT = 5000


class MockSsl:
    """Context manager to mock a ssl service."""

    def __init__(self, port: int = DEFAULT_PORT):
        port_str = str(port)
        self.process = subprocess.Popen(["python3", __file__, "--port", port_str])

    def __enter__(self):
        """Implement the context manager interface and ensures proper startup."""
        time.sleep(SLEEP_TIME)
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        """Implement the context manager interface."""
        self.process.terminate()


class SslResource(flask_restful.Resource):
    """
    Resource that can be used to mock a SSL service.

    Only returns 200 when the send token is TRUE.
    """

    @staticmethod
    def get() -> flask.Response:
        """
        Return if an authorization request is valid.

        Returns
        -------
        flask.Response
            flask_api.status.HTTP_200_OK or flask_api.status.HTTP_401_UNAUTHORIZED
        """
        request_token = flask.request.headers.get("Authorization")
        if request_token == "TRUE":
            return flask.Response(status=flask_api.status.HTTP_200_OK)

        return flask.Response(status=flask_api.status.HTTP_401_UNAUTHORIZED)


def create_run_ssl(port: int = DEFAULT_PORT):
    """
    Create and runs a mock ssl server on localhost.

    Parameters
    ----------
    port
        Port the service runs on.
    """
    app = flask.Flask(__name__)
    api = flask_restful.Api(app)
    api.add_resource(SslResource, "/")

    app.run(debug=True, port=port)


def main():
    """Parse the command line and run a ssl dummy."""
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--port",
        type=int,
        help="Port the mock ssl service will be run on.",
        default=DEFAULT_PORT,
    )
    args = parser.parse_args()
    create_run_ssl(args.port)


if __name__ == "__main__":
    main()
