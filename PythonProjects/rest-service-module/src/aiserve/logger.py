"""Module implementing top level logger."""
import logging
import sys

import psutil

# Example of the format output:
# 2020-05-11 15:11:19 DEBUG    base.py:104 - some debug message
_FORMAT = "%(name)s %(levelname)s %(pathname)s:%(lineno)d - %(message)-s"
_DATEFORMAT = "%Y-%m-%d %H:%M:%S"


def configure_debug_logging() -> None:
    """Configure a logger with debugging messages."""
    logging.basicConfig(format=_FORMAT, datefmt=_DATEFORMAT, level=logging.DEBUG)


def configure_deployment_logging() -> None:
    """Configure a logger with no debugging messages."""
    logging.basicConfig(format=_FORMAT, datefmt=_DATEFORMAT, level=logging.INFO)


# Gunicorn specific constants
_GUNICORN_LOGGER_NAME = "gunicorn.error"
_GUNICORN_NAME = "gunicorn"


class _AiserveLogger(logging.Logger):
    """
    Create a high level logger for aiserve applications.

    Parameters
    ----------
    name
        Name of the logger.
    """

    _logger_configured = False
    _logging_level = None

    def __init__(self, name: str):
        self._process_name = psutil.Process().name()
        self._logger_name = name
        super().__init__(name)

    def set_logging_level(self, level: int):
        """
        Set the logging level.

        Notes
        -----
        Once this function is called, the instance of the logger gets initialised
        and configured.

        Parameters
        ----------
        level
            The logging level.
        """
        self._logging_level = level
        self.setLevel(level)
        if not self._logger_configured:
            self._configure_logger()

    def _add_stream_handler(self):
        """Add stream handler and formatters to current logger."""
        formatter = logging.Formatter(fmt=_FORMAT, datefmt=_DATEFORMAT)
        streamhandler = logging.StreamHandler(sys.stderr)
        streamhandler.setLevel(self._logging_level)
        streamhandler.setFormatter(formatter)
        self.addHandler(streamhandler)

    def _extend_logging_to_gunicorn(self):
        """Add Gunicorn handlers to current logger."""
        gunicorn_logger = logging.getLogger(_GUNICORN_LOGGER_NAME)
        gunicorn_handler = gunicorn_logger.handlers[0]
        gunicorn_handler.setLevel(logging.WARN)
        self.handlers.extend(gunicorn_logger.handlers)

    def _configure_logger(self):
        """Configure the logger."""
        self._add_stream_handler()

        if self._process_name == _GUNICORN_NAME:
            self._extend_logging_to_gunicorn()

        self._logger_configured = True


# Create a singleton that is shared and only initialised once.
_AISERVELOGGER = _AiserveLogger(__name__)


def get_logger() -> logging.Logger:
    """
    Access the singleton.

    Returns
    -------
    logging.Logger
        The singleton.
    """
    return _AISERVELOGGER
