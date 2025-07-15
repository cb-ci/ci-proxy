# Test scripts

this directory contains some test scripts

Groovy scrpts can be executed from https://<CI_URL>/script


Run the testpod with
```BASH
 kubectl apply -f testPodProxy.yaml
 kubectl logs -f pod/groovy-runner
```

```BASH
➜  scripts git:(main) ✗ kubectl logs -fpod/groovy-runner
WARNING: Using incubator modules: jdk.incubator.vector, jdk.incubator.foreign
HTTP 200 OK
URL: https://api.github.com/repos/cb-ci-templates/ci-poc-params-update/branches
Request Method: GET

Response Headers:
(status): [HTTP/1.1 200 OK]
Server: [github.com]
X-RateLimit-Resource: [core]
Access-Control-Allow-Origin: [*]
X-RateLimit-Used: [2]
X-Content-Type-Options: [nosniff]
X-RateLimit-Reset: [1752616753]
x-github-api-version-selected: [2022-11-28]
Date: [Tue, 15 Jul 2025 21:03:15 GMT]
Referrer-Policy: [origin-when-cross-origin, strict-origin-when-cross-origin]
X-Frame-Options: [deny]
Strict-Transport-Security: [max-age=31536000; includeSubdomains; preload]
Access-Control-Expose-Headers: [ETag, Link, Location, Retry-After, X-GitHub-OTP, X-RateLimit-Limit, X-RateLimit-Remaining, X-RateLimit-Used, X-RateLimit-Resource, X-RateLimit-Reset, X-OAuth-Scopes, X-Accepted-OAuth-Scopes, X-Poll-Interval, X-GitHub-Media-Type, X-GitHub-SSO, X-GitHub-Request-Id, Deprecation, Sunset]
X-RateLimit-Remaining: [58]
Cache-Control: [public, max-age=60, s-maxage=60]
X-GitHub-Media-Type: [github.v3]
ETag: ["9c34cd84a3a3621381f41a0425afbb4e30141580c8f9bacd9cefabca81eb02c7"]
Content-Security-Policy: [default-src 'none']
Vary: [Accept,Accept-Encoding, Accept, X-Requested-With]
X-RateLimit-Limit: [60]
X-XSS-Protection: [0]
Content-Length: [434]
X-GitHub-Request-Id: [838E:20CD0:7B32B7:F8A700:6876C213]
Content-Type: [application/json; charset=utf-8]

Response Body:
[{"name":"dev","commit":{"sha":"33e4c4e166bbb5fe9edbd35384f0e0cd93ba0114","url":"https://api.github.com/repos/cb-ci-templates/ci-poc-params-update/commits/33e4c4e166bbb5fe9edbd35384f0e0cd93ba0114"},"protected":false},{"name":"main","commit":{"sha":"d6b7526a380d49bc7a91c1843a1b312e8c00d107","url":"https://api.github.com/repos/cb-ci-templates/ci-poc-params-update/commits/d6b7526a380d49bc7a91c1843a1b312e8c00d107"},"protected":false}]
```
