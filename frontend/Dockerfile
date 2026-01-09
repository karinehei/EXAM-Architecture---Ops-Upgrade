# Frontend Dockerfile for Angular (development mode)
FROM node:20-alpine AS build

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
# Using npm install instead of npm ci since we don't have package-lock.json yet
# For production, consider generating package-lock.json and using npm ci
RUN npm install

# Copy source code
COPY . .

# Expose port for dev server
EXPOSE 4200

# Start dev server
CMD ["npm", "start", "--", "--host", "0.0.0.0", "--port", "4200", "--disable-host-check"]
