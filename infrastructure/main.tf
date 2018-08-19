provider "aws" {
  region  = "${var.aws-region}"
  profile = "${var.aws-profile}"
}

data "aws_vpc" "csd" {
  tags {
    Environment = "${var.environment}"
  }
}

data "aws_subnet" "csd" {
  tags {
    Environment = "${var.environment}"
  }
}

data "aws_security_group" "csd-app" {
  name = "csd-app-security-group"
}

resource "aws_iam_instance_profile" "beanstalk-ec2" {
  name = "BeanstalkEC2User"
  role = "${aws_iam_role.beanstalk-ec2.name}"
}

resource "aws_iam_role" "beanstalk-ec2" {
  name = "BeanstalktEC2Role"

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "ec2.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
EOF
}

resource "aws_elastic_beanstalk_application" "epis-service" {
  name        = "epis-service"
  description = "EPIS service"
}

resource "aws_elastic_beanstalk_environment" "epis-service" {
  name                = "epis-service"
  application         = "${aws_elastic_beanstalk_application.epis-service.name}"
  solution_stack_name = "64bit Amazon Linux 2018.03 v2.7.1 running Java 8"

  setting {
    namespace = "aws:ec2:vpc"
    name      = "VPCId"
    value     = "${data.aws_vpc.csd.id}"
  }

  setting {
    namespace = "aws:ec2:vpc"
    name      = "Subnets"
    value     = "${data.aws_subnet.csd.id}"
  }

  setting {
    namespace = "aws:autoscaling:asg"
    name      = "MaxSize"
    value     = "1"
  }

  setting {
    namespace = "aws:autoscaling:launchconfiguration"
    name      = "EC2KeyName"
    value     = "DevOps"
  }

  setting {
    namespace = "aws:autoscaling:launchconfiguration"
    name      = "SecurityGroups"
    value     = "${data.aws_security_group.csd-app.id}"
  }

  setting {
    namespace = "aws:autoscaling:launchconfiguration"
    name      = "IamInstanceProfile"
    value     = "${aws_iam_instance_profile.beanstalk-ec2.id}"
  }

  setting {
    namespace = "aws:elasticbeanstalk:application"
    name      = "Application Healthcheck URL"
    value     = "/health"
  }

  setting {
    namespace = "aws:elasticbeanstalk:environment"
    name      = "EnvironmentType"
    value     = "SingleInstance"
  }

  setting {
    namespace = "aws:elasticbeanstalk:application:environment"
    name      = "SPRING_PROFILES_ACTIVE"
    value     = "production"
  }
}
