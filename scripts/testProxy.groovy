import java.net.*

def urlString = "http://squid-dev-proxy.squid.svc.cluster.local"
def proxyHost = "proxy.example.com"
def proxyPort = 3128

// Set up the proxy
System.setProperty("http.proxyHost", proxyHost)
System.setProperty("http.proxyPort", proxyPort.toString())

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