provider "aws" {
  region = var.region

  default_tags {
    tags = {
      Project     = var.project
      Environment = var.environment
      ManagedBy   = "terraform"
    }
  }
}

# TODO: VPC - 3 AZs, public (ALB, NAT), private (EKS Fargate), isolated (RDS, ElastiCache) subnets
# TODO: EKS cluster + Fargate profiles per namespace (trade-capture, market-data, valuation, settlement, reporting, bff)
# TODO: IAM roles for service accounts (IRSA), one per service
# TODO: RDS for PostgreSQL 15 (Multi-AZ optional for the demo)
# TODO: MSK (Kafka) cluster with Schema Registry
# TODO: ElastiCache for Redis
# TODO: ECR repositories, one per service (tf-<service>)
# TODO: S3 buckets - reports/archives and cross-region backups with object lock
# TODO: Secrets Manager - DB credentials, JWT signing key, ENTSO-E API token
# TODO: KMS keys for secrets and volume encryption
# TODO: ALB + ACM certificate + Route 53 record for the BFF gateway
