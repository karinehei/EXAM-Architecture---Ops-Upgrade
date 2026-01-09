-- Migration: Add GIN index for full-text search
-- This is PostgreSQL-specific and should be applied to production databases
-- H2 (used in tests) does not support GIN indexes

-- Full-text search index for filter queries (PostgreSQL GIN index)
-- This significantly improves performance of LIKE queries with wildcards
CREATE INDEX IF NOT EXISTS idx_reports_title_content_gin 
ON reports USING gin(to_tsvector('english', coalesce(title, '') || ' ' || coalesce(content, '')));
