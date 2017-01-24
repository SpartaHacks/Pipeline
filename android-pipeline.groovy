def jenkinsBuild() {
    node('android') {
        try {
            stage('setup') {
                checkout scm

                env.ANDROID_HOME = "/root/android_sdk/"
                env.PATH = env.PATH + ":/root/android_sdk/platform_tools/:/root/android_sdk/tools/"
            }
    
            stage('clean build') {
                sh "./gradlew clean"
            }
    
            stage('assemble') {
                if(env.BRANCH_NAME == 'master') {
                    sh ".gradle assembleDebug"
                }
                else {
                    sh "./gradlew assembleDebug"
                }
            }
    
            stage('archive app') {
                if(env.BRANCH_NAME == 'master') {
                    sh "./gradle assembleRelease"
                }
                else {
                    step([$class: 'ArtifactArchiver', artifacts: '**/apk/app-debug.apk', fingerprint: true])
                }
            }
        } 
        catch (e) {
            echo '***Error: Build failed with error ' + e.toString()
            throw e
	    failure = true
        }

        finally {
	    echo "Cleaning work directory"
            sh "git clean -xfd"
	    notifyBuild(currentBuild.result)
        }
    }
}

def notifyBuild(failure) {
  // Override default values based on build status
  if (!failure) {
    color = 'GREEN'
    colorCode = '#00FF00'
    buildStatus = 'SUCCESSFUL'
  } else {
    color = 'RED'
    colorCode = '#FF0000'
    buildStatus = 'FAILURE'
  }

  def subject = "${buildStatus}: Job '${env.JOB_NAME}'"
  def summary = "${subject} ${env.BUILD_URL}"

  // Send notifications
  slackSend (color: colorCode, message: summary)
}   

return this;

