pipeline {
    agent {
        label 'maven-jdk17'
    }

    parameters {
        choice(
            name: 'FORCE_PROFILE',
            choices: ['auto', 'dev', 'qa', 'prod'],
            description: 'Force profile. Choose auto to detect from branch.'
        )
    }

    environment {
        DOCKERHUB_USERNAME = "bhanurajeev"
        IMAGE_NAME         = "quarkus-devops-demo"
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 40, unit: 'MINUTES')
    }

    stages {
        stage('Checkout & Decide Profile') {
            steps {
                checkout scm
                script {
                    def branch = env.BRANCH_NAME ?: env.GIT_BRANCH?.replaceAll('origin/', '') ?: 'unknown'
                    echo "Branch: ${branch}"

                    if (params.FORCE_PROFILE != 'auto') {
                        env.PROFILE = params.FORCE_PROFILE
                    } else if (branch == 'main' || branch == 'master') {
                        env.PROFILE = 'prod'
                    } else if (branch == 'qa') {
                        env.PROFILE = 'qa'
                    } else {
                        env.PROFILE = 'dev'
                    }

                    env.IMAGE_TAG  = "${env.BUILD_NUMBER}-${env.PROFILE}"
                    env.FULL_IMAGE = "${DOCKERHUB_USERNAME}/${IMAGE_NAME}:${env.IMAGE_TAG}"
                    env.LATEST_TAG = "${env.PROFILE}-latest"

                    echo "Profile → ${env.PROFILE}"
                    echo "Image   → ${env.FULL_IMAGE}"
                }
            }
        }

        stage('Build & Test') {
            steps {
                container('maven') {
                    sh "mvn -B clean test -Dquarkus.profile=${PROFILE}"
                }
            }
        }

        stage('Package + Build & Push Image (Jib)') {
            steps {
                container('maven') {
                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'USER',
                        passwordVariable: 'PASS'
                    )]) {
                        sh """
                            mvn -B package -DskipTests \
                              -Dquarkus.profile=${PROFILE} \
                              -Dquarkus.container-image.build=true \
                              -Dquarkus.container-image.push=true \
                              -Dquarkus.container-image.group=${DOCKERHUB_USERNAME} \
                              -Dquarkus.container-image.name=${IMAGE_NAME} \
                              -Dquarkus.container-image.tag=${IMAGE_TAG} \
                              -Dquarkus.container-image.username=\$USER \
                              -Dquarkus.container-image.password=\$PASS
                        """
                    }
                }
            }
        }
    }

    post {
        success {
            echo "✅ Build succeeded"
            echo "Profile : ${env.PROFILE}"
            echo "Image   : ${env.FULL_IMAGE}"
            echo ""
            echo "Deploy manually with:"
            echo "helm upgrade --install quarkus-devops-demo ./charts/quarkus-devops-demo \\"
            echo "  -f ./charts/quarkus-devops-demo/values-${PROFILE}.yaml \\"
            echo "  --set image.tag=${IMAGE_TAG} \\"
            echo "  --namespace ${PROFILE} --create-namespace"
        }
        failure {
            echo "❌ Build failed"
        }
    }
}