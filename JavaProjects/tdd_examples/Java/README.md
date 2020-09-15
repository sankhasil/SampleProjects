# Java Unit Testing
![Java Logo](http://icons.iconarchive.com/icons/dakirby309/simply-styled/128/Java-icon.png) 

JUnit is an open source framework designed by Kent Beck, Erich Gamma for the purpose of writing and running test cases for java programs. 
In the case of web applications JUnit is used to test the application with out server. This framework builds a relationship between development and testing process
## Execution Tips for Java Unit Tests

The Maven Surefire Plugin will scan when the below command is triggered.

```
$ mvn test
```

It’s better to generate a project site to view the unit test result in HTML format.

```
$ mvn site
```
The project site will be generated at project\target\site, clicks index.html