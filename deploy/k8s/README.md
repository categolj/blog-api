## How to deploy blog-api to Kubernetes


### Create a secret for PostgreSQL

```
export NAMESPACE=...
export DOMAIN=yourdomain.com
export CLUSTER_ISSUER=your-cluster-issuer

kubectl create secret generic blog-db --from-literal DATABASE_URL="postgres://username:password@postgres.example.com/blog" --dry-run=client -oyaml | kubectl apply -f- -n ${NAMESPACE}

sed -i.bak -e "s/example.com/${DOMAIN}/g" -e "s/letsencrypt/${CLUSTER_ISSUER}/g" ingress.yaml

kubectl apply -f deployment.yaml -f service.yaml -f ingress.yaml -n ${NAMESPACE}
```