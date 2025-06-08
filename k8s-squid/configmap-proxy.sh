#!/bin/bash

# Name of the ConfigMap
CONFIG_MAP_NAME=configmap-proxy-env
NAMESPACE=cjoc1  # change this if needed


export HTTP_PROXY='http://squid-dev-proxy.squid.svc.cluster.local:3128'
export HTTPS_PROXY='http://squid-dev-proxy.squid.svc.cluster.local:3128'
export NO_PROXY='localhost,127.0.0.1,.svc.cluster.local,.cluster.local,.beescloud.com'



# Create the ConfigMap YAML file
cat <<EOF > configmap-proxy.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: ${CONFIG_MAP_NAME}
  namespace: ${NAMESPACE}
data:
  HTTP_PROXY:  "${HTTP_PROXY}"
  HTTPS_PROXY: "${HTTPS_PROXY}"
  NO_PROXY:    "${NO_PROXY}"
EOF

echo "Generated configmap-proxy.yaml"

# Optionally apply it to your Kubernetes cluster
kubectl delete cm $CONFIG_MAP_NAME n ${NAMESPACE}
kubectl apply -f configmap-proxy.yaml -n ${NAMESPACE}
