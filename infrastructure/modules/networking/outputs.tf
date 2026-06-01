output "vpc_id" {
  value = aws_vpc.main.id
}

output "public_subnet_ids" {
  value = aws_subnet.public[*].id
}

output "private_subnet_ids" {
  value = aws_subnet.private[*].id
}

output "sg_external_alb_id" {
  value = aws_security_group.external_alb.id
}

output "sg_intake_id" {
  value = aws_security_group.intake.id
}

output "sg_internal_id" {
  value = aws_security_group.internal.id
}

output "sg_rds_id" {
  value = aws_security_group.rds.id
}

output "sg_mq_id" {
  value = aws_security_group.mq.id
}

output "sg_redis_id" {
  value = aws_security_group.redis.id
}

output "sg_efs_id" {
  value = aws_security_group.efs.id
}
