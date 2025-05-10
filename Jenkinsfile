pipeline {
    agent any

    environment {
        MAVEN_OPTS = '-Xmx1024m'
    }

    stages {
        stage('Build') {
            steps {
                echo 'Building the application...'
                sh 'mvn clean compile'
            }
        }

        stage('Test') {
            steps {
                echo 'Running tests...'
                sh 'mvn test'
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo 'Build and test successful!'
        }
        failure {
            echo 'Build failed!'
        }
    }
}