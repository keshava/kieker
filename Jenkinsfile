#!/usr/bin/env groovy

pipeline {

  //environment {
  //}

  agent {
    docker {
      image 'openjdk:8-jdk-alpine'
      label 'kieker-slave-docker'
    }
  }

  //triggers {
    //cron(env.BRANCH_NAME == 'master' ? '@daily' : '')
  //}

  stages {
    stage('Precheck') {
      when {
        expression {
          (env.CHANGE_TARGET != null) && (env.CHANGE_TARGET == 'stable')
        }
      }
      steps {
        echo "BRANCH_NAME: ${BRANCH_NAME}"
        echo "CHANGE_TARGET: ${CHANGE_TARGET}"
        echo "NODE_NAME: ${NODE_NAME}"
        echo "NODE_LABELS: ${NODE_LABELS}"
        error "It is not allowed to create pull requests towards the 'stable' branch. Create a new pull request towards the 'master' branch please."
      }
    }

    stage('Compile') {
      steps {
        dir(env.WORKSPACE) {
          sh './gradlew --parallel compileJava'
          sh './gradlew --parallel compileTestJava'
        }
      }
    }


    stage('Unit Test') {
      steps {
        dir(env.WORKSPACE) {
          sh './gradlew --parallel test'
        }
      }
    }
  }

  post {
    cleanup {
      deleteDir()
    }
  }
}

