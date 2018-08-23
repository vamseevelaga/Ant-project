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
                //sh 'sudo chown -R ${USER}:${USER} .'
                deleteDir()
                echo "Checkout ${GERRIT_REFSPEC} code..."
                checkout([$class: 'GitSCM',  branches: [[name: "*/${GERRIT_REFSPEC}"]], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'krishna', url: 'git@github.com:vamseevelaga/Ant-project.git']]])
		
                        }

            }


        stage('Trigger all daily testing') {


            steps {



                script {

                List<String> sourceChanged = sh(returnStdout: true, script: "git diff --name-only origin/master..origin/${env.BRANCH_NAME}").split()
                    def isSourceChanged = false
                  // buildstatusfile is the file which maintains the changes of the build, if it is modified the pipeline will be executed.
                for (int i = 0; i < sourceChanged.size(); i++) {
                          if (sourceChanged[i].contains("buildstatusfile")) {
                          isSourceChanged=true


                        parallel(

                        "Daily VMware Release ${data.get(1)}": {
                            build job: "vmware-${data.get(0)}-${data.get(1)}"
                        },
                        "Daily VMware HA Release ${data.get(1)}": {
                            build job: "daily-vmware-ha-${data.get(0)}-${data.get(1)}"
                        },
                        "E2C Deploy Release ${data.get(1)}": {
                            build job: "daily-e2c-deploy-${data.get(0)}-${data.get(1)}"
                        },
                        "E2C Upgrade Release ${data.get(1)}": {
                            build job: "daily-e2c-upgrade-${data.get(0)}-${data.get}(1)"
                        }
                )

                }
                }

                if (isSourceChanged ) {
                            error("Source files changed but CHANGELOG was not updated!")
                        }

                    }

            }
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
