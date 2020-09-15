"""
Provides a context manager that runs a flask app in the background.

A flask.Flask can be passed to the constructor of the context manager, the passed app
will then be run in a separate
not blocking process. This can be used for testing.
"""

import multiprocessing
import time

import flask

# This is needed to give the app time to properly set up before use, setting this to
# low results in failed tests.
SLEEP_TIME = 0.3


class BackApp:
    """Context manager to start a flask.Flask in a separate process."""

    def __init__(self, app: flask.Flask):
        """
        Start the specified app in a separate thread.

        Parameters
        ----------
        app
            The app that will be started.
        """
        self.app = app
        self.process = multiprocessing.Process(target=app.run)
        self.process.start()

    def __enter__(self):
        """
        Implement the context manager interface and ensure proper startup.

        Returns
        -------
        BackApp
            self
        """
        # Leave time for the app to start
        time.sleep(SLEEP_TIME)
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        """Implement the context manager interface and ensure proper shutdown."""
        self.process.terminate()
