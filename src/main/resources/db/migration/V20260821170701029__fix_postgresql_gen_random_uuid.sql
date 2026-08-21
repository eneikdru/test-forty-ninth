-- Enable pgcrypto extension if needed for gen_random_uuid in older PostgreSQL versions
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
