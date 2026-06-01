variable "project_name" {
  type = string
}

variable "environment" {
  type = string
}

variable "subnet_ids" {
  type = list(string)
}

variable "security_group_id" {
  type = string
}

variable "instance_type" {
  type    = string
  default = "mq.m5.large"
}

variable "username" {
  type    = string
  default = "eventadmin"
}

variable "password_secret_arn" {
  type = string
}
