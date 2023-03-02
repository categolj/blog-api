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
$ curl -s http://localhost:8080/entries/1 | jq .
{
  "entryId": 1,
  "frontMatter": {
    "title": "Welcome to CategolJ!",
    "categories": [
      {
        "name": "Blog"
      },
      {
        "name": "Posts"
      },
      {
        "name": "Templates"
      }
    ],
    "tags": [
      {
        "name": "CategolJ"
      },
      {
        "name": "Hello World"
      }
    ]
  },
  "content": "Welcome\n\n**Hello world**, this is my first Categolj blog post.\n\nI hope you like it!",
  "created": {
    "name": "admin",
    "date": "2022-11-26T08:42:52.032376Z"
  },
  "updated": {
    "name": "admin",
    "date": "2022-11-26T08:42:52.032376Z"
  }
}
```

Delete the entry


```
curl -s -u admin:changeme -XDELETE http://localhost:8080/entries/1
```

Go to http://localhost:8080/swagger-ui/index.html for the Swagger UI

(`/swagger-ui/4.15.5/index.html` for native image)

### GitHub integration

TBD

### Run on Kubernetes

TBD

### Run on Tanzu Application Platform

TBD

## License

Licensed under the Apache License, Version 2.0.
