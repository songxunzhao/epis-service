variable "aws-region" {
  description = "AWS region to launch servers."
  default     = "eu-central-1"
}

variable "aws-profile" {
  default = "tuleva"
}

variable "environment" {
 default = "csd"
}

# Service environment
variable "client_id" {}

variable "client_secret" {}

variable "jdbc_database_url" {}

variable "jdbc_database_username" {}

variable "jdbc_database_password" {}

variable "mhub_keystore_password" {}

variable "mhub_keystore_part1" {}

variable "mhub_keystore_part2" {}

variable "mhub_userid" {}

variable "mhub_password" {}

variable "mq_host" {}

variable "mq_password" {}

variable "mq_username" {}

variable "rollbar_access_token" {}

variable "spring_profiles_active" {}

variable "token_info_uri" {}
