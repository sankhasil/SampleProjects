#noinspection CucumberUndefinedStep
Feature: Testing a REST API with Karate

Scenario Outline: when a user wants to login in single sign on.
    Given url '<api>'
    And request {"username":"<username>","password":"<password>"}
    When method POST
    Then status <responseCode>

    Examples:
        | api                         | username | password | responseCode |
        | https://sso.ai4bd.org/login | wrong    | wrong    | 401          |
        | https://sso.ai4bd.org/login | correct  | wrong    | 401          |
        | https://sso.ai4bd.org/login | red.muh  | #(java.lang.System.getenv('MUH_PASSWORD'))    | 201          |


Scenario: Login and verify
    Given url 'https://sso.ai4bd.org/login'
    And request {"username":"red.muh","password":"#(java.lang.System.getenv('MUH_PASSWORD'))"}
    When method POST
    Then status 201

    Given url 'https://sso.ai4bd.org/verify'
    And header Authorization = 'Bearer ' + response
    When method GET
    Then status 200