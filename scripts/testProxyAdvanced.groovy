import java.net.*
import java.util.Base64

/* ────────────────────────── CONFIGURATION ─────────────────────────────── */
def URL_STRING     = 'https://api.github.com/repos/cb-ci-templates/ci-poc-params-update/branches'
def PROXY_HOST     = 'squid-dev-proxy.squid.svc.cluster.local'
def PROXY_PORT     = 3128

def USE_BASIC_AUTH = true
def USERNAME       = 'bitbucketUser'
def PASSWORD       = System.getenv('BB_PASS') ?: 'changeMe'
def BEARER_TOKEN   = System.getenv('BB_PAT')  ?: 'tokenValue'

def CONNECT_TIMEOUT = 5_000
def READ_TIMEOUT    = 5_000
/* ───────────────────────────────────────────────────────────────────────── */

System.setProperty('sun.net.http.allowRestrictedHeaders', 'true')

// Optional: system-wide proxy settings (can be skipped if you use Proxy object below)
System.setProperty("http.proxyHost", PROXY_HOST)
System.setProperty("http.proxyPort", PROXY_PORT.toString())
System.setProperty("https.proxyHost", PROXY_HOST)
System.setProperty("https.proxyPort", PROXY_PORT.toString())

HttpURLConnection conn = null

try {
    // Optional: use Proxy object instead of relying on global system properties
    // def proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(PROXY_HOST, PROXY_PORT))
    // conn = (HttpURLConnection) new URL(URL_STRING).openConnection(proxy)

    conn = (HttpURLConnection) new URL(URL_STRING).openConnection()

    /* ---------- Request setup ---------- */
    conn.setRequestMethod('GET')
    conn.setConnectTimeout(CONNECT_TIMEOUT)
    conn.setReadTimeout(READ_TIMEOUT)
    conn.setRequestProperty('Accept', 'application/json')
    conn.setRequestProperty('User-Agent', 'groovy-client/1.0')

    if (USE_BASIC_AUTH) {
        def basic = Base64.encoder.encodeToString("${USERNAME}:${PASSWORD}".bytes)
        conn.setRequestProperty('Authorization', "Basic $basic")
    } else {
        conn.setRequestProperty('Authorization', "Bearer $BEARER_TOKEN")
    }

    /* ---------- Execute request ---------- */
    def rc  = conn.responseCode
    def msg = conn.responseMessage
    def inputStream = rc < 400 ? conn.inputStream : conn.errorStream
    def body = inputStream?.getText('UTF-8') ?: ''

    println "HTTP $rc $msg"
    println "URL: ${conn.URL}"
    println "Request Method: ${conn.requestMethod}"

    println "\nResponse Headers:"
    conn.headerFields.each { key, value -> println "${key ?: '(status)'}: ${value}" }

    println "\nResponse Body:"
    println (rc == 200 ? body : "[ERROR $rc] $body")

    // Optional: force exit if non-200
    // if (rc != 200) System.exit(1)

} catch (Exception e) {
    println "Exception: ${e.getClass().getName()} - ${e.message}"
    e.printStackTrace()
    System.exit(1)
} finally {
    try { conn?.inputStream?.close() } catch (Exception ignore) {}
    try { conn?.errorStream?.close() } catch (Exception ignore) {}
    conn?.disconnect()
}
