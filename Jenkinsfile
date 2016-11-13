node {
    stage 'Checkout'

    // Checkout code from repository
    checkout scm

    // Mark the code build 'stage'....
    stage 'Build'
    env.PATH = "${tool 'Maven 3'}/bin:${env.PATH}"
    sh 'mvn clean install'
}