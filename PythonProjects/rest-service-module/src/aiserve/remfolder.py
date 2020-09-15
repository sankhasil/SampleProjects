"""
Module that implements the possibility to cache a remote folder to a local folder.

This module makes use of wget to download remote content and is therefore not very
portable, another implementation
using urllib seems possible but would be connected to a lot more work, and i'm not
sure this work would pay of as
our deploy environment is highly controlled via docker.
"""
# The following is disabled but it should be enabled at the next refactoring
# pylint: disable=attribute-defined-outside-init

import enum
import hashlib
import pathlib
import shutil
import subprocess
import tempfile
from typing import Union
import urllib

import aiserve.logger

LOGGER = aiserve.logger.get_logger()

ROOT_SYNC_FOLDER = "/tmp/aiserve/remote_model_sync"


class WgetExitCode(enum.Enum):
    """
    Definition of the exit codes returned by wget.

    Also can be found here: https://www.gnu.org/software/wget/manual/html_node/Exit
    -Status.html
    """

    OK = 0
    GENERIC_ERROR = 1
    PARSER_ERROR = 2
    FILE_IO_ERROR = 3
    NETWORK_FAILURE = 4
    SSL_FAILURE = 5
    AUTHENTICATION_FAILURE = 6
    PROTOCOL_ERROR = 7
    INTERNAL_SERVER_ERROR = 8


class WgetError(Exception):
    """Exception raised on wget errors, includes the exit wget exit code."""

    def __init__(self, message: str, exit_code: WgetExitCode):
        """
        Init exception with exit code.

        Parameters
        ----------
        message
            Exception message, is passed to super.
        exit_code
            Exit code specifying the nature of the error/failure.
        """
        super().__init__(message)
        self.exit_code = exit_code


class Md5Error(Exception):
    """Raised if there is a problem with md5 checksum verification."""


def sync_from_remote(
    remote_path: str,
    remote_user: str,
    remote_password: str,
    local_path: Union[str, pathlib.Path],
    ssl: bool = True,
):
    """
    Match a remote folder to a local folder using md5 for verification.

    Parameters
    ----------
    remote_path
        Url to the remote download server.
    remote_user
        User used for authentication on remote server.
    remote_password
        Password used for authentication on remote server.
    local_path
        Local path the remote folder will be matched to.
    ssl
        http or https

    Raises
    ------
    Md5Error
        Is raised if there are problems checking or downloading the md5sum.txt file.
    WgetError
        Is raised if the call to wget produces an error/failure code.

    """
    # Ensure that local_path is of type pathlib.Path, this allows the user to also
    # pass a simple str.
    local_path = pathlib.Path(local_path)

    # Check if the path already exists, if yes the path is considered cached and not
    # downloaded.
    if local_path.exists():

        # Check local folder using the remote md5sum.txt file.
        try:
            md5_ok = is_valid_local_remote_md5(
                remote_path, remote_user, remote_password, local_path, ssl
            )

        except WgetError as error:
            error.message = (  # type: ignore
                f"The download of the remote md5sum.txt file failed with "
                f"the exit code "
                f"{WgetExitCode(error.exit_code)}."
            )
            raise error

        # If verification is successful early return is possible, else an md5 error
        # is raised.
        if md5_ok:
            return

        raise Md5Error(
            "The local folder was compared to the remote md5sum.txt and the checksums "
            "don't match. \n"
            "Possible error sources: \n"
            "1. The local folder was changed -> remove the local folder. \n"
            "2. The remote md5sum.txt has changed -> remove the local folder. \n"
            "3. The local folder exists with other contend -> choose another local "
            "folder. \n"
            "4. An earlier download attempt was interrupted -> remove the local folder."
        )

    # If the local folder does not exist it gets downloaded from the specified
    # remote. A temporary directory is used
    # to store the downloaded data till it is verified.
    with tempfile.TemporaryDirectory() as tmp_dir:

        # Make a call to wget and wait for the subprocess to return. This may raise a
        # WgetError which i
        # intentionally don't catch.
        remote_url = build_remote_url(remote_path, remote_user, remote_password, ssl)
        call_wget(
            [
                "-R",
                "index.html*",
                "--level=10",
                "--recursive",
                "--no-parent",
                "-P",
                tmp_dir,
                remote_url,
            ]
        )

        # Create paths to temporary folder and md5sum.txt as helper variables.
        temporary_folder_path = pathlib.Path(tmp_dir, remote_path)
        temporary_md5_path = pathlib.Path(temporary_folder_path, "md5sum.txt")

        try:
            # Check if md5 of downloaded files match to the ones in the downloaded
            # md5sum.txt file.
            md5_ok = is_valid_folder_md5(temporary_folder_path, temporary_md5_path)

            if md5_ok:
                # If md5 check is successful the md5sum.txt is removed.
                temporary_md5_path.unlink()
                # The existence of the local parent folder is ensured.
                local_path.parent.mkdir(exist_ok=True, parents=True)
                # And the temporary folder is copied from the temporary location to
                # the target local path.
                shutil.copytree(temporary_folder_path, local_path)
                return

            # If the md5 check fails a Md5Error is raised.
            raise Md5Error(
                "The folder was downloaded but the downloaded files don't match "
                "to the remote md5. \n"
                "Please verify the remote md5sum.txt file is correct and try "
                "again."
            )

        except FileNotFoundError as error:
            # A FileNotFoundError indicates that the md5sum.txt was not found,
            # most probable reason is that it does not
            # exist in te remote location.
            raise Md5Error(
                "The remote folder could be downloaded, but seems to contain no "
                "md5sum.txt file. \n"
                "Please add a md5sum.txt file to the remote location using the "
                "following command: \n"
                "find -type f \\( -not -name \"md5sum.txt\" \\) -exec md5sum '{}' \\; "
                "> md5sum.txt"
            ) from error

        except shutil.Error as error:
            # Error copying files over, try to clean up.
            shutil.rmtree(local_path)
            raise error


def is_valid_folder_md5(
    folder_path: Union[str, pathlib.Path], md5_path: Union[str, pathlib.Path]
) -> bool:
    """
    Check if the folder is valid.

    Generate md5 for all files in a folder and its subfolders and compare to a file
    containing md5 for all of them.

    Parameters
    ----------
    folder_path
        Path to the folder that the check shall be run on.
    md5_path
        Path to the file containing the target md5 for all of the files.

    Returns
    -------
    bool
        True if all md5 match, False otherwise.

    Raises
    ------
    FileNotFoundError
        Is raised if the target file or folder does not exist.
    """
    # Ensure paths are pathlib.Path and still allow the user tu pass a str.
    folder_path = pathlib.Path(folder_path)
    md5_path = pathlib.Path(md5_path)

    # Test if folder and file exist.
    if not md5_path.exists():
        raise FileNotFoundError(f"The specified file does not exist: {md5_path}")
    if not folder_path.is_dir():
        raise NotADirectoryError(f"The specified path is no directory: {folder_path}")

    # Generate md5 for all files below the folder_path and store the relative paths
    # in a dict.
    folder_md5 = {}
    for file_path in filter(
        lambda path: path.is_file(), folder_path.rglob("*[" "!md5sum.txt]*")
    ):
        folder_md5["./" + str(file_path.relative_to(folder_path))] = hashlib.md5(
            open(str(file_path), "rb").read()
        ).hexdigest()

    # Load the target md5 from the specified file.
    path_md5 = {}
    with open(md5_path, "r") as file:
        lines = file.readlines()
        for line in lines:
            md5, file_path = line.split()  # type: ignore
            path_md5[file_path] = md5

    # Only return True if all key:value pairs match.
    return folder_md5 == path_md5


def build_remote_url(
    remote_path: str,
    remote_user: str = None,
    remote_password: str = None,
    ssl: bool = True,
) -> str:
    """
    Build a correct url including username and password.

    Parameters
    ----------
    remote_path
        Remote server path.
    remote_user
        Remote username.
    remote_password
        Remote password for specified username.
    ssl
        http or https

    Returns
    -------
    str
        Custom remote url using the specified parameter.

    Examples
    --------
    Example with ssl.
    >>> print(build_remote_url("ai4bd.com", "ai4bd", "psw"))
    https://ai4bd:psw@ai4bd.com

    Example without ssl.
    >>> print(build_remote_url("ai4bd.com", "ai4bd", "psw", False))
    http://ai4bd:psw@ai4bd.com

    Example without authentication.
    >>> print(build_remote_url("ai4bd.com"))
    https://ai4bd.com
    """
    http = "https" if ssl else "http"
    uses_credentials = remote_user is not None and remote_password is not None
    if uses_credentials:
        return f"{http}://{remote_user}:{remote_password}@{remote_path}"
    return f"{http}://{remote_path}"


def is_valid_local_remote_md5(
    remote_path: str,
    remote_user: str,
    remote_password: str,
    local_path: Union[str, pathlib.Path],
    ssl=True,
) -> bool:
    r"""
    Compare a local folder to a remote folder.

    To make comparision possible the remote folder has to contain the file md5sum.txt
    wich has a listing of all
    the contained files and their m5d checksums. This file can be created on the
    remote using this command:
        find -type f \( -not -name "md5sum.txt" \) -exec md5sum '{}' \; > md5sum.txt")

    Parameters
    ----------
    remote_path
        Url to the remote download server.
    remote_user
        User used for authentication on remote server.
    remote_password
        Password used for authentication on remote server.
    local_path
        Local path the remote folder will be matched to.
    ssl
        http or https

    Returns
    -------
    bool
        True if all md5 match, False otherwise.

    Raises
    ------
    WgetError
        Is raised if the call to wget produces an error/failure code.
    """
    # Download the md5sum.txt file to a temporary location.
    with tempfile.TemporaryDirectory() as tmp_dir:
        remote_url = build_remote_url(
            remote_path + "md5sum.txt", remote_user, remote_password, ssl
        )
        call_wget(["-P", tmp_dir, remote_url])

        # Check the folder md5 checksum using the downloaded file.
        md5_path = pathlib.Path(tmp_dir, "md5sum.txt")
        return is_valid_folder_md5(local_path, md5_path)


def call_wget(additional_params: list):
    """
    Make a call to wget using a list of additional prams.

    Parameters
    ----------
    additional_params
        List of parameters that will be passed using subprocess.call.
    """
    # Call to wget, a exit code 0 - 1 is returned. Meaning can be found here:
    # https://www.gnu.org/software/wget/manual/html_node/Exit-Status.html
    exit_code = subprocess.call(["wget"] + additional_params)

    # Raise exception if download did not go fine.
    if WgetExitCode(exit_code) != WgetExitCode.OK:
        raise WgetError(
            f"Download failed with exit code: {WgetExitCode(exit_code)}",
            WgetExitCode(exit_code),
        )


def sync_from_remote_if_url(model_path_url: str) -> pathlib.Path:
    """
    Sync a remote to a local folder if model_path_url is a url.

    If model_path_url is a remote location it will be synced to a automatically
    defined local folder.

    Parameters
    ----------
    model_path_url
        Either a local path or a remote url.

    Returns
    -------
    pathlib.Path
        Path to the local folder.
    """
    url = urllib.parse.urlparse(model_path_url)  # type: ignore
    is_http = url.netloc and url.scheme in ["http", "https"]
    if is_http:
        if url.hostname is None or url.password is None or url.username is None:
            raise ValueError(
                f"You should provide host, username and password. Got {model_path_url}."
            )
        local_sync_path = _local_path_for_remote_sync(url.hostname + url.path)
        sync_from_remote(
            remote_path=url.hostname + url.path,
            remote_user=url.username,
            remote_password=url.password,
            local_path=local_sync_path,
        )
        new_local_path = local_sync_path
    else:
        new_local_path = model_path_url  # type: ignore
    return pathlib.Path(new_local_path)


def _local_path_for_remote_sync(remote_url) -> pathlib.Path:
    """
    Get the local folder a remote url shall be synced to.

    Parameters
    ----------
    remote_url
        URL of the remote location.

    Returns
    -------
    pathlib.Path
        Folder the remote shall be synced to.
    """
    return pathlib.Path(ROOT_SYNC_FOLDER).joinpath(remote_url)
