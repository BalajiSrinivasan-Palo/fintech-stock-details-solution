# Build stage
FROM node:18-alpine as build

WORKDIR /app

# Install yarn
RUN apk add --no-cache yarn

# Copy package files first
COPY package.json ./

# Install dependencies using yarn without lockfile
RUN yarn install

# Copy source code
COPY . .

# Build the app
RUN yarn build

# Production stage
FROM nginx:alpine

# Copy build files from build stage
COPY --from=build /app/build /usr/share/nginx/html

# Copy nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"] 