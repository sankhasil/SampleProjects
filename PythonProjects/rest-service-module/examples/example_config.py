"""Adapted configuration used to showcase the functionality of aiserve."""

import pathlib
import tempfile

import src.aiserve.configuration
import examples.batched_addition_processor

# Here resources with their target endpoints are defined. The temporary file os only
# created to showcase functionality
# without adding an additional folder to the repo, in a real use case a already
# existing folder could be used.
with tempfile.TemporaryDirectory() as RESOURCE_FOLDER_PATH:
    RESOURCE_FOLDER_PATH = pathlib.Path(RESOURCE_FOLDER_PATH)
    # Create a file that is used by the processor to initialize.
    with open(RESOURCE_FOLDER_PATH.joinpath("multi.txt"), "w") as f:
        f.write("20")

    (
        REQUEST_RESOURCE,
        RESULT_RESOURCE,
    ) = examples.batched_addition_processor.AddMultiplyProcess.create_from_folder_infer_class(  # noqa: E501
        RESOURCE_FOLDER_PATH,
        preserver_class=src.aiserve.dictpreserver.DictPreserver,
        batch_size=10,
    ).as_resource()
    # Multiple resources can easily be added to the same API.
    RESOURCES = {"/add_multiply": REQUEST_RESOURCE, "/results": RESULT_RESOURCE}
    NAME = "Example"

CONFIGURATION = src.aiserve.configuration.make_processor_configuration(
    RESOURCES, service_info=NAME
)
