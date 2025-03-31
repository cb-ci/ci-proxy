import javax.servlet.http.HttpServletRequest
import jenkins.model.Jenkins

// Get the current HTTP request
def request = org.kohsuke.stapler.Stapler.getCurrentRequest()

if (request) {
    def headers = [:]

    // Collect headers
    def headerNames = request.getHeaderNames()
    while (headerNames.hasMoreElements()) {
        def headerName = headerNames.nextElement()
        headers[headerName] = request.getHeader(headerName)
    }

    // Print headers to the console
    println "=== Incoming HTTP Headers ==="
    headers.each { key, value ->
        println "${key}: ${value}"
    }
} else {
    println "No current HTTP request found. Run this script in an actual request context."
}
