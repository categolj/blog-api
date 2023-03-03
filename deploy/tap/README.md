## How to deploy blog-api to Tanzu Application Platform


### Create a secret for PostgreSQL

Set `DATABASE_URL` according to your environment as follows.
If you don't have an accessible PostgreSQL instance, please refer to [this documentation](../postgresql).

```
export NAMESPACE=...

kubectl create secret generic blog-db --from-literal DATABASE_URL="postgres://username:password@postgres.example.com/blog" --dry-run=client -oyaml | kubectl apply -f- -n ${NAMESPACE}
```

In multi cluster mode, this secret should be created in "Run" clusters.

### Image to URL (native)

```
tanzu apps workload apply -f https://github.com/categolj/blog-api/raw/main/deploy/tap/workload-native.yaml -n ${NAMESPACE}
```

or 

```
kubectl apply -f https://github.com/categolj/blog-api/raw/main/deploy/tap/workload-native.yaml -n ${NAMESPACE}
```

### Image to URL (JVM)

```
tanzu apps workload apply -f https://github.com/categolj/blog-api/raw/main/deploy/tap/workload-jvm.yaml -n ${NAMESPACE}
```

or

```
kubectl apply -f https://github.com/categolj/blog-api/raw/main/deploy/tap/workload-jvm.yaml -n ${NAMESPACE}
```

### Source to URL (JVM)

```
tanzu apps workload apply -f https://github.com/categolj/blog-api/raw/main/deploy/tap/workload-source2url.yaml -n ${NAMESPACE}
```
or

```
kubectl apply -f https://github.com/categolj/blog-api/raw/main/deploy/tap/workload-source2url.yaml -n ${NAMESPACE}
```