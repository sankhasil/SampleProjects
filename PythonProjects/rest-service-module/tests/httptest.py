"""
Script that spins up a http server from a local folder.

This is used to test wget functionality, there are a number of pytest plugins that
try to solve the problem of mocking
a http server, but non of them spins up a real server that can be accessed by
external programs.


Example
-------
The server can be run from the test module using the following code line:

    with tests.httptest.MockHttp(tmp_folder_path, port=TEST_PORT):
        --- code using the server ---
"""

import argparse
import http.server
import os
import pathlib
import socketserver
import subprocess
import time
from typing import Union

# This is needed to give the Mock time to properly setup, setting this to low results
# in failed tests.
SLEEP_TIME = 0.5
DEFAULT_PORT = 8111


class MockHttp:
    """
    Implements a mocking http server.

    It runs on a local folder as context manager.
    """

    def __init__(self, folder_path: Union[str, pathlib.Path], port: int = DEFAULT_PORT):
        """
        Start a http server in a subprocess using a local folder and specified port.

        To start the server this method calls this file in a subprocess as a script.
        This runs the script specified
        in the if __name__ == "__main__": clause.

        Parameters
        ----------
        folder_path
            Path to the folder used as root for the server.
        port
            Local port the server will listen to.
        """
        # Ensure pathlib.Path and str can be used for folder_path.
        folder_path_str = str(folder_path)
        # Start a http server in a subprocess.
        self.sub = subprocess.Popen(
            ["python3", __file__, "--port", str(port), folder_path_str]
        )

    def __enter__(self):
        """
        Implement the context manager interface.

        It ensures that the http server has enough time to set up.

        Returns
        -------
        MockHttp
            self
        """
        # Leave time for the http server to set up.
        time.sleep(SLEEP_TIME)
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        """
        Implement the context manager interface.

        It ensures proper shutdown of the run http server.
        """
        self.sub.terminate()


def mock_sftp(port=DEFAULT_PORT):
    """
    Run a http server from the current directory.

    Parameters
    ----------
    port
        Port the server is run on.
    """
    handler = http.server.SimpleHTTPRequestHandler

    # Allow address reuse to prevent collision when executed in fast iteration.
    socketserver.TCPServer.allow_reuse_address = True

    with socketserver.TCPServer(("", port), handler) as httpd:

        # Start the actual server and try to close it safely.
        try:
            httpd.serve_forever()
        finally:
            httpd.server_close()


if __name__ == "__main__":
    # Parse path to the folder the http server will run from.
    PARSER = argparse.ArgumentParser()
    PARSER.add_argument("path", type=str, help="Root folder of the http server.")
    PARSER.add_argument(
        "--port", type=int, help="Port the server will run on.", default=DEFAULT_PORT
    )
    ARGS = PARSER.parse_args()

    # Change to target folder and run.
    os.chdir(ARGS.path)
    mock_sftp(port=ARGS.port)
