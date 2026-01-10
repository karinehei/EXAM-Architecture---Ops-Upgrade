# Backend Dockerfile for Play Framework
FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /app

# Install sbt
# Use --allow-releaseinfo-change to handle repository updates gracefully
RUN apt-get update --allow-releaseinfo-change || true && \
    apt-get install -y curl && \
    curl -L https://github.com/sbt/sbt/releases/download/v1.9.7/sbt-1.9.7.tgz | tar xz -C /usr/local && \
    ln -s /usr/local/sbt/bin/sbt /usr/local/bin/sbt && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Copy build files
COPY build.sbt ./
COPY project ./project

# Download dependencies (cached layer)
# Use -batch mode to prevent interactive prompts
RUN sbt -batch update

# Copy source code
COPY . .

# Copy OpenAPI spec for Swagger UI
COPY conf/api-docs/openapi.yaml conf/api-docs/openapi.yaml

# Build application
RUN sbt -batch dist

# Runtime stage
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Install unzip and curl (for health checks)
# Use --allow-releaseinfo-change to handle repository updates gracefully
RUN apt-get update --allow-releaseinfo-change || true && \
    apt-get install -y unzip curl && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Copy built distribution
COPY --from=build /app/target/universal/exam-backend-*.zip ./
RUN unzip exam-backend-*.zip && \
    rm exam-backend-*.zip && \
    mv exam-backend-* exam-backend

WORKDIR /app/exam-backend

EXPOSE 9000

# Create startup script that removes stale RUNNING_PID file
RUN echo '#!/bin/bash\n\
# Remove stale RUNNING_PID file if it exists\n\
if [ -f RUNNING_PID ]; then\n\
  echo "Removing stale RUNNING_PID file..."\n\
  rm -f RUNNING_PID\n\
fi\n\
# Start the application\n\
exec ./bin/exam-backend -Dconfig.file=conf/application.conf\n\
' > /app/exam-backend/start.sh && chmod +x /app/exam-backend/start.sh

CMD ["/app/exam-backend/start.sh"]
