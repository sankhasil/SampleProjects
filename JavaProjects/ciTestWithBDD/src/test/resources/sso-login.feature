Feature: Test SSO Login Service
	As a user, I want to check sso service user login inputs
	So that I can be sure that no wrong username or password not able to login
	Also correct user able to login and receives the authorization token.



	Scenario Outline: when a user wants to login in single sign on.
		Given sso server address <api>

        When <username> and <password> is provided
        Then sso login should respond with <responseCode>
        And for correct username and password a valid authorization token. 

        Examples:
        | api                         | username | password | responseCode |
        | https://sso.ai4bd.org/login | wrong    | wrong    | 401          |
        | https://sso.ai4bd.org/login | correct  | wrong    | 401          |
        | https://sso.ai4bd.org/login | env:AUTHORIZATION_CREDS_ADMIN_USR | env:AUTHORIZATION_CREDS_ADMIN_PSW | 201 |




