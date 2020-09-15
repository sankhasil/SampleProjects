"""Data Code Extractor Processor."""


import io
import pathlib
from typing import List, Mapping, Sequence

import aiserve.logger
import aiserve.process
import flask
import flask_restful
import PIL.Image
import werkzeug.exceptions

import aiqcheck.detector.qr as extractor

LOGGER = aiserve.logger.get_logger()
PIL_IMAGE_MODE: str = "RGB"


class AiqProcessor(aiserve.process.BaseProcessor):
    """Processor qr code detection and extraction."""

    def preprocess(self) -> PIL.Image.Image:
        """
        Open the data from a request as an PIL.Image.Image.

        Returns
        -------
        PIL.Image.Image
            Image decoded from the request.
        """

        LOGGER.info("Incoming request")
        if not flask_restful.request.data:
            raise werkzeug.exceptions.BadRequest(
                "The request does not contain data, send image data with request."
            )

        try:
            return PIL.Image.open(io.BytesIO(flask_restful.request.data)).convert(
                PIL_IMAGE_MODE
            )
        except (IOError, OSError):
            raise werkzeug.exceptions.PreconditionFailed(
                "The passed data could not be decoded to an image, send image data."
            ) from IOError or OSError

    @staticmethod
    def _kwargs_from_folder(folder_path: pathlib.Path) -> Mapping[str, str]:
        return {}

    def process(  # pylint: disable=arguments-differ
        self, input_data: Sequence[PIL.Image.Image]
    ) -> List[List[extractor.DetectedCodeWithPosition]]:
        """
        Extract the qr code from a image.

        Parameters
        ----------
        input_data
            List of images.

        Returns
        -------
        List
            List
                DetectedCodeWithPosition
                Each detected code is specified by the following:
                type, content and position inside the input image.
        """
        return [extractor.decode_code_on_page(image) for image in input_data]

    def postprocess(  # pylint: disable=arguments-differ
        self, input_data: List[List[extractor.DetectedCodeWithPosition]]
    ) -> flask.Response:
        """
        Create a response based on the results.

        Parameters
        ----------
        List
            List
                DetectedCodeWithPosition retured by process.

        Returns
        -------
        flask.Response
            Formatted response.
        """
        if isinstance(input_data, Exception):
            raise werkzeug.exceptions.PreconditionFailed(
                f"Data received for input has an error. data={input_data}"
            )
        return flask.jsonify(input_data)
