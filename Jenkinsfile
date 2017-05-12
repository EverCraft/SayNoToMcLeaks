node {
	currentBuild.displayName = "SayNoToMcLeaks #${env.BUILD_NUMBER}"
	
	stage('Stage Checkout') {
		checkout scm
		sh "git submodule update --init"
	}
	
	stage('Stage Build') {
		echo "My branch is : ${env.BRANCH_NAME}"
		sh "./gradlew clean build shadowJar -PBUILD_NUMBER=${env.BUILD_NUMBER}"
	}

	stage('Stage Upload') {
	    archive 'build/lib/*.jar'
	}
}
