"""Fixtures available for all test files."""
import pathlib
import shutil
import subprocess
import tempfile
from typing import Iterator

import dummyprocessor
import pytest  # type: ignore

import aiserve.dictpreserver as aiserve_dictpreserver

# Definition of constants used as return values for tests.
PREPROCESS_RETURN_DUMMY = "PREPROCESS_RETURN"
PROCESS_RETURN_DUMMY = "PROCESS_RETURN"
POSTPROCESS_RETURN_DUMMY = "POSTPROCESS_RETURN_DUMMY"


@pytest.fixture
def dummy_processor_instance(  # pylint: disable=protected-access
    mocker,
) -> dummyprocessor.DummyProcessor:
    """
    Fixture instantiating the Dummy subclass and mocking the private class methods.

    Returns
    -------
    DummyProcessor
        Processor with mocked private methods.
    """
    dummy_processor_instance = dummyprocessor.DummyProcessor(
        aiserve_dictpreserver.DictPreserver
    )

    # Mocking private methods using defined constants as return value.
    dummy_processor_instance.preprocess = mocker.Mock(  # type: ignore
        return_value=PREPROCESS_RETURN_DUMMY
    )
    dummy_processor_instance._process = mocker.Mock(  # type: ignore
        return_value=PROCESS_RETURN_DUMMY
    )
    dummy_processor_instance._postprocess = mocker.Mock(  # type: ignore
        return_value=POSTPROCESS_RETURN_DUMMY
    )

    return dummy_processor_instance


@pytest.fixture
def tmp_folder_path() -> Iterator[pathlib.Path]:
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
    tmp_folder_path_inside = pathlib.Path(tempfile.mkdtemp())
    tmp_subfolder_path = tmp_folder_path_inside.joinpath("subfolder")
    tmp_file_path = tmp_folder_path_inside.joinpath("temp_file.txt")
    tmp_subfolder_file_path = tmp_subfolder_path.joinpath("temp_file.txt")
    tmp_md5_file_path = tmp_folder_path_inside.joinpath("md5sum.txt")

    # Create temporary subfolder and files.
    tmp_subfolder_path.mkdir()
    tmp_file_path.touch()
    tmp_subfolder_file_path.touch()

    # Calculate md5 sum for all files contained in the folder structure ans store
    # result in the md5sum.txt file.
    with tmp_md5_file_path.open("w") as file:
        subprocess.call(
            ["md5sum", "./temp_file.txt"], stdout=file, cwd=tmp_folder_path_inside
        )
        subprocess.call(
            ["md5sum", "./subfolder/temp_file.txt"],
            stdout=file,
            cwd=tmp_folder_path_inside,
        )

    # yield the temporary path, don't use return to allow later teardown.
    yield tmp_folder_path_inside
    # Teardown of created folder to keep /tmp clean.
    shutil.rmtree(tmp_folder_path_inside)
