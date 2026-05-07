pipeline {
    agent any

    environment {
        SONAR_HOST_URL = 'http://sonarqube:9000'
        CHROME_BIN = '/usr/bin/chromium'
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
                sh '''
                    node -v
                    npm -v
                    rm -rf node_modules package-lock.json .angular
                    npm install --legacy-peer-deps
                '''
            }
        }

        stage('Build Angular') {
            steps {
                sh 'npm run build -- --configuration production'
            }
        }

        stage('Tests + Coverage') {
            steps {
                sh 'npm run test -- --watch=false --browsers=ChromeHeadlessNoSandbox --code-coverage'
            }
        }

        stage('Debug Coverage') {
            steps {
                sh '''
                    echo "=== COVERAGE FILE CHECK ==="
                    find . -name lcov.info || true
                    ls -R coverage || true
                '''
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        npx sonar-scanner \
                        -Dsonar.projectKey=forum-frontend \
                        -Dsonar.sources=src \
                        -Dsonar.host.url=${SONAR_HOST_URL} \
                        -Dsonar.javascript.lcov.reportPaths=coverage/school-management/lcov.info
                    '''
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t nacef17/forum-frontend:latest .'
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

    post {
        success {
            echo 'Pipeline frontend terminé avec succès ✅'
        }
        failure {
            echo 'Pipeline échoué — vérifier les logs ❌'
        }
    }
}