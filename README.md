# Fintech Portfolio Management System

## Overview

A full-stack application for managing and tracking investment portfolios with real-time market data integration.

### Key Features

- Portfolio management
- Aplpha API market data integration
- Secure authentication and authorization
- Data visualization and analytics
- Automated data synchronization from Gihub
- Message queue integration for scalability

### Technology Stack

- **Frontend**: React, TypeScript, Material-UI
- **Backend**: Spring Boot, Java
- **Database**: PostgreSQL
- **Message Queue**: Apache Kafka
- **Containerization**: Docker
- **Authentication**: JWT

## Solution Architecture

### 1. Data Integration Layer

- GitHub data synchronization
- Market data integration via AlphaVantage API
- Message queue for high-volume data processing

### 2. Core Services

- Portfolio Management
- Market Data Processing
- Performance Calculation
- Authentication & Authorization

### 3. Security Features

- JWT-based authentication
- Encrypted sensitive data
- API key security
- Session management

## Setup Instructions

### Prerequisites

- Docker and Docker Compose
- Node.js (v16+)
- Java 17
- Maven

### Installation Steps

1. Clone the repository:

```bash
git clone <repository-url>
cd fintech-portfolio
```

2. Environment Setup:
 application.properties ,   replace ${GITUB_TOKEN} with your token

```bash
# Create .env file in root directory
ENCRYPTION_KEY=your-encryption-key
JWT_SECRET=your-jwt-secret
ALPHAVANTAGE_API_KEY=your-api-key
```
3. Build and Run:

```bash
# Start all services
docker-compose up --build -d

# Frontend development
cd frontend
npm install
npm start

# Backend development
cd backend
mvn spring-boot:run
```

4. Access the application:

- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- PgAdmin: http://localhost:5050

## Usage Guide

### Authentication

```bash
# Login with demo credentials
username: demo
password: demo
```

### API Endpoints

- `/api/auth/login`: Authentication
- `/api/portfolios`: Portfolio management
- `/api/market-data`: Market data endpoints

### Development

1. Frontend Development:

```bash
cd frontend
npm start
```

2. Backend Development:

```bash
cd backend
mvn spring-boot:run
```

3. Database Management:

```bash
# Access PgAdmin
URL: http://localhost:5050
Email: admin@fintech.com
Password: admin
```

## Testing

```bash
# Frontend tests
cd frontend
npm test

# Backend tests
cd backend
mvn test
```

## Deployment

The application is containerized and can be deployed using Docker Compose:

```bash
docker-compose up -d
```

## Architecture Decisions

1. **Message Queue Integration**

   - Used Kafka for handling high-volume market data
   - Ensures scalable and reliable data processing

2. **Security Implementation**

   - JWT for stateless authentication
   - Encrypted sensitive data
   - Secure API key handling

3. **Data Management**
   - PostgreSQL for relational data
   - Real-time market data integration
   - Automated GitHub sync

## Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

[MIT License](LICENSE)

## Environment Setup

1. Copy `backend/src/main/resources/application.properties.template` to `application.properties`
2. Set up the following environment variables:
   - GITHUB_TOKEN: Your GitHub Personal Access Token
