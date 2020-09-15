"""Test suite for the remote folder (remfolder) module."""
# pylint: disable=redefined-outer-name

import pathlib
import shutil
import subprocess
from typing import Iterator
import unittest.mock

import httptest
import pytest  # type: ignore

import aiserve
import aiserve.remfolder

_NEW_FOLDER_NAME = "longer/more/complex/name/new"
_TMP_FILE_NAME = "temp_file"
_TEST_PORT = 8111


@pytest.fixture
def tmp_folder_path(tmp_path) -> Iterator[pathlib.Path]:
    """
    Set up a temporary folder containing a dummy folder structure and files.

    The integrated temp_file fixture is not used as the folder structure has to be
    quit complex.

    Yields
    ------
    pathlib.Path
        The path to the created folder.
    """
    # Define paths of dummy folders and files in the temporary folder.
    tmp_folder = tmp_path / "remfolder"
    tmp_folder.mkdir()
    tmp_subfolder_path = tmp_folder.joinpath("subfolder")
    tmp_file_path = tmp_folder.joinpath(_TMP_FILE_NAME)
    tmp_subfolder_file_path = tmp_subfolder_path.joinpath("temp_file.txt")
    tmp_md5_file_path = tmp_folder.joinpath("md5sum.txt")

    # Create temporary subfolder and files.
    tmp_subfolder_path.mkdir()
    tmp_file_path.touch()
    tmp_subfolder_file_path.touch()

    # Calculate md5 sum for all files contained in the folder structure ans store
    # result in the md5sum.txt file.
    with tmp_md5_file_path.open("w") as file_stream:
        subprocess.call(["md5sum", "./temp_file"], stdout=file_stream, cwd=tmp_folder)
        subprocess.call(
            ["md5sum", "./subfolder/temp_file.txt"], stdout=file_stream, cwd=tmp_folder
        )

    # yield the temporary path, don't use return to allow later teardown.
    yield tmp_folder
    # Teardown of created folder to keep /tmp clean.
    shutil.rmtree(tmp_folder)


@pytest.fixture
def new_folder_path(tmp_path):
    """Create path to directory inside a empty directory."""
    return tmp_path.joinpath(_NEW_FOLDER_NAME)


def test_is_valid_folder_md5(tmp_folder_path):
    """
    Test the nominal case of a md5 check that is successful.

    Parameters
    ----------
    tmp_folder_path
        Path to a temporary folder created via fixture.
    """
    assert aiserve.remfolder.is_valid_folder_md5(
        tmp_folder_path, tmp_folder_path.joinpath("md5sum.txt")
    )


def test_is_valid_folder_md5_string_input(tmp_folder_path):
    """
    Test a md5 check.

    Test the nominal case of a md5 check that is successful with str as input
    parameters.

    Parameters
    ----------
    tmp_folder_path
        Path to a temporary folder created via fixture.
    """
    assert aiserve.remfolder.is_valid_folder_md5(
        str(tmp_folder_path), str(tmp_folder_path.joinpath("md5sum.txt")),
    )


def test_is_valid_folder_md5_no_match_new_file(tmp_folder_path):
    """Test the nominal case of a md5 check that fails due to new file in directory."""
    # Create a new file in the director.
    tmp_folder_path.joinpath("new_file.txt").touch()

    assert not aiserve.remfolder.is_valid_folder_md5(
        tmp_folder_path, tmp_folder_path.joinpath("md5sum.txt")
    )


def test_is_valid_folder_md5_file_not_exist(tmp_folder_path):
    """
    Test for correct exception raise when md5sum file does not exist.

    Parameters
    ----------
    tmp_folder_path
        Path to a temporary folder created via fixture.
    """
    with pytest.raises(FileNotFoundError):
        aiserve.remfolder.is_valid_folder_md5(
            tmp_folder_path, tmp_folder_path.joinpath("not_existing_file"),
        )


def test_is_valid_folder_md5_folder_not_exist(tmp_folder_path):
    """
    Test for correct exception raise when target folder is missing.

    Parameters
    ----------
    tmp_folder_path
        Path to a temporary folder created via fixture.
    """
    with pytest.raises(NotADirectoryError):
        aiserve.remfolder.is_valid_folder_md5(
            tmp_folder_path.joinpath("not_existing_folder.zxz"),
            tmp_folder_path.joinpath("md5sum.txt"),
        )


@unittest.mock.patch("aiserve.remfolder.subprocess.call")
def test_call_wget(wget_mock):
    """
    Test if the correct subprocess call is created.

    This test case has a problem: It uses knowledge of the internal working ok
    call_wget. The alternative would be
    to perform an actual download and check for file existence. An actual download
    brings in more error sources
    than the actual function and constraints the test to only succeed if an internet
    connection is present.
    The whitebox testing strategy is choose as it seems to be the lesser of two evils.

    Parameters
    ----------
    wget_mock
        Mock mocking the subprocess.call function.
    """
    # Mock the subprocess call to a succeeding call.
    wget_mock.return_value = 0

    aiserve.remfolder.call_wget(["additional_parameters"])

    # Check if the correct subprocess call was made
    wget_mock.assert_called_with(["wget", "additional_parameters"])


@unittest.mock.patch("aiserve.remfolder.subprocess.call")
def test_call_wget_fail(wget_mock):
    """
    Test if correct exections are raised on failed wget.

    This test case has a problem: It uses knowledge of the internal working ok
    call_wget. The alternative would be
    to perform an actual download and check for file existence. An actual download
    brings in more error sources
    than the actual function and constraints the test to only succeed if an internet
    connection is present.
    The whitebox testing strategy is choose as it seems to be the lesser of two evils.

    Parameters
    ----------
    wget_mock
        Mock mocking the subprocess.call function.
    """
    # The first possible exit code is skipped as it is succeeding.
    possible_exit_code_list = range(1, len(aiserve.remfolder.WgetExitCode))

    # Iterate over all failing exit codes and check if the correct exception is raised.
    for exit_code in possible_exit_code_list:
        wget_mock.return_value = exit_code

        with pytest.raises(aiserve.remfolder.WgetError) as error:
            aiserve.remfolder.call_wget([])

            # Check if exception contains correct exit code in exit_code field
            assert error.exit_code == aiserve.remfolder.WgetExitCode(exit_code)


def test_build_remote_url():
    """Test if the remote url is build correctly."""
    remote_path = "RemotePath"
    remote_user = "RemoteUser"
    remote_password = "RemotePassword"

    url = aiserve.remfolder.build_remote_url(remote_path, remote_user, remote_password)

    assert url == f"https://{remote_user}:{remote_password}@{remote_path}"


def test_build_remote_url_not_ssl():
    """Test if the remote url is build correctly without ssl."""
    remote_path = "RemotePath"
    remote_user = "RemoteUser"
    remote_password = "RemotePassword"

    url = aiserve.remfolder.build_remote_url(
        remote_path, remote_user, remote_password, False
    )

    assert url == f"http://{remote_user}:{remote_password}@{remote_path}"


def test_build_remote_url_not_user():
    """Test if the remote url is build correctly without authentication."""
    remote_path = "RemotePath"

    url = aiserve.remfolder.build_remote_url(remote_path, None, None)

    assert url == f"https://{remote_path}"


def test_call_wget_real_server(tmp_folder_path, tmp_path):
    """
    Test the nominal case of wget with a real server.

    Parameters
    ----------
    tmp_folder_path
        Path to a temporary folder created via fixture.
    tmp_path
        Path to an empty temporary path as download target.
    """
    # Start a real http server in the temporary folder.
    # The path to the script to start the server is build relative to this file.
    with httptest.MockHttp(tmp_folder_path, port=_TEST_PORT):
        aiserve.remfolder.call_wget(
            ["-P", str(tmp_path), f"http://localhost:{_TEST_PORT}/{_TMP_FILE_NAME}"]
        )

    assert tmp_path.joinpath("temp_file").is_file()


def test_is_valid_local_remote_md5(tmp_folder_path):
    """
    Test the nominal case of the check succeeding.

    Parameters
    ----------
    tmp_folder_path
        Path to a temporary folder created via fixture.
    """
    # Start a real http server in the temporary folder.
    # The path to the script to start the server is build relative to this file.
    with httptest.MockHttp(tmp_folder_path, port=_TEST_PORT):
        check_ok = aiserve.remfolder.is_valid_local_remote_md5(
            f"localhost:{_TEST_PORT}/", None, None, tmp_folder_path, False
        )
    assert check_ok


def test_sync_from_remote(tmp_folder_path, new_folder_path):
    """
    Test the nominal case of fetching a remote folder with success.

    Parameters
    ----------
    tmp_folder_path
        Path to a temporary folder created via fixture.
    new_folder_path
        Path to an empty temporary path as download target.
    """
    # pylint: disable=import-outside-toplevel
    import filecmp

    with httptest.MockHttp(tmp_folder_path, port=_TEST_PORT):
        aiserve.remfolder.sync_from_remote(
            f"localhost:{_TEST_PORT}/", None, None, new_folder_path, False
        )

    # Check if the files in both folders where coped correctly, ignoring md5sum.txt.
    origin_subfolder_path = tmp_folder_path.joinpath("subfolder")
    target_subfolder_path = new_folder_path.joinpath("subfolder")
    for path_1, path_2 in [
        (tmp_folder_path, new_folder_path),
        (origin_subfolder_path, target_subfolder_path),
    ]:
        dir_comperator = filecmp.dircmp(path_1, path_2, ignore=["md5sum.txt"])
        assert dir_comperator.right_only == []
        assert dir_comperator.left_only == []


def test_sync_from_remote_cached(tmp_folder_path, new_folder_path):
    """
    Test the case that the folder was already downloaded and is only checked for md5.

    Parameters
    ----------
    tmp_folder_path
        Path to a temporary folder created via fixture.
    new_folder_path
        Path to an empty temporary path as download target.
    """
    # pylint: disable=import-outside-toplevel
    import filecmp

    with httptest.MockHttp(tmp_folder_path, port=_TEST_PORT):
        aiserve.remfolder.sync_from_remote(
            f"localhost:{_TEST_PORT}/", None, None, new_folder_path, False
        )
        aiserve.remfolder.sync_from_remote(
            f"localhost:{_TEST_PORT}/", None, None, new_folder_path, False
        )

    # Check if the files in both folders where coped correctly, ignoring md5sum.txt.
    origin_subfolder_path = tmp_folder_path.joinpath("subfolder/")
    target_subfolder_path = new_folder_path.joinpath("subfolder/")
    for path_1, path_2 in [
        (tmp_folder_path, new_folder_path),
        (origin_subfolder_path, target_subfolder_path),
    ]:
        dir_comperator = filecmp.dircmp(path_1, path_2, ignore=["md5sum.txt"])
        assert dir_comperator.right_only == []
        assert dir_comperator.left_only == []


def test_sync_from_remote_cached_no_match(tmp_folder_path, new_folder_path):
    """
    Test the case that the folder was already downloaded and changed.

    Parameters
    ----------
    tmp_folder_path
        Path to a temporary folder created via fixture.
    new_folder_path
        Path to an empty temporary path as download target.
    """
    with httptest.MockHttp(tmp_folder_path, port=_TEST_PORT):
        aiserve.remfolder.sync_from_remote(
            f"localhost:{_TEST_PORT}/", None, None, new_folder_path, False
        )
        new_folder_path.joinpath("edit.txt").touch()
        with pytest.raises(aiserve.remfolder.Md5Error):
            aiserve.remfolder.sync_from_remote(
                f"localhost:{_TEST_PORT}/", None, None, new_folder_path, False
            )


def test_sync_from_remote_no_md5(tmp_folder_path, new_folder_path):
    """
    Test the case that the folder was already downloaded and changed.

    Parameters
    ----------
    tmp_folder_path
        Path to a temporary folder created via fixture.
    new_folder_path
        Path to an empty temporary path as download target.
    """
    # Remove the remote md5sum.txt to trigger exception.
    tmp_folder_path.joinpath("md5sum.txt").unlink()

    with httptest.MockHttp(tmp_folder_path, port=_TEST_PORT):
        with pytest.raises(aiserve.remfolder.Md5Error):
            aiserve.remfolder.sync_from_remote(
                f"localhost:{_TEST_PORT}/", None, None, new_folder_path, False
            )


@unittest.mock.patch("aiserve.remfolder.subprocess.call")
def test_sync_from_remote_cached_wget_error(wget_mock, tmp_folder_path, tmp_path):
    """
    Test the case that the folder was already downloaded and changed.

    Parameters
    ----------
    wget_mock
        Mock for the subprocess.call method.
    tmp_folder_path
        Path to a temporary folder created via fixture.
    tmp_path
        Path to an empty temporary path as download target.
    """
    # The first possible exit code is skipped as it is succeeding.
    possible_exit_code_list = range(1, len(aiserve.remfolder.WgetExitCode))

    # Iterate over all failing exit codes and check if the correct exception is raised.
    for exit_code in possible_exit_code_list:
        wget_mock.return_value = exit_code

        with httptest.MockHttp(tmp_folder_path, port=_TEST_PORT):
            with pytest.raises(aiserve.remfolder.WgetError) as error:
                aiserve.remfolder.sync_from_remote(
                    f"localhost:{_TEST_PORT}/", None, None, tmp_path, False
                )

                # Check if exception contains correct exit code in exit_code field
                assert error.exit_code == aiserve.remfolder.WgetExitCode(exit_code)


def test_sync_from_remote_corrupted_download(tmp_folder_path, new_folder_path):
    """
    Test if the correct exception is raised if md5 does not match after download.

    An download error is forced by simply adding a file as "corruption" to the source
    folder. The md5sum.txt in that
    folder still has the old contend -> the exception should be triggered.

    Parameters
    ----------
    tmp_folder_path
        Path to a temporary folder created via fixture.
    new_folder_path
        Path to an empty temporary path as download target.
    """
    tmp_folder_path.joinpath("mock_corruption.txt").touch()

    with httptest.MockHttp(tmp_folder_path, port=_TEST_PORT):
        with pytest.raises(aiserve.remfolder.Md5Error):
            aiserve.remfolder.sync_from_remote(
                f"localhost:{_TEST_PORT}/", None, None, new_folder_path, False
            )
