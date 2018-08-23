pipeline {
    agent any

    options {
        timestamps()
        skipStagesAfterUnstable()
        timeout(time: 4, unit: 'HOURS')
    }
    environment {
        REPO_DIR = "$WORKSPACE"
        CICD_DIR = "cicd"
    }
    stages {
        stage('Check out code') {
            steps {
                echo 'Initial cleanup and checkout...'
		sh 'git branch -a > branches'
		sh 'cat branches'	
                        }

            }


        stage('Trigger all daily testing') {


            steps {

			sh 'git branch -a > branches'
                sh 'cat branches'



        }
        stage('Promote RC to artifactory') {
            steps {
                build job: "${JOB_NAME}"
            }
        }
    }
    post {
        always {
            echo 'Publish daily release info'
            sh '$CICD_DIR/daily_release/daily_release.sh'
            archiveArtifacts 'artifact.properties'
        }
    }
}
