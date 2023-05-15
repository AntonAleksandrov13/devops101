provider "aws" {
  region = "eu-west-1"
}

terraform {
  backend "s3" {
    bucket = "terraform-state-eu-west-1-asasdad"
    key = "app"
    region = "eu-west-1"
  }
}