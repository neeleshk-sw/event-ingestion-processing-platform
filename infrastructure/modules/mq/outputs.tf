output "endpoint" {
  description = "AMQPS endpoint for RabbitMQ (host only)"
  value       = replace(replace(aws_mq_broker.main.instances[0].endpoints[0], "amqps://", ""), ":5671", "")
}

output "amqps_endpoint" {
  description = "Full AMQPS endpoint"
  value       = aws_mq_broker.main.instances[0].endpoints[0]
}

output "console_url" {
  description = "RabbitMQ management console URL"
  value       = aws_mq_broker.main.instances[0].console_url
}

output "broker_id" {
  description = "MQ broker ID"
  value       = aws_mq_broker.main.id
}
