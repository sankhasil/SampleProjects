"""Simple implementation of the preserver interface using dicts."""
import collections
import threading
from typing import Dict, List, Optional, Tuple, TypeVar
import uuid

import aiserve.logger
import aiserve.preserver

LOGGER = aiserve.logger.get_logger()

# Type of input which is passed to the process function of processor
ProcessFuncType = TypeVar(
    "ProcessFuncType", Optional[bytes], Optional[Tuple[bytes, str]]
)


class RequestElement:  # pylint: disable=too-few-public-methods
    """
    Define a request as it's stored in the DictPreserver.

    Attributes
    ----------
    request
        The request as parsed by a prepossessing method.
    result
        Json serializable result of the processing.
    status
        Status code of the RequestElement.
    """

    def __init__(self, request_parameters):
        """
        Create new RequestElement using request parameters.

        Parameters
        ----------
        request_parameters
            Parameters of the request.
        """
        self.request: ProcessFuncType = request_parameters
        self.result: dict = None
        self.status: aiserve.preserver.RequestStatus = aiserve.preserver.RequestStatus.PROCESSING  # noqa: E501


class DictPreserver(aiserve.preserver.BasePreserver):
    """Implementation of the preserver interface using python dict."""

    def __init__(self):
        """Create a dictionary to store data."""
        self.data = collections.OrderedDict()

    def result(self, request_id: str) -> Tuple[aiserve.preserver.RequestStatus, dict]:
        """
        Get result for an request using the requests id.

        Also gets status code to show if result is already valid.

        Parameters
        ----------
        request_id
            Id of the request that resulted in the result.

        Returns
        -------
        aiserve.preserver.RequestStatus
            Status code of the request.
        dict
            Stored result in json serializable format, only valid if ResultStatus is
            ResultStatus.READY.
        """
        # Return if the requested id does not exist.
        if not self._id_exists(request_id):
            return aiserve.preserver.RequestStatus.DOES_NOT_EXIST, dict()

        # Return status code and result if id does exist.
        request_element = self.data[request_id]

        return request_element.status, request_element.result

    def request(self, request_id: str) -> ProcessFuncType:
        """
        Get request corresponding to the passed id.

        Parameters
        ----------
        request_id
            Id of the requested request.

        Returns
        -------
        T
            Request object, none if request does not exist.
        """
        if not self._id_exists(request_id):
            return None
        return self.data[request_id].request

    def status(self, request_id: str) -> aiserve.preserver.RequestStatus:
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
        if not self._id_exists(request_id):
            LOGGER.debug(f"Request id {request_id} requested is not there in preserver")
            return aiserve.preserver.RequestStatus.DOES_NOT_EXIST
        return self.data[request_id].status

    def delete_result(self, request_id: str):
        """
        Delete result of target request.

        Parameters
        ----------
        request_id
            Id of the request.
        """
        self.data[request_id].status = aiserve.preserver.RequestStatus.DELETED
        self.data[request_id].result = {}

    def next_request_ids(self, num_requests: int) -> Tuple[str, ...]:
        """
        Return a list num_request elements.

        It contains the ids of the next requests to be processed.

        Parameters
        ----------
        num_requests
            Number of requested request ids.

        Returns
        -------
        List[str]
            List of length num_requests or less containing request ids. Return 0 if
            no pending request
        """
        # Accumulate request ids that still have to be processed.

        request_list: List[str] = []
        for request_id, element in self.data.items():
            if element.status == aiserve.preserver.RequestStatus.PROCESSING:
                request_list.append(request_id)

            # Early return if list has reached length of batch size.
            if len(request_list) == num_requests:
                LOGGER.debug(
                    f"The size of {request_list} extracted"
                    f" is equal to batch_size {num_requests}"
                )
                return tuple(request_list)

        LOGGER.debug(
            f"The size of {request_list} extracted is less "
            f"than batch_size {num_requests}"
        )

        return tuple(request_list)

    def new_request(self, request_data: object) -> str:
        """
        Create a new request to be processed, returning a uuid to track it.

        Parameters
        ----------
        request_data
            Data of the request.

        Returns
        -------
        str
            uuid of the request.
        """
        request_uuid = str(uuid.uuid4())
        self.data[request_uuid] = RequestElement(request_data)
        LOGGER.debug(
            f"Status for {threading.current_thread().getName()} "
            f"is {self.get_status_of_all_request_ids()}"
        )
        return request_uuid

    def store_result(self, request_id: str, result: object) -> bool:
        """
        Store result for a request.

        Parameters
        ----------
        request_id
            Id of the request the result belongs to.
        result
            Result that will be stored for the request.

        Returns
        -------
        bool
            False if request_id is unknown, True otherwise.
        """
        if not self._id_exists(request_id):
            return False

        # Store result and set request state.
        LOGGER.debug(f"Storing result for request id : {request_id}")
        self.data[request_id].result = result
        self.data[request_id].status = aiserve.preserver.RequestStatus.READY
        LOGGER.debug(
            f"Result stored for request_id {request_id}"
            f" is {self.data[request_id].status}"
        )
        current_status = self.get_status_of_all_request_ids()
        LOGGER.debug(f"The current status of requests: {current_status}")
        return True

    def _id_exists(self, request_id: str) -> bool:
        """
        Check whether an id exists.

        Parameters
        ----------
        request_id
            Id that will be checked
        Returns
        -----
        bool
            True if id exists, False otherwise.
        """
        return request_id in self.data

    def get_status_of_all_request_ids(self) -> Dict[str, str]:
        """
        Peek view into the request id's inside preserver and their status.

        This function is added primarily as a debugging tool to be invoked
        to debug issues with aiserve.

        Returns
        -------
        Dict mapping between request id and status
        """
        request_id_status_mapping = {
            request_id: self.data[request_id].status
            for request_id in list(self.data.keys())
        }
        return request_id_status_mapping
