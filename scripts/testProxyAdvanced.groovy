import java.net.*
import java.util.Base64

/* ────────────────────────── CONFIGURATION ─────────────────────────────── */
// Target REST endpoint
def URL_STRING   = 'https://api.github.com/repos/cb-ci-templates/ci-poc-params-update/branches'

// Proxy settings
def PROXY_HOST   = 'squid-dev-proxy.squid.svc.cluster.local'
def PROXY_PORT   = 3128

// Authentication
def USE_BASIC_AUTH = true          // set to false to use a Bearer token
def USERNAME       = 'bitbucketUser'
def PASSWORD       = System.getenv('BB_PASS') ?: 'changeMe'
def BEARER_TOKEN   = System.getenv('BB_PAT')  ?: 'tokenValue'

// Timeouts (ms)
def CONNECT_TIMEOUT = 5_000
def READ_TIMEOUT    = 5_000
/* ───────────────────────────────────────────────────────────────────────── */

/* GitHub blocks requests that lack a User‑Agent header.
   Java’s built‑in HTTP client normally strips “Authorization” when a proxy is in use.
   Enabling this system flag preserves the header across the proxy hop.            */
System.setProperty('sun.net.http.allowRestrictedHeaders', 'true')

// Set up the proxy
System.setProperty("http.proxyHost", proxyHost)
System.setProperty("http.proxyPort", proxyPort.toString())
System.setProperty("https.proxyHost", proxyHost)
System.setProperty("https.proxyPort", proxyPort.toString())
HttpURLConnection conn = null

HttpURLConnection conn = null
try {
    /* Build a proxy object instead of relying on global system properties.
       That way, only this connection – not the entire JVM – is proxied.       */
    /*
    def proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(PROXY_HOST, PROXY_PORT))
    conn = (HttpURLConnection) new URL(URL_STRING).openConnection(proxy)
    */

    conn = (HttpURLConnection) new URL(URL_STRING).openConnection()

    /* ---------- Request setup ---------- */
    conn.requestMethod  = 'GET'
    conn.connectTimeout = CONNECT_TIMEOUT
    conn.readTimeout    = READ_TIMEOUT
    conn.setRequestProperty('Accept', 'application/json')
    conn.setRequestProperty('User-Agent', 'groovy-client/1.0')

    // Set the appropriate Authorization header
    if (USE_BASIC_AUTH) {
        def basic = Base64.encoder.encodeToString("${USERNAME}:${PASSWORD}".bytes)
        conn.setRequestProperty('Authorization', "Basic $basic")
    } else {
        conn.setRequestProperty('Authorization', "Bearer $BEARER_TOKEN")
    }

    /* ---------- Execute request ---------- */
    def rc          = conn.responseCode
    def inputStream = rc < 400 ? conn.inputStream : conn.errorStream
    def body        = inputStream.getText('UTF-8')

    println "HTTP $rc"
    println (rc == 200 ? "OK:\n$body" : "Error $rc:\n$body")
    if (rc != 200) System.exit(1)

} catch (Exception e) {
    e.printStackTrace()
    System.exit(1)
} finally {
    conn?.inputStream?.close()
    conn?.errorStream?.close()
    conn?.disconnect()
}
