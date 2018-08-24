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
	commit = "true"
    }
    stages {
        stage('Trigger all daily testing') {
            steps {
                parallel(
                        'Daily VMware Release 1.1.x': {
			sh("git checkout remotes/origin/ant-rel1")
		    	sh(" git log --since=1.days > change; if [ -n change ]; then echo " file is zero size"; commit=false; export commit; if")
		println "the commit value env.commit"
			
                            build job: 'vmware-rel-1.1.x'
                        }
                )
            }
        }
        stage('Promote RC to artifactory') {
            steps {
                build job: 'promote-rc-1.1.x'
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
