pipeline {
    agent {
        docker {
            image 'maven:3.9.5-eclipse-temurin-17'
            args '-v $HOME/.m2:/root/.m2'
        }
    }

    environment {
        DOCKER_USERNAME = credentials('docker-username')
        DOCKER_PASSWORD = credentials('docker-password')
        IMAGE_NAME = 'spring-boot-jenkins-demo'
        IMAGE_TAG = "${env.BUILD_NUMBER}"
        SERVER_IP = credentials('server-ip')
        SSH_USER = 'root'
        SSH_KEY = credentials('ssh-key')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -f Dockerfile -t ${DOCKER_USERNAME}/${IMAGE_NAME}:${IMAGE_TAG} .'
            }
        }

        stage('Push Docker Image') {
            steps {
                sh 'echo ${DOCKER_PASSWORD} | docker login -u ${DOCKER_USERNAME} --password-stdin'
                sh 'docker push ${DOCKER_USERNAME}/${IMAGE_NAME}:${IMAGE_TAG}'
                sh 'docker tag ${DOCKER_USERNAME}/${IMAGE_NAME}:${IMAGE_TAG} ${DOCKER_USERNAME}/${IMAGE_NAME}:latest'
                sh 'docker push ${DOCKER_USERNAME}/${IMAGE_NAME}:latest'
            }
        }

        stage('Deploy to VM') {
            steps {
                script {
                    // Create .env file for docker-compose
                    writeFile file: '.env', text: """
                    DOCKER_USERNAME=${DOCKER_USERNAME}
                    IMAGE_NAME=${IMAGE_NAME}
                    IMAGE_TAG=${IMAGE_TAG}
                    """

                    // Transfer files to the server
                    sh 'scp -i ${SSH_KEY} -o StrictHostKeyChecking=no docker-compose.prod.yml ${SSH_USER}@${SERVER_IP}:/root/'
                    sh 'scp -i ${SSH_KEY} -o StrictHostKeyChecking=no .env ${SSH_USER}@${SERVER_IP}:/root/.env'

                    // SSH into the VM and deploy
                    sh '''
                        ssh -i ${SSH_KEY} -o StrictHostKeyChecking=no ${SSH_USER}@${SERVER_IP} << 'EOF'
                        # Login to Docker Hub
                        echo ${DOCKER_PASSWORD} | docker login -u ${DOCKER_USERNAME} --password-stdin

                        # Pull the image
                        docker pull ${DOCKER_USERNAME}/${IMAGE_NAME}:${IMAGE_TAG}

                        # Deploy using docker-compose
                        cd /root
                        docker-compose -f docker-compose.prod.yml down || true
                        docker-compose -f docker-compose.prod.yml up -d

                        # Verify deployment
                        docker ps | grep spring-boot-app
                        EOF
                    '''
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo 'Pipeline executed successfully!'
        }
        failure {
            echo 'Pipeline execution failed!'
        }
    }
}