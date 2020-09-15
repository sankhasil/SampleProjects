"""Module to upload a folder."""
import hashlib
import logging
import pathlib
import subprocess

import aiserve.remfolder


def _check_md5(input_path: pathlib.Path) -> bool:
    """
    Check local md5sum file.

    Parameters
    ----------
    input_path
        Local folder.

    Returns
    -------
    bool
        True if all md5 correct, False otherwise.
    """
    logging.info("Checking for md5sum file")
    md5file = input_path / "md5sum.txt"
    try:
        return aiserve.remfolder.is_valid_folder_md5(
            folder_path=input_path, md5_path=md5file,
        )
    except FileNotFoundError:
        return False


def _create_md5(input_path: pathlib.Path):
    """
    Create md5sum file for folder.

    Parameters
    ----------
    input_path
        Local folder.
    """
    logging.info("Updating md5sum file")
    md5file = input_path / "md5sum.txt"
    all_files = input_path.rglob("*[!md5sum].*")

    with md5file.open("w", encoding="utf-8") as output_file:
        for file_path in all_files:
            with file_path.open("rb") as binary_file:
                checksum = hashlib.md5(binary_file.read()).hexdigest()
            file_name = "./" + str(file_path.relative_to(input_path))
            output_file.write(str(checksum) + "  " + str(file_name) + "\n")


def _send_folder(
    server: str, user: str, input_path: pathlib.Path, output_path: pathlib.Path
):
    """
    Send folder to the destination.

    Parameters
    ----------
    server
        Name of the server where to send the files.
    user
        Name of the user.
    input_path
        Files to send.
    output_path
        Where to send the files.
    """
    target = user + "@" + server + ":" + str(output_path)
    subprocess.run(["scp", "-r", str(input_path), target], check=True)


def send_to_remote(
    input_path: pathlib.Path, user: str, server: str, remote_path: pathlib.Path
):
    """
    Send a folder and its files to a remote folder.

    This solution is currently not optimum because it doesn't check
    the md5 file in the remote location.

    Parameters
    ----------
    input_path
        Local path the remote folder will be matched to.
    user
        User to connect to the server.
    server
        Remote download server.
    remote_path
        Path to the remote download server.
    """
    valid_md5 = _check_md5(input_path)
    if not valid_md5:
        _create_md5(input_path)

    _send_folder(
        server=server, user=user, input_path=input_path, output_path=remote_path
    )
