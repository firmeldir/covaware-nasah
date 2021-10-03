# covaware-nasah
## _United to Fight the Global Crisis_

# Android application
To open Android Project install at least Arctic Fox 2020.3.1 with Kotlin.

# Covid prediction model
Provides a Flask API that allows to calculate Covid risks

### Example
http://ec2co-ecsel-u4qnetyi7ch9-1971977146.eu-west-1.elb.amazonaws.com:5000/risks?city=Los%20Angeles&places=%5B0,1,4,0,3,0,5,2,2%5D

## Pre-requisites for local dev
To run the prediction locally, a `.csv` file with the data is needed currently.

https://drive.google.com/file/d/14Ahl_VqDa9gIVZPtrOQnWLLdpAKxsBAw/view?usp=sharing

## Deployment
Currently deployed to AWS [ECS](https://aws.amazon.com/ecs/?whats-new-cards.sort-by=item.additionalFields.postDateTime&whats-new-cards.sort-order=desc&ecs-blogs.sort-by=item.additionalFields.createdDate&ecs-blogs.sort-order=desc).

[ECR](https://aws.amazon.com/ecr/) public Docker image: https://gallery.ecr.aws/w8q0t3l7/model

Deployment URL: http://ec2co-ecsel-u4qnetyi7ch9-1971977146.eu-west-1.elb.amazonaws.com:5000/

$ 