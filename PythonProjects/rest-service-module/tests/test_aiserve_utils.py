"""Tests for the utils.py module in aiserve."""

import os

import pytest  # type: ignore

import aiserve.configuration as aiserve_config
import aiserve.utils


def test_batch_size_is_1_when_none_is_passed():
    """
    Checks is None is passed to the function.

    A default batch size of 1 is returned.
    """
    assert aiserve.utils.convert_env_var_to_int(None) == 1
    assert isinstance(aiserve.utils.convert_env_var_to_int(None), int)


@pytest.mark.parametrize(
    "processor_batch_size_env, correct_value", [("10", 10), (10, 10)]
)
def test_batch_size_is_correctly_typecasted(
    processor_batch_size_env, correct_value, mocker
):
    """
    Checks the correctly type casted value of batch size is returned.

    Checks for different values.

    Parameters
    ----------
    processor_batch_size_env: str/int
        Dummy value of processor_batch_size
    correct_value: int
        Correct value which should be returned from the function
    mocker
        Mocker object from pytest-mock
    """
    patched_getenv = mocker.patch("os.getenv")
    patched_getenv.return_value = processor_batch_size_env
    assert aiserve.utils.convert_env_var_to_int("BATCH_SIZE") == correct_value
    assert isinstance(aiserve.utils.convert_env_var_to_int("BATCH_SIZE"), int)
    assert os.getenv.called_once_with("BATCH_SIZE")  # pylint: disable=no-member


@pytest.mark.parametrize("processor_batch_size_env", ["Ten", "One", "1.5", "-1", "-20"])
def test_nonsensical_batch_size_raises_valueerror(processor_batch_size_env, mocker):
    """
    Checks that is raises TypeError.

    If any non sensical value is passed as batch_size the code raises TypeError.

    Parameters
    ----------
    processor_batch_size_env: str
        Value of processor batch size env variable name
    mocker
        Mocker object from pytest-mock
    """
    patched_getenv = mocker.patch("os.getenv")
    patched_getenv.return_value = processor_batch_size_env
    with pytest.raises(ValueError):
        aiserve.utils.convert_env_var_to_int("BATCH_SIZE")
    assert os.getenv.called_once_with("BATCH_SIZE")  # pylint: disable=no-member


def test_execution_mode_not_set_gives_default_value():
    """Test that the function get_execution_mode_value return default value."""
    default_value = aiserve_config.ASYNC_MODE_STR
    assert (
        aiserve.utils.get_execution_mode_value(env_var_name="EXECUTION_MODE")
        == default_value
    )


@pytest.mark.parametrize("value", ("Something", 1, 11, "Async", "async"))
def test_execution_mode_with_wrong_env_var_value(value, mocker):
    """Test the func get_execution_mode_value with wrong value for env var."""
    default_value = aiserve_config.ASYNC_MODE_STR
    patched_getenv_func = mocker.patch("os.getenv")
    patched_getenv_func.return_value = value
    assert (
        aiserve.utils.get_execution_mode_value(env_var_name="EXECUTION_MODE")
        == default_value
    )


@pytest.mark.parametrize("value", ("ASYNC", "SYNC"))
def test_get_execution_mode_with_correct_values_for_env_var(value, mocker):
    """Test the func get_execution_mode_value with correct value."""
    patched_getenv_func = mocker.patch("os.getenv")
    patched_getenv_func.return_value = value
    assert (
        aiserve.utils.get_execution_mode_value(env_var_name="EXECUTION_MODE") == value
    )
