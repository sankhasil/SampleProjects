Meta:

Narrative:
As a user
I want to check sso service user login inputs
So that I can be sure that no wrong username or password not able to login
Also correct user able to login and receives the authorization token.



Scenario: when a user wants to login in single sign on.

Given sso login api

When username wrong and password wrong is provided
Then sso login respond: 401 unauthorized

When username wrong and password $ai4muh! is provided
Then sso login respond: 401 unauthorized



