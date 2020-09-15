"""Test suite for the upload module."""
# pylint: disable=protected-access
import pathlib

import httptest

import aiserve.upload_module

_TEST_PORT = 8111


def test_check_md5_true(tmp_folder_path):
    """
    Test checking md5sum file for a correct md5sum file.

    Parameters
    ----------
    tmp_folder_path_str_fixture
        Path to a temporary folder created via fixture.
    """
    assert aiserve.upload_module._check_md5(tmp_folder_path)


def test_check_md5_false(tmp_folder_path):
    """
    Test checking md5sum file for a non correct md5sum file.

    Parameters
    ----------
    tmp_folder_path
        Path to a temporary folder created via fixture.
    """
    tmp_folder_path = pathlib.Path(tmp_folder_path)
    tmp_file_path = tmp_folder_path.joinpath("temp_file2.txt")
    tmp_file_path.touch()
    assert not aiserve.upload_module._check_md5(tmp_folder_path)


def test_create_md5(tmp_folder_path):
    """
    Test creating md5sum file.

    Parameters
    ----------
    tmp_folder_path
        Path to a temporary folder created via fixture.
    """
    aiserve.upload_module._create_md5(tmp_folder_path)
    assert aiserve.upload_module._check_md5(tmp_folder_path)


def test_send_to_remote(mocker, tmp_folder_path):
    """
    Test checking sending file.

    Parameters
    ----------
    mocker
        Mocker.
    tmp_folder_path
        Path to a temporary folder created via fixture.
    """
    mock_send_folder = mocker.patch("aiserve.upload_module._send_folder")
    mock_send_folder.return_value = True
    tmp_folder_path = pathlib.Path(tmp_folder_path)
    tmp_file_path = tmp_folder_path.joinpath("temp_file3.txt")
    tmp_file_path.touch()
    with httptest.MockHttp(tmp_folder_path, port=_TEST_PORT):
        aiserve.upload_module.send_to_remote(
            input_path=tmp_folder_path,
            user=None,
            server=None,
            remote_path=f"localhost:{_TEST_PORT}/",
        )
    assert aiserve.upload_module._check_md5(tmp_folder_path)
