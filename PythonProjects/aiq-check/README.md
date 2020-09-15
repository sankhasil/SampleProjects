# aiqcheck


## Installation

To install the package, run

    pip install .

To install it in development mode, run

    pip install -e .


## Docker

The service is implemented via aiserve. To install aiserve from our PyPI server,
the PIP_EXTRA_INDEX_URL environment variable needs to be specified.
How to to this is documented [here](https://ontos4dds.atlassian.net/wiki/spaces/OTS/pages/554598412/Python+Development#PythonDevelopment-UsingourPyPIrepository)

To build the docker image, execute

    docker build --build-arg PIP_EXTRA_INDEX_URL=${PIP_EXTRA_INDEX_URL} -t aiqcheck .

Create an environment file `<name of environment file>.env` to set the environment variables:

    AIQ_MODEL_PATH=""
    AIQ_POST_ENDPOINT=/post
    AIQ_GET_ENDPOINT=/get
    AIQ_INFO="AI DATA CODE DETECT AND EXTRACTION"
    AIQ_LOGGING_LEVEL=DEBUG
    AIQ_EXECUTION_MODE=SYNC

To run it, execute

    docker run --rm -ti -p 5000:8000 --env-file <name of environment file>.env aiqcheck

Assuming, the service is running on port 5000, the POST request should look like the following:

    curl -X POST \
         --header "Content-Type: image/png" \
         --data-binary "@<image input path>" \
         localhost:5000/post

This returns a request ID. To get the produced result, run a GET request like this:

    curl -X GET \
         -d request-id=<the request ID returned by the POST request> \
         localhost:5000/get \
         --output "<output file path>"


## Using CLI
To see how to run the command line interface run

    aiqcheck --help

To see which environment variables have to be set, run

    aiqcheck service --help

To execute AIQ-Check via the command line interface, run

    aiqcheck decode <image path>

For detailed result

    aiqcheck decode <image path> --verbose

To start AIQ-Check as service, if no port is provided it will start in 5000 as default port.

    aiqcheck service --port=<port> [Optinal]
