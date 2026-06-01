variable "project_name" {
  type = string
}

variable "environment" {
  type = string
}

variable "vpc_id" {
  type = string
}

variable "private_subnet_ids" {
  type = list(string)
}

variable "security_group_id" {
  type = string
}

variable "instance_class" {
  type    = string
  default = "db.t3.medium"
}

variable "multi_az" {
  type    = bool
  default = true
}

variable "allocated_storage" {
  type    = number
  default = 100
}

variable "db_name" {
  type    = string
  default = "eventdb"
}

variable "db_username" {
  type    = string
  default = "eventadmin"
}

variable "db_password_secret_arn" {
  type = string
}
