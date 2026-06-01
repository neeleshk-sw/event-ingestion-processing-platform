# -----------------------------------------------------------------------------
# Secrets Module — AWS Secrets Manager
# -----------------------------------------------------------------------------

# ---- Random Password Generation ----

resource "random_password" "db" {
  length           = 32
  special          = true
  override_special = "!#$%&*()-_=+[]{}|:,.<>?"
}

resource "random_password" "mq" {
  length           = 32
  special          = true
  override_special = "!#$%&*()-_=+[]{}|:,.<>?"
}

resource "random_password" "redis" {
  length  = 64
  special = false # ElastiCache AUTH tokens don't support all special chars
}

# ---- Secrets ----

resource "aws_secretsmanager_secret" "db_password" {
  name                    = "/${var.project_name}/${var.environment}/db/password"
  description             = "RDS PostgreSQL master password"
  recovery_window_in_days = var.environment == "prod" ? 30 : 0

  tags = {
    Name = "${var.project_name}-${var.environment}-db-password"
  }
}

resource "aws_secretsmanager_secret_version" "db_password" {
  secret_id     = aws_secretsmanager_secret.db_password.id
  secret_string = random_password.db.result
}

resource "aws_secretsmanager_secret" "mq_password" {
  name                    = "/${var.project_name}/${var.environment}/rabbitmq/password"
  description             = "Amazon MQ RabbitMQ admin password"
  recovery_window_in_days = var.environment == "prod" ? 30 : 0

  tags = {
    Name = "${var.project_name}-${var.environment}-mq-password"
  }
}

resource "aws_secretsmanager_secret_version" "mq_password" {
  secret_id     = aws_secretsmanager_secret.mq_password.id
  secret_string = random_password.mq.result
}

resource "aws_secretsmanager_secret" "redis_auth_token" {
  name                    = "/${var.project_name}/${var.environment}/redis/auth-token"
  description             = "ElastiCache Redis AUTH token"
  recovery_window_in_days = var.environment == "prod" ? 30 : 0

  tags = {
    Name = "${var.project_name}-${var.environment}-redis-auth-token"
  }
}

resource "aws_secretsmanager_secret_version" "redis_auth_token" {
  secret_id     = aws_secretsmanager_secret.redis_auth_token.id
  secret_string = random_password.redis.result
}
