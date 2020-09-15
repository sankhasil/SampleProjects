"""Module that contains a dummy processor."""
import pathlib
from typing import Iterator
from unittest import mock

import aiserve.process

# Ignore protected-access error because a protected function
# needs to be mocked.
# pylint: disable=protected-access


class DummyProcessor(aiserve.process.BaseProcessor):
    """The Abstract base class has to be subclassed to make it instantiatable."""

    def preprocess(self):
        """Do nothing to sidestep definition constrain of abstract method."""

    def _process(self, input_data: Iterator[aiserve.process.PreProcessedData]):
        """Do nothing to sidestep definition constrain of abstract method."""
        return [0]

    def _postprocess(self, input_data: aiserve.process.ProcessedData):
        """Do nothing to sidestep definition constrain of abstract method."""

    @staticmethod
    def _kwargs_from_folder(folder_path: pathlib.Path):
        """Do nothing to sidestep definition constrain of abstract method."""


class DummyProcessorForAsync(aiserve.process.BaseProcessor):
    """The Abstract base class has to be subclassed to make it instantiatable."""

    def __init__(self, preserver_class, batch_size):
        """Init method for async processor."""
        self.preserver = preserver_class
        self.batch_size = batch_size
        super().__init__(preserver_class, batch_size)

    def preprocess(self):
        """Do nothing to sidestep definition constrain of abstract method."""

    def _process(self, input_data: Iterator[aiserve.process.PreProcessedData]):
        """Do nothing to sidestep definition constrain of abstract method."""
        return [0]

    def _postprocess(self, input_data: aiserve.process.ProcessedData):
        """Do nothing to sidestep definition constrain of abstract method."""

    @staticmethod
    def _kwargs_from_folder(folder_path: pathlib.Path):
        """Do nothing to sidestep definition constrain of abstract method."""


# As _kwargs_from_folder is a staticmethod it can't be mocked on instance level,
# this mocks it on class level.
# A better approach is welcome.
DummyProcessor._kwargs_from_folder = mock.Mock(return_value={})  # type: ignore
DummyProcessorForAsync._kwargs_from_folder = mock.Mock(return_value={})  # type: ignore
