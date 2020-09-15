"""
Implements an interface for preservation of processor results.

The results can be accessed via id.
"""

import abc
import enum
from typing import Optional, Tuple, TypeVar

# Type of input which is passed to the process function of processor
ProcessFuncType = TypeVar(
    "ProcessFuncType", Optional[bytes], Optional[Tuple[bytes, str]]
)


class RequestStatus(enum.Enum):
    """Definition of different request status."""

    READY = 0  # Used as status code if ready result is returned.
    PROCESSING = 1  # Indicates that the result is not ready yet, try at a later time.
    DELETED = 2  # Id is correct but result was already deleted.
    DOES_NOT_EXIST = 3  # Id is not known.


class BasePreserver(abc.ABC):
    """Definition of the interface used to preserve result data in a processor."""

    @abc.abstractmethod
    def result(self, request_id: str) -> Tuple[RequestStatus, dict]:
        """
        Return saved result if it's ready, status otherwise.

        Parameters
        ----------
        request_id
            Id of the requested result.

        Returns
        -------
        RequestStatus
            Status of the result with request_id.
        dict
            Stored result in json serializable format, only valid if ResultStatus is
            ResultStatus.READY
        """

    @abc.abstractmethod
    def request(self, request_id: str) -> ProcessFuncType:
        """
        Return saved request corresponding to request_id.

        Parameters
        ----------
        request_id
            Id of the request.

        Returns
        -------
        T
            Request corresponding to the requested id.
        """

    def status(self, request_id: str) -> RequestStatus:
        """
        Return status of request.

        Parameters
        ----------
        request_id
            Id of the request.

        Returns
        -------
        RequestStatus
            Status of the request.
        """

    def delete_result(self, request_id: str):
        """
        Delete result of target request.

        Parameters
        ----------
        request_id
            Id of the request.
        """

    def next_request_ids(self, num_requests: int) -> Tuple[str, ...]:
        """
        Return list of request ids to be processed next.

        Parameters
        ----------
        num_requests
            Number of requests to be returned.

        Returns
        -------
        List[str]
            List of length num_requests or less containing request ids. Return 0 if
            no pending request.
        """

    @abc.abstractmethod
    def new_request(self, request_data: object) -> str:
        """
        Create a new result entry and sets its state to RequestStatus.PROCESSING.

        Parameters
        ----------
        request_data
            The data extracted from the request.

        Returns
        -------
        str
            The id created for the new request.
        """

    @abc.abstractmethod
    def store_result(self, request_id: str, result: object) -> bool:
        """
        Store a new result to a known id.

        Sets the RequestStatus to RequestStatus.READY.

        Parameters
        ----------
        request_id
            Id of the request that created the result.
        result
            Result that will be stored.

        Returns
        -------
        bool
            True if storing was successful, False otherwise.
        """
