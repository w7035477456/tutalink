CREATE SCHEMA IF NOT EXISTS tutalink;
-- Flyway V1: links + clicks in schema tutalink (never public)

CREATE TABLE IF NOT EXISTS tutalink.links (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(32)  NOT NULL,
    original_url    TEXT         NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    expires_at      TIMESTAMPTZ,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    version         BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uq_links_code UNIQUE (code)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_links_code ON tutalink.links (code);

CREATE TABLE IF NOT EXISTS tutalink.clicks (
    id              BIGSERIAL PRIMARY KEY,
    link_id         BIGINT       NOT NULL REFERENCES tutalink.links (id) ON DELETE CASCADE,
    clicked_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    ip_hash         VARCHAR(64),
    country         VARCHAR(8),
    referrer        TEXT,
    user_agent      TEXT,
    utm_source      VARCHAR(255),
    utm_medium      VARCHAR(255),
    utm_campaign    VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_clicks_link_clicked_at
    ON tutalink.clicks (link_id, clicked_at);
