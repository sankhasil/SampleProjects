"""QR Code Extractor."""

import dataclasses
from enum import Enum, unique
import tempfile
from typing import Callable, List, Sequence, Tuple

import aiserve.logger
import cv2  # pylint: disable=no-member
import numpy as np
import PIL.Image
import zbar
import zxing

LOGGER = aiserve.logger.get_logger()

_BLACK_PIXEL_VALUE = 0
_WHITE_PIXEL_VALUE = 255
_IMAGE_SUFFIX = ".png"


@unique
class CodeType(Enum):
    """Data code enum."""

    QR = "QR-Code"
    Bar = "Barcode"


_SUPPORTED_CODE_TYPE = {
    "QR_CODE": CodeType.QR,
    "CODE-128": CodeType.Bar,
    "UPC_E": CodeType.Bar,
    "UPC-A": CodeType.Bar,
}


@dataclasses.dataclass(frozen=True)
class Point:
    """Definition of a two-dimensional point."""

    x: int  # pylint: disable=invalid-name
    y: int  # pylint: disable=invalid-name


DatacodeCorners = Tuple[Point, Point, Point, Point]


@dataclasses.dataclass(frozen=True)
class DetectedCodeWithPosition:
    """Definition of a detected data code including its position."""

    code_type: str
    content: str
    confidence: float
    position: DatacodeCorners


DataCodeDecoder = Callable[[PIL.Image.Image], Sequence[DetectedCodeWithPosition]]

DECODER_IMPLEMENTATIONS: List[DataCodeDecoder] = []


def decode_code_on_page(image: PIL.Image.Image) -> List[DetectedCodeWithPosition]:
    """
    Try to detect data codes on a given image.

    Notes
    -----
    This method is able to detect QR codes as well as barcodes (type 128).
    Data matrices can not be detected!

    Parameters
    ----------
    target_image
        Input image.
    """
    for decoder in DECODER_IMPLEMENTATIONS:
        result = decoder(image)
        if result:
            return result
    return []


def register_implementation(implementation: DataCodeDecoder) -> DataCodeDecoder:
    """Decorate registring an implementation."""
    DECODER_IMPLEMENTATIONS.append(implementation)
    return implementation


@register_implementation
def decoder_zbar_based(target_image: PIL.Image.Image) -> List[DetectedCodeWithPosition]:
    """Decode implementation for data code detection based on Zbar Library.

    Parameters
    ----------
    target_image
        Input image.
    """
    binarized_image = _binarize(target_image)
    code_scanner = zbar.Scanner()
    scan_result = code_scanner.scan(binarized_image)
    LOGGER.debug(f"Zbar result {scan_result}")
    return [_convert_zbar_result(result) for result in scan_result]


@register_implementation
def decoder_zxing_based(
    target_image: PIL.Image.Image,
) -> List[DetectedCodeWithPosition]:
    """Decode implementation for data code detection based on Zxing Library.

    Parameters
    ----------
    target_image
        Input image.
    """
    reader = zxing.BarCodeReader()
    with tempfile.NamedTemporaryFile(suffix=_IMAGE_SUFFIX) as temp_img_file:
        target_image.save(temp_img_file)
        result = reader.decode(temp_img_file.name, try_harder=True)
        LOGGER.debug(f"zxing result {result}")
        if result is not None:
            return [
                DetectedCodeWithPosition(
                    code_type=_SUPPORTED_CODE_TYPE.get(result.format).value
                    if _SUPPORTED_CODE_TYPE.get(result.format) is not None
                    else result.format,
                    content=result.raw,
                    confidence=1.0,
                    position=tuple(Point(x=x, y=y) for (x, y) in result.points),
                )
            ]
        return []


def _convert_zbar_result(zbar_result: zbar.Symbol) -> DetectedCodeWithPosition:
    return DetectedCodeWithPosition(
        code_type=_SUPPORTED_CODE_TYPE.get(zbar_result.type).value
        if _SUPPORTED_CODE_TYPE.get(zbar_result.type) is not None
        else zbar_result.type,
        content=zbar_result.data.decode("utf-8"),
        confidence=1.0,
        position=tuple(
            Point(x=x, y=y)
            for (x, y) in zbar_result.position  # pylint: disable=no-member
        ),
    )


def _binarize(image: PIL.Image.Image) -> np.ndarray:
    """
    Apply image binarization.

    Parameters
    ----------
    image
        Image to binarize.

    Returns
    -------
    np.ndarray
        Binarized image.
    """
    image = np.asarray(image.convert("L"))
    _, image = cv2.threshold(  # pylint: disable=no-member
        src=image,
        thresh=_BLACK_PIXEL_VALUE,
        maxval=_WHITE_PIXEL_VALUE,
        type=cv2.THRESH_BINARY + cv2.THRESH_OTSU,  # pylint: disable=no-member
    )
    return image
