
# Proxy settings for CasC/Helm

CloudBees CI Proxy configurations required in 

* Step1: Helm values
* Step2: CasC Operations Center 
* Step3: CasC Controller
* Step4: Ephemeral Agents (pod.yaml)

I tested this successfully with CasC bundles during a helm deploy wit CasC SCM Retriever enabled

The configuration context are shown below

# Step0: Proxy installation 

I used a [Squid Proxy](k8s-squid/deploy-squid.yaml) and [Kubernetes Network Policy](k8s-squid/networkpolicy.yaml) to test the configuration below (Step1 - Step4)

Your specific proxy server and port might be different. See [README.md](k8s-squid/README.md) for details and how I set up squid proxy

Once Squid is installed, you can watch the access log with

> kubectl exec -ti  deployment.apps/squid-dev-proxy  -- tail -f /var/log/squid/access.log

## Network Policy

I recommend to apply these Network Policy [networkpolicy.yaml](k8s-squid/networkpolicy.yaml) 

* It uses `egress` rules to restrict/deny outbound traffic from cloudbees pods (cjoc, controller, pod-agent)
* The onliest way to the outer world is passing the proxy (running in *another* dedicated namespace!)
* You might adjust the IP CIDR ranges in the network policy according to your cluster 
* Network policies must be enabled in your cluster before you can apply/use them. For GKE i did it like this: 
  * `gcloud container clusters update $CLUSTER_NAME   --enable-network-policy`

* See also https://kubernetes.io/docs/concepts/services-networking/network-policies/

# Step1: Helm values

NOTE: XX.XX.XX.beescloud.com is my hostName

helm values:
```
OperationsCenter:
  Enabled: true
  # OperationsCenter.HostName -- The hostname used to access Operations Center through the ingress controller.
  HostName: XX.XX.XX.beescloud.com  
  ContainerEnv:
    - name: HTTP_PROXY
      value: http://squid-dev-proxy.squid.svc.cluster.local:3128
    - name: HTTPS_PROXY
      value: http://squid-dev-proxy.squid.svc.cluster.local:3128
    - name: NO_PROXY
      value: localhost,127.0.0.1,.svc.cluster.local,.beescloud.com
  JavaOpts:
     -Dhttps.proxyHost=http://squid-dev-proxy.squid.svc.cluster.local
     -Dhttps.proxyPort=3128
     -Dhttp.proxyHost=http://squid-dev-proxy.squid.svc.cluster.local
     -Dhttp.proxyPort=3128
     -Dhttp.nonProxyHosts=localhost\|127.0.0.1\|*.svc.cluster.local\|*.beescloud.com
Master:
  Enabled: true
  # These properties will be exposed to https://ci.yourdomain.com/cjoc/manage/masterProvisioning/  -> Global Java Options
  # Global Java options enforced on startup by system property or environment variable MASTER_GLOBAL_JAVA_OPTIONS:
  JavaOpts:
    -Dhttps.proxyHost=http://squid-dev-proxy.squid.svc.cluster.local
    -Dhttps.proxyPort=3128
    -Dhttp.proxyHost=http://squid-dev-proxy.squid.svc.cluster.local
    -Dhttp.proxyPort=3128
    -Dhttp.nonProxyHosts=localhost\|127.0.0.1\|*.svc.cluster.local\|*.beescloud.com

```

## Notes:

* `-Dhttp.nonProxyHosts`
  * use `*.` as wildcard:
  * use `|` as delimiter: 
    * In Java system properties, the pipe `|` character is used to separate different non-proxy hosts
    * escape the pipe `|` character with `\` 
    * When using Helm values:
      * You NEED to escape it with backslashes like this `\|` , otherwise it leads you to an `command not found` error during cjoc startup.
    * When NOT using Helm values:
      * If you adjust the `-Dhttp.nonProxyHosts` on the Jenkins Web UI, the `\` escape character before the `|`delimiter is optional
  * IP Address Ranges:
    * CIDR patterns like `10.0.0.0/14,34.118.224.0/20` are not supported. 
    * `-Dhttp.nonProxyHosts` only accept explicit IP addresses or hostnames
  * Wildcard Subdomain like `*.svc.cluster.local` or `*.yourdomain.com`:
    * The wildcard for subdomains, like `*.cluster.local`, is correct and should work as expected
  * `*.beescloud.com` 
    * this is my second level domain used by CjoC .Yours might be different and need to adjusted
* `NO_PROXY`
  * use `,` as delimiter
  * CIDR range like `10.0.0.0/14,34.118.224.0/20` might be optional because not all tools can interpret it
  * use `.` as wildcard
    * `*` as wildcard does NOT work
    * However, here https://docs.cloudbees.com/docs/cloudbees-ci-kb/latest/cloudbees-ci-on-modern-cloud-platforms/considerations-for-http-proxy-configuration#_resolution it says it should work!?
* `HTTP_PROXY`, `HTTPS_PROXY` , `NO_PROXY`
  * use just the variables with uppercase
  * mixed with lower case like `http_proxy`,  `https_proxy` , `no_proxy` leads to `UnknownHostException http://squid-dev-proxy.squid.svc.cluster.local`
  * See also [State of proxy variable today](https://about.gitlab.com/blog/2021/01/27/we-need-to-talk-no-proxy/)

# Step2: CasC Operations Center

jenkins.yaml

```
jenkins:
  proxy:
    name: "squid-dev-proxy.squid.svc.cluster.local"
    noProxyHost: |-
      localhost
      127.0.0.1
      *.svc.cluster.local
      *.beescloud.com
    port: 3128
masterprovisioning:
  kubernetes:
    envVars: |-
      HTTP_PROXY=http://squid-dev-proxy.squid.svc.cluster.local:3128
      HTTPS_PROXY=http://squid-dev-proxy.squid.svc.cluster.local:3128
      NO_PROXY=localhost,.svc.cluster.local,.beescloud.com
```

## Notes:

* `jenkins.proxy.name`
  * add the proxy FQDN only!
    * use `squid-dev-proxy.squid.svc.cluster.local`
    * do NOT use `http://squid-dev-proxy.squid.svc.cluster.local`
    
# Step3: CasC Controller

jenkins.yaml

```
jenkins:
  proxy:
    name: "squid-dev-proxy.squid.svc.cluster.local"
    noProxyHost: |-
      localhost
      127.0.0.1
      *.svc.cluster.local
      *.beescloud.com
```
# Step4: Ephemeral Agents (pod) 

An agent pod template requires Proxy settings
See for the full example
* [Jenkinsfile-proxy.groovy](Jenkinsfile-proxy.groovy) 
* [Jenkinsfile-no-proxy.groovy](Jenkinsfile-no-proxy.groovy) 

```
...
  containers:
    - name: curltest
      image: curlimages/curl:latest
      env:
        - name: HTTP_PROXY
          value: http://squid-dev-proxy.squid.svc.cluster.local:3128
        - name: HTTPS_PROXY
          value: http://squid-dev-proxy.squid.svc.cluster.local:3128
        - name: NO_PROXY
          value: localhost,127.0.0.1,.cluster.local,.beescloud.com
 ...
```
The Pipeline log shows that squid proxy is used:

```
....
[Pipeline] sh
+ curl -v https://www.google.com
* Uses proxy env variable NO_PROXY == 'localhost,127.0.0.1,.cluster.local,.beescloud.com'
* Uses proxy env variable HTTPS_PROXY == 'http://squid-dev-proxy.squid.svc.cluster.local:3128'
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed

  0     0    0     0    0     0      0      0 --:--:-- --:--:-- --:--:--     0* Host squid-dev-proxy.squid.svc.cluster.local:3128 was resolved.
* IPv6: (none)
* IPv4: 34.118.229.2
*   Trying 34.118.229.2:3128...
* CONNECT tunnel: HTTP/1.1 negotiated
* allocate connect buffer
* Establish HTTP proxy tunnel to www.google.com:443
> CONNECT www.google.com:443 HTTP/1.1
> Host: www.google.com:443
> User-Agent: curl/8.12.1
> Proxy-Connection: Keep-Alive
> 
< HTTP/1.1 200 Connection established
....
```

# Seen Issues

Here are some issues i have seen:

## command not found

Without the `\` escape character in the helm values you will get an `command not found` error as shown below during the start of cjoc

```
://squid-dev-proxy.squid.svc.cluster.local -Dhttps.proxyPort=3128 -Dhttp.proxyHost=http://squid-dev-proxy.squid.svc.cluster.local -Dhttp.proxyPort=3128 -Dhttp.nonProxyHosts=127.0.0.1
++ localhost
/usr/local/bin/jenkins.sh: line 58: localhost: command not found
++ '*.svc.cluster.local'
/usr/local/bin/jenkins.sh: line 58: *.svc.cluster.local: command not found
++ .beescloud.com -Dcom.sun.management.jmxremote.port=4000 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.net.debug=ssl:handshake -Dcom.cloudbees.jenkins.cjp.installmanager.CJPPluginManager.enablePluginCatalogInOC=true -Djenkins.security.ManagePermission=true -Djenkins.security.SystemReadPermission=true -XX:+AlwaysPreTouch -XX:+UseStringDeduplication -XX:+ParallelRefProcEnabled -Dcore.casc.config.bundle=/var/jenkins_config/oc-casc-bundle -XX:+AlwaysActAsServerClassMachine -XX:+DisableExplicitGC '-Dcb.distributable.name=Docker Common CJE' -Dcb.distributable.commit_sha=1ec4d9ad44fce52a1865c9ac0dfbebc15711b4f1 -jar /usr/share/jenkins/jenkins.war --webroot=/tmp/jenkins/war --pluginroot=/tmp/jenkins/plugins --httpPort=8080 --prefix=/cjoc
/usr/local/bin/jenkins.sh: line 58: .beescloud.com: command not found
VM settings:
    Max. Heap Size (Estimated): 462.12M
    Using VM: OpenJDK 64-Bit Server VM

Usage: java [options] <mainclass> [args...]
           (to execute a class)
.....
```

* Works

```
# helm values
  JavaOpts:
     ...
     -Dhttp.nonProxyHosts=localhost\|127.0.0.1\|*.svc.cluster.local\|*.beescloud.com
```

* Doesn't work and leads to `command not found`

```
# helm values
  JavaOpts:
     ...
     -Dhttp.nonProxyHosts=localhost|127.0.0.1|*.svc.cluster.local|*.beescloud.com
```

## UnknownHostException http://squid-dev-proxy.squid.svc.cluster.local

* `HTTP_PROXY`, `HTTPS_PROXY`
  * use just the variables with uppercase
  * A mix lower case like `http_proxy` `https_proxy` leads to `UnknownHostException http://squid-dev-proxy.squid.svc.cluster.local`

---

## NO_PROXY URL contains invalid entry: '*.svc.cluster.local'

When using `*` in `NO_PROXY` i have seen a Pipeline fail with the error bellow

* worked : filter subdomains by `.domain`
* worked not: filter subdomains by `*.domain`

This is against what i have seen here https://docs.cloudbees.com/docs/cloudbees-ci-kb/latest/cloudbees-ci-on-modern-cloud-platforms/considerations-for-http-proxy-configuration#_resolution

```
java.net.MalformedURLException: NO_PROXY URL contains invalid entry: '*.svc.cluster.local'
	at PluginClassLoader for kubernetes-client-api//io.fabric8.kubernetes.client.utils.HttpClientUtils.isHostMatchedByNoProxy(HttpClientUtils.java:249)
	at PluginClassLoader for kubernetes-client-api//io.fabric8.kubernetes.client.utils.HttpClientUtils.configureProxy(HttpClientUtils.java:216)
	at PluginClassLoader for kubernetes-client-api//io.fabric8.kubernetes.client.utils.HttpClientUtils.applyCommonConfiguration(HttpClientUtils.java:185)
Caused: io.fabric8.kubernetes.client.KubernetesClientException: An error has occurred.
```
* Works:

```
  - name: NO_PROXY
    value: localhost,127.0.0.1,.svc.cluster.local,.beescloud.com
```

* Doesnt work:

```
  - name: NO_PROXY
    value: localhost,127.0.0.1,*.svc.cluster.local,*.beescloud.com
```


## BELOW ARE JUST SOME PRIVATE NOTES (CAN BE IGNORED)

# CIDR range for GKE

```
gcloud container clusters describe <YOUR-CLUSTER> --format="value(clusterIpv4Cidr, servicesIpv4Cidr)"
# --region <REGION> 
```

# CIDR Range for openShift

```
oc get network.config.openshift.io cluster -o yaml
oc get network.config.openshift.io cluster -o jsonpath='{.spec.clusterNetwork[*].cidr}'
oc get network.config.openshift.io cluster -o jsonpath='{.spec.externalIP}'
```

# Web UI: Env Vars: HTTP_PROXY / HTTPS_PROXY / NO_PROXY

State of variable today: https://about.gitlab.com/blog/2021/01/27/we-need-to-talk-no-proxy/

* https://example.com/cjoc/manage/masterProvisioning/  -> Global Environment Variables
* environment variables `HTTP_PROXY`, ` HTTPS_PROXY`, `NO_PROXY`
* use uppercase only
* `NO_PROXY` No quotes allowed, delimiter is `,` and   `*` is not allowed

# Web UI: System properties

* `http.nonProxyHosts`: No quotes allowed, delimiter is `|` and  `*` is  allowed.
* See also doc https://docs.cloudbees.com/docs/cloudbees-ci-kb/latest/best-practices/proxy-setup-validation  references `https.nonProxyHosts` !?

Go -> https://ci.whatever.com/cjoc/manage/masterProvisioning/ ->  System Properties
```
https.proxyHost=http://xxx.xxx.xxx.xx
https.proxyPort=3128
http.proxyHost=http://xxx.xxx.xxx.xx
http.proxyPort=3128
http.nonProxyHosts=localhost|127.0.0.1|*.svc.cluster.local|*.eks.amazonaws.com|*.cloudbees.com|*.jenkins.io|*.example.com
```
# Web UI: HTTP Proxy Configuration

* https://example.com/CONTROLLER/manage/configure -> HTTP Proxy Configuration

(Set Server without protocol, just  FQDN or IP)
Server:XX.XXX.XXX.XXX
Port:3128

No Proxy Host

```
127.0.0.1
*.svc.cluster.local
*.eks.amazonaws.com
*..cloudbees.com
*.jenkins.io
*.example.com
```

# Experimental -Dhttps.nonProxyHosts

`-Dhttps.nonProxyHosts` is not documented. Does it matter in some cases?

* https://docs.cloudbees.com/docs/cloudbees-ci-kb/latest/cloudbees-ci-on-modern-cloud-platforms/considerations-for-http-proxy-configuration#_resolution
* https://docs.cloudbees.com/docs/cloudbees-ci-kb/latest/best-practices/proxy-setup-validation
* https://stackoverflow.com/questions/17902830/jvm-arguments-for-https-nonproxyhosts
* https://docs.oracle.com/en/java/javase/21/core/java-networking.html#GUID-2C88D6BD-F278-4BD5-B0E5-F39B2BFAA840
* https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/net/doc-files/net-properties.html

if the `-Dhttps.nonProxyHosts` property is not defined, Java will fall back to using http.nonProxyHosts for HTTPS requests as well. This behavior applies both to Java applications and Jenkins.

Here's how it works:

* If `https.nonProxyHosts` is not explicitly set, Java and Jenkins will use the value from `http.nonProxyHosts` for both HTTP and HTTPS requests.
* This means you don't have to define `https.nonProxyHosts` separately if it's the same as `http.nonProxyHosts`; Java will apply the http.nonProxyHosts settings to both HTTP and HTTPS traffic.
