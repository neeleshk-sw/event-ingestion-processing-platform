# -----------------------------------------------------------------------------
# Global Variables
# -----------------------------------------------------------------------------

variable "project_name" {
  description = "Project name used for resource naming and tagging"
  type        = string
  default     = "event-platform"
}

variable "environment" {
  description = "Deployment environment (dev, staging, prod)"
  type        = string
  default     = "prod"

  validation {
    condition     = contains(["dev", "staging", "prod"], var.environment)
    error_message = "Environment must be one of: dev, staging, prod."
  }
}

variable "aws_region" {
  description = "AWS region for all resources"
  type        = string
  default     = "us-east-1"
}

variable "aws_account_id" {
  description = "AWS account ID (used for ECR URIs and IAM ARNs)"
  type        = string
}

# -----------------------------------------------------------------------------
# Networking
# -----------------------------------------------------------------------------

variable "vpc_cidr" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "availability_zones" {
  description = "Availability zones to use"
  type        = list(string)
  default     = ["us-east-1a", "us-east-1b"]
}

variable "public_subnet_cidrs" {
  description = "CIDR blocks for public subnets"
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "private_subnet_cidrs" {
  description = "CIDR blocks for private subnets"
  type        = list(string)
  default     = ["10.0.11.0/24", "10.0.12.0/24"]
}

# -----------------------------------------------------------------------------
# Database
# -----------------------------------------------------------------------------

variable "db_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t3.medium"
}

variable "db_multi_az" {
  description = "Enable Multi-AZ for RDS"
  type        = bool
  default     = true
}

variable "db_allocated_storage" {
  description = "Allocated storage in GB for RDS"
  type        = number
  default     = 100
}

variable "db_name" {
  description = "PostgreSQL database name"
  type        = string
  default     = "eventdb"
}

variable "db_username" {
  description = "Master database username"
  type        = string
  default     = "eventadmin"
}

# -----------------------------------------------------------------------------
# Message Queue
# -----------------------------------------------------------------------------

variable "mq_instance_type" {
  description = "Amazon MQ instance type"
  type        = string
  default     = "mq.m5.large"
}

variable "mq_username" {
  description = "Amazon MQ admin username"
  type        = string
  default     = "eventadmin"
}

# -----------------------------------------------------------------------------
# ElastiCache
# -----------------------------------------------------------------------------

variable "redis_node_type" {
  description = "ElastiCache Redis node type"
  type        = string
  default     = "cache.t3.micro"
}

# -----------------------------------------------------------------------------
# ECS
# -----------------------------------------------------------------------------

variable "ecs_task_cpu" {
  description = "CPU units for ECS tasks (1 vCPU = 1024)"
  type        = number
  default     = 512
}

variable "ecs_task_memory" {
  description = "Memory in MiB for ECS tasks"
  type        = number
  default     = 1024
}

variable "ecs_desired_count" {
  description = "Desired number of ECS task instances per service"
  type        = number
  default     = 2
}

variable "ecs_capacity_provider" {
  description = "Default capacity provider strategy (FARGATE or FARGATE_SPOT)"
  type        = string
  default     = "FARGATE"
}

# -----------------------------------------------------------------------------
# Domain / TLS
# -----------------------------------------------------------------------------

variable "domain_name" {
  description = "Domain name for the platform (e.g., event-platform.example.com). Leave empty to skip ACM/Route53."
  type        = string
  default     = ""
}

# -----------------------------------------------------------------------------
# CI/CD
# -----------------------------------------------------------------------------

variable "github_org" {
  description = "GitHub organization or username"
  type        = string
  default     = ""
}

variable "github_repo" {
  description = "GitHub repository name"
  type        = string
  default     = ""
}

# -----------------------------------------------------------------------------
# Service Definitions
# -----------------------------------------------------------------------------

variable "services" {
  description = "Map of microservice configurations"
  type = map(object({
    port          = number
    has_db        = bool
    has_rabbitmq  = bool
    has_redis     = bool
    has_efs       = bool
    is_public     = bool
    db_schema     = optional(string, "")
    image_tag     = optional(string, "latest")
  }))
  default = {
    intake-service = {
      port         = 8081
      has_db       = true
      has_rabbitmq = true
      has_redis    = false
      has_efs      = true
      is_public    = true
      db_schema    = "intake_schema"
    }
    validation-service = {
      port         = 8082
      has_db       = false
      has_rabbitmq = false
      has_redis    = false
      has_efs      = false
      is_public    = false
      db_schema    = ""
    }
    normalization-service = {
      port         = 8083
      has_db       = false
      has_rabbitmq = false
      has_redis    = false
      has_efs      = false
      is_public    = false
      db_schema    = ""
    }
    enrichment-service = {
      port         = 8084
      has_db       = false
      has_rabbitmq = false
      has_redis    = false
      has_efs      = false
      is_public    = false
      db_schema    = ""
    }
    routing-service = {
      port         = 8085
      has_db       = true
      has_rabbitmq = true
      has_redis    = false
      has_efs      = false
      is_public    = false
      db_schema    = "delivery_schema"
    }
    delivery-service = {
      port         = 8086
      has_db       = true
      has_rabbitmq = true
      has_redis    = false
      has_efs      = false
      is_public    = false
      db_schema    = "delivery_schema"
    }
    recovery-service = {
      port         = 8087
      has_db       = true
      has_rabbitmq = false
      has_redis    = false
      has_efs      = false
      is_public    = false
      db_schema    = "recovery_schema"
    }
    audit-service = {
      port         = 8088
      has_db       = true
      has_rabbitmq = false
      has_redis    = true
      has_efs      = false
      is_public    = false
      db_schema    = "audit_schema"
    }
  }
}
