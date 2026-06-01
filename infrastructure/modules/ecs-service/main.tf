# -----------------------------------------------------------------------------
# ECS Service Module — Reusable per-service (instantiated 8 times)
# -----------------------------------------------------------------------------

locals {
  container_name = var.service_name
  ecr_image      = "${var.aws_account_id}.dkr.ecr.${var.aws_region}.amazonaws.com/${var.project_name}/${var.service_name}:${var.image_tag}"
  has_efs        = var.efs_file_system_id != ""
}

# ---- Task Definition ----

resource "aws_ecs_task_definition" "main" {
  family                   = "${var.project_name}-${var.environment}-${var.service_name}"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = var.cpu
  memory                   = var.memory
  execution_role_arn       = var.task_execution_role_arn
  task_role_arn            = var.task_role_arn

  container_definitions = jsonencode([
    {
      name      = local.container_name
      image     = local.ecr_image
      essential = true

      portMappings = [
        {
          containerPort = var.service_port
          protocol      = "tcp"
          name          = var.service_name
          appProtocol   = "http"
        }
      ]

      environment = [
        for k, v in var.environment_variables : {
          name  = k
          value = v
        }
      ]

      secrets = [
        for k, v in var.secret_variables : {
          name      = k
          valueFrom = v
        }
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = var.log_group_name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = var.service_name
        }
      }

      healthCheck = {
        command     = ["CMD-SHELL", "wget -q --spider http://localhost:${var.service_port}/actuator/health || exit 1"]
        interval    = 30
        timeout     = 10
        retries     = 3
        startPeriod = 60
      }

      mountPoints = local.has_efs ? [
        {
          containerPath = "/app/bulk"
          sourceVolume  = "bulk-storage"
          readOnly      = false
        }
      ] : []
    }
  ])

  # EFS volume (conditional — only for intake-service)
  dynamic "volume" {
    for_each = local.has_efs ? [1] : []
    content {
      name = "bulk-storage"

      efs_volume_configuration {
        file_system_id          = var.efs_file_system_id
        transit_encryption      = "ENABLED"
        authorization_config {
          access_point_id = var.efs_access_point_id
          iam             = "ENABLED"
        }
      }
    }
  }

  tags = {
    Name    = "${var.project_name}-${var.environment}-${var.service_name}-task"
    Service = var.service_name
  }
}

# ---- ECS Service ----

resource "aws_ecs_service" "main" {
  name            = var.service_name
  cluster         = var.cluster_id
  task_definition = aws_ecs_task_definition.main.arn
  desired_count   = var.desired_count
  launch_type     = var.capacity_provider == "FARGATE" ? "FARGATE" : null

  # Use capacity provider strategy for FARGATE_SPOT
  dynamic "capacity_provider_strategy" {
    for_each = var.capacity_provider == "FARGATE_SPOT" ? [1] : []
    content {
      capacity_provider = "FARGATE_SPOT"
      weight            = 1
      base              = 0
    }
  }

  network_configuration {
    subnets          = var.private_subnet_ids
    security_groups  = [var.security_group_id]
    assign_public_ip = false
  }

  # Service Connect (DNS-based service discovery)
  service_connect_configuration {
    enabled   = true
    namespace = var.service_connect_namespace_arn

    service {
      port_name = var.service_name

      client_alias {
        port     = var.service_port
        dns_name = "${var.service_name}.${var.service_connect_ns}"
      }
    }
  }

  # ALB target group registration (intake-service only)
  dynamic "load_balancer" {
    for_each = var.attach_to_alb ? [1] : []
    content {
      target_group_arn = var.target_group_arn
      container_name   = local.container_name
      container_port   = var.service_port
    }
  }

  # Rolling deployment
  deployment_configuration {
    maximum_percent         = 200
    minimum_healthy_percent = 100
  }

  deployment_circuit_breaker {
    enable   = true
    rollback = true
  }

  # Allow time for Spring Boot startup
  health_check_grace_period_seconds = var.attach_to_alb ? 120 : 0

  # Ignore changes to desired_count (for auto-scaling)
  lifecycle {
    ignore_changes = [desired_count]
  }

  tags = {
    Name    = "${var.project_name}-${var.environment}-${var.service_name}"
    Service = var.service_name
  }
}
