"""Test for logger module in aiserve."""
# pylint: disable=protected-access, redefined-outer-name
import pytest

import aiserve.logger


@pytest.fixture
def debug_logger():
    """Create logger with level debug."""
    logger = aiserve.logger._AiserveLogger("debug")
    logger.set_logging_level(10)
    return logger


@pytest.fixture
def info_logger():
    """Create logger with level info."""
    logger = aiserve.logger._AiserveLogger("info")
    logger.set_logging_level(20)
    return logger


@pytest.fixture
def unconfigured_logger():
    """Create logger with level info."""
    logger = aiserve.logger._AiserveLogger("info")
    return logger


def test_debug_logger(debug_logger):
    """Test logger of level debug."""
    assert debug_logger._logger_configured
    assert debug_logger._logging_level == 10
    assert debug_logger._logger_name == "debug"


def test_info_logger(info_logger):
    """Test logger of level info."""
    assert info_logger._logging_level == 20
    assert info_logger._logger_name == "info"


def test_unconfigured_logger(unconfigured_logger):
    """Test unconfigured logger."""
    assert not unconfigured_logger._logger_configured
