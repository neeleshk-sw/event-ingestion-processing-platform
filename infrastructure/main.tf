# -----------------------------------------------------------------------------
# AWS Cloud Deployment — Root Module Orchestrator
# Wires all infrastructure modules together
# -----------------------------------------------------------------------------

locals {
  name_prefix        = "${var.project_name}-${var.environment}"
  service_connect_ns = "${var.project_name}.local"

  # Service URL chain for ECS task environment variables
  service_urls = {
    intake-service = {
      SERVICE_VALIDATION_URL = "http://validation-service.${local.service_connect_ns}:8082"
      SERVICE_AUDIT_URL      = "http://audit-service.${local.service_connect_ns}:8088"
    }
    validation-service = {
      SERVICE_NORMALIZATION_URL = "http://normalization-service.${local.service_connect_ns}:8083"
      SERVICE_AUDIT_URL         = "http://audit-service.${local.service_connect_ns}:8088"
    }
    normalization-service = {
      SERVICE_ENRICHMENT_URL = "http://enrichment-service.${local.service_connect_ns}:8084"
      SERVICE_AUDIT_URL      = "http://audit-service.${local.service_connect_ns}:8088"
    }
    enrichment-service = {
      SERVICE_ROUTING_URL = "http://routing-service.${local.service_connect_ns}:8085"
      SERVICE_AUDIT_URL   = "http://audit-service.${local.service_connect_ns}:8088"
    }
    routing-service = {
      SERVICE_DELIVERY_URL = "http://delivery-service.${local.service_connect_ns}:8086"
      SERVICE_AUDIT_URL    = "http://audit-service.${local.service_connect_ns}:8088"
    }
    delivery-service = {
      SERVICE_AUDIT_URL = "http://audit-service.${local.service_connect_ns}:8088"
    }
    recovery-service = {
      SERVICE_AUDIT_URL = "http://audit-service.${local.service_connect_ns}:8088"
    }
    audit-service = {}
  }
}

# =============================================================================
# Networking
# =============================================================================

module "networking" {
  source = "./modules/networking"

  project_name         = var.project_name
  environment          = var.environment
  vpc_cidr             = var.vpc_cidr
  availability_zones   = var.availability_zones
  public_subnet_cidrs  = var.public_subnet_cidrs
  private_subnet_cidrs = var.private_subnet_cidrs
  aws_region           = var.aws_region
}

# =============================================================================
# ECR Repositories
# =============================================================================

module "ecr" {
  source = "./modules/ecr"

  project_name  = var.project_name
  environment   = var.environment
  service_names = keys(var.services)
}

# =============================================================================
# Secrets Manager
# =============================================================================

module "secrets" {
  source = "./modules/secrets"

  project_name = var.project_name
  environment  = var.environment
}

# =============================================================================
# RDS PostgreSQL
# =============================================================================

module "rds" {
  source = "./modules/rds"

  project_name      = var.project_name
  environment       = var.environment
  vpc_id            = module.networking.vpc_id
  private_subnet_ids = module.networking.private_subnet_ids
  security_group_id = module.networking.sg_rds_id
  instance_class    = var.db_instance_class
  multi_az          = var.db_multi_az
  allocated_storage = var.db_allocated_storage
  db_name           = var.db_name
  db_username       = var.db_username
  db_password_secret_arn = module.secrets.db_password_secret_arn
}

# =============================================================================
# Amazon MQ (RabbitMQ)
# =============================================================================

module "mq" {
  source = "./modules/mq"

  project_name      = var.project_name
  environment       = var.environment
  subnet_ids        = [module.networking.private_subnet_ids[0]]
  security_group_id = module.networking.sg_mq_id
  instance_type     = var.mq_instance_type
  username          = var.mq_username
  password_secret_arn = module.secrets.mq_password_secret_arn
}

# =============================================================================
# ElastiCache Redis
# =============================================================================

module "elasticache" {
  source = "./modules/elasticache"

  project_name       = var.project_name
  environment        = var.environment
  private_subnet_ids = module.networking.private_subnet_ids
  security_group_id  = module.networking.sg_redis_id
  node_type          = var.redis_node_type
  auth_token_secret_arn = module.secrets.redis_auth_token_secret_arn
}

# =============================================================================
# EFS (Bulk File Storage)
# =============================================================================

module "efs" {
  source = "./modules/efs"

  project_name       = var.project_name
  environment        = var.environment
  private_subnet_ids = module.networking.private_subnet_ids
  security_group_id  = module.networking.sg_efs_id
}

# =============================================================================
# IAM Roles
# =============================================================================

module "iam" {
  source = "./modules/iam"

  project_name       = var.project_name
  environment        = var.environment
  aws_region         = var.aws_region
  aws_account_id     = var.aws_account_id
  secret_arns        = module.secrets.all_secret_arns
  efs_file_system_arn = module.efs.file_system_arn
  github_org         = var.github_org
  github_repo        = var.github_repo
}

# =============================================================================
# ECS Cluster
# =============================================================================

module "ecs_cluster" {
  source = "./modules/ecs-cluster"

  project_name          = var.project_name
  environment           = var.environment
  service_connect_ns    = local.service_connect_ns
  service_names         = keys(var.services)
  capacity_provider     = var.ecs_capacity_provider
}

# =============================================================================
# ALB (External Load Balancer)
# =============================================================================

module "alb" {
  source = "./modules/alb"

  project_name      = var.project_name
  environment       = var.environment
  vpc_id            = module.networking.vpc_id
  public_subnet_ids = module.networking.public_subnet_ids
  security_group_id = module.networking.sg_external_alb_id
  domain_name       = var.domain_name
}

# =============================================================================
# ECS Services (8 microservices)
# =============================================================================

module "ecs_services" {
  source   = "./modules/ecs-service"
  for_each = var.services

  # Core
  project_name   = var.project_name
  environment    = var.environment
  service_name   = each.key
  service_port   = each.value.port
  aws_region     = var.aws_region
  aws_account_id = var.aws_account_id

  # ECS
  cluster_id            = module.ecs_cluster.cluster_id
  cluster_name          = module.ecs_cluster.cluster_name
  task_execution_role_arn = module.iam.task_execution_role_arn
  task_role_arn          = module.iam.task_role_arn
  cpu                    = var.ecs_task_cpu
  memory                 = var.ecs_task_memory
  desired_count          = var.ecs_desired_count
  capacity_provider      = var.ecs_capacity_provider
  image_tag              = each.value.image_tag

  # Networking
  private_subnet_ids = module.networking.private_subnet_ids
  security_group_id  = each.value.is_public ? module.networking.sg_intake_id : module.networking.sg_internal_id
  service_connect_ns = local.service_connect_ns

  # Service Connect namespace ARN
  service_connect_namespace_arn = module.ecs_cluster.service_connect_namespace_arn

  # ALB (intake-service only)
  attach_to_alb    = each.value.is_public
  target_group_arn = each.value.is_public ? module.alb.target_group_arn : ""

  # Environment variables
  environment_variables = merge(
    # OTLP tracing endpoint for all services
    {
      MANAGEMENT_OTLP_TRACING_ENDPOINT = "http://tempo.${local.service_connect_ns}:4318/v1/traces"
    },

    # Database connection (for DB-connected services)
    each.value.has_db ? {
      DB_HOST     = module.rds.endpoint
      DB_PORT     = "5432"
      DB_USERNAME = var.db_username
    } : {},

    # RabbitMQ connection (for MQ-connected services)
    each.value.has_rabbitmq ? {
      SPRING_RABBITMQ_HOST        = module.mq.endpoint
      SPRING_RABBITMQ_PORT        = "5671"
      SPRING_RABBITMQ_USERNAME    = var.mq_username
      SPRING_RABBITMQ_SSL_ENABLED = "true"
    } : {},

    # Redis connection (audit-service only)
    each.value.has_redis ? {
      SPRING_DATA_REDIS_HOST = module.elasticache.endpoint
    } : {},

    # Service URL chain
    lookup(local.service_urls, each.key, {}),
  )

  # Secrets (injected from Secrets Manager)
  secret_variables = merge(
    each.value.has_db ? {
      DB_PASSWORD = module.secrets.db_password_secret_arn
    } : {},
    each.value.has_rabbitmq ? {
      SPRING_RABBITMQ_PASSWORD = module.secrets.mq_password_secret_arn
    } : {},
    each.value.has_redis ? {
      SPRING_DATA_REDIS_PASSWORD = module.secrets.redis_auth_token_secret_arn
    } : {},
  )

  # EFS (intake-service only)
  efs_file_system_id = each.value.has_efs ? module.efs.file_system_id : ""
  efs_access_point_id = each.value.has_efs ? module.efs.access_point_id : ""

  # Log group
  log_group_name = module.ecs_cluster.log_group_names[each.key]
}
