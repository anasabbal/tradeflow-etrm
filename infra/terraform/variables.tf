variable "project" {
  description = "Project name used for tagging and resource naming"
  type        = string
  default     = "tradeflow"
}

variable "region" {
  description = "AWS region (spec targets eu-west-1)"
  type        = string
  default     = "eu-west-1"
}

variable "environment" {
  description = "Deployment environment"
  type        = string
  default     = "demo"
}
