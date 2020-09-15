"""File which defines the flask resources to be imported."""

from dataclasses import dataclass
import threading
from typing import Any, Callable, Dict, Union

import flask
import flask_api
import flask_restful
from flask_restful import reqparse
import werkzeug

import aiserve.async_request_handler
import aiserve.logger
import aiserve.preserver
import aiserve.process  # noqa: F401
import aiserve.utils

LOGGER = aiserve.logger.get_logger()


@dataclass
class AsyncResourceBase(flask_restful.Resource):
    """Base class to async Flask resources."""

    processor: aiserve.process.BaseProcessor
    async_request_handler: aiserve.async_request_handler.AsyncRequestHandler


class RequestPostingResource(AsyncResourceBase):
    """Resource for handling of POST requests for Async processor."""

    def post(self):
        """Execute code belonging to the post endpoint."""
        thread_name = threading.current_thread().getName()
        LOGGER.debug(f"Current thread inside post ... {thread_name}")
        pre_processed_data = self.processor.preprocess()

        result_id = self.async_request_handler.handle_incoming_request(
            pre_processed_data
        )
        LOGGER.debug(f"Received {result_id} as response from the async request handler")

        return flask.jsonify({"request-id": result_id})


class ResultRetrievalResource(AsyncResourceBase):
    """Resource to retrieve finished results via get, this will be returned."""

    def get(self):
        """Execute code belonging to the get endpoint."""
        # Extract request id from request.
        parser = reqparse.RequestParser()
        parser.add_argument("request-id", type=str, required=True)
        args = parser.parse_args()

        # Return different flask.Response depending on request state.
        status, processed_result = self.async_request_handler.get_result_for_a_request(
            request_id=args["request-id"]
        )
        LOGGER.info(f"Status returned for {args['request-id']} is {status}")

        if not status == aiserve.preserver.RequestStatus.READY:
            return _RESPONSE_MAPPING[aiserve.preserver.RequestStatus.PROCESSING](
                processed_result
            )

        _handle_exceptions_in_processing(processed_result)
        assert not isinstance(processed_result, Exception)

        LOGGER.info(f"Entering postprocess function...since status is {status}")

        post_processed_result = _run_rasing_500_on_error(
            function=self.processor.postprocess, input_data=processed_result,
        )

        LOGGER.debug(f"Completed post processing for request-id : {args['request-id']}")

        return _RESPONSE_MAPPING[aiserve.preserver.RequestStatus.READY](
            post_processed_result
        )


class RequestPostingResourceSync(flask_restful.Resource):
    """Resource for handling of POST requests for Sync processor."""

    def __init__(self, processor: "aiserve.process.BaseProcessor"):
        self.processor = processor

    def post(self):
        """Execute code belonging to the post endpoint."""
        pre_processed_result = _run_rasing_500_on_error(
            function=self.processor.preprocess
        )

        LOGGER.debug("Completed preprocessing for request")

        # Process the data returned after preprocessing
        # Clients expect data as a list , even if the batch size is 1
        [processed_result] = _run_rasing_500_on_error(
            function=self.processor, input_preprocessed=[pre_processed_result],
        )

        LOGGER.debug(
            "Got result from processor for request data... going to postprocess"
        )

        # There should be only one result in the list
        # This is the postprocess part of the pipeline
        post_process_result = _run_rasing_500_on_error(
            function=self.processor.postprocess, input_data=processed_result,
        )

        LOGGER.debug("Post processing completed")

        return _response_ready(post_process_result)


def _handle_exceptions_in_processing(error: Exception) -> None:
    """
    Raise a werkzeug.exceptions or a InternalServerError based on error.

    Checks the type of error passed. If it is an object of werkzeug.exceptions
    then just raise it otherwise make sure an InternalServerError is raised.

    Parameters
    ----------
    error : Exception
        The exception object which is caught by handler.

    Raises
    ------
        werkzeug.exceptions
    """
    if isinstance(error, werkzeug.exceptions.HTTPException):
        raise error

    if isinstance(error, Exception):
        raise werkzeug.exceptions.InternalServerError(f"{error}")


def _run_rasing_500_on_error(
    function: Callable, *args: Any, **kwargs: Any
) -> Union[Exception, Any]:
    """
    Run the process_function with the arguments.

    This function is meant to be a general utility function
    which runs a function with the arguments, log the relevant messages
    and also handles any exceptions raised.

    Parameters
    ----------
    process_function:
        Callable to run for the stage
    args:
        Arguments for the callable
    kwargs:
        Keyword arguments for the callable

    Raises
    ------
        werkzeug.exceptions in case of an exception

    Returns
    -------
    The result of function executed either an exception or results
    """
    thread_name = threading.current_thread().getName()
    LOGGER.debug(f"Current thread ... {thread_name}")
    try:
        LOGGER.debug(f"Calling function {function} with arguments {args} and {kwargs}")
        return function(*args, **kwargs)
    except Exception as error:  # pylint: disable=broad-except
        LOGGER.exception(
            f"Encountered an error: {error}", exc_info=error,
        )
        _handle_exceptions_in_processing(error)

    return None


def _response_does_not_exist(_) -> flask.Response:
    """
    Create response for not existing Id.

    Returns
    -------
    flask.Response
        Created Response.
    """
    return flask.Response(
        status=flask_api.status.HTTP_404_NOT_FOUND,
        headers={"Info": "Id never existed."},
    )


def _response_deleted(_) -> flask.Response:
    """
    Create response for deleted Id.

    Returns
    -------
    flask.Response
        Created Response.
    """
    return flask.Response(
        status=flask_api.status.HTTP_404_NOT_FOUND, headers={"Info": "Result deleted."}
    )


def _response_processing(_) -> flask.Response:
    """
    Create response for id that is still processing.

    Returns
    -------
    flask.Response
        Created response.
    """
    return flask.Response(
        status=flask_api.status.HTTP_428_PRECONDITION_REQUIRED,
        headers={"Info": "Processing not finished yet."},
    )


def _response_ready(result: Dict) -> flask.Response:
    """
    Create a response for a Id that has a ready result.

    Parameters
    ----------
    result
        Result as stored in the preserver.

    Returns
    -------
    flask.Response
        Created response.
    """
    LOGGER.debug(f"Inside the response ready function... with {result}")
    if not isinstance(result, flask.Response):
        result = flask.jsonify(result)
    LOGGER.debug(f"Returning {result} from response ready function")
    return result  # type: ignore


# Map return status codes to response creation methods.
_RESPONSE_MAPPING = {
    aiserve.preserver.RequestStatus.DOES_NOT_EXIST: _response_does_not_exist,
    aiserve.preserver.RequestStatus.DELETED: _response_deleted,
    aiserve.preserver.RequestStatus.PROCESSING: _response_processing,
    aiserve.preserver.RequestStatus.READY: _response_ready,
}
