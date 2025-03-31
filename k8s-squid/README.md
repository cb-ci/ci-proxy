# Install Squid

* https://dev.to/woovi/overcoming-ip-restrictions-leveraging-squid-proxy-on-kubernetes-for-api-consumption-20fd
* https://github.com/ahmetb/kubernetes-network-policy-recipes/blob/master/01-deny-all-traffic-to-an-application.md

## Install

see  [deploySquid.sh](deploySquid.sh)

> ./deploySquid.sh

## Watch squid access log

> kubectl exec -ti  deployment.apps/squid-dev-proxy  -- tail -f /var/log/squid/access.log

# Network Policies

## Enable network policy 

> gcloud container clusters update $CLUSTER_NAME   --enable-network-policy

## Verify if network policies are enabled 

> gcloud container clusters describe $CLUSTER_NAME

## Apply network policy to restrict outbound access 

see [applyNetworkPolicy.sh](applyNetworkPolicy.sh)


#  test with curl
```
export http_proxy="http://squid-dev-proxy.squid.svc.cluster.local:3128"
export https_proxy="http://squid-dev-proxy.squid.svc.cluster.local:3128"
curl -v https://www.google.com
```