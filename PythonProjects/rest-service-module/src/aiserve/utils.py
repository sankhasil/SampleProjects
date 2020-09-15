"""Module for utility functions to be reused."""

import logging
import os
from typing import Optional

import aiserve.configuration
import aiserve.logger
import aiserve.process  # pylint: disable=unused-import

LOGGER = aiserve.logger.get_logger()


def convert_env_var_to_int(env_var_name: Optional[str], default_value: int = 1) -> int:
    """
    Convert the environment variable to integer.

    If no environment variable name has been supplied or the
    environment variable is not defined at startup returns 1
    If a negative value has been supplied as the environment variable
    would raise a ValueError
    Otherwise tries to typecast the returned string into integer and returns it.

    Parameters
    ----------
    env_var_name: str
        String representing the name of the env variable
    default_value: int
        Just a default value to return if environment variable
        is not there

    Returns
    -------
    int
        1 or the integer value of batch_size supplied as environment variable

    Raises
    ------
    ValueError
        If the environment variable contains a negative value
    """
    LOGGER.debug(f"Checking the batch size from env variable {env_var_name}")
    if not env_var_name or not os.getenv(env_var_name):
        LOGGER.debug(f"Returning default value for batch size : {default_value}")
        return default_value
    env_value = int(os.getenv(env_var_name))  # type: ignore
    if env_value < 0:
        raise ValueError(f"{env_var_name} should have a value greater than 0")
    LOGGER.debug(f"The batch size extracted is {env_value}")
    return env_value


def get_execution_mode_value(env_var_name: str) -> str:
    """
    Get the value of execution mode from environment variable.

    Parameters
    ----------
    env_var_name:
        String representing the env variable name to reference

    Returns
    -------
        The execution mode string.
    """
    execution_mode_value = os.getenv(env_var_name)

    if execution_mode_value is None:
        LOGGER.warning(f"No value found for environment variable {env_var_name}")
        return aiserve.configuration.ASYNC_MODE_STR

    if execution_mode_value not in (
        aiserve.configuration.ASYNC_MODE_STR,
        aiserve.configuration.NON_ASYNC_MODE_STR,
    ):
        LOGGER.warning(
            f"Value of execution mode has to be either "
            f"{aiserve.configuration.ASYNC_MODE_STR} or "
            f"{aiserve.configuration.NON_ASYNC_MODE_STR}"
        )
        return aiserve.configuration.ASYNC_MODE_STR

    return execution_mode_value


def _string_to_logging_level(logging_level_string: str) -> int:
    """
    Transform a string representation of a logging level to one used by logging.

    This takes strings like "DEBUG" and transforms them to a logging level as used by
    the logging module. Input is cast to upper case to support "debug". If no legal
    string is passed "NOTSET" is returned.

    Parameters
    ----------
    logging_level_string
        String representation of a logging level.

    Returns
    -------
    int
        Logging level as used by the logging module.
    """
    logging_level_string_upper = logging_level_string.upper()
    logging_level: int = logging.getLevelName(logging_level_string_upper)
    if isinstance(logging_level, str):
        logging_level = logging.NOTSET

    return logging_level


def get_logging_level_from_envvar(log_env: str) -> int:
    """
    Get logging level from environment variables.

    Parameters
    ----------
    log_env
        Environment variable for logger level.

    Returns
    -------
    int
        Level of logger.
    """
    logging_level_input: str = os.environ.get(log_env, "NOTSET")
    return _string_to_logging_level(logging_level_input)
