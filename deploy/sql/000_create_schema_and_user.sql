-- Run once as superuser test_user1:
--   Mac: always
--   Ubuntu Primary: xbox2 when DEPLOY_ARCHITECTURE=ALL-IN-ONE,
--                   xbox1 when DEPLOY_ARCHITECTURE=MINI_PRODUCTION
-- Replica hosts receive the schema via streaming replication — do not run there.
-- Creates schema tutalink + role tutalink_user with privileges only on that schema.

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
