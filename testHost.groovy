import java.net.URL
import java.net.HttpURLConnection

def connectToRemoteHost(urlString) {
    try {
        // Create URL object
        URL url = new URL(urlString)

        // Open connection
        HttpURLConnection connection = (HttpURLConnection) url.openConnection()
        connection.setRequestMethod("GET") // You can change this to POST, PUT, etc.
        connection.setConnectTimeout(5000) // Set connection timeout (in milliseconds)
        connection.setReadTimeout(5000)    // Set read timeout (in milliseconds)

        // Get response code
        int responseCode = connection.getResponseCode()
        println "Response Code: $responseCode"

        // Read response
        if (responseCode == HttpURLConnection.HTTP_OK) {
            def response = connection.inputStream.text
            println "Response: $response"
        } else {
            println "Failed to connect. Response Code: $responseCode"
        }

        // Disconnect
        connection.disconnect()
    } catch (Exception e) {
        println "Error: ${e.message}"
    }
}

// Example usage
connectToRemoteHost("https://www.google.com")