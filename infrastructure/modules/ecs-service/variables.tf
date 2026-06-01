variable "project_name" {
  type = string
}

variable "environment" {
  type = string
}

variable "service_name" {
  type = string
}

variable "service_port" {
  type = number
}

variable "aws_region" {
  type = string
}

variable "aws_account_id" {
  type = string
}

# ECS
variable "cluster_id" {
  type = string
}

variable "cluster_name" {
  type = string
}

variable "task_execution_role_arn" {
  type = string
}

variable "task_role_arn" {
  type = string
}

variable "cpu" {
  type    = number
  default = 512
}

variable "memory" {
  type    = number
  default = 1024
}

variable "desired_count" {
  type    = number
  default = 2
}

variable "capacity_provider" {
  type    = string
  default = "FARGATE"
}

variable "image_tag" {
  type    = string
  default = "latest"
}

# Networking
variable "private_subnet_ids" {
  type = list(string)
}

variable "security_group_id" {
  type = string
}

variable "service_connect_ns" {
  description = "Service Connect namespace name (e.g., event-platform.local)"
  type        = string
}

variable "service_connect_namespace_arn" {
  description = "Service Connect namespace ARN"
  type        = string
}

# ALB
variable "attach_to_alb" {
  description = "Whether to register with an ALB target group"
  type        = bool
  default     = false
}

variable "target_group_arn" {
  description = "ALB target group ARN (required when attach_to_alb = true)"
  type        = string
  default     = ""
}

# Environment variables
variable "environment_variables" {
  description = "Map of environment variable name to value"
  type        = map(string)
  default     = {}
}

# Secrets from Secrets Manager
variable "secret_variables" {
  description = "Map of secret env var name to Secrets Manager ARN"
  type        = map(string)
  default     = {}
}

# EFS
variable "efs_file_system_id" {
  description = "EFS file system ID (empty = no EFS mount)"
  type        = string
  default     = ""
}

variable "efs_access_point_id" {
  description = "EFS access point ID (empty = no EFS mount)"
  type        = string
  default     = ""
}

# Logging
variable "log_group_name" {
  description = "CloudWatch log group name"
  type        = string
}
