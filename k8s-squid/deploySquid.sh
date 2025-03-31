#! /bin/bash
NS_SQUID=squid
kubectl create ns $NS_SQUID
kubectl apply -f deploy-squid.yaml -n $NS_SQUID
kubectl rollout status deployment squid-dev-proxy -n $NS_SQUID
kubectl exec -n $NS_SQUID -ti  deployment.apps/squid-dev-proxy  -- tail -f /var/log/squid/access.log