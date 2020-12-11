import requests
import pytest
import pytest_bdd


@pytest.fixture
def context():
    """Context fixture used to pass info from when to then in feature tests."""
    return {}


@pytest_bdd.scenario(
    "ssologin.feature",
    "when a user wants to login in single sign on."
)
def test_login():
    """Just a simple name for the overall feature test."""
    pass


@pytest_bdd.given("sso login <api>", target_fixture="api")
def api(api):
    """Return the api to query."""
    return api


@pytest_bdd.given("<username> and <password> is provided", target_fixture='credentials')
def credentials(username, password):
    """Return the credentials to query the api."""
    return username, password


@pytest_bdd.when("I query the service api with the credentials")
def query_service(api, credentials, context):
    """Query the service using the credentials."""
    username, password = credentials  # Unpack credentials to get username and password
    context['status_code'] = requests.post(api, data={
        'username': username,
        'password': password
    }).status_code


@pytest_bdd.then("sso login service <responseCode>")
def check_response_code(responseCode, context):
    """Check that the response code from api is same as expected."""
    assert str(context['status_code']) == responseCode
