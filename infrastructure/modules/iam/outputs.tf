output "task_execution_role_arn" {
  description = "ECS task execution role ARN"
  value       = aws_iam_role.ecs_task_execution.arn
}

output "task_role_arn" {
  description = "ECS task role ARN"
  value       = aws_iam_role.ecs_task.arn
}

output "github_deploy_role_arn" {
  description = "GitHub Actions deploy role ARN"
  value       = var.github_org != "" ? aws_iam_role.github_deploy[0].arn : ""
}
