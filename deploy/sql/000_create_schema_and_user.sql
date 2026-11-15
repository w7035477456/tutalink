-- Run once as superuser test_user1 on Mac and on Primary (xbox1).
-- Creates schema tutalink + role tutalink_user with privileges only on that schema.
-- Replica receives this via streaming replication — do not run on the Replica.

CREATE SCHEMA IF NOT EXISTS tutalink;

DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'tutalink_user') THEN
    CREATE ROLE tutalink_user LOGIN PASSWORD 'CHANGE_ME_SET_IN_ENV';
  END IF;
END
$$;

GRANT USAGE, CREATE ON SCHEMA tutalink TO tutalink_user;
ALTER ROLE tutalink_user SET search_path TO tutalink;

ALTER DEFAULT PRIVILEGES IN SCHEMA tutalink
  GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO tutalink_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA tutalink
  GRANT USAGE, SELECT ON SEQUENCES TO tutalink_user;

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA tutalink TO tutalink_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA tutalink TO tutalink_user;
