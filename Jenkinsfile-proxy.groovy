// Uses Declarative syntax to run commands inside a container.
pipeline {
    agent {
        kubernetes {
            yaml '''
                kind: Pod
                metadata:
                  name: cloudbees-core
                spec:
                  containers:
                    - name: custom-agent
                      image: curlimages/curl:latest
                      env:
                        - name: HTTP_PROXY
                          value: http://squid-dev-proxy.squid.svc.cluster.local:3128
                        - name: HTTPS_PROXY
                          value: http://squid-dev-proxy.squid.svc.cluster.local:3128
                        - name: NO_PROXY
                          value: localhost,127.0.0.1,.cluster.local,.beescloud.com
                          # value: localhost,127.0.0.1,.cluster.local,.beescloud.com,.cloudbees.com
                      runAsUser: 1000
                      command:
                        - cat
                      tty: true
                      workingDir: "/home/jenkins/agent"
                      securityContext:
                        runAsUser: 1000
                '''
            defaultContainer 'custom-agent'
            retries 2
        }
    }
    stages {
        stage('Main') {
            steps {
                sh 'hostname'
                sh "curl -v https://docs.cloudbees.com"
            }
        }
    }
}
