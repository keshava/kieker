#!/usr/bin/env groovy

pipeline {

  environment {
    DOCKER_ARGS = '--rm -u `id -u`'
  }

  agent {
    docker {
      image '8-jdk-alpine'
      args env.DOCKER_ARGS
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
        echo "BRANCH_NAME: $env.BRANCH_NAME"
        echo "CHANGE_TARGET: $env.CHANGE_TARGET"
        echo "NODE_NAME: $env.NODE_NAME"
        echo "NODE_LABELS: $env.NODE_LABELS"
        error "It is not allowed to create pull requests towards the 'stable' branch. Create a new pull request towards the 'master' branch please."
      }
    }

    stage('Compile') {
      steps {
        dir(env.WORKSPACE) {
          sh './gradlew compileJava'
          sh './gradlew compileTestJava'
        }
      }
    }


    stage('Unit Test') {
      steps {
        dir(env.WORKSPACE) {
          sh './gradlew test'
        }
      }
    }
  }
}

