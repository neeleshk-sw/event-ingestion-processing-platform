# -----------------------------------------------------------------------------
# Dev Environment
# -----------------------------------------------------------------------------

terraform {
  required_version = ">= 1.7.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.6"
    }
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = var.project_name
      Environment = var.environment
      ManagedBy   = "terraform"
    }
  }
}

module "platform" {
  source = "../../"

  # Core
  project_name   = var.project_name
  environment    = var.environment
  aws_region     = var.aws_region
  aws_account_id = var.aws_account_id

  # Networking
  vpc_cidr             = var.vpc_cidr
  availability_zones   = var.availability_zones
  public_subnet_cidrs  = var.public_subnet_cidrs
  private_subnet_cidrs = var.private_subnet_cidrs

  # Database — smaller, single-AZ
  db_instance_class    = var.db_instance_class
  db_multi_az          = var.db_multi_az
  db_allocated_storage = var.db_allocated_storage

  # Message Queue — smaller
  mq_instance_type = var.mq_instance_type

  # ElastiCache
  redis_node_type = var.redis_node_type

  # ECS — FARGATE_SPOT, single task
  ecs_task_cpu          = var.ecs_task_cpu
  ecs_task_memory       = var.ecs_task_memory
  ecs_desired_count     = var.ecs_desired_count
  ecs_capacity_provider = var.ecs_capacity_provider

  # Domain (usually empty for dev)
  domain_name = var.domain_name

  # CI/CD
  github_org  = var.github_org
  github_repo = var.github_repo
}

# Pass through variables
variable "project_name" {
  type    = string
  default = "event-platform"
}

variable "environment" {
  type    = string
  default = "dev"
}

variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "aws_account_id" {
  type = string
}

variable "vpc_cidr" {
  type    = string
  default = "10.0.0.0/16"
}

variable "availability_zones" {
  type    = list(string)
  default = ["us-east-1a", "us-east-1b"]
}

variable "public_subnet_cidrs" {
  type    = list(string)
  default = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "private_subnet_cidrs" {
  type    = list(string)
  default = ["10.0.11.0/24", "10.0.12.0/24"]
}

variable "db_instance_class" {
  type    = string
  default = "db.t3.micro"
}

variable "db_multi_az" {
  type    = bool
  default = false
}

variable "db_allocated_storage" {
  type    = number
  default = 20
}

variable "mq_instance_type" {
  type    = string
  default = "mq.t3.micro"
}

variable "redis_node_type" {
  type    = string
  default = "cache.t3.micro"
}

variable "ecs_task_cpu" {
  type    = number
  default = 256
}

variable "ecs_task_memory" {
  type    = number
  default = 512
}

variable "ecs_desired_count" {
  type    = number
  default = 1
}

variable "ecs_capacity_provider" {
  type    = string
  default = "FARGATE_SPOT"
}

variable "domain_name" {
  type    = string
  default = ""
}

variable "github_org" {
  type    = string
  default = ""
}

variable "github_repo" {
  type    = string
  default = ""
}

# ---- Outputs ----

output "alb_dns_name" {
  value = module.platform.alb_dns_name
}

output "rds_endpoint" {
  value = module.platform.rds_endpoint
}

output "ecr_repository_urls" {
  value = module.platform.ecr_repository_urls
}

output "ecs_cluster_name" {
  value = module.platform.ecs_cluster_name
}
