import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.PullRequests
import jetbrains.buildServer.configs.kotlin.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.buildFeatures.dockerSupport
import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.buildFeatures.pullRequests
import jetbrains.buildServer.configs.kotlin.buildSteps.dockerCommand
import jetbrains.buildServer.configs.kotlin.buildSteps.python
import jetbrains.buildServer.configs.kotlin.projectFeatures.dockerRegistry
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

    buildType(Test)
    buildType(BuildDocker)

    features {
        dockerRegistry {
            id = "PROJECT_EXT_2"
            name = "Docker Registry"
            userName = "antonaleksandrovjetbrains"
            password = "credentialsJSON:196654d3-5a3b-478b-962a-9d84ba208f33"
        }
    }
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
                namesAndTags = "antonaleksandrovjetbrains/devops101app:latest"
            }
        }
        dockerCommand {
            name = "Push"
            commandType = push {
                namesAndTags = "antonaleksandrovjetbrains/devops101app:latest"
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
                dockerRegistryId = "PROJECT_EXT_2"
            }
        }
    }
})

object Test : BuildType({
    name = "Test"

    vcs {
        root(DslContext.settingsRoot)
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
            vcsRootExtId = "${DslContext.settingsRoot.id}"
            provider = github {
                authType = vcsRoot()
                filterAuthorRole = PullRequests.GitHubRoleFilter.MEMBER
            }
        }
        commitStatusPublisher {
            publisher = github {
                githubUrl = "https://api.github.com"
                authType = personalToken {
                    token = "credentialsJSON:b9208138-14dc-41ce-9aee-b20ec5ff705b"
                }
            }
            param("github_oauth_user", "AntonAleksandrov13")
        }
    }
})

object HttpsGithubComAntonAleksandrov13devops101refsHeadsMain1 : GitVcsRoot({
    name = "https://github.com/AntonAleksandrov13/devops101#refs/heads/main (1)"
    url = "https://github.com/AntonAleksandrov13/devops101"
    branch = "refs/heads/main"
    authMethod = password {
        userName = "AntonAleksandrov13"
        password = "credentialsJSON:b9208138-14dc-41ce-9aee-b20ec5ff705b"
    }
    param("oauthProviderId", "tc-cloud-github-connection")
})
