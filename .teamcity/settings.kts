import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.PullRequests
import jetbrains.buildServer.configs.kotlin.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.buildFeatures.dockerSupport
import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.buildFeatures.provideAwsCredentials
import jetbrains.buildServer.configs.kotlin.buildFeatures.pullRequests
import jetbrains.buildServer.configs.kotlin.buildSteps.dockerCommand
import jetbrains.buildServer.configs.kotlin.buildSteps.python
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2022.10"

project {

    vcsRoot(HttpsGithubComAntonAleksandrov13devops101refsHeadsMain1)
    vcsRoot(HttpsGithubComAntonAleksandrov13devops101refsHeadsMain)

    buildType(Test)
    buildType(Deploy)
    buildType(BuildDocker)
}

object BuildDocker : BuildType({
    name = "Build"

    vcs {
        root(HttpsGithubComAntonAleksandrov13devops101refsHeadsMain1)
    }

    steps {
        dockerCommand {
            name = "Build Image"
            commandType = build {
                source = file {
                    path = "app/Dockerfile"
                }
                contextDir = "app"
                namesAndTags = "antonaleksandrovjetbrains/devops101app:%build.number%"
            }
        }
        dockerCommand {
            name = "Push"
            commandType = push {
                namesAndTags = "antonaleksandrovjetbrains/devops101app:%build.number%"
            }
        }
    }

    triggers {
        vcs {
        }
    }

    features {
        perfmon {
        }
        dockerSupport {
            loginToRegistry = on {
                dockerRegistryId = "PROJECT_EXT_4"
            }
        }
    }
})

object Deploy : BuildType({
    name = "Deploy"

    vcs {
        root(HttpsGithubComAntonAleksandrov13devops101refsHeadsMain)
    }

    steps {
        script {
            name = "Terraform apply"
            workingDir = "infrastructure"
            scriptContent = "terraform init && terraform apply -var ecs_image_tag=${BuildDocker.depParamRefs.buildNumber} -auto-approve"
            dockerImage = "hashicorp/terraform"
        }
    }

    triggers {
        vcs {
        }
    }

    features {
        perfmon {
        }
        provideAwsCredentials {
            awsConnectionId = "AmazonWebServicesAws"
        }
    }

    dependencies {
        snapshot(BuildDocker) {
        }
    }
})

object Test : BuildType({
    name = "Test"

    vcs {
        root(HttpsGithubComAntonAleksandrov13devops101refsHeadsMain)
    }

    steps {
        python {
            name = "Tests"
            workingDir = "app"
            environment = venv {
            }
            command = pytest {
                scriptArguments = "tests/test.py"
            }
        }
    }

    triggers {
        vcs {
        }
    }

    features {
        perfmon {
        }
        pullRequests {
            provider = github {
                authType = vcsRoot()
                filterAuthorRole = PullRequests.GitHubRoleFilter.MEMBER
            }
        }
        commitStatusPublisher {
            publisher = github {
                githubUrl = "https://api.github.com"
                authType = personalToken {
                    token = "credentialsJSON:5d9809b6-ec73-4604-a16e-d1fec1ae3eac"
                }
            }
            param("github_oauth_user", "AntonAleksandrov13")
        }
    }
})

object HttpsGithubComAntonAleksandrov13devops101refsHeadsMain : GitVcsRoot({
    name = "https://github.com/AntonAleksandrov13/devops101#refs/heads/main"
    url = "https://github.com/AntonAleksandrov13/devops101"
    branch = "refs/heads/main"
    branchSpec = "refs/heads/*"
    authMethod = password {
        userName = "AntonAleksandrov13"
        password = "credentialsJSON:5d9809b6-ec73-4604-a16e-d1fec1ae3eac"
    }
    param("oauthProviderId", "tc-cloud-github-connection")
})

object HttpsGithubComAntonAleksandrov13devops101refsHeadsMain1 : GitVcsRoot({
    name = "https://github.com/AntonAleksandrov13/devops101#refs/heads/main (1)"
    url = "https://github.com/AntonAleksandrov13/devops101"
    branch = "refs/heads/main"
    authMethod = password {
        userName = "AntonAleksandrov13"
        password = "credentialsJSON:5d9809b6-ec73-4604-a16e-d1fec1ae3eac"
    }
    param("oauthProviderId", "tc-cloud-github-connection")
})
