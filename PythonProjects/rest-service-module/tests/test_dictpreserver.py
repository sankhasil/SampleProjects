"""Tests for logic inside dictpreserver which have not been covered."""
import pytest

# pylint: disable=redefined-outer-name
import aiserve.dictpreserver
import aiserve.preserver


@pytest.fixture
def create_dummy_dict_preserver_object():
    """Fixture function for creating a dictpreserver object."""
    dict_preserver_object = aiserve.dictpreserver.DictPreserver()
    dummy_request_data = {"dummy": 123}
    dict_preserver_object.data["123"] = aiserve.dictpreserver.RequestElement(
        request_parameters=dummy_request_data
    )
    return dict_preserver_object


def test_initialize_a_request_element():
    """Test a simple initialization of RequestElement class."""
    request_data = {}
    request_element_object = aiserve.dictpreserver.RequestElement(
        request_parameters=request_data
    )
    assert request_element_object.request == request_data
    assert request_element_object.result is None
    assert request_element_object.status == aiserve.preserver.RequestStatus.PROCESSING


def test_get_result_for_dict_preserver(create_dummy_dict_preserver_object):
    """Test that the result method of dictpreserver works."""
    dict_preserver_object = create_dummy_dict_preserver_object
    existent_result = aiserve.preserver.RequestStatus.PROCESSING, None
    non_existent_result = aiserve.preserver.RequestStatus.DOES_NOT_EXIST, {}
    assert dict_preserver_object.result("non-existent") == non_existent_result
    assert dict_preserver_object.result("123") == existent_result


def test_request_method_for_dict_preserver(create_dummy_dict_preserver_object):
    """Test the request method of dictpreserver."""
    dict_preserver_object = create_dummy_dict_preserver_object
    assert dict_preserver_object.request(request_id="non-existent") is None
    assert dict_preserver_object.request(request_id="123") == {"dummy": 123}


def test_status_method_for_dict_preserver(create_dummy_dict_preserver_object):
    """Test the status method of dictpreserver."""
    dict_preserver_object = create_dummy_dict_preserver_object
    assert (
        dict_preserver_object.status(request_id="non-existent")
        == aiserve.preserver.RequestStatus.DOES_NOT_EXIST
    )
    assert (
        dict_preserver_object.status(request_id="123")
        == aiserve.preserver.RequestStatus.PROCESSING
    )


def test_delete_request_method_for_dict_preserver(create_dummy_dict_preserver_object):
    """Test the delete method of dictpreserver."""
    dict_preserver_object = create_dummy_dict_preserver_object
    dict_preserver_object.delete_result(request_id="123")
    assert (
        dict_preserver_object.data["123"].status
        == aiserve.preserver.RequestStatus.DELETED
    )
    assert dict_preserver_object.data["123"].result == {}


def test_next_request_ids_method_for_dict_preserver(create_dummy_dict_preserver_object):
    """Test the next request id's method of dictpreserver."""
    dict_preserver_object = create_dummy_dict_preserver_object
    dummy_request_data = {}
    dict_preserver_object.data["421"] = aiserve.dictpreserver.RequestElement(
        request_parameters=dummy_request_data
    )
    assert len(dict_preserver_object.next_request_ids(num_requests=2)) == 2
    assert len(dict_preserver_object.next_request_ids(num_requests=3)) == 2
    assert len(dict_preserver_object.next_request_ids(num_requests=1)) == 1


def test_new_request_method_for_dict_preserver(
    mocker, create_dummy_dict_preserver_object
):
    """Test the new request method of dictpreserver."""
    mocked_uuid4_method = mocker.patch("uuid.uuid4")
    dict_preserver_object = create_dummy_dict_preserver_object
    dict_preserver_object.new_request(request_data={})
    mocked_uuid4_method.assert_called_once()
    assert len(dict_preserver_object.data.keys()) == 2


def test_store_result_method_for_dict_preserver(create_dummy_dict_preserver_object):
    """Test the store result method of dictpreserver."""
    dict_preserver_object = create_dummy_dict_preserver_object
    done_result = {"result": "done"}
    dict_preserver_object.store_result(request_id="123", result=done_result)
    assert dict_preserver_object.data["123"].result == done_result
    assert (
        dict_preserver_object.data["123"].status
        == aiserve.preserver.RequestStatus.READY
    )


def test_store_result_method_for_dict_preserver_when_error(
    create_dummy_dict_preserver_object,
):
    """Test the store result method of dictpreserver when error happens."""
    dict_preserver_object = create_dummy_dict_preserver_object
    error_result = SyntaxError("There is a syntax erro")
    dict_preserver_object.store_result(request_id="123", result=error_result)
    assert dict_preserver_object.data["123"].result == error_result
    assert (
        dict_preserver_object.data["123"].status
        == aiserve.preserver.RequestStatus.READY
    )


def test_store_result_method_for_dict_preserver_for_non_existent(
    create_dummy_dict_preserver_object,
):
    """Test the store result method of dictpreserver for non existent req-id."""
    dict_preserver_object = create_dummy_dict_preserver_object
    assert not dict_preserver_object.store_result(request_id="non-existent", result={})
