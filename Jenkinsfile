pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    stages {

        stage('Build') {
            steps {
                dir('BookingApplication') {
                    bat 'mvn clean install'
                }

            }
        }

        stage('Test') {
            steps {
                dir('BookingApplication') {
                    bat 'mvn test'
                }

            }
        }

        stage('Done') {
            steps {
                echo 'Backend Build Successful'
            }
        }
    }
}