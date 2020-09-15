"""
Implements methods to parse a configuration.

This module is meant to be extended when additional way of configuration are to be
implemented.
"""
import os
from typing import NamedTuple, Optional, Tuple, Type, Union

import aiserve.async_request_handler
import aiserve.configuration
import aiserve.dictpreserver
import aiserve.logger
import aiserve.preserver
import aiserve.process
import aiserve.remfolder
import aiserve.resources

LOGGER = aiserve.logger.get_logger()


class ResourceParameters(NamedTuple):
    """
    Contains all the information needed to create a Processor.

    The attributes specialize for a sync processor only.For async
    an additional class is defined below which adds some attributes apart
    from the ones defined inside this class.

    Attributes
    ----------
    model_path_url
        Model path extracted from envvar.
    post_endpoint
       Value of the post endpoint.
    get_endpoint
        Value of the get endpoint.
    processor_batch_size
        Batch size value for processor.
    processor_class
        Class of the processor.
    preserver_class
        Class of the used preserver.
    execution_mode
        String representing the value of execution mode.
    """

    model_path_url: str
    post_endpoint: str
    get_endpoint: Optional[str]
    processor_batch_size: int
    processor_class: Type[aiserve.process.BaseProcessor]
    preserver_class: Type[aiserve.preserver.BasePreserver]
    execution_mode: str


class AppConfig(NamedTuple):
    """All config needed to create a flask app."""

    resource_parameter: ResourceParameters
    sso_url: Optional[str]
    service_info: Optional[str]


def get_application_config_from_envvar(  # pylint: disable=too-many-arguments
    sso_url_env: str,
    service_info_env: str,
    model_path_url_env: str,
    post_endpoint_env: str,
    get_endpoint_env: str,
    execution_mode: str,
    processor_batch_size_env: str,
    processor_class=aiserve.process.BaseProcessor,
    preserver_class=aiserve.dictpreserver.DictPreserver,
) -> AppConfig:
    """
    Extract the environment variables and create resource parameters for sync.

    Parameters
    ----------
    sso_url_env
        Envvar used to configure the sso service url, defaults to not using sso.
    service_info_env
        Envvar used to define information about the running service.
    model_path_url_env
        Either a local or remote folder containing all the processor information.
    post_endpoint_env
        Envvar used to configure the post endpoint.
    get_endpoint_env
        Envvar used to configure the get endpoint.
    execution_mode:
        String representing either sync or async execution.
    processor_batch_size_env
        Envvar used to represent the batch_size for processor.
    processor_class
        Root class of the processor. Only processors that are below the specified
        class can be instantiated via automatic processor inference. If the specified
        model folder contains no metadata the class defined here will be directly used.
    preserver_class
        Class that is used to preserve request and result data. This defaults to a
        simple dictionary implementation.
    """
    sso_url = os.environ[sso_url_env] if sso_url_env is not None else None
    service_info = (
        os.environ[service_info_env] if service_info_env is not None else None
    )
    batch_size = (
        aiserve.utils.convert_env_var_to_int(processor_batch_size_env)
        if execution_mode == aiserve.configuration.ASYNC_MODE_STR
        else 1
    )
    model_path_url = os.environ[model_path_url_env]
    post_endpoint = os.environ[post_endpoint_env]
    get_endpoint = (
        os.environ[get_endpoint_env]
        if execution_mode == aiserve.configuration.ASYNC_MODE_STR
        else None
    )

    LOGGER.info(f"The execution mode is {execution_mode}")
    LOGGER.debug(f"sso_url: {sso_url} and service_info : {service_info}")
    LOGGER.debug(f"Extracted model path url as {model_path_url}")
    LOGGER.debug(f"Batch size is {batch_size}")

    return AppConfig(
        resource_parameter=ResourceParameters(
            model_path_url=model_path_url,
            post_endpoint=post_endpoint,
            get_endpoint=get_endpoint,
            processor_class=processor_class,
            processor_batch_size=batch_size,
            preserver_class=preserver_class,
            execution_mode=execution_mode,
        ),
        sso_url=sso_url,
        service_info=service_info,
    )


def configuration_for_app(
    resource_parameters: Tuple[ResourceParameters],
    logging_level: int = 10,
    sso_url: str = None,
    service_info: str = None,
) -> aiserve.configuration.ServiceConfiguration:
    """
    Create the configuration needed for app.

    Parameters
    ----------
    resource_parameters
        Parameters needed to initialize the processors.
    logging_level
       The logging level of the application.
    sso_url
        The sso_url value extracted from the environment variable.
    service_info
        service info information extracted from envvar

    Returns
    -------
    aiserve.configuration.ServiceConfiguration
        The configuration as parsed from the environment.
    """
    # Create resource for all parametrized resource keys.
    resources_configuration = [
        _resource_from_parameters(parameters) for parameters in resource_parameters
    ]

    LOGGER.debug(f"The updated resources dict :{resources_configuration}")

    service_info = (
        "Aiserve - no info specified" if service_info is None else service_info
    )

    use_sso = sso_url is not None

    return aiserve.configuration.ServiceConfiguration(
        logging_level, use_sso, sso_url, resources_configuration, service_info
    )


def _resource_from_parameters(
    resource_params: ResourceParameters,
) -> aiserve.configuration.ResourceConfiguration:
    """
    Create a resource given resource parameters.

    Parameters
    ----------
    resource_params
        Parameters defining one resource.

    Returns
    -------
    dict
        Endpoint -> resource mappings
        {post_endpoint: post_resource,
         get_endpoint: get_resource}
    """
    resource_cls_kwargs: Union[
        aiserve.configuration.ResourceClsKwargsAsync,
        aiserve.configuration.ResourceClsKwargsSync,
    ]

    resource_folder = aiserve.remfolder.sync_from_remote_if_url(
        resource_params.model_path_url
    )
    LOGGER.debug(f"The resource folder extracted is {resource_folder}")

    execution_mode = resource_params.execution_mode
    post_endpoint = resource_params.post_endpoint

    if execution_mode == aiserve.configuration.NON_ASYNC_MODE_STR:

        # For sync mode we don't need batch_size to be passed in since it would not
        # have any effect
        processor = resource_params.processor_class.create_from_folder_infer_class(
            resource_folder
        )

        resource_cls_kwargs = aiserve.configuration.ResourceClsKwargsSync(
            processor=processor
        )

        return aiserve.configuration.ResourceConfiguration(
            resource_cls_kwargs=resource_cls_kwargs,
            endpoints={post_endpoint: aiserve.resources.RequestPostingResourceSync},
        )

    get_endpoint = resource_params.get_endpoint
    assert get_endpoint is not None

    # For ASYNC mode of execution we have to pass in the batch_size
    # So that processors which want to use it can use it
    # For SYNC mode the batch_size should have a value of 1 hence no harm done
    processor = resource_params.processor_class.create_from_folder_infer_class(
        resource_folder, batch_size=resource_params.processor_batch_size
    )
    preserver = resource_params.preserver_class()

    resource_cls_kwargs = aiserve.configuration.ResourceClsKwargsAsync(
        processor=processor,
        async_request_handler=aiserve.async_request_handler.AsyncRequestHandler(
            processor=processor,
            preserver=preserver,
            batch_size=resource_params.processor_batch_size,
        ),
    )

    return aiserve.configuration.ResourceConfiguration(
        resource_cls_kwargs=resource_cls_kwargs,
        endpoints={
            post_endpoint: aiserve.resources.RequestPostingResource,
            get_endpoint: aiserve.resources.ResultRetrievalResource,
        },
    )
