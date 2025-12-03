
# vaping-stamps-api

## Project Title

Vaping Stamps API Microservices

## Project Description

The UK Government announced the introduction of a new excise duty on vaping products to be introduced from 1st October 2026. 

Vaping Stamps APIs will complement HMRC's traditional tax compliance efforts to ensure that only approved businesses sell vaping products and that the necessary duty is paid. It will also help tackle the illicit trade that occurs in this business sector and so damages legitimate businesses. 

The APIs will provide a facility for the third party client stamp producer to only sell stamps to traders that are approved by HMRC. It will also enable the third party client to forward the necessary information to HMRC which is required to track compliance.  

## Requirements
```
Developed using Scala 3 with the Play Framework and suitable to run on JRE 21 or later.
```

## Running the service

### To run the whole stack under Service Manager
```
sm2 --start VAPING_STAMPS_API_ALL
```

### To control the service locally from the console
```
sbt run
```

To stop the service running:
```
sm2 --stop VAPING_STAMPS_API
```

## Endpoints

### 1. Health Check

The service exposes a simple health-check endpoint used for monitoring and deployment verification.

```
GET /health
```

This endpoint reports the operational status of the service and its backing MongoDB instance.

Behaviour

When the application is running:

```
Returns 200 
OK
```
and when the Service is Unavailable:

```
Returns 503 
The service cannot complete the health-check. 
```


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
