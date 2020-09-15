# aiserve


Aiserve provides a barebone to implement RESTful services using flask. Functionality like logging, security and app
creation are handled by aiserve The actual implementation of the functionality is added by the user using a interface
provided by aiserve.

A top level explanation of the architecture and ideas behind this repository can be found here:
https://ontos4dds.atlassian.net/wiki/spaces/EIG/pages/745406536/AISERVE

## Repository structure

```
rest-service-module
│   README.md
│   pylintrc
|   requirements.txt
│   setup.py
|   setup.cfg
|   bitbucket-pipelines.yml         Used to automatically version and publish to pypi.
|
└───src.aiserve                     The actual module
|   |   factory.py                  A Flask app factory for arbitrary processors.
|   |   process.py                  Abstract base class as processor prototype.
|   |   remfolder.py                Provides functionality to cache remote folders locally.
|   |   configuration.py            Provides a configuration interface for the app factory.
|   |   confparse.py                Implements functions to parse configurations to the pre-defined interface.
|   |   preserver.py                Implements a interface used to preserve request data.
|   |   dictpreserver.py            Simple implementation of the  preserver interface using dict.
│   │   default_config.py           Default config for flask.
|   |   ...
│   │
│   └───examples                Working examples of the use of aiserve.
│       │   app.py
│       │   ...
│
└───tests                       Contains a pytest testsuite.
    │   test_context.py             Provides context to make testing possible without proper installation.
    |   test_aiserve_factory.py
    │   ...
```


## Installation

There are two ways to install aiserve which are both presented here.

###Installation via pip and our pypi server (recommended)

###### Add  pypi server to pip find links

*Please be aware that the <username> and <password> tags have to be substituted. Correct values can be found here: https://ontos4dds.atlassian.net/wiki/spaces/OTS/pages/554598412/Python+Development*

```console
export PIP_EXTRA_INDEX_URL=https://<username>:<password>@pypi.ai4bd.org/simple
```
###### Install using pip

```console
pip install aiserve
```

### Using Aiserve logging in a service

Aiserve provides a logger interface that can be used by any service. Furthermore, if the app was started with gunicorn, the logger also logs the messages to gunicorn error log.
The logger has a unique format for all service.

Aiserve logger can then be used like a normal logger from the logging library, for example:

```python
import aiserve.logger
logger = aiserve.logger.get_logger()
logger.debug("some debug message")
logger.info("some information message")
```

### Using CLI

To upload a folder with AISERVE via the command line interface, run

    aiserve-upload <input path> <user name> <server name> <output path>

Explanation of the parameters

`<input path>` - path to the directory that contains data that
should be uploaded

`<user name>` - user name on the remote machine for the ssh
connection

`<server name>` - name or ip adress of the server where the files
should be copied to

`<output path>` - the directory path on the remote machine, where
the data should be stored


## Testing

This repository contains an extensive pytest tesing suite in the "tests" subfolder. This can be used to either test for
correct installation or verify code changes. The tests can be run using the following command from the project root
directory:

*Please make sure pytest and pytest-cov are installed*
```console
pytest --cov=aiserve tests/*
```

## Making changes

If you want to make changes to this repository please follow our custom git workflow:
https://ontos4dds.atlassian.net/wiki/spaces/OTS/pages/47647447/...+Use+Git+for+our+Projects

And follow the rules stated for python development:
https://ontos4dds.atlassian.net/wiki/spaces/OTS/pages/554598412/Python+Development
