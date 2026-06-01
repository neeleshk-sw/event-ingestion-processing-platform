# -----------------------------------------------------------------------------
# Networking Module — Security Groups
# -----------------------------------------------------------------------------

# ---- External ALB Security Group ----

resource "aws_security_group" "external_alb" {
  name_prefix = "${var.project_name}-${var.environment}-alb-"
  description = "Security group for external ALB"
  vpc_id      = aws_vpc.main.id

  ingress {
    description = "HTTP from internet"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "HTTPS from internet"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-sg-external-alb"
  }

  lifecycle {
    create_before_destroy = true
  }
}

# ---- Intake Service Security Group ----

resource "aws_security_group" "intake" {
  name_prefix = "${var.project_name}-${var.environment}-intake-"
  description = "Security group for intake-service (public-facing)"
  vpc_id      = aws_vpc.main.id

  # ALB → intake-service on port 8081
  ingress {
    description     = "From ALB on port 8081"
    from_port       = 8081
    to_port         = 8081
    protocol        = "tcp"
    security_groups = [aws_security_group.external_alb.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-sg-intake"
  }

  lifecycle {
    create_before_destroy = true
  }
}

# ---- Internal Services Security Group ----

resource "aws_security_group" "internal" {
  name_prefix = "${var.project_name}-${var.environment}-internal-"
  description = "Security group for internal microservices (7 services)"
  vpc_id      = aws_vpc.main.id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-sg-internal"
  }

  lifecycle {
    create_before_destroy = true
  }
}

# Self-referencing rule: internal services can talk to each other on 8082–8088
resource "aws_security_group_rule" "internal_self" {
  type                     = "ingress"
  from_port                = 8082
  to_port                  = 8088
  protocol                 = "tcp"
  security_group_id        = aws_security_group.internal.id
  source_security_group_id = aws_security_group.internal.id
  description              = "Internal services communicate on 8082-8088"
}

# Intake → internal services on 8082–8088
resource "aws_security_group_rule" "intake_to_internal" {
  type                     = "ingress"
  from_port                = 8082
  to_port                  = 8088
  protocol                 = "tcp"
  security_group_id        = aws_security_group.internal.id
  source_security_group_id = aws_security_group.intake.id
  description              = "Intake service to internal services"
}

# ---- RDS Security Group ----

resource "aws_security_group" "rds" {
  name_prefix = "${var.project_name}-${var.environment}-rds-"
  description = "Security group for RDS PostgreSQL"
  vpc_id      = aws_vpc.main.id

  ingress {
    description     = "PostgreSQL from intake-service"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.intake.id]
  }

  ingress {
    description     = "PostgreSQL from internal services"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.internal.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-sg-rds"
  }

  lifecycle {
    create_before_destroy = true
  }
}

# ---- Amazon MQ Security Group ----

resource "aws_security_group" "mq" {
  name_prefix = "${var.project_name}-${var.environment}-mq-"
  description = "Security group for Amazon MQ RabbitMQ"
  vpc_id      = aws_vpc.main.id

  # AMQPS (TLS) from intake
  ingress {
    description     = "AMQPS from intake-service"
    from_port       = 5671
    to_port         = 5671
    protocol        = "tcp"
    security_groups = [aws_security_group.intake.id]
  }

  # AMQPS (TLS) from internal services
  ingress {
    description     = "AMQPS from internal services"
    from_port       = 5671
    to_port         = 5671
    protocol        = "tcp"
    security_groups = [aws_security_group.internal.id]
  }

  # Management console (HTTPS)
  ingress {
    description     = "RabbitMQ management from intake-service"
    from_port       = 443
    to_port         = 443
    protocol        = "tcp"
    security_groups = [aws_security_group.intake.id]
  }

  ingress {
    description     = "RabbitMQ management from internal services"
    from_port       = 443
    to_port         = 443
    protocol        = "tcp"
    security_groups = [aws_security_group.internal.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-sg-mq"
  }

  lifecycle {
    create_before_destroy = true
  }
}

# ---- ElastiCache Redis Security Group ----

resource "aws_security_group" "redis" {
  name_prefix = "${var.project_name}-${var.environment}-redis-"
  description = "Security group for ElastiCache Redis"
  vpc_id      = aws_vpc.main.id

  # Redis only from internal services (audit-service)
  ingress {
    description     = "Redis from internal services"
    from_port       = 6379
    to_port         = 6379
    protocol        = "tcp"
    security_groups = [aws_security_group.internal.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-sg-redis"
  }

  lifecycle {
    create_before_destroy = true
  }
}

# ---- EFS Security Group ----

resource "aws_security_group" "efs" {
  name_prefix = "${var.project_name}-${var.environment}-efs-"
  description = "Security group for EFS (bulk storage)"
  vpc_id      = aws_vpc.main.id

  # NFS only from intake-service
  ingress {
    description     = "NFS from intake-service"
    from_port       = 2049
    to_port         = 2049
    protocol        = "tcp"
    security_groups = [aws_security_group.intake.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-sg-efs"
  }

  lifecycle {
    create_before_destroy = true
  }
}
