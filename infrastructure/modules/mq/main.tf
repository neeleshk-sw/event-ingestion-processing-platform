# -----------------------------------------------------------------------------
# Amazon MQ Module — RabbitMQ Broker
# -----------------------------------------------------------------------------

data "aws_secretsmanager_secret_version" "mq_password" {
  secret_id = var.password_secret_arn
}

resource "aws_mq_broker" "main" {
  broker_name = "${var.project_name}-${var.environment}"

  engine_type        = "RabbitMQ"
  engine_version     = "3.13"
  host_instance_type = var.instance_type
  deployment_mode    = "SINGLE_INSTANCE"

  publicly_accessible = false
  subnet_ids          = var.subnet_ids
  security_groups     = [var.security_group_id]

  user {
    username = var.username
    password = data.aws_secretsmanager_secret_version.mq_password.secret_string
  }

  auto_minor_version_upgrade = true

  logs {
    general = true
  }

  maintenance_window_start_time {
    day_of_week = "SUNDAY"
    time_of_day = "04:00"
    time_zone   = "UTC"
  }

  encryption_options {
    use_aws_owned_key = true
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-rabbitmq"
  }
}
