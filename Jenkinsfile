pipeline {
    agent {
        docker {
            image 'zim0101/jenkins-maven-17-agent:latest'
            args '-v $HOME/.m2:/root/.m2 -v /var/run/docker.sock:/var/run/docker.sock'
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
                sh 'docker build -f Dockerfile.prod -t ${DOCKER_USERNAME}/${IMAGE_NAME}:${IMAGE_TAG} .'
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
                sshagent(['ssh-key']) {
                    // Create the .env file content
                    sh '''
                        echo "DOCKER_USERNAME=${DOCKER_USERNAME}" > .env
                        echo "IMAGE_NAME=${IMAGE_NAME}" >> .env
                        echo "IMAGE_TAG=${IMAGE_TAG}" >> .env
                    '''
                    
                    // Use SSH to create directory if it doesn't exist
                    sh 'ssh -o StrictHostKeyChecking=no ${SSH_USER}@${SERVER_IP} "mkdir -p /root"'
                    
                    // Copy files to remote server
                    sh 'scp -o StrictHostKeyChecking=no docker-compose.prod.yml ${SSH_USER}@${SERVER_IP}:/root/'
                    sh 'scp -o StrictHostKeyChecking=no .env ${SSH_USER}@${SERVER_IP}:/root/'
                    
                    // Execute deployment commands on remote server
                    sh '''
                        ssh -o StrictHostKeyChecking=no ${SSH_USER}@${SERVER_IP} "
                            echo ${DOCKER_PASSWORD} | docker login -u ${DOCKER_USERNAME} --password-stdin
                            docker pull ${DOCKER_USERNAME}/${IMAGE_NAME}:${IMAGE_TAG}
                            cd /root
                            docker-compose -f docker-compose.prod.yml down || true
                            docker-compose -f docker-compose.prod.yml up -d
                            docker ps | grep spring-boot-app
                        "
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