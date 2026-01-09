-- EXAM System Database Schema

CREATE TABLE IF NOT EXISTS reports (
  id BIGSERIAL PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  content TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for sorting by creation date (used in pagination)
CREATE INDEX IF NOT EXISTS idx_reports_created_at ON reports(created_at DESC);

-- Full-text search index for filter queries (title and content)
-- This significantly improves performance of LIKE queries with wildcards
CREATE INDEX IF NOT EXISTS idx_reports_title_content_gin ON reports USING gin(to_tsvector('english', coalesce(title, '') || ' ' || coalesce(content, '')));

-- Alternative: B-tree indexes for prefix matching (if full-text search is not suitable)
-- CREATE INDEX IF NOT EXISTS idx_reports_title_prefix ON reports(title text_pattern_ops);
-- CREATE INDEX IF NOT EXISTS idx_reports_content_prefix ON reports(content text_pattern_ops);
