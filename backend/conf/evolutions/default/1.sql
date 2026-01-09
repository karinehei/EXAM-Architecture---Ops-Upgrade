# --- !Ups

CREATE TABLE reports (
  id BIGSERIAL PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  content TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for sorting by creation date (used in pagination)
CREATE INDEX idx_reports_created_at ON reports(created_at DESC);

-- Full-text search index for filter queries (PostgreSQL GIN index)
-- This significantly improves performance of LIKE queries with wildcards
CREATE INDEX idx_reports_title_content_gin ON reports USING gin(to_tsvector('english', coalesce(title, '') || ' ' || coalesce(content, '')));

# --- !Downs

DROP TABLE IF EXISTS reports;
