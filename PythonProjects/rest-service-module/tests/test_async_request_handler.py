"""Test for the async_request_handler."""
# pylint: disable=redefined-outer-name
import dummyprocessor
import pytest
import testfixtures

import aiserve.async_request_handler
import aiserve.dictpreserver
import aiserve.logger


@pytest.fixture
def create_dummy_async_processor():
    """Create an instance of async request handler and dictpreserver."""
    dict_preserver_obj = aiserve.dictpreserver.DictPreserver()
    async_request_handler_obj = aiserve.async_request_handler.AsyncRequestHandler(
        processor=dummyprocessor.DummyProcessorForAsync(
            preserver_class=aiserve.dictpreserver.DictPreserver, batch_size=1
        ),
        preserver=dict_preserver_obj,
    )
    return async_request_handler_obj, dict_preserver_obj


@pytest.fixture
def create_log_capture_handler():
    """Create a handler for capturing logs."""
    logger = aiserve.logger.get_logger()
    logger.set_logging_level(10)
    create_log_capture_handler = testfixtures.LogCapture()
    logger.addHandler(create_log_capture_handler)
    return create_log_capture_handler


@pytest.fixture
def setup_for_handle_incoming_request_method(create_dummy_async_processor, mocker):
    """Create relevant mock objects for handle incoming request method."""
    async_request_handler_obj, dict_preserver_obj = create_dummy_async_processor
    patched_new_request_method_of_dict_preserver = mocker.patch.object(
        dict_preserver_obj, "new_request"
    )
    dummy_request_data = {"dummy": 123}
    patched_new_request_method_of_dict_preserver.return_value = "123"
    return (
        async_request_handler_obj,
        patched_new_request_method_of_dict_preserver,
        dummy_request_data,
    )


def test_initialize_async_request_handler(create_dummy_async_processor):
    """Test that async request handler can be initialized properly."""
    # pylint: disable=protected-access
    async_request_handler_obj, _ = create_dummy_async_processor
    assert not async_request_handler_obj._processing_loop_is_running


def test_handle_incoming_request_method(
    setup_for_handle_incoming_request_method, create_log_capture_handler, mocker
):
    """Test that the handle incoming request method works fine."""
    # pylint: disable=protected-access
    (
        async_request_handler_obj,
        patched_new_request_method_of_dict_preserver,
        dummy_request_data,
    ) = setup_for_handle_incoming_request_method

    patched_thread_creation = mocker.patch("threading.Thread")

    async_request_handler_obj.handle_incoming_request(request_data=dummy_request_data)

    # Check if the log calls have been made
    create_log_capture_handler.check(
        ("aiserve.logger", "INFO", "The processing loop is not running starting it...")
    )
    create_log_capture_handler.uninstall()
    patched_new_request_method_of_dict_preserver.assert_called_with(dummy_request_data)

    patched_thread_creation.assert_called_with(
        target=async_request_handler_obj._process_requests_using_preserver,
        args=(async_request_handler_obj.processor, async_request_handler_obj.preserver),
    )
