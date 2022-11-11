# Blog API

A headless Blog Entries API

## Prerequisites

* Java 17+
* PostgreSQL
* Zipkin (Optional)

## Getting Started


### Prepare GitHub integration

TBD

### Run blog-api locally

#### How to build blog-api

```
./mvnw clean package -DskipTests
```

#### How to run blog-api

Run Postgresql and zipkin

```
docker-compose up
```

Run the app

blog.github.access-token

```
java -jar target/blog-api-5.0.0-SNAPSHOT.jar --blog.github.access-token=TBD --blog.github.webhook-secret=TBD 
```

```
curl -s https://localhost:8080/entries
```

Go to http://localhost:8080/swagger-ui/index.html for the Swagger UI

### Run on Kubernetes

TBD

### Run on Tanzu Application Platform

TBD

## License

Licensed under the Apache License, Version 2.0.
