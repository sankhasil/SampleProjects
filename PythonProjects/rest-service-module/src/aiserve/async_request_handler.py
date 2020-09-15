"""File which has the class to encapsulate processing loop."""
import threading
from typing import Any

import aiserve.logger
import aiserve.preserver
import aiserve.process

LOGGER = aiserve.logger.get_logger()


class AsyncRequestHandler:
    """Class responsible for handling of async requests using preservers."""

    def __init__(
        self,
        processor: aiserve.process.BaseProcessor,
        preserver: aiserve.preserver.BasePreserver,
        batch_size: int = 1,
    ):
        self.batch_size = batch_size
        self.processor = processor
        self.preserver = preserver
        self._processing_loop_is_running = False

    def handle_incoming_request(self, request_data: Any) -> str:
        """
        Handle an incoming post request to a Flask resource.

        Used the preserver to create a new request id and return it ,
        starts its processing loop if not running and process the request using
        preserver.

        Parameters
        ----------
        request_data:
                Preprocessed request data.

        Returns
        -------
                String representing the new request id got from preserver
        """
        result_id = self.preserver.new_request(request_data)

        if not self._processing_loop_is_running:
            LOGGER.info("The processing loop is not running starting it...")
            threading.Thread(
                target=self._process_requests_using_preserver,
                args=(self.processor, self.preserver),
            ).start()
        else:
            LOGGER.debug("Processing loop is running ...")

        return result_id

    def get_result_for_a_request(self, request_id: str):
        """
        Get the status of a request_id from preserver.

        Parameters
        ----------
        request_id
                String representing
        Returns
        -------

        """
        status, result = self.preserver.result(request_id)
        return status, result

    def _process_requests_using_preserver(
        self,
        processor: aiserve.process.BaseProcessor,
        preserver: aiserve.preserver.BasePreserver,
    ):
        """
        Run as a child thread to process request asynchronously.

        Start processing loop, ends if there are no new requests to be processed.
        The processing loop is meant to be started in a separate thread and runs till
        all requests are processed.
        As the _process method is only meant to work for a fixed batch size padding
        is handled here.

        Parameters
        ----------
        processor:
        An instance of BaseProcessor.
        preserver:
        An instance of BasePresever.
        """
        thread_name = threading.current_thread().getName()

        self._processing_loop_is_running = True
        LOGGER.debug(
            f"Starting processing loop inside thread {thread_name}"
            f" and current_count of threads are : {threading.active_count()}"
        )

        while self._processing_loop_is_running:

            # Get list of request ids to be processed next.
            LOGGER.debug(f"Getting the next request id's for {self.batch_size}")

            request_id_tuple = preserver.next_request_ids(num_requests=self.batch_size)

            LOGGER.info(f"Extracted {request_id_tuple} as batch from dictpreserver")

            processing_not_done = bool(request_id_tuple)

            if not processing_not_done:
                # Exit loop if all requests are processed.
                LOGGER.debug("All requests are processed ...")
                self._processing_loop_is_running = False
                break

            LOGGER.debug("There are request id's remaining to be processed")
            # Add padding if not enough requests are to be processed to fill a
            # list of batch_size.
            padding = self.batch_size - len(request_id_tuple)
            request_id_tuple_padded = (
                request_id_tuple + (request_id_tuple[0],) * padding
            )
            LOGGER.debug(f"Applied padding of {padding}")
            LOGGER.debug(f"request id tuple after padding is {request_id_tuple_padded}")
            # Generate a list of inputs as returned by preprocess corresponding
            # to th request ids.
            multiple_processed_inputs = (
                preserver.request(request_id) for request_id in request_id_tuple_padded
            )

            # Run batched processing and update results.
            try:
                result_list = processor(multiple_processed_inputs)
            except Exception as error:  # pylint: disable=broad-except
                LOGGER.exception(
                    f"Batch processing encountered an exception {error}",
                    exc_info=error,
                )
                # Store error status for all the requests in the batch
                # This is not the ideal solution but it is impossible to know
                # which requests inside a batch succeeded and which ones
                # failed without changing the client side code.
                # The best solution for client code is to instead of
                # throwing an exception just return the exception object
                result_list = [error] * self.batch_size

            for request_id, result in zip(request_id_tuple, result_list):
                preserver.store_result(request_id, result)
