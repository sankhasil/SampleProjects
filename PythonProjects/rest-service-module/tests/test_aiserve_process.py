"""
Test suite for the aiserve.process module.

As the process module only implements a abstract base class this should be understood
less as a test of actual
functionality than a test for the defined interface. Completing this suite without
errors should ensure that the
target interface was not changed as other modules heavily depend on it.
"""

# pylint: disable=protected-access
import json
import pathlib
from typing import Dict
from unittest import mock

import dummyprocessor
import pytest  # type: ignore

import aiserve.process

# Definition of constants used as return values for tests.
PREPROCESS_RETURN_DUMMY = "PREPROCESS_RETURN"
PROCESS_RETURN_DUMMY = "PROCESS_RETURN"
POSTPROCESS_RETURN_DUMMY = "POSTPROCESS_RETURN_DUMMY"
DUMMY_PATH = "/test/path"

BAD_CLASSIFIER_NAME = "BAD_NAME"
# Reference name of the metadata file, if the implementation is changed in process a
# lot of interfaces break, hence it is hard coded here to warn against change.
METADATA_FILE_REFERENCE: str = "metadata.json"
CLASSIFIER_CLASS_KEY_REFERENCE: str = "classifier_class"


def test_processor_call(dummy_processor_instance):
    """
    Test processor __call__ order.

    Test if a calling a processor instance executes the desired pipeline: _process ->
    _postprocess and
    returns the return value of _postprocess.

    Parameters
    ----------
    dummy_processor_instance
        Processor instance fixture.
    """
    # Assign relevant mocks to a mock parent to be able to check execution order
    # later on.
    mock_manager = mock.Mock()
    mock_manager.configure_mock(
        first=dummy_processor_instance._process,
        second=dummy_processor_instance._postprocess,
    )

    # Check if result of _postprocessign is returned.
    input_mock = mock.Mock()
    assert dummy_processor_instance(input_mock) == PROCESS_RETURN_DUMMY

    # Check if methods are called in the correct order and parameters are forwarded
    # passed correctly.
    mock_manager.assert_has_calls([mock.call.first(input_mock)])


def test_init_abstract_base():
    """Test if initialization of abstract BaseProcessor is blocked."""
    # Expect a TyperError that is raised when trying to instantiate a abstract base
    # class.
    # pylint: disable=abstract-class-instantiated
    with pytest.raises(TypeError):
        aiserve.process.BaseProcessor()


def test_from_folder_kwarg_call(dummy_processor_instance):
    """
    Tests the create_from_folder_infer_class interface.

    Tests if the create_from_folder_infer_class method correctly calls the
    _kwargs_from_folder method and uses it's return value to
    instantiate a instance of the class.

    Parameters
    ----------
    dummy_processor_instance
        Processor instance fixture.
    """
    # Add the mock to a parent mock to check call later on.
    mock_manager = mock.Mock()
    mock_manager.configure_mock(first=dummy_processor_instance._kwargs_from_folder)

    # Check if create_from_folder_infer_class returns the correct type.
    assert isinstance(
        dummy_processor_instance.create_from_folder_infer_class(
            pathlib.Path(DUMMY_PATH)
        ),
        aiserve.process.BaseProcessor,
    )

    # Check if create_from_folder_infer_class correctly calls the _kwargs_from_folder
    # method.
    mock_manager.assert_has_calls([mock.call.first(pathlib.Path(DUMMY_PATH))])


@pytest.mark.parametrize(
    "processor_class", [aiserve.process.BaseProcessor, dummyprocessor.DummyProcessor]
)
def test_from_folder_automatic_processor_inference(
    tmp_path: pathlib.Path, processor_class: aiserve.process.BaseProcessor
):
    """
    Test if automatic processor inference works.

    This tests if automatic inference of the processor from a metadata file works
    from the whole inheritance tree.

    Parameters
    ----------
    tmp_path
        Temporary folder used for metadata.

    processor_class
        Class the create_from_folder_infer_class method is invoked from.
    """
    # Set up the metadata file.
    meta_file_path = tmp_path.joinpath(METADATA_FILE_REFERENCE)
    data = {
        CLASSIFIER_CLASS_KEY_REFERENCE: dummyprocessor.DummyProcessor.processor_name()
    }
    with meta_file_path.open("w") as meta_file:
        json.dump(data, meta_file)

    # Creation of processor from folder - this used automatic processor inference.
    processor = processor_class.create_from_folder_infer_class(tmp_path)

    # Check if the produced processor has precisely the correct type.
    assert isinstance(processor, dummyprocessor.DummyProcessor)


def test_from_folder_automatic_processor_inference_bad_processor_name(
    tmp_path: pathlib.Path,
):
    """
    Test if correct exception is raised in the case of a not valid processor name.

    Parameters
    ----------
    tmp_path
        Temporary folder used for metadata.
    """
    # Set up the metadata file with bad processor class name.
    meta_file_path = tmp_path.joinpath(METADATA_FILE_REFERENCE)
    data = {CLASSIFIER_CLASS_KEY_REFERENCE: BAD_CLASSIFIER_NAME}
    with meta_file_path.open("w") as meta_file:
        json.dump(data, meta_file)

    with pytest.raises(aiserve.process.NotAProcessorError):
        aiserve.process.BaseProcessor.create_from_folder_infer_class(tmp_path)


def test_from_folder_automatic_processor_inference_bad_metadata(tmp_path: pathlib.Path):
    """
    Check if correct exception is raised in the case of defunct metadata file.

    Parameters
    ----------
    tmp_path
        Temporary folder used for metadata.
    """
    # Set up the metadata file.
    meta_file_path = tmp_path.joinpath(METADATA_FILE_REFERENCE)
    meta_file_path.touch()

    with pytest.raises(IOError):
        aiserve.process.BaseProcessor.create_from_folder_infer_class(tmp_path)


def test_from_folder_automatic_processor_inference_classname_missing(
    tmp_path: pathlib.Path,
):
    """
    Test if correct exception is raised in the case of missing classname in metadata.

    Parameters
    ----------
    tmp_path
        Temporary folder used for metadata.
    """
    # Set up the metadata file.
    meta_file_path = tmp_path.joinpath(METADATA_FILE_REFERENCE)
    data: Dict[str, str] = {}
    with meta_file_path.open("w") as meta_file:
        json.dump(data, meta_file)

    with pytest.raises(KeyError):
        aiserve.process.BaseProcessor.create_from_folder_infer_class(tmp_path)


@pytest.mark.parametrize("batch_size", (1, 2, 3, 5, 10, 100))
def test_processor_from_create_from_folder_infer_class_async(
    tmp_path: pathlib.Path, batch_size
):
    """
    Test that the processor object returned by create_from_folder_infer_class.

    The processor object should have the preserver_class and batch_size set.
    Basically the test ensures that the batch_size and preserver_class are
    passed to the processor initializer.
    """
    processor = dummyprocessor.DummyProcessorForAsync.create_from_folder_infer_class(
        tmp_path, batch_size=batch_size
    )

    assert processor is not None
    assert isinstance(processor, aiserve.process.BaseProcessor)
    assert isinstance(processor, dummyprocessor.DummyProcessorForAsync)
    assert processor.preserver is None
    assert processor.batch_size == batch_size


def test_processor_from_create_from_folder_infer_class_async_default_batch_size(
    tmp_path: pathlib.Path,
):
    """
    Test that the processor object returned by create_from_folder_infer_class.

    In this case we do not pass batch_size explicity, in that case it should
    default to 1.
    """
    processor = dummyprocessor.DummyProcessorForAsync.create_from_folder_infer_class(
        tmp_path
    )

    assert processor is not None
    assert isinstance(processor, aiserve.process.BaseProcessor)
    assert isinstance(processor, dummyprocessor.DummyProcessorForAsync)
    assert processor.preserver is None
    assert processor.batch_size == 1


def test_create_from_folder_infer_class_sync(tmp_path: pathlib.Path, mocker):
    """
    Test that the init method of BaseProcessor is correctly called.

    The arguments of preserver_class and batch_size is passed from inside
    create_from_folder_infer_class method.
    """
    mocked_base_processor_init = mocker.patch("aiserve.process.BaseProcessor.__init__")
    mocked_base_processor_init.return_value = None
    _ = dummyprocessor.DummyProcessor.create_from_folder_infer_class(tmp_path)
    mocked_base_processor_init.assert_called_with(preserver_class=None, batch_size=1)
