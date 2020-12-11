Feature: Check SSO Login service with Scenario outline
    which response valid user


    Scenario Outline: when a user wants to login in single sign on.
        Given sso login <api>
        And <username> and <password> is provided

        When I query the service api with the credentials
        Then sso login service <responseCode>

        Examples:
        | api                         | username | password | responseCode |
        | https://sso.ai4bd.org/login | wrong    | wrong    | 401          |
        | https://sso.ai4bd.org/login | correct  | wrong    | 401          |
       

