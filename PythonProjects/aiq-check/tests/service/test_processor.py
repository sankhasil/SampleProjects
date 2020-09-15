"""Tests for AIQ service processor."""
# pylint: disable=redefined-outer-name, protected-access
import PIL.Image

from aiqcheck.detector.qr import DetectedCodeWithPosition
from aiqcheck.service import processor

# Images are converted to this color mode
PIL_IMAGE_MODE: str = "L"

DATA_CODE_FORMAT: str = "mock_type"
DATA_CODE_CONTENT: str = "mock content."


def test_process(shared_datadir, mocker):
    """Test creation of result data."""
    test_processor = processor.AiqProcessor("dummyClass", 1, "dummyArgument")

    mocked_qr_detect_on_page = mocker.patch("aiqcheck.detector.qr.decode_code_on_page")
    mocked_qr_detect_on_page.return_value = [
        DetectedCodeWithPosition(
            code_type=DATA_CODE_FORMAT,
            content=DATA_CODE_CONTENT,
            confidence=0.0,
            position=None,
        )
    ]
    # Get test png file.
    input_data = [
        PIL.Image.open(shared_datadir / "science-abc-barcode.jpg").convert(
            PIL_IMAGE_MODE
        )
    ]
    results = test_processor.process(input_data=input_data)

    for result in results:
        for extract in result:
            assert extract.code_type == DATA_CODE_FORMAT
            assert extract.confidence == 0.0
            assert extract.content == DATA_CODE_CONTENT
