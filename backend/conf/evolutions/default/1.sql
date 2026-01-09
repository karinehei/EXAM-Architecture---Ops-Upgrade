# --- !Ups

CREATE TABLE reports (
  id BIGSERIAL PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  content TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_reports_created_at ON reports(created_at);

# --- !Downs

DROP TABLE IF EXISTS reports;
