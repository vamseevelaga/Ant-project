pipeline {
    agent any


        stage('Trigger all daily testing') {


            steps {

			sh 'git branch -a > branches'
                sh 'cat branches'



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
