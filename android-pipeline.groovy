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
        }

        finally {
	    echo "Cleaning work directory"
            sh "git clean -xfd"
	    notifyBuild(currentBuild.result)
        }
    }
}

def notifyBuild(String buildStatus = 'STARTED') {
  // build status of null means successful
  buildStatus =  buildStatus ?: 'SUCCESSFUL'

  // Default values
  def colorName = 'RED'
  def colorCode = '#FF0000'
  def subject = "${buildStatus}: Job '${env.JOB_NAME}'"
  def summary = "${subject} ${env.BUILD_URL}"

  // Override default values based on build status
  if (buildStatus == 'STARTED') {
    color = 'YELLOW'
    colorCode = '#FFFF00'
  } else if (buildStatus == 'SUCCESSFUL') {
    color = 'GREEN'
    colorCode = '#00FF00'
  } else {
    color = 'RED'
    colorCode = '#FF0000'
  }

  // Send notifications
  slackSend (color: colorCode, message: summary)
}   

return this;

