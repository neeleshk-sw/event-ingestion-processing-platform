# -----------------------------------------------------------------------------
# ElastiCache Module — Redis
# -----------------------------------------------------------------------------

data "aws_secretsmanager_secret_version" "redis_auth_token" {
  secret_id = var.auth_token_secret_arn
}

resource "aws_elasticache_subnet_group" "main" {
  name       = "${var.project_name}-${var.environment}-redis-subnet"
  subnet_ids = var.private_subnet_ids

  tags = {
    Name = "${var.project_name}-${var.environment}-redis-subnet-group"
  }
}

resource "aws_elasticache_replication_group" "main" {
  replication_group_id = "${var.project_name}-${var.environment}"
  description          = "Redis for ${var.project_name} ${var.environment} (audit-service cache)"

  # Engine
  engine               = "redis"
  engine_version       = "7.1"
  node_type            = var.node_type
  num_cache_clusters   = var.environment == "prod" ? 2 : 1

  # Networking
  subnet_group_name  = aws_elasticache_subnet_group.main.name
  security_group_ids = [var.security_group_id]
  port               = 6379

  # Security
  at_rest_encryption_enabled = true
  transit_encryption_enabled = true
  auth_token                 = data.aws_secretsmanager_secret_version.redis_auth_token.secret_string

  # Maintenance
  maintenance_window       = "sun:05:00-sun:06:00"
  snapshot_window          = "03:00-04:00"
  snapshot_retention_limit = var.environment == "prod" ? 3 : 0
  auto_minor_version_upgrade = true

  # Multi-AZ (prod only)
  automatic_failover_enabled = var.environment == "prod"
  multi_az_enabled           = var.environment == "prod"

  tags = {
    Name = "${var.project_name}-${var.environment}-redis"
  }
}
