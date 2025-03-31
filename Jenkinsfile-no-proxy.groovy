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
                echo "expected to run into an time out because of missing proxy"
                sh "curl -v https://docs.cloudbees.com"
            }
        }
    }
}
