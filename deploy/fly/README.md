## How to deploy blog-api to [Fly.io](https://fly.io/)

```
export BLOG_NAME="blog-${RANDOM}"

$ flyctl postgres create -n ${BLOG_NAME}-db -r nrt

automatically selected personal organization: Toshiaki Maki
? Select configuration: Development - Single node, 1x shared CPU, 256MB RAM, 1GB disk
Creating postgres cluster in organization personal
Creating app...
Setting secrets on app blog-13823-db...
Provisioning 1 of 1 machines with image flyio/postgres-flex:15.2@sha256:caf62aa598b9a4ca63b11f0674239a730aad7091ffa4171a0c9665aba4184ccc
Waiting for machine to start...
Machine 17811616a57789 is created
==> Monitoring health checks
  Waiting for 17811616a57789 to become healthy (started, 3/3)

Postgres cluster blog-13823-db created
  Username:    postgres
  Password:    bnAP6ohgbvfy2Vu
  Hostname:    blog-13823-db.internal
  Flycast:     fdaa:0:2819:0:1::a
  Proxy port:  5432
  Postgres port:  5433
  Connection string: postgres://postgres:bnAP6ohgbvfy2Vu@[fdaa:0:2819:0:1::a]:5432

Save your credentials in a secure place -- you won't be able to see them again!

Connect to postgres
Any app within the Toshiaki Maki organization can connect to this Postgres using the above connection string

Now that you've set up Postgres, here's what you need to understand: https://fly.io/docs/postgres/getting-started/what-you-should-know/
```


```
flyctl postgres connect -a ${BLOG_NAME}-db
```

```
CREATE DATABASE blog;
\q
```

```
flyctl apps create --name ${BLOG_NAME}-api --machines
```

```
flyctl postgres attach -a ${BLOG_NAME}-api ${BLOG_NAME}-db
```

```
wget https://github.com/categolj/blog-api/raw/main/deploy/fly/fly.toml
flyctl deploy -a ${BLOG_NAME}-api
```

```
flyctl logs -a ${BLOG_NAME}-api
```