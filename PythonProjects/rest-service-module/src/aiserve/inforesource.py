"""Implements functionality to generate a info endpoint for a service."""

from typing import List, Type

import flask_restful  # type: ignore

import aiserve.configuration
import aiserve.logger

LOGGER = aiserve.logger.get_logger()


def create_info_resource(
    configuration: aiserve.configuration.ServiceConfiguration,
) -> Type[flask_restful.Resource]:
    """
    Create a new Resource class that returns information about the configuration.

    This is a class factory that analyzes a ServiceConfiguration and creates a message
    containing information about it. Then a new Resource is created that returns the
    message via its get endpoint.

    """
    # Extract information from the ServiceConfiguration.
    endpoint_names: List[str] = [
        endpoint_name
        for resource_config in configuration.RESOURCE_CONFIGURATION
        for endpoint_name in resource_config.endpoints.keys()
    ]

    service_info = configuration.SERVICE_INFO

    # Construct message.
    endpoint_description = str(endpoint_names) if endpoint_names else "No Endpoints"
    response_message = str(
        {"service info": service_info, "endpoints": endpoint_description}
    )

    # Create a new class that will later be returned.
    class InfoResource(flask_restful.Resource):
        """Resource that returns information about a service."""

        @staticmethod
        def get() -> str:
            """Return information about the service currently running."""
            return response_message

    return InfoResource
