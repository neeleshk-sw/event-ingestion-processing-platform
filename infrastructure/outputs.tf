# -----------------------------------------------------------------------------
# Root Outputs
# -----------------------------------------------------------------------------

output "vpc_id" {
  description = "VPC ID"
  value       = module.networking.vpc_id
}

output "alb_dns_name" {
  description = "External ALB DNS name (use for CNAME/alias)"
  value       = module.alb.dns_name
}

output "alb_url" {
  description = "ALB URL for intake-service"
  value       = var.domain_name != "" ? "https://api.${var.domain_name}" : "http://${module.alb.dns_name}"
}

output "rds_endpoint" {
  description = "RDS PostgreSQL endpoint"
  value       = module.rds.endpoint
}

output "mq_endpoint" {
  description = "Amazon MQ RabbitMQ endpoint"
  value       = module.mq.endpoint
}

output "mq_console_url" {
  description = "Amazon MQ management console URL"
  value       = module.mq.console_url
}

output "elasticache_endpoint" {
  description = "ElastiCache Redis endpoint"
  value       = module.elasticache.endpoint
}

output "ecs_cluster_name" {
  description = "ECS cluster name"
  value       = module.ecs_cluster.cluster_name
}

output "ecr_repository_urls" {
  description = "ECR repository URLs per service"
  value       = module.ecr.repository_urls
}

output "efs_file_system_id" {
  description = "EFS file system ID for bulk storage"
  value       = module.efs.file_system_id
}
