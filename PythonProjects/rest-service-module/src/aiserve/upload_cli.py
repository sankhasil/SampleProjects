"""CLI to upload a folder."""
import logging
import pathlib

import click

import aiserve.logger
import aiserve.upload_module


@click.command()
@click.argument(
    "input_path",
    type=click.Path(
        exists=True, dir_okay=True, file_okay=False, readable=True, writable=True
    ),
)
@click.argument("remote_user", type=str)
@click.argument("server", type=str)
@click.argument(
    "output_path", type=click.Path(dir_okay=True, file_okay=False, writable=True)
)
@click.option("--debug", is_flag=True, help="Runs the command in debug mode")
def start_cli_for_uploading(
    input_path: str, remote_user: str, server: str, output_path: str, debug: bool,
) -> None:
    """
    Upload a folder and create a md5sum file.

    Parameters
    ----------
    input_path
        Local path the remote folder will be matched to.
    remote_user
        Remote user to connect to the server.
    server
        Remote download server.
    output_path
        Path to the remote download server.
    debug
        Logging configuration.
    """
    if debug:
        aiserve.logger.configure_debug_logging()
    else:
        aiserve.logger.configure_deployment_logging()

    # Load the folder.
    logging.info(  # pylint: disable=logging-format-interpolation
        f"Upload to {output_path}"
    )
    aiserve.upload_module.send_to_remote(
        input_path=pathlib.Path(input_path),
        user=remote_user,
        server=server,
        remote_path=pathlib.Path(output_path),
    )
    logging.info("Files have been sent.")
