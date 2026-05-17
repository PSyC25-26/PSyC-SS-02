pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/PSyC25-26/PSyC-SS-02.git'
            }
        }

        stage('Build') {
            steps {
                dir('banca-online') {
                    sh 'chmod +x mvnw && ./mvnw clean compile -B'
                }
            }
        }

        stage('Test') {
            steps {
                dir('banca-online') {
                    sh './mvnw test -B'
                }
            }
            post {
                always {
                    junit 'banca-online/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Coverage Report') {
            steps {
                dir('banca-online') {
                    sh './mvnw jacoco:report'
                }
            }
        }
    }

    post {
        success {
            echo 'Build successful! ✅'
        }
        failure {
            echo 'Build failed! ❌'
        }
    }
}