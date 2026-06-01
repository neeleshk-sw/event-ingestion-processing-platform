variable "project_name" {
  type = string
}

variable "environment" {
  type = string
}

variable "vpc_id" {
  type = string
}

variable "public_subnet_ids" {
  type = list(string)
}

variable "security_group_id" {
  type = string
}

variable "domain_name" {
  description = "Domain name for ACM cert and Route 53. Leave empty to skip TLS setup."
  type        = string
  default     = ""
}
