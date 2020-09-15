"""
Module that implements the basic functionality of a arbitrary processor.

The processor includes functionality to create Resources that can then be easily
deployed via flask.
"""

import abc
import json
import pathlib
from typing import Dict, Iterator, List, Optional, Tuple, Type, TypeVar
import warnings

import flask

import aiserve.logger
import aiserve.preserver

LOGGER = aiserve.logger.get_logger()

METADATA_FILE: str = "metadata.json"
PROCESSOR_CLASSNAME_KEY: str = "classifier_class"

# Type of input which is passed to the process function of processor
ProcessFuncType = TypeVar(
    "ProcessFuncType", Optional[bytes], Optional[Tuple[bytes, str]]
)

PreProcessedData = TypeVar("PreProcessedData")
ProcessedData = TypeVar("ProcessedData")


class BaseProcessor(abc.ABC):
    """
    Abstract base class for a synchronous processor which does sync processing.

    Must be subclassed to implement a processor working with the RestAppFactory. When
    subclassing this class three methods must be implemented:

    preprocess:
        Handling everything before actual processing e.g. loading request data and
        transforming it, apply data
        normalization etc.

    _process:
        Handling actual data processing, here a Neural Network could be applied or an
        other algorithm may be applied.

    _postprocess:
        Handling postprocessing of data, the output of this method must be a string
        returnable via http. A normal step
        would be to jsonify in this method.

    When calling an subclass instance a pipeline (preprocess -> _process ->
    _postprocess) is called and the result
    returned.
    """

    def __init__(
        self, *args, **kwargs,
    ):
        """
        Init method for BaseProcessor.

        This would be deprecated in the next major release.

        Parameters
        ----------
        args
            Position arguments are deleted and have not effect.
        kwargs
            Key word arguments are just deleted and have no effect.
        """
        # Just kept to keep pylint happy we don't need any of the variables
        del args
        del kwargs

    def __call__(
        self, input_preprocessed: Iterator[PreProcessedData]
    ) -> List[ProcessedData]:
        """
        Process input data.

        This method is built as a pipeline:  _process -> _postprocess and takes as
        input a list of preprocessed inputs.

        Returns
        -------
            A dict that must be json serializable.

        """
        output: List[ProcessedData] = self.process(input_preprocessed)
        LOGGER.debug(f"Output length from pre process function is : {len(output)}")
        return output

    @classmethod
    def processor_name(cls) -> str:
        """Representation that is used for referencing in metafiles."""
        return cls.__name__

    def preprocess(self) -> PreProcessedData:
        """
        Preprocessing for the target processing has to be implemented here.

        Extracting data and variables from the request and apply further
        postprocessing. This instruction is not
        batched and is called as soon as a new request arrives. Time consuming and
        batchable work should not be handled
        here.

        Returns
        -------
         Data of an arbitrary type that can accumulated to a list of batch_size and
         forwarded to _process.

        """

    def process(self, input_data: Iterator[PreProcessedData]) -> List[ProcessedData]:
        """Perform the main processing."""
        warnings.warn("You should overwrite this function!", DeprecationWarning)
        return self._process(input_data)

    def postprocess(self, input_data: ProcessedData) -> flask.Response:
        """
        Call the _postprocess method of the processor class.

        This method is there so that our Flask resource methods
        don't have to call a private method (_postprocess) directly.

        Parameters
        ----------
        input_data
            Data returned by the process function of processor.

        Returns
        -------
            Data returned by the concrete _postprocess function of processor.
        """
        warnings.warn("You should overwrite this function!", DeprecationWarning)
        return self._postprocess(input_data)

    def _process(self, input_data: Iterator[PreProcessedData]) -> List[ProcessedData]:
        """
        Execute processing algorithm has to be implemented here.

        Applying and Neural Network or another algorithm should be implemented here.
        This method call has to only
        handle input lists of the size defined by batch_size. All the padding that is
        needed is handled by the
        _processing_loop.

        Parameters
        ----------
        input_data
            Iterator of input of arbitrary type returned by preprocess.

        Returns
        -------
            Data of arbitrary type that is passed to _postprocess.

        """

    def _postprocess(self, input_data: ProcessedData) -> flask.Response:
        """
        Execute postprocessing and convert the output to a flask.Response.

        Parameters
        ----------
        input_data
            Iterator of input of arbitrary type returned by _process.

        Returns
        -------
        dict
            Json serializable result of the processing.
        """

    @classmethod
    def _processor_class_from_folder(
        cls, folder_path: pathlib.Path
    ) -> Type["BaseProcessor"]:
        """
        Extract the correct processor class from a target folder.

        Parameters
        ----------
        folder_path
            Folder containing information about the target class.

        Returns
        -------
        Type[BaseProcessor]
            Target processor class.

        """
        metadata_path = folder_path.joinpath(METADATA_FILE)
        if metadata_path.is_file():
            LOGGER.debug(f"The metadata file is {metadata_path}")
            metadata = cls._read_metadata(metadata_path)
            LOGGER.debug(f"The metadata read from the metadata file is {metadata}")
            processor_class_name = cls._processor_class_name_from_metadata(metadata)
            LOGGER.debug(
                f"The processor_class_name from metadata is {processor_class_name}"
            )
            processor_class = cls._get_processor_class(processor_class_name)
            LOGGER.debug(f"Correct processor class {processor_class}")
        else:
            processor_class = cls  # type: ignore
        return processor_class

    @staticmethod
    def _read_metadata(metadata_path: pathlib.Path) -> Dict:
        """
        Load metadata from a file to a dict.

        Parameters
        ----------
        metadata_path
            Path to the metadata file.

        Returns
        -------
        Dict
            Dictionary containing metadata.

        Raises
        ------
        IOError
            When the metadata file could not be read.
        """
        with metadata_path.open("r") as metadata_file:
            try:
                metadata = json.load(metadata_file)
            except json.decoder.JSONDecodeError as error:
                raise IOError(
                    f"There was a problem reading the file "
                    f"{metadata_path}. Please check if the file "
                    f"contains valid .json."
                ) from error
        return metadata

    @staticmethod
    def _processor_class_name_from_metadata(metadata: Dict) -> str:
        """
        Get the correct processor class name from the metadata.

        Parameters
        ----------
        metadata
            All the metadata.

        Returns
        -------
        str
            Name of the processor class defined in the metadata.

        Raises
        ------
        KeyError
            When the metadata does not contain the key needed for classname extraction.
        """
        try:
            processor_class_name = metadata[PROCESSOR_CLASSNAME_KEY]
        except KeyError as error:
            raise KeyError(
                f"The file {METADATA_FILE} does not contain a "
                f"{PROCESSOR_CLASSNAME_KEY} key. Please check "
                f"if the key is misspelled or missing."
            ) from error
        return processor_class_name

    @classmethod
    def _get_processor_class(cls, processor_class_name: str) -> Type["BaseProcessor"]:
        """
        Fetch the correct processor class.

        Parameters
        ----------
        processor_class_name
            Name of the processor class.

        Returns
        -------
        Type[BaseProcessor]
            Processor as defined by the input name.

        Raises
        ------
        KeyError
            When the class defined in the metadata file is not a valid processor class.
        """
        name_subclass = cls._create_name_class_mapping()
        try:
            processor_class = name_subclass[processor_class_name]
        except KeyError as error:
            raise NotAProcessorError(
                f"The processor class {processor_class_name} "
                f"defined in the file {METADATA_FILE} is not a "
                f"subclass of {cls.processor_name()}. Please use a available "
                f"class: \n\n {name_subclass}."
            ) from error
        return processor_class

    @classmethod
    def _create_name_class_mapping(cls,) -> Dict[str, Type["BaseProcessor"]]:
        """
        Create a mapping from names to possible processor subclasses.

        Returns
        -------
        aiserve.process.BaseProcessor

        """
        name_subclass = {
            subclass.processor_name(): subclass for subclass in cls.__subclasses__()
        }
        LOGGER.debug(f"Name and subclass mapping : {name_subclass}")

        name_subclass.update({cls.processor_name(): cls})
        return name_subclass  # type: ignore

    @staticmethod
    @abc.abstractmethod
    def _kwargs_from_folder(folder_path: pathlib.Path):
        """
        Extract the keyword arguments form the given folder.

        Parameters
        ----------
        folder_path
            Path to the folder containing resources needed to instantiate the class.

        Returns
        -------
        dict
            Contains the keyword arguments to call the class constructor.
        """

    @classmethod
    def create_from_folder_infer_class(
        cls, folder_path: pathlib.Path, batch_size: int = 1,
    ) -> "BaseProcessor":  # PEP484 for python < 3.7
        """
        Create an instance of the class from a folder, the correct type is inferred.

        This method is used to deploy processors that depend on external resources
        that can be stored in a local folder. If there is a metadata file in the
        specified folder the correct class to instantiate will be inferred
        automatically. If no metadata is present in the folder direct instantiation
        will be attempted.

        The next major upgrade of aiserve should remove the preserver_class param.

        Parameters
        ----------
        folder_path
            Path to the folder containing resources needed to instantiate the class.
        batch_size
            Target batch size the processing step uses.

        Returns
        -------
        BaseProcessor
            Return an instance of the class itself.

        Raises
        ------
        IOError
            When the metadata file could not be read.
        KeyError
            When the metadata does not contain the key needed for classname extraction.
        KeyError
            When the class defined in the metadata file is not a valid processor class.
        """
        processor_class = cls._processor_class_from_folder(folder_path)
        LOGGER.debug(f"The processor class inferred from folder is {processor_class}")

        # The below code block should be changed too in the next major version upgrade
        # The processor classes would not need the preserver class
        return processor_class.create_from_folder(
            folder_path=folder_path, batch_size=batch_size
        )

    @classmethod
    def create_from_folder(
        cls, folder_path: pathlib.Path, batch_size: int = 1
    ) -> "BaseProcessor":
        """
        Create an instance of the class from a folder. Simple class Factory.

        This method ignores possible information about the specific class in a
        metadata file stored in the target folder and directly instantiates.

        The next major upgrade of aiserve should remove the preserver_class param.

        Parameters
        ----------
        folder_path
            Path to the folder containing resources needed to instantiate the class.
        batch_size
            Target batch size the processing step uses.

        Returns
        -------
        BaseProcessor
            Return an instance of the class itself.
        """
        # Extract the keyword arguments form the given folder and construct
        # instance.
        kwargs = cls._kwargs_from_folder(folder_path)
        LOGGER.debug(
            f"The kwargs extracted for {cls} is {kwargs}... creating an object of class"
        )

        # The preserver class is not used by any processor hence we pass None here
        # The batch_size may be used.
        return cls(preserver_class=None, batch_size=batch_size, **kwargs)


class NotAProcessorError(Exception):
    """Exception to raise when a referenced processor does not exist."""
