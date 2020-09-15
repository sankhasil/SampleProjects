"""
Easy example application.

Example showing the use of aiserve to apply a simple processing pipeline.

Examples
--------
While the app is running it's possible to post a request and acquire a request id as
answer:
    $ curl -d "num_1=3&num_2=2" -X POST http://127.0.0.1:5000/add_multiply

Then the result can be obtained using the request id

"""

import aiserve.factory
import examples.example_config

if __name__ == "__main__":
    # Create a flask.Flask using the created resource.
    APP = aiserve.factory.create_app(examples.example_config.CONFIGURATION)

    # Start the App
    APP.run(debug=True)
