#! /bin/bash


NAMESPACE=cjoc1
#kubectl delete -n $NAMESPACE -f networkpolicy.yaml
kubectl apply -n $NAMESPACE -f networkpolicy.yaml
NAMESPACE=cloudbees-core
#kubectl delete -n $NAMESPACE -f networkpolicy.yaml
kubectl apply -n $NAMESPACE -f networkpolicy.yaml


