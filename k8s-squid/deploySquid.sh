#! /bin/bash

kubectl create ns squid
kubectl apply -f deploy-squid.yaml -n squid

kubectl exec -ti  deployment.apps/squid-dev-proxy  -- tail -f /var/log/squid/access.log