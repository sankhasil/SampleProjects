# API Descriptions - OpenAPI Specifications

This repository contains OpenAPI specifications for APIs provided by different projects.

## OpenAPI Specifications

[OpenAPI](https://github.com/OAI/OpenAPI-Specification) specifications allows standardized documentation of RESTful API. Users of the API can use this document to get each request parameters and responses.

## Swagger
From Swagger [website](https://swagger.io/docs/specification/about/):
>Swagger is a set of open-source tools built around the OpenAPI Specification that can help you design, build, document and consume REST APIs.

The major Swagger tools include:

[Swagger Editor](http://editor.swagger.io/) – browser-based editor where you can write OpenAPI specs.

[Swagger UI](https://swagger.io/tools/swagger-ui/) – renders OpenAPI specs as interactive API documentation.

[Swagger Codegen](https://github.com/swagger-api/swagger-codegen) – generates server stubs and client libraries from an OpenAPI spec.

## Validate with swagger-cli

[swagger-cli](https://github.com/APIDevTools/swagger-cli) is an opensource toll which can validate if the structure of an OpenAPI file is correct according to the specifications.

To use swagger-cli [Node.js](https://nodejs.org/en/download/) is prerequisite.

Installing swagger-cli globally allows us to use it in any directory. To install swagger-cli globally use this command:

```npm install -g @apidevtools/swagger-cli```

To validate a specific OpenAPI file run this command:

```swagger-cli validate <path-to-open-api-file>```

To see all the option available run this command:

``` swagger-cli --help ```


## Usage with [Postman](https://www.postman.com/)

Using OpenAPI specification file we can create a Postman collection with all the requests present in the specification.

To import a OpenAPI collection in Postman select **Import** option and then select **ImportFile**.

---

## Next Steps

* [ ] Add git prehook to validate OpenAPI file(s) with swagger-cli



