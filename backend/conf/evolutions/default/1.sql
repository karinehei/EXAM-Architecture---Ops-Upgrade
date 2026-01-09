# --- !Ups

CREATE TABLE reports (
  id BIGSERIAL PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  content TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for sorting by creation date (used in pagination)
CREATE INDEX idx_reports_created_at ON reports(created_at DESC);

-- Note: GIN index for full-text search is PostgreSQL-specific and not supported by H2
-- For production PostgreSQL, apply the GIN index manually or via a separate migration:
-- CREATE INDEX idx_reports_title_content_gin ON reports USING gin(to_tsvector('english', coalesce(title, '') || ' ' || coalesce(content, '')));

# --- !Downs

DROP TABLE IF EXISTS reports;
