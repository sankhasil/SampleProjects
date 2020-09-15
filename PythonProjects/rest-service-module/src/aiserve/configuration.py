"""
Module that implements a interface to configure a Service.

Explicit extraction of this object adds the benefit of decoupling configuration the
method from the processor. This way the creation of the configuration can be changed
freely by simply implementing a new way to create a configuration object.
"""
# "invalid-name" is disabled to allow upper case members in the configuration as
# normally used by flask.
#  pylint: disable=invalid-name

from typing import Dict, List, NamedTuple, Optional, Type, Union

import flask_restful  # type: ignore

import aiserve.async_request_handler
import aiserve.preserver
import aiserve.process

# Define dictionary keys used to define configuration parameters. Changing this
# changes the configuration interface!
LOGGING_LEVEL = "LOGGING_LEVEL"
SECURITY_USE_SSO = "SECURITY_USE_SSO"
SECURITY_SSO_SERVICE = "SECURITY_SSO_SERVICE"
RESOURCE_CONFIGURATION = "RESOURCE_CONFIGURATION"
SERVICE_INFO = "SERVICE_INFO"
ASYNC_MODE_STR = "ASYNC"
NON_ASYNC_MODE_STR = "SYNC"


class ServiceConfiguration(NamedTuple):
    """
    Data structure containing the information to create a service.

    Attributes
    ----------
    LOGGING_LEVEL
        Set the logging level used by the logging module. See logging module for more
        information about the mapping from level to int.
    SECURITY_USE_SSO
        Whether to use sso verification on incoming requests or not. If this is set
        to False SECURITY_SSO_SERVICE will be ignored.
    SECURITY_SSO_SERVICE
        The address of the sso service that will be requested when sso if activated.
        This is ignored if SECURITY_USE_SSO is set to False.
    ENDPOINT_RESOURCES
        Mapping resource endpoints to flask_restful.Resources.
    SERVICE_INFO
        Information about the service.
    """

    LOGGING_LEVEL: int
    SECURITY_USE_SSO: bool
    SECURITY_SSO_SERVICE: Optional[str]
    RESOURCE_CONFIGURATION: List["ResourceConfiguration"]
    SERVICE_INFO: str


class ResourceClsKwargsSync(NamedTuple):
    """Dataclass which is the resource class kwargs for sync processor."""

    processor: aiserve.process.BaseProcessor

    def asdict(self):
        """Return the dict representation of namedtuple."""
        return self._asdict()  # pylint: disable=no-member


class ResourceClsKwargsAsync(NamedTuple):
    """Dataclass which is the resource class kwargs for sync async processor."""

    processor: aiserve.process.BaseProcessor
    async_request_handler: aiserve.async_request_handler.AsyncRequestHandler

    def asdict(self):
        """Return the dict representation of namedtuple."""
        return self._asdict()  # pylint: disable=no-member


class ResourceConfiguration(NamedTuple):
    """Data structure containing info to create a flask resource."""

    endpoints: Dict[str, Type[flask_restful.Resource]]
    resource_cls_kwargs: Union[ResourceClsKwargsAsync, ResourceClsKwargsSync]
