Feature: Test Sample Service
	As a user, I want to check sample service.
	



	Scenario Outline: Sample service invoked.
		
        When <user> is provided
        Then service should respond with <responseCode>
        And correct <response> 

        Examples:
        | user  | response 									| responseCode |
        | David | Hello David in Micronaut  | 200          |
        | NULL  |                           | 406          |
        | Sam 	| Hello Sam in Micronaut 		| 200 				 |




