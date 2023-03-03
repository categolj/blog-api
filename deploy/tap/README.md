## How to deploy blog-api to Tanzu Application Platform


### Create a secret for PostgreSQL

```
export NAMESPACE=...

kubectl create secret generic blog-db --from-literal DATABASE_URL="postgres://username:password@postgres.example.com/blog" --dry-run=client -oyaml | kubectl apply -f- -n ${NAMESPACE}
```

In multi cluster mode, this secret should be created in "Run" clusters.

### Image to URL (native)

```
tanzu apps workload apply -f workload-native.yaml -n ${NAMESPACE} 
```

or 

```
kubectl apply -f workload-native.yaml -n ${NAMESPACE} 
```

### Image to URL (JVM)

```
tanzu apps workload apply -f workload-jvm.yaml -n ${NAMESPACE} 
```

or

```
kubectl apply -f workload-jvm.yaml -n ${NAMESPACE} 
```