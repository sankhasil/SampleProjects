"""Script to start up the service, can be called via gunicorn."""

import os

import aiserve.appcreate

from aiqcheck.service import processor

APP = aiserve.appcreate.app_envvar_single_processor(
    model_path_url_env="AIQ_MODEL_PATH",
    post_endpoint_env="AIQ_POST_ENDPOINT",
    get_endpoint_env="AIQ_GET_ENDPOINT",
    service_info_env="AIQ_INFO",
    execution_mode_env="AIQ_EXECUTION_MODE",
    logging_env="AIQ_LOGGING_LEVEL",
    processor_class=processor.AiqProcessor,
)


def main():
    """Run service in development environment."""
    APP.run(debug=True, port=os.environ.get("AIQ_PORT", 5000))


def start_debug_service(port: int) -> None:
    """Run service in development environment with port."""
    APP.run(debug=True, port=port)


if __name__ == "__main__":
    main()
