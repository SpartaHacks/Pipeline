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
                sh "./gradlew assembleDebug"
            }
    
            stage('archive app') {
                step([$class: 'ArtifactArchiver', artifacts: '**/apk/app-debug.apk', fingerprint: true])
            }
        } 
        catch (e) {
            echo '***Error: Build failed with error ' + e.toString()
            throw e
        }

        finally {
	    echo "Cleaning work directory"
            sh "git clean -xfd"
        }
    }
}   

return this;

