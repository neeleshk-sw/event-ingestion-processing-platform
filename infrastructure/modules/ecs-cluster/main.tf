# -----------------------------------------------------------------------------
# ECS Cluster Module
# -----------------------------------------------------------------------------

resource "aws_service_discovery_http_namespace" "main" {
  name        = var.service_connect_ns
  description = "Service Connect namespace for ${var.project_name}"

  tags = {
    Name = "${var.project_name}-${var.environment}-namespace"
  }
}

resource "aws_ecs_cluster" "main" {
  name = "${var.project_name}-${var.environment}"

  service_connect_defaults {
    namespace = aws_service_discovery_http_namespace.main.arn
  }

  setting {
    name  = "containerInsights"
    value = "enabled"
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-cluster"
  }
}

resource "aws_ecs_cluster_capacity_providers" "main" {
  cluster_name = aws_ecs_cluster.main.name

  capacity_providers = ["FARGATE", "FARGATE_SPOT"]

  default_capacity_provider_strategy {
    capacity_provider = var.capacity_provider
    weight            = 1
    base              = 1
  }
}

# ---- CloudWatch Log Groups ----

resource "aws_cloudwatch_log_group" "services" {
  for_each = toset(var.service_names)

  name              = "/ecs/${var.project_name}/${each.value}"
  retention_in_days = var.environment == "prod" ? 30 : 7

  tags = {
    Name    = "${var.project_name}-${var.environment}-${each.value}-logs"
    Service = each.value
  }
}
