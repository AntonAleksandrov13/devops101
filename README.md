# Devops101: culture vs job title

This repository aims to illustrate step-by-step journey of DevOps transformation of a single app/product.

*This repository was created solely for illustratory purposes and does not imply usage of best practices in terms of
technologies.*

## DevOps cycle

DevOps lifecycle is a combination of different phases of continuous software development, integration, testing,
deployment, and monitoring. A competent DevOps lifecycle is necessary to leverage the full benefits of the DevOps
methodology.
![devops-cycle](./docs/devops-cycle.png)

In this repository, this process will be illustrated on a smaller scale(one Hello World Flask app). However, the same
practices could be applied on a larger scale.

## What's the situation?

Assume you are DevOps engineer working in a company *A*. You have just been assigned to a product team that builds
product called *Hello World* app. This is a collaborative project about 5 software engineers are working on it. It is
your job to apply DevOps practices to make sure that:

1. The application is tested before release.
2. Changes to the application can be done as quickly as possible.
3. The application runs at all times, and we might need to scale quickly.
4. The application is monitored for both metrics and logs.
5. The system is extensible and new integrations can be delivered painlessly.

Let's assume also for now, that only one folder exists - `./app`. In this folder, there's a simple Flask application
that can only accept GET requests on root path. There are also a few tests in `./app/tests` folder. We can
see `Dockerfile` and `requirements.txt` files also.

Based on the given facts, we can already identify different stages of DevOps cycle we need to fulfill.

* Requirement 1 shows that we need to automatically test code and make sure no new bugs are not introduced. This is
  known as Continuous Integration.
* Requirement 2 implies that we need to have automation around application delivery process also known as Continuous
  Delivery.
* Requirement 3 emphases application availability for the customers. This is deploy and operate stages of DevOps cycle.
* Requirement 4 is about monitoring and operating the system. What do we need to do to have maximum level of insights
  about the application?
* Requirement 5 is about planning as well as deploy phases. Where do we deploy this application in order to ensure that?

## What can we already see?

We can already see some technologies used in the project - Python(Flask), PyTest, Docker.

Since automation has been mentioned already a few times we need to use CI/CD tool. Using tools such as TeamCity or
GitHub Actions we can make sure that process is repeatable and automatic.
Hence, our automation will be built around CI/CD tools. In our case it will be TeamCity offered
by [TeamCity Cloud](https://www.jetbrains.com/teamcity/cloud/).

There's scalability requirement we have, so we will deploy this application to a cloud provider. Either cloud provider
is fine for a small case like this. We will use AWS as the most popular one. In more complex systems options have to be
carefully considered due to potential high costs of provider specifics.

## Requirement 1 and 2

There are number of practices we can adopt to satisfy these requirements. One of the being defining Git Branching
strategy.

There are a number of such strategies available such
as [GitFlow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow)
or [Trunk-Based](https://trunkbaseddevelopment.com/). We will use single long-lived branch `main` to which push is
restricted and only tested pull requests can be merged.

The idea is: we can test code when it's pushed to feature branch hence checking before review if this code passes our
tests. Once the feature branch is merged into `main` we can build and deploy code immediately. This way we make sure
that tested code ends up deployed as quick as possible. Additionally, there's a little room for error since it's done
automatically using CI/CD.

Below you can see how to enable this in GitHub:

![enable-protection](./docs/protect-main-branch.png)

Additionally, we will enable status checks. This is a callback from CI/CD systems that confirms that everything runs as
expected. In our case, we will enable check for Tests.

![enable-checks](./docs/enable-checks.png)

Next, we need to configure running tests in TeamCity:

First, create a project.

![create-project](./docs/create-project.png)

Create build configuration.

![create-build-conf](./docs/create-build-conf.png)

Add a build step to the build configuration. This step will install dependencies from `requirements.txt` and
run `pytest`

![enable-tests](./docs/enable-tests.png)

Add VCS trigger. This will make sure that test are ran everytime you push code.

![add-vcs-trigger](./docs/add-vcs-trigger.png)

Add Pull Requests and Commit status publisher build features, so TeamCity knows about Pull Requests and sends statuses
back when code is ran.

![build-features](./docs/build-features.png)

Now, when you open a pull request in GitHub, a build on TeamCity should be triggered.

![new-pr](./docs/new-openned-pull-request.png)

After a while TeamCity will finish the build and report status check back.

![pr-example.png](./docs/pr-example.png)

Now, let's make sure that we build the app after it was merged. Since the app already has a Dockerfile we will build and
push Docker image to Docker Hub.

![build-conf.png](./docs/add-docker-build-conf.png)

Also, two build steps need to be added. One is for building the image and second one for push operation.

![build-step.png](./docs/add-docker-build-step.png)

![build-step.png](./docs/add-docker-push-build-step.png)

In the end, Docker support needs to be enabled to establish password protected access for Docker push.

![build-step.png](./docs/add-docker-support.png)

Let's merge the pull request we have opened before. If we wait a minute, a new build will be triggered in Build configuration.

![triggered-build.png](./docs/triggered-build.png)

