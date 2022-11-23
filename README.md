# Blog API

A headless Blog Entries API

## Prerequisites

* Java 17+
* PostgreSQL
* Zipkin (Optional)

## Getting Started

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

```
java -jar target/blog-api-5.0.0-SNAPSHOT.jar 
```

Download a blog entry template

```
curl -s http://localhost:8080/entries/template.md > template.md
```


Put a first blog entry from template

```
curl -s -u admin:changeme -XPUT http://localhost:8080/entries/1 -H "Content-Type: text/markdown" -d "$(cat template.md)"
```

or if you don't want to specify the entry id, post it

```
curl -s -u admin:changeme -XPOST http://localhost:8080/entries -H "Content-Type: text/markdown" -d "$(cat template.md)"
```

Check the entries

```
curl -s http://localhost:8080/entries
curl -s http://localhost:8080/entries/1
```

Delete the entry


```
curl -s -u admin:changeme -XDELETE http://localhost:8080/entries/1
```

Go to http://localhost:8080/swagger-ui/index.html for the Swagger UI

### GitHub integration

TBD

### Run on Kubernetes

TBD

### Run on Tanzu Application Platform

TBD

## License

Licensed under the Apache License, Version 2.0.
