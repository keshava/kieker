#!/usr/bin/env groovy

pipeline {

  environment {
    DOCKER_ARGS = ''
  }

  agent none

  options {
    buildDiscarder logRotator(artifactNumToKeepStr: '10')
    timeout(time: 2, unit: 'HOURS')
    retry(1)
    parallelsAlwaysFailFast()
  }

  triggers {
    cron(env.BRANCH_NAME == 'master' ? '@daily' : '')
  }

  stages {
    stage('Precheck') {
      when {
        changeRequest target: 'stable'
      }
      steps {
        error "It is not allowed to create pull requests towards the 'stable' branch. Create a new pull request towards the 'master' branch please."
      }
    }
    stage('Default Docker Stages') {
      agent {
        docker {
          image 'kieker/kieker-build:openjdk8'
          args env.DOCKER_ARGS
          label 'kieker-slave-docker'
        }
      }
      stages {
        stage('Initial Cleanup') {
          steps {
            // Make sure that no remainders from previous builds interfere.
            sh './gradlew clean'
          }
        }

        stage('Compile') {
          steps {
            sh './gradlew compileJava'
            sh './gradlew compileTestJava'
          }
        }

        stage('Unit Test') {
          steps {
            sh './gradlew --parallel test'
            step([
                $class              : 'CloverPublisher',
                cloverReportDir     : env.WORKSPACE + '/build/reports/clover',
                cloverReportFileName: 'clover.xml',
                healthyTarget       : [methodCoverage: 70, conditionalCoverage: 80, statementCoverage: 80],
                unhealthyTarget     : [methodCoverage: 50, conditionalCoverage: 50, statementCoverage: 50], // optional, default is none
            ])
          }
          post {
            always {
              junit '**/build/test-results/test/*.xml'
            }
          }
        }

        stage('Static Analysis') {
          steps {
            sh './gradlew check'
          }
          post {
            always {
              // Report results of static analysis tools
            
              recordIssues(
                enabledForFailure: true,
                tools: [
                  java(),
                  javaDoc(),
                  checkStyle(
                    pattern: '**/build/reports/checkstyle/*.xml'
                  ),
                  pmdParser(
                    pattern: '**/build/reports/pmd/*.xml'
                  ),
                  spotBugs(
                    pattern: '**/build/reports/findbugs/*.xml'
                  )
                ]
              )
            }
          }
        }
        
        stage('Distribution Build') {
          steps {
            sh './gradlew build distribute'
          }
        }
      }
    }
  }
}

