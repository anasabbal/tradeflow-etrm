-- Creates one schema per service in the shared local database.
-- In production each service owns its own database instance (see docs/architecture.md).
CREATE SCHEMA IF NOT EXISTS trades;
CREATE SCHEMA IF NOT EXISTS prices;
CREATE SCHEMA IF NOT EXISTS valuations;
CREATE SCHEMA IF NOT EXISTS settlements;
