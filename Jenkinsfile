pipeline {
    agent any

    environment {
        SONAR_HOST_URL = 'http://sonarqube:9000'
        SONAR_TOKEN = credentials('sonar-token')
        npm_config_platform = 'linux'
        NODE_OPTIONS = '--openssl-legacy-provider'
    }

    stages {

        stage('Checkout') {
            steps {
                git url: 'https://github.com/nacef20/Esprit-PI4-4TSAE6-2025-2026-JungleInEnglish-Frontend.git',
                    branch: 'devops',
                    credentialsId: 'github-token'
            }
        }

        stage('Install Dependencies') {
            steps {
                dir('frontend') {
                    sh '''
                        rm -rf node_modules package-lock.json
                        npm install --legacy-peer-deps
                    '''
                }
            }
        }

        stage('Build Angular') {
            steps {
                dir('frontend') {
                    sh 'npm run build -- --configuration production'
                }
            }
        }

        stage('Tests') {
            steps {
                dir('frontend') {
                    sh 'npm run test -- --watch=false --browsers=ChromeHeadless'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                dir('frontend') {
                    withSonarQubeEnv('SonarQube') {
                        sh """
                            sonar-scanner \
                            -Dsonar.projectKey=forum-frontend \
                            -Dsonar.sources=src \
                            -Dsonar.host.url=$SONAR_HOST_URL \
                            -Dsonar.login=$SONAR_TOKEN
                        """
                    }
                }
            }
        }

        stage('Docker Build') {
            steps {
                dir('frontend') {
                    sh 'docker build -t nacef17/forum-frontend:latest .'
                }
            }
        }

        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-token',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh '''
                        echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
                        docker push nacef17/forum-frontend:latest
                    '''
                }
            }
        }
    }
}