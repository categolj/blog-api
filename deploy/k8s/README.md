## How to deploy blog-api to Kubernetes


### Create a secret for PostgreSQL

Set `DATABASE_URL` according to your environment as follows.
If you don't have an accessible PostgreSQL instance, please refer to [this documentation](../postgresql).

```
export NAMESPACE=...

kubectl create secret generic blog-db --from-literal DATABASE_URL="postgres://username:password@postgres.example.com/blog" --dry-run=client -oyaml | kubectl apply -f- -n ${NAMESPACE}
```

### Deploy a Deployment & Service & Ingress 

```
export DOMAIN=yourdomain.com
export CLUSTER_ISSUER=your-cluster-issuer

curl -L https://github.com/categolj/blog-api/raw/main/deploy/k8s/ingress.yaml |
  sed -e "s/example.com/${DOMAIN}/g" -e "s/letsencrypt/${CLUSTER_ISSUER}/g" > ingress.yaml 

kubectl apply \
  -f https://github.com/categolj/blog-api/raw/main/deploy/k8s/deployment.yaml \
  -f https://github.com/categolj/blog-api/raw/main/deploy/k8s/service.yaml \
  -f ingress.yaml \
  -n ${NAMESPACE}
```

### Deploy a Knative Service

```
kubectl apply -f https://github.com/categolj/blog-api/raw/main/deploy/k8s/kservice.yaml -n ${NAMESPACE}
```