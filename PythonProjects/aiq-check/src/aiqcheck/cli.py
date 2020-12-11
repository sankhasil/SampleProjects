"""Cli for aiqcheck."""
import logging

import click

import aiqcheck.detector.qr as datacode_detector

# import aiqcheck.service.app as aiq_service


def configure_logging() -> None:
    """Configure logging of the application."""
    log_format = "%(asctime)s - %(name)s - %(levelname)s - %(message)s"
    logger = logging.getLogger()
    logger.setLevel("DEBUG")
    file_handler = logging.FileHandler("aiqcheck.log")
    formatter = logging.Formatter(log_format)
    file_handler.setFormatter(formatter)
    logger.addHandler(file_handler)


@click.group(
    help="""
To Start aiqcheck as service,

    aiqcheck service <port>[Optional]

    default port 5000

To decode and extract

    aiqcheck decode <imagePath>

To get detailed result for decode and extract\n
    aiqcheck decode <imagePath> --verbose

"""
)
def entry_point():
    """Aiqcheck entry point."""


@entry_point.command(
    help="""
    \b
    Please set below environment variables:
    AIQ_MODEL_PATH=""
    AIQ_POST_ENDPOINT=/post
    AIQ_GET_ENDPOINT=/get
    AIQ_INFO="AI DATA CODE DETECT AND EXTRACTION"
    AIQ_LOGGING_LEVEL=DEBUG
    AIQ_EXECUTION_MODE=SYNC

    Set desired port as an argument, default is 5000.
    """
)
@click.option(
    "--port",
    type=int,
    default="5000",
    show_default=True,
    help="Port when running app in debug mode.",
)
def service(port) -> None:
    """Start the service in port provided in argument, default is 5000."""
    import aiqcheck.service.app  # pylint: disable=import-outside-toplevel

    aiqcheck.service.app.start_debug_service(port=port)


@entry_point.command(help="Image file for detect and extract qr code.")
@click.argument(
    "image",
    type=click.Path(exists=True, file_okay=True, dir_okay=False),
    nargs=1,
    required=True,
)
@click.option("--verbose", is_flag=True, help="Set to print detailed result.")
def decode(image, verbose):
    """Decode and shows the image having data code."""
    input_image = datacode_detector.PIL.Image.open(image).convert("L")
    result = datacode_detector.decode_code_on_page(input_image)[0]
    if verbose:
        click.echo(
            f"""The Deode Result is:
        type: {result.code_type}
        content: {result.content}
        confidence: {result.confidence}
        positions: {result.position}
        """
        )
    else:
        click.echo(
            f"""The Deode Result is:
        type: {result.code_type}
        content: {result.content}
        """
        )


entry_point.add_command(service)
entry_point.add_command(decode)
