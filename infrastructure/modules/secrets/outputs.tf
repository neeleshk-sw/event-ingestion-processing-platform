output "db_password_secret_arn" {
  description = "ARN of the database password secret"
  value       = aws_secretsmanager_secret.db_password.arn
}

output "mq_password_secret_arn" {
  description = "ARN of the MQ password secret"
  value       = aws_secretsmanager_secret.mq_password.arn
}

output "redis_auth_token_secret_arn" {
  description = "ARN of the Redis auth token secret"
  value       = aws_secretsmanager_secret.redis_auth_token.arn
}

output "all_secret_arns" {
  description = "List of all secret ARNs (for IAM policies)"
  value = [
    aws_secretsmanager_secret.db_password.arn,
    aws_secretsmanager_secret.mq_password.arn,
    aws_secretsmanager_secret.redis_auth_token.arn,
  ]
}

output "db_password" {
  description = "Database password value (sensitive)"
  value       = random_password.db.result
  sensitive   = true
}

output "mq_password" {
  description = "MQ password value (sensitive)"
  value       = random_password.mq.result
  sensitive   = true
}

output "redis_auth_token" {
  description = "Redis auth token value (sensitive)"
  value       = random_password.redis.result
  sensitive   = true
}
