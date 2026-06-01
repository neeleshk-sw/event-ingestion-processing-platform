variable "project_name" {
  type = string
}

variable "environment" {
  type = string
}

variable "service_connect_ns" {
  description = "Service Connect namespace name"
  type        = string
}

variable "service_names" {
  description = "List of service names for log group creation"
  type        = list(string)
}

variable "capacity_provider" {
  description = "Default capacity provider (FARGATE or FARGATE_SPOT)"
  type        = string
  default     = "FARGATE"
}
