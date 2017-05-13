node {
	currentBuild.displayName = "SayNoToMcLeaks #${env.BUILD_NUMBER}"
	
	stage('Stage Checkout') {
		checkout scm
	}
	
	stage('Stage Build') {
		echo "My branch is : ${env.BRANCH_NAME}"
		sh "chmod +x gradlew"
		sh "./gradlew clean build -PBUILD_NUMBER=${env.BUILD_NUMBER}"
	}

	stage('Stage Upload') {
	    archive 'build/libs/*.jar'
	}
}
