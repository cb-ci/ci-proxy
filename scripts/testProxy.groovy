import java.net.*

def urlString = "https://google.com"
def proxyHost = "squid-dev-proxy.squid.svc.cluster.local"
def proxyPort = 3128

// Set up the proxy
System.setProperty("http.proxyHost", proxyHost)
System.setProperty("http.proxyPort", proxyPort.toString())
System.setProperty("https.proxyHost", proxyHost)
System.setProperty("https.proxyPort", proxyPort.toString())
//System.setProperty("https.nonProxyHosts","google.com")

// Create a URL object
def url = new URL(urlString)

// Open a connection
def connection = url.openConnection() as HttpURLConnection

// Set request method (GET, POST, etc.)
connection.requestMethod = "GET"

// Set timeouts (optional)
connection.setConnectTimeout(5000) // 5 seconds
connection.setReadTimeout(5000) // 5 seconds

// Get the response code
def responseCode = connection.responseCode
println "Response Code: ${responseCode}"

// Read the response (optional)
def inputStream = connection.inputStream
def response = inputStream.text
println "Response: ${response}"

// Close the connection
connection.disconnect()
