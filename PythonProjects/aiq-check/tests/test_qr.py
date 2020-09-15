"""Qr detector test."""
import PIL.Image

import aiqcheck.detector.qr as test_extractor

QR_CODE_CONTENT: str = "http://www.scienceabc.com"
BAR_CODE_CONTENT: str = "051111407592"

PIL_IMAGE_MODE: str = "L"


def test_decode_code_on_page_for_qr(shared_datadir):
    """Test Qr Code Detection and Extraction."""
    data_file = shared_datadir / "science-abc-barcode.jpg"
    input_image = PIL.Image.open(data_file).convert(PIL_IMAGE_MODE)
    results = test_extractor.decode_code_on_page(input_image)
    for result in results:
        assert result.code_type == test_extractor.CodeType.QR.value
        assert result.confidence == 1.0
        assert result.content == QR_CODE_CONTENT


def test_decode_code_on_page_for_no_detection(shared_datadir):
    """Test Qr Code Detection and Extraction."""
    data_file = shared_datadir / "barcodeOnly.png"
    input_image = PIL.Image.open(data_file).convert(PIL_IMAGE_MODE)
    results = test_extractor.decode_code_on_page(input_image)
    assert not results


def test_decode_code_on_page_for_barcode(shared_datadir):
    """Test Qr Code Detection and Extraction."""
    data_file = shared_datadir / "bar-code.jpg"
    input_image = PIL.Image.open(data_file).convert(PIL_IMAGE_MODE)
    results = test_extractor.decode_code_on_page(input_image)
    for result in results:
        assert result.code_type == test_extractor.CodeType.Bar.value
        assert result.confidence == 1.0
        assert result.content == BAR_CODE_CONTENT


def test_decoder_zbar_based(shared_datadir):
    """Test Qr Code Detection and Extraction using zbar strategy."""
    data_file = shared_datadir / "science-abc-barcode.jpg"
    input_image = PIL.Image.open(data_file).convert(PIL_IMAGE_MODE)
    results = test_extractor.decoder_zbar_based(target_image=input_image)
    for result in results:
        assert result.code_type == test_extractor.CodeType.QR.value
        assert result.confidence == 1.0
        assert result.content == QR_CODE_CONTENT


def test_decoder_zxing_based(shared_datadir):
    """Test Qr Code Detection and Extraction using zxing strategy."""
    data_file = shared_datadir / "science-abc-barcode.jpg"
    input_image = PIL.Image.open(data_file).convert(PIL_IMAGE_MODE)
    results = test_extractor.decoder_zxing_based(target_image=input_image)
    for result in results:
        assert result.code_type == test_extractor.CodeType.QR.value
        assert result.confidence == 1.0
        assert result.content == QR_CODE_CONTENT
