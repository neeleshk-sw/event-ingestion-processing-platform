# -----------------------------------------------------------------------------
# EFS Module — Elastic File System for Bulk Storage
# -----------------------------------------------------------------------------

resource "aws_efs_file_system" "main" {
  creation_token = "${var.project_name}-${var.environment}-bulk"
  encrypted      = true

  performance_mode = "generalPurpose"
  throughput_mode  = "bursting"

  tags = {
    Name = "${var.project_name}-${var.environment}-bulk-storage"
  }
}

# Mount targets in each private subnet
resource "aws_efs_mount_target" "main" {
  count = length(var.private_subnet_ids)

  file_system_id  = aws_efs_file_system.main.id
  subnet_id       = var.private_subnet_ids[count.index]
  security_groups = [var.security_group_id]
}

# Access point for intake-service bulk uploads
resource "aws_efs_access_point" "bulk" {
  file_system_id = aws_efs_file_system.main.id

  posix_user {
    uid = 1000
    gid = 1000
  }

  root_directory {
    path = "/bulk"

    creation_info {
      owner_uid   = 1000
      owner_gid   = 1000
      permissions = "755"
    }
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-bulk-access-point"
  }
}
