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

(`/swagger-ui/4.18.1/index.html` for native image)

### Use an existing PostgreSQL instance

Set `DATABASE_URL` according to your environment as follows.
If you don't have an accessible PostgreSQL instance, please refer to [this documentation](./deploy/postgresql).

```
export DATABASE_URL=postgres://username:password@postgres.example.com/blog
java -jar target/blog-api-5.0.0-SNAPSHOT.jar
```


### How to build a docker image

#### JVM

```
./mvnw -V spring-boot:build-image -DskipTests -Dspring-boot.build-image.imageName=IMAGE_NAME
```

You can use `ghcr.io/categolj/blog-api:jvm` as a pre-built image

#### Native

```
./mvnw -V -Pnative spring-boot:build-image -DskipTests -Dspring-boot.build-image.imageName=IMAGE_NAME
```

You can use `ghcr.io/categolj/blog-api:native` as a pre-built image

### GitHub integration

TBD

## How to deploy blog-api to XXXX

* [Kubernetes](./deploy/k8s)
* [Tanzu Application Platform](./deploy/tap)
* [Fly.io](./deploy/fly)
* [IBM Cloud Code Engine](./deploy/codeengine)

## License

Licensed under the Apache License, Version 2.0.
