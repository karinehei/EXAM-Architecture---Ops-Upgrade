#!/bin/bash

# Seed script to populate database with test data for performance testing
# This script creates a large number of reports to simulate realistic load

BASE_URL="${K6_BASE_URL:-http://localhost:9000}"
NUM_REPORTS="${NUM_REPORTS:-1000}"

echo "Seeding database with $NUM_REPORTS reports..."
echo "Target: $BASE_URL"

for i in $(seq 1 $NUM_REPORTS); do
  title="Test Report $i"
  content="This is test content for report $i. " 
  # Add some variation to content for filtering tests
  if [ $((i % 10)) -eq 0 ]; then
    content="${content}Special keyword: performance"
  fi
  
  curl -s -X POST "$BASE_URL/api/v1/reports" \
    -H "Content-Type: application/json" \
    -H "X-Request-ID: seed-$i" \
    -d "{\"title\":\"$title\",\"content\":\"$content\"}" \
    > /dev/null
  
  if [ $((i % 100)) -eq 0 ]; then
    echo "Created $i reports..."
  fi
done

echo "Done! Created $NUM_REPORTS reports."
