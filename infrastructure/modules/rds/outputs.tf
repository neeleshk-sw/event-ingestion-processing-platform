output "endpoint" {
  description = "RDS instance endpoint (host only, no port)"
  value       = split(":", aws_db_instance.main.endpoint)[0]
}

output "full_endpoint" {
  description = "RDS instance endpoint (host:port)"
  value       = aws_db_instance.main.endpoint
}

output "db_name" {
  description = "Database name"
  value       = aws_db_instance.main.db_name
}
