variable "project_name" {
  type = string
}

variable "environment" {
  type = string
}

variable "aws_region" {
  type = string
}

variable "aws_account_id" {
  type = string
}

variable "secret_arns" {
  description = "List of Secrets Manager ARNs the execution role can access"
  type        = list(string)
}

variable "efs_file_system_arn" {
  description = "ARN of the EFS file system"
  type        = string
}

variable "github_org" {
  description = "GitHub organization or username (empty = skip OIDC setup)"
  type        = string
  default     = ""
}

variable "github_repo" {
  description = "GitHub repository name"
  type        = string
  default     = ""
}
