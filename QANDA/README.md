# StaffBase Q&A Application
This application is a plugin for Question and Answers. It is designed over a very simple architecture called service oriented recently known as microservice architecture. Spring Boot provides excellent easy way to implement using this architecture. The design is 4 layer or 4 tier architecture where the last two layer implementation is added here.
![Image of Architecture](https://github.com/sankhasil/SampleProjects/QANDA/images/architectureProposal.png)

## Philosophy
- Segregating the Architecture layers into a 4 tier model provides benefits of the 3 Tier model. This inherently supports distributed environment. 
- Further reactivity makes calls nonblocking and allows to become resilient. 
- Ideally each functionality would be developed using a feature toggle, but by becoming reactive we do not need to have a plethora of conditions to support toggles. Individual service can turn on and turn off it self based on toggles and stop processing the event.
- Keeping the Data Structure different for Read and Write allows for specialized services and repos of faster retrieval through pre aggregated, cached data.

## Initial Domain Models
### Topic
Its the base of Post or Question creation. Without a Topic post or question cannot exists.
### Post
It can be a Post or marked as Question. A post can have list of comments in text. If it is a question then it can have list of Answers.
### Answer
It must have a post id associated with it. Post requires to be a question. Without that it should not get created.

![Image of Architecture](https://github.com/sankhasil/SampleProjects/QANDA/images/EnitityDiagram.jpg)
### BaseModel
The Base Model for any entity. Has the Audit Trail Data.



## UseCase
![Image of Architecture](https://github.com/sankhasil/SampleProjects/QANDA/images/userCase.jpg)

## Vision
![Image of Architecture](https://github.com/sankhasil/SampleProjects/QANDA/images/ecosystemVision.png)

![Image of Architecture](https://github.com/sankhasil/SampleProjects/QANDA/images/microserviceCommunicationVision.png)
