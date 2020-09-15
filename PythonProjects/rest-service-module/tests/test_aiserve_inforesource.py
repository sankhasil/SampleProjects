"""Test suite for info resource creation."""

import logging
import dummyprocessor
import flask_restful  # type: ignore

import aiserve.configuration
import aiserve.inforesource


class DummyFlaskResource(flask_restful.Resource):
    """Dummy Flask resource with post endpoint."""

    def __init__(self, processor):
        self.processor = processor

    def post(self):
        """Post endpoint code for resource."""


def test_create_info_resource():
    """Test if creation of endpoint resource yields a valid resource."""
    # Create dummy configuration.
    resource_configuration = [
        aiserve.configuration.ResourceConfiguration(
            endpoints={"/": DummyFlaskResource},
            resource_cls_kwargs={"processor": dummyprocessor.DummyProcessor},
        )
    ]
    configuration = aiserve.configuration.ServiceConfiguration(
        LOGGING_LEVEL=logging.DEBUG,
        SECURITY_USE_SSO=False,
        SECURITY_SSO_SERVICE=None,
        RESOURCE_CONFIGURATION=resource_configuration,
        SERVICE_INFO="Aiserve - no info specified",
    )

    # Check if creation works.
    info_resource = aiserve.inforesource.create_info_resource(configuration)
    assert issubclass(info_resource, flask_restful.Resource)

    # Check if message is a string as expected.
    info_resource_message = info_resource.get()
    assert isinstance(info_resource_message, str)
