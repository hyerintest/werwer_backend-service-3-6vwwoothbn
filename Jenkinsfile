podTemplate(
    containers: [
        containerTemplate(name: 'helm-kubectl', image: 'registry.turacocloud.com/turaco-common/helm-kubectl:latest', command: 'cat', ttyEnabled: true),
        containerTemplate(name: 'argocd', image: 'registry.turacocloud.com/turaco-common/argocd:latest', command: 'cat', ttyEnabled: true)
    ],
    imagePullSecrets: ['harbor-secret']) {
    node(POD_LABEL) {
        // Java 8 환경 설정 추가
        environment {
            JAVA_HOME = '/usr/lib/jvm/java-8-openjdk'  // Java 8 설치 경로
        }
        
        if ("$IS_SONAR" == "true") {
            stage('Sonarqube Build') {
                git (branch: "$BRANCH", url: "https://$SOURCE_REPO_URL/${GROUP_NAME}_${SERVICE_NAME}.git", credentialsId: "$CREDENTIAL_ID")
                echo "SonarQube analysis..."
                sh "chmod +x ./gradlew"
                // JAVA_HOME 설정 추가
                sh """
                    export JAVA_HOME=${JAVA_HOME}
                    export PATH=${JAVA_HOME}/bin:$PATH
                    ./gradlew clean build jib -PdockerRegistry=$IMAGE_REPO_NAME -PdockerUser=$HARBOR_USER -PdockerPassword=$HARBOR_PASSWORD -PserviceName=$ARGO_APPLICATION -PcommitRev=$COMMIT_ID
                """
                sh "sleep 60"
                // ... 나머지 코드는 그대로 유지
            }
        }
        stage('Build') {
            git (branch: "$BRANCH", url: "https://$SOURCE_REPO_URL/${GROUP_NAME}_${SERVICE_NAME}.git", credentialsId: "$CREDENTIAL_ID")
            sh "git rev-parse --short HEAD > commit-id.txt"
            def COMMIT_ID = readFile("commit-id.txt").trim()
            echo "Gradle Build ing..."
            sh "mkdir -p logs"
            sh "chmod +x ./gradlew"
            // JAVA_HOME 설정 추가
            sh """
                export JAVA_HOME=${JAVA_HOME}
                export PATH=${JAVA_HOME}/bin:$PATH
                ./gradlew clean build jib -PdockerRegistry=$IMAGE_REPO_NAME -PdockerUser=$HARBOR_USER -PdockerPassword=$HARBOR_PASSWORD -PserviceName=$ARGO_APPLICATION -PcommitRev=$COMMIT_ID
            """
            git (branch: "master", url: "https://$SOURCE_REPO_URL/${GROUP_NAME}_HelmChart.git", credentialsId: "$CREDENTIAL_ID")
            dir ("$STAGE/$SERVICE_NAME") {
                sh "git rev-parse --short HEAD > commit-id.txt"
                sh "find ./ -name values.yaml -type f -exec sed -i \'s/^\\(\\s*tag\\s*:\\s*\\).*/\\1\"\'$ARGO_APPLICATION-$COMMIT_ID\'\"/\' {} \\;"
                sh 'git config --global user.email "info@twolinecode.com"'
                sh 'git config --global user.name "jenkins-runner"'
                sh 'git add ./values.yaml'
                sh "git commit --allow-empty -m \"Pushed Helm Chart: $ARGO_APPLICATION-$COMMIT_ID\""
                withCredentials([gitUsernamePassword(credentialsId: "$CREDENTIAL_ID", gitToolName: 'git-tool')]) {
                    sh '''
                    while :
                    do
                        git pull --rebase origin master
                        if git push origin master
                        then
                            break
                        fi
                    done
                    '''
                }
            }
        }
        stage('Deploy') {
            dir("$STAGE/Common") {
                container('helm-kubectl'){
                    echo "helm-kubectl ing ..."
                    sh "helm template . > ./common.yaml"
                    sh "kubectl --kubeconfig ../$KUBECONFIG apply -f common.yaml"
                    sh "kubectl --kubeconfig ../$KUBECONFIG get secret argocd-initial-admin-secret -n tlc-support -o jsonpath='{.data.password}' | base64 -d > argocd-password.txt"
                    def PASSWORD = readFile("argocd-password.txt")
                    container('argocd') {
                        echo "Sync ArgoCD ing..."
                        sh "argocd login $ARGO_ENDPOINT:80 --grpc-web-root-path argocd --username admin --password $PASSWORD --plaintext --skip-test-tls"
                        sh "argocd app get $ARGO_APPLICATION --refresh"
                        sh "argocd app sync $ARGO_APPLICATION"
                    }
                }
            }
        }
    }
}
