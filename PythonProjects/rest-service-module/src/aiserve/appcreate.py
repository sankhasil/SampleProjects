"""
Module implementing top level wrappers for app creation.

This is meant as an entry point for the user of the aiserve module. Very specific
functions are offered here that set sensible default values for the more exotic
parameters of the underling functions.
"""
import flask

import aiserve.configuration
import aiserve.confparse
import aiserve.dictpreserver
import aiserve.factory
import aiserve.logger
import aiserve.process
import aiserve.utils


def app_envvar_single_processor(  # pylint: disable=too-many-locals
    model_path_url_env,
    post_endpoint_env,
    get_endpoint_env=None,
    service_info_env=None,
    processor_batch_size_env=None,
    logging_env="LOGGING_LEVEL",
    sso_url_env=None,
    execution_mode_env=None,
    processor_class=aiserve.process.BaseProcessor,
    preserver_class=aiserve.dictpreserver.DictPreserver,
) -> flask.Flask:
    """
    Create a service app form a local or remote folder.

    Parameters
    ----------
    model_path_url_env
        Either a local or remote folder containing all the processor information.
    post_endpoint_env
        Envvar used to configure the post endpoint.
    get_endpoint_env
        Envvar used to configure the get endpoint.
    service_info_env
        Envvar used to define information about the running service.
    processor_batch_size_env
        Envvar used to configure the used batch size, defaults to 1.
    logging_env
        Envvar used to configure the logging level.
    sso_url_env
        Envvar used to configure the sso service url, defaults to not using sso.
    execution_mode_env
        Envvar used to run the service is sync or async mode
    processor_class
        Root class of the processor. Only processors that are below the specified
        class can be instantiated via automatic processor inference. If the specified
        model folder contains no metadata the class defined here will be directly used.
    preserver_class
        Class that is used to preserve request and result data. This defaults to a
        simple dictionary implementation.

    Returns
    -------
    flask.Flask
        Rest service app.

    """
    # pylint: disable=too-many-arguments

    # Extract the log and execution mode env var
    logging_level = aiserve.utils.get_logging_level_from_envvar(logging_env)
    execution_mode = (
        aiserve.configuration.ASYNC_MODE_STR
        if execution_mode_env is None
        else aiserve.utils.get_execution_mode_value(execution_mode_env)
    )

    logger = aiserve.logger.get_logger()

    logger.set_logging_level(logging_level)  # type: ignore

    logger.debug(f"The log level set to {logging_level}")

    app_config = aiserve.confparse.get_application_config_from_envvar(
        sso_url_env=sso_url_env,
        service_info_env=service_info_env,
        model_path_url_env=model_path_url_env,
        post_endpoint_env=post_endpoint_env,
        get_endpoint_env=get_endpoint_env,
        execution_mode=execution_mode,
        processor_batch_size_env=processor_batch_size_env,
        processor_class=processor_class,
        preserver_class=preserver_class,
    )

    configuration = aiserve.confparse.configuration_for_app(
        (app_config.resource_parameter,),
        logging_level=logging_level,
        sso_url=app_config.sso_url,
        service_info=app_config.service_info,
    )

    logger.debug(f"Configuration for async service is created : {configuration}")

    return aiserve.factory.create_app(configuration)
