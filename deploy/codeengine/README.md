## How to deploy blog-api to Kubernetes


```
IBM_CLOUD_USERNAME=...
IBM_CLOUD_PASSWORD=...

ibmcloud login -r jp-tok -u ${IBM_CLOUD_USERNAME} -p ${IBM_CLOUD_PASSWORD} -g Default

ibmcloud ce project create --name blog
```


### Using `ibmcloud`

```
ibmcloud ce project select --name blog
```

#### Create a secret for PostgreSQL

Set `DATABASE_URL` according to your environment as follows.
If you don't have an accessible PostgreSQL instance, please refer to [this documentation](../postgresql).

```
ibmcloud ce secret create --name blog-db --from-literal DATABASE_URL="postgres://username:password@postgres.example.com/blog"
```

### Create an application

```
ibmcloud ce app create \
  --name blog-api \
  -e spring.application.name="\${CE_SUBDOMAIN}:\${CE_APP}" \
  -e logging.level.io.opentelemetry.exporter.zipkin.ZipkinSpanExporter="ERROR" \
  --env-from-secret blog-db \
  --image ghcr.io/categolj/blog-api:native \
  --memory 1G \
  --cpu 0.5
```

### Using `kubectl`

```
ibmcloud ce project select --name blog
export KUBECONFIG=$(ibmcloud ce project current --output jsonpath='{.kube_config_file}')
```

#### Create a secret for PostgreSQL

Set `DATABASE_URL` according to your environment as follows.
If you don't have an accessible PostgreSQL instance, please refer to [this documentation](../postgresql).

```
kubectl create secret generic blog-db --from-literal DATABASE_URL="postgres://username:password@postgres.example.com/blog" --dry-run=client -oyaml | kubectl apply -f-
```

### Deploy a Knative Service

```
kubectl apply -f https://github.com/categolj/blog-api/raw/main/deploy/k8s/kservice.yaml
```