# EmpVerify Blockchain API

A secure, enterprise-grade REST API for managing employment records on Hyperledger Fabric blockchain. This system provides tamper-proof employment verification, document management, and ex-employee search capabilities.

## 📋 Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [System Architecture](#system-architecture)
- [Quick Start](#quick-start)
- [API Documentation](#api-documentation)
- [Security & Access Control](#security--access-control)
- [Configuration](#configuration)
- [Deployment](#deployment)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)

## 🌟 Overview

EmpVerify is a blockchain-based employment record management system that provides:

- **Immutable Records**: Employment data stored on blockchain cannot be tampered with
- **Privacy Protection**: Sensitive data is encrypted and access-controlled
- **Ex-Employee Verification**: Quick search and verification for background checks
- **Document Management**: Secure storage and retrieval of employment documents
- **Multi-Organization Support**: Different access levels for employers and verifiers

### What Makes This Special?

- **Automatic Employee ID Generation**: No manual ID management
- **Duplicate Prevention**: Smart detection of duplicate employee records
- **Natural Identifiers**: Use National ID + Employer ID instead of remembering system IDs
- **Role-Based Security**: Different organizations have different access levels
- **File Management**: Upload and download employment documents securely

## 🚀 Key Features

### Core Functionality
- ✅ Create and update employment records
- ✅ Search ex-employees for verification
- ✅ Upload and download employment documents
- ✅ View complete employment history
- ✅ Automatic duplicate detection

### Advanced Features
- 🔐 **Role-Based Access Control** (Org1: Data Entry, Org2: Verification)
- 🔍 **Smart Search** (by name, National ID, employer, employment dates)
- 📁 **Document Management** (contracts, reviews, termination letters)
- 🛡️ **Data Classification** (public, restricted, highly restricted)
- 🔄 **Smart Upsert** (automatically create or update records)

### Security Features
- 🔑 API Key authentication
- 🏢 Organization-based permissions
- 🗃️ Private data collections
- 📝 Audit trail for all changes
- 🚫 Immutable core fields protection

## 🏗️ System Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Client Apps   │    │   Web Browser   │    │  Mobile Apps    │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌─────────────▼─────────────┐
                    │    EmpVerify REST API     │
                    │   (Spring Boot App)       │
                    └─────────────┬─────────────┘
                                  │
                    ┌─────────────▼─────────────┐
                    │   Hyperledger Fabric      │
                    │   Blockchain Network      │
                    │                           │
                    │  ┌─────────┐ ┌─────────┐  │
                    │  │  Org1   │ │  Org2   │  │
                    │  │ (Entry) │ │(Verify) │  │
                    │  └─────────┘ └─────────┘  │
                    └───────────────────────────┘
```

### Components Explained

- **REST API**: Your main interface - handles all requests and responses
- **Blockchain Network**: Stores the actual employment records securely
- **Org1 (Data Entry)**: Organizations that create employment records
- **Org2 (Verification)**: Organizations that verify ex-employee records

## 🚀 Quick Start

### Prerequisites

Before you begin, ensure you have:

- Java 21 or higher
- A running Hyperledger Fabric network with the employment record chaincode
- Access to the blockchain network (certificates and connection profiles)

### 1. Clone and Build

```bash
# Clone the repository
git clone <repository-url>
cd empverify-blockchain-api

# Build the application
./gradlew build
```

### 2. Configure Your Environment

Create an `application-local.properties` file:

```properties
# Basic Configuration
server.port=8080

# Blockchain Network Settings
fabric.network.base-path=/path/to/your/fabric-network
fabric.network.channel-name=employeechannel
fabric.network.contract-name=employment-records

# API Security (use strong keys in production)
empverify.security.api-keys=your-org1-key,your-org2-key

# Identity Mapping (map API keys to blockchain users)
empverify.identity.api-key-mapping.your-org1-key.user-name=org1User
empverify.identity.api-key-mapping.your-org1-key.msp-id=Org1MSP
empverify.identity.api-key-mapping.your-org1-key.role=member
```

### 3. Start the API

```bash
# Run locally
./gradlew bootRun

# Or run with specific profile
./gradlew runLocal
```

### 4. Test the Connection

```bash
# Check if the API is running
curl -H "X-API-Key: your-org1-key" http://localhost:8080/api/v1/employment-records/health
```

Expected response:
```json
{
  "status": "UP",
  "message": "Blockchain connection is healthy"
}
```

## 📚 API Documentation

### Base URL
```
http://localhost:8080/api/v1
```

### Authentication
All requests require an API key in the header:
```
X-API-Key: your-api-key-here
```

### Core Endpoints

#### 1. Create Employment Record

**POST** `/employment-records`

Creates a new employment record with automatic duplicate detection.

```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-org1-key" \
  -d '{
    "employee_name": {
      "full_name": "John Doe",
      "national_id": "GHA-123456789-0"
    },
    "employer_id": "COMPANY001",
    "employer_name": "Tech Solutions Ltd",
    "job_title": "Software Developer",
    "tenure": {
      "start_date": "2022-01-15",
      "end_date": "2024-12-31",
      "duration_months": 36
    },
    "performance_rating": 8.5,
    "eligible_for_rehire": true
  }' \
  http://localhost:8080/api/v1/employment-records
```

**Response:**
```json
{
  "success": true,
  "message": "Employment record created successfully",
  "data": "Employee ID: EMP-2024-000001 created",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

#### 2. Search Ex-Employees

**POST** `/employment-records/search`

Search for ex-employees using various criteria.

```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-org2-key" \
  -d '{
    "employee_name": "John Doe",
    "employer_id": "COMPANY001",
    "search_type": "exact"
  }' \
  http://localhost:8080/api/v1/employment-records/search
```

**Response:**
```json
{
  "success": true,
  "message": "Search completed: 1 result(s) found",
  "data": {
    "total_results": 1,
    "results": [
      {
        "employee_id": "EMP-2024-000001",
        "employee_name": "John Doe",
        "employer_id": "COMPANY001",
        "job_title": "Software Developer",
        "employment_start_date": "2022-01-15",
        "employment_end_date": "2024-12-31",
        "eligible_for_rehire": true,
        "match_score": 1.0,
        "match_type": "exact"
      }
    ]
  }
}
```

#### 3. Get Employment Record (User-Friendly)

**GET** `/employment-records/by-identifiers`

Retrieve employment record using natural identifiers instead of system-generated IDs.

```bash
curl -H "X-API-Key: your-org2-key" \
  "http://localhost:8080/api/v1/employment-records/by-identifiers?nationalId=GHA-123456789-0&employerId=COMPANY001"
```

#### 4. Upload Employment Document

**POST** `/employment-records/{employeeId}/files/upload`

Upload employment documents (contracts, reviews, etc.).

```bash
curl -X POST \
  -H "X-API-Key: your-org1-key" \
  -F "file=@employment_contract.pdf" \
  -F "documentType=employment_contract" \
  http://localhost:8080/api/v1/employment-records/EMP-2024-000001/files/upload
```

#### 5. Verify Employment (Quick Check)

**GET** `/employment-records/search/verification`

Quick verification for background checks.

```bash
curl -H "X-API-Key: your-org2-key" \
  "http://localhost:8080/api/v1/employment-records/search/verification?name=John%20Doe&employer=COMPANY001&expectedStartDate=2022-01"
```

### Search Options

The API supports multiple search methods:

1. **By National ID** (most precise)
2. **By Name + Employer** (common case)
3. **By Employment Dates** (for period verification)
4. **By Employer Only** (list all ex-employees)
5. **Fuzzy Matching** (for name variations)

### File Management

Supported document types:
- `employment_contract`
- `resignation_letter`
- `termination_letter`
- `performance_review`
- `disciplinary_document`
- `exit_interview`
- `id_verification`
- `custom_document`

## 🔐 Security & Access Control

### Organizations and Roles

#### Org1MSP (Data Entry Organizations)
- **Members**: Can create, update, and view all employment records
- **Admins**: Full access including highly restricted documents

#### Org2MSP (Verification Organizations)
- **Members**: Can search and view basic employment information
- **Admins**: Can view sensitive information (performance ratings, departure reasons)

### Data Classification

1. **Basic Employment Data** (Available to both organizations)
    - Employee name, employer, job title, employment dates

2. **Sensitive Personnel Data** (Org1 + Org2 Admins)
    - Performance ratings, departure reasons, supervisor info

3. **Highly Restricted Data** (Org1 Admins only)
    - ID verification documents, disciplinary records

### API Key Configuration

```properties
# Map API keys to blockchain identities
empverify.identity.api-key-mapping.org1-member-key.user-name=org1Member
empverify.identity.api-key-mapping.org1-member-key.msp-id=Org1MSP
empverify.identity.api-key-mapping.org1-member-key.role=member

empverify.identity.api-key-mapping.org2-admin-key.user-name=org2Admin
empverify.identity.api-key-mapping.org2-admin-key.msp-id=Org2MSP
empverify.identity.api-key-mapping.org2-admin-key.role=admin
```

## ⚙️ Configuration

### Environment Profiles

The API supports different deployment environments:

- **Local Development**: `application.properties`
- **EC2 Production**: `application-ec2.properties`
- **Docker**: `application-docker.properties`

### Key Configuration Options

```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/api/v1

# Blockchain Network
fabric.network.base-path=/path/to/fabric-network
fabric.network.channel-name=employeechannel
fabric.network.contract-name=employment-records

# Security
empverify.security.enabled=true
empverify.security.api-keys=key1,key2,key3

# Duplicate Prevention
empverify.duplicate-prevention.enabled=true
empverify.duplicate-prevention.strict-mode=false
empverify.duplicate-prevention.check-similar-names=true

# File Storage (AWS S3)
empverify.s3.bucket-name=empverify-documents
empverify.s3.region=us-east-1
spring.servlet.multipart.max-file-size=10MB
```

### Database Requirements

This API **does not require a traditional database**. All data is stored on the blockchain:

- Employment records → Blockchain private data collections
- Documents → S3 (references stored on blockchain)
- Search indices → Generated dynamically from blockchain data

## 🚀 Deployment

### Local Development

```bash
# 1. Start your Hyperledger Fabric network
cd /path/to/fabric-samples/test-network
./network.sh up createChannel -ca
./network.sh deployCC -ccn employment-records -ccp /path/to/chaincode

# 2. Start the API
./gradlew runLocal
```

### EC2 Production

```bash
# 1. Copy application to EC2
scp -r empverify-blockchain-api/ user@your-ec2:/opt/empverify/

# 2. Configure production settings
cp application-ec2.properties application.properties

# 3. Run as service
java -jar empverify-blockchain-api.jar --spring.profiles.active=ec2
```

### Docker Deployment

```bash
# Build Docker image
./gradlew bootJar
docker build -t empverify-api .

# Run container
docker run -d \
  -p 8080:8080 \
  -v /fabric-network:/fabric-network:ro \
  -e SPRING_PROFILES_ACTIVE=docker \
  empverify-api
```

### Health Checks

Monitor your deployment:

```bash
# Basic health
curl http://localhost:8080/api/v1/employment-records/health

# Blockchain connection
curl -H "X-API-Key: your-key" \
  http://localhost:8080/api/v1/system/rbac/connection-status

# System information
curl -H "X-API-Key: your-key" \
  http://localhost:8080/api/v1/employment-records/system/info
```

## 🔧 Troubleshooting

### Common Issues

#### 1. "Blockchain connection is down"

**Problem**: API cannot connect to the blockchain network.

**Solutions**:
```bash
# Check if your Fabric network is running
docker ps | grep hyperledger

# Verify connection profile path
ls -la /path/to/fabric-network/organizations/peerOrganizations/

# Check API logs
tail -f logs/empverify-api.log
```

#### 2. "Invalid API key"

**Problem**: Authentication failed.

**Solutions**:
- Verify API key in request header: `X-API-Key: your-key`
- Check key mapping in `application.properties`
- Ensure user certificates exist in the specified paths

#### 3. "Employment record not found"

**Problem**: Cannot find employee record.

**Solutions**:
```bash
# Check if record exists using system ID
curl -H "X-API-Key: your-key" \
  http://localhost:8080/api/v1/employment-records/EMP-2024-000001

# Search by natural identifiers
curl -H "X-API-Key: your-key" \
  "http://localhost:8080/api/v1/employment-records/by-identifiers?nationalId=GHA-123456789-0&employerId=COMPANY001"
```

#### 4. "Duplicate record detected"

**Problem**: Trying to create duplicate employment record.

**Solutions**:
- Use the smart upsert endpoint: `POST /employment-records/smart-upsert`
- Check for existing records first
- Adjust duplicate prevention settings

### Debug Mode

Enable detailed logging:

```properties
logging.level.com.empverify=DEBUG
logging.level.org.hyperledger.fabric=INFO
```

### Support Commands

```bash
# Get current employee counter
curl -H "X-API-Key: your-key" \
  http://localhost:8080/api/v1/employment-records/system/counter

# Check duplicate prevention settings
curl -H "X-API-Key: your-key" \
  http://localhost:8080/api/v1/employment-records/system/duplicate-prevention

# Test access levels
curl -H "X-API-Key: your-key" \
  http://localhost:8080/api/v1/system/rbac/access-test
```

## 📖 API Reference

### Interactive Documentation

When the API is running, visit:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/api-docs

### Common Response Codes

| Code | Meaning | Description |
|------|---------|-------------|
| 200 | OK | Request successful |
| 201 | Created | Resource created successfully |
| 400 | Bad Request | Invalid request data |
| 401 | Unauthorized | Invalid or missing API key |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Duplicate resource detected |
| 500 | Internal Error | Server or blockchain error |

### Rate Limiting

Currently no rate limiting is implemented. For production, consider adding:
- API Gateway with rate limiting
- Request throttling based on API key
- Circuit breakers for blockchain calls

## 🤝 Contributing

### Development Setup

1. **Fork and clone** the repository
2. **Set up local Fabric network** for testing
3. **Create feature branch**: `git checkout -b feature/amazing-feature`
4. **Make changes** and add tests
5. **Submit pull request**

### Code Style

- Follow Java naming conventions
- Use meaningful variable names
- Add comprehensive JavaDoc for public methods
- Include unit tests for new features

### Testing

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests EmploymentRecordServiceTest

# Generate test coverage report
./gradlew jacocoTestReport
```

## 📄 License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.

## 🆘 Support

For support and questions:

1. **Check the troubleshooting section** above
2. **Review API documentation** at `/swagger-ui.html`
3. **Enable debug logging** to see detailed error messages
4. **Check blockchain network status** independently

### Useful Resources

- [Hyperledger Fabric Documentation](https://hyperledger-fabric.readthedocs.io/)
- [Spring Boot Reference](https://spring.io/projects/spring-boot)
- [API Testing with Postman Collection](./postman/EmpVerify-API.postman_collection.json)

---

**Built with ❤️ for secure employment verification**