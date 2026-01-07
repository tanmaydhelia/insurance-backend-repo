# Insurance Management System - Microservices Backend

A comprehensive insurance management system built using Spring Boot microservices architecture with Eureka service discovery, API Gateway, and JWT-based authentication.

## 🏗️ Architecture Overview

This system follows a microservices architecture pattern with the following components:

```
                          ┌──────────────────────┐
                          │  Config Server       │ 8888
                          │ (Spring Cloud Config)│
                          └──────────┬───────────┘
                                     │  spring.config.import
┌─────────────────┐                  │
│   API Gateway   │ 9000             │
│ (Spring Cloud   )                  ▼
│    Gateway)     │        ┌──────────────────┐
└────────┬────────┘        │ Service Registry │ 8761
         │                 │     (Eureka)     │
         │                 └────────┬─────────┘
         │                          │
         ├─────────┬─────────┬──────┴──────┬──────────┬──────────┐
         ▼         ▼         ▼             ▼          ▼          ▼
┌────────────┐ ┌──────────┐ ┌───────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────┐
│ Identity   │ │ Policy   │ │ Claims    │ │ Hospital │ │ Billing  │ │ Notification │
│ 8085       │ │ 8095     │ │ 8100      │ │ 8105     │ │ 8115     │ │ 8110         │
└────┬───────┘ └────┬─────┘ └────┬──────┘ └────┬─────┘ └────┬─────┘ └────┬─────────┘
     │              │            │             │            │             │
     │              └────────────┼─────────────┘            │             │
     │                           │                          │             │
     ▼                           ▼                          ▼             ▼
 MySQL DBs                 Kafka (9092)               Razorpay API     SMTP (Gmail)
 (identity, policy,        (notification events)      (billing)        (emails)
 claims, provider, billing)
```

## 📋 Microservices Overview

### 1. Service Registry (Eureka Server)
**Port:** `8761`  
**Purpose:** Service discovery and registration for all microservices

#### Features:
- Eureka Server for service discovery
- All microservices register themselves with the registry
- Enables dynamic service lookup and load balancing

#### Access:
- Eureka Dashboard: `http://localhost:8761`

---

### 2. API Gateway
**Port:** `9000`  
**Purpose:** Single entry point for all client requests with routing and authentication

#### Features:
- Routes requests to appropriate microservices
- JWT-based authentication filter
- CORS configuration for frontend integration
- Load balancing through service discovery

#### Routes Configuration:
| Path | Target Service | Authentication Required |
|------|---------------|------------------------|
| `/auth/**` | Identity Service | No (Public endpoints) |
| `/policy/**` | Policy Service | Yes |
| `/claims/**` | Claims Service | Yes |
| `/hospitals/**` | Hospital Service | Yes |

#### CORS Configuration:
- Allowed Origins: `http://localhost:4200`
- Allowed Methods: `GET, POST, PUT, DELETE, OPTIONS`
- Credentials: Enabled

---

### 3. Identity Service
**Port:** `8085`  
**Database:** `insurance_identity_db`  
**Purpose:** User authentication, authorization, and JWT token management

#### User Roles:
- `ROLE_ADMIN` - System administrator
- `ROLE_AGENT` - Insurance agents
- `ROLE_CLAIMS_OFFICER` - Claims processing officers
- `ROLE_USER` - Regular customers/members
- `ROLE_PROVIDER` - Healthcare providers

#### Endpoints:

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/auth/register` | Register new user | No |
| POST | `/auth/token` | Login and get JWT token | No |
| POST | `/auth/google` | Google OAuth login | No |
| GET | `/auth/validate` | Validate JWT token | No |
| PUT | `/auth/change-password` | Change user password | Yes |
| GET | `/auth/users/{email}` | Get user by email | Yes |

#### Request/Response Models:

**User Registration (POST `/auth/register`)**
```json
Request:
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "password": "password123",
  "role": "ROLE_USER"
}

Response: "User registered successfully"
```

**Login (POST `/auth/token`)**
```json
Request:
{
  "username": "john.doe@example.com",
  "password": "password123"
}

Response: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." (JWT Token)
```

**Change Password (PUT `/auth/change-password`)**
```json
Request:
{
  "email": "john.doe@example.com",
  "oldPassword": "password123",
  "newPassword": "newPassword456"
}

Response: "Password changed successfully"
```

---

### 4. Policy Service
**Port:** `8095`  
**Database:** `insurance_policy_db`  
**Purpose:** Manage insurance plans and policy enrollments

#### Features:
- Create and manage insurance plans
- Policy enrollment for users
- Query policies by member or agent
- Automatic policy number generation

#### Endpoints:

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/policy/plans` | Create new insurance plan | Yes |
| GET | `/policy/plans` | Get all insurance plans | Yes |
| POST | `/policy/policies/enroll` | Enroll in a policy | Yes |
| GET | `/policy/policies/member/{userId}` | Get policies for a member | Yes |
| GET | `/policy/policies/agent/{agentId}` | Get policies managed by agent | Yes |
| GET | `/policy/policies/{id}` | Get specific policy details | Yes |

#### Request/Response Models:

**Create Insurance Plan (POST `/policy/plans`)**
```json
Request:
{
  "name": "Health Plus Plan",
  "description": "Comprehensive health coverage",
  "basePremium": 5000.00,
  "coverageAmount": 500000.00
}

Response:
{
  "id": 1,
  "name": "Health Plus Plan",
  "description": "Comprehensive health coverage",
  "basePremium": 5000.00,
  "coverageAmount": 500000.00
}
```

**Policy Enrollment (POST `/policy/policies/enroll`)**
```json
Request:
{
  "userId": 101,
  "agentId": 5,
  "planId": 1,
  "startDate": "2024-01-01",
  "endDate": "2025-01-01"
}

Response:
{
  "id": 1,
  "policyNumber": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "startDate": "2024-01-01",
  "endDate": "2025-01-01",
  "premium": 5000.00,
  "status": "ACTIVE",
  "userId": 101,
  "agentId": 5,
  "planName": "Health Plus Plan",
  "coverageAmount": 500000.00
}
```

#### Policy Status Types:
- `ACTIVE` - Policy is currently active
- `EXPIRED` - Policy has expired
- `CANCELLED` - Policy has been cancelled
- `PENDING` - Policy enrollment pending approval

---

### 5. Claims Service
**Port:** `8100`  
**Database:** `insurance_claim_db`  
**Purpose:** Manage insurance claim submissions and processing

#### Features:
- Submit insurance claims
- Track claim status
- Query claims by member or provider
- Claims officer workflow for processing

#### Claim Status Types:
- `PENDING` - Claim submitted, awaiting review
- `APPROVED` - Claim approved
- `REJECTED` - Claim rejected
- `UNDER_REVIEW` - Claim being reviewed

#### Submission Sources:
- `MEMBER` - Submitted by policyholder
- `PROVIDER` - Submitted by hospital/provider

#### Endpoints:

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/claims/submit` | Submit new claim | Yes |
| GET | `/claims/{id}` | Get claim details | Yes |
| GET | `/claims/member/{userId}` | Get all claims for a member | Yes |
| GET | `/claims/provider/{hospitalId}` | Get all claims for a provider | Yes |
| GET | `/claims/open` | Get all open/pending claims | Yes |
| PUT | `/claims/{id}/status` | Update claim status | Yes |

#### Request/Response Models:

**Submit Claim (POST `/claims/submit`)**
```json
Request:
{
  "policyId": 1,
  "hospitalId": 10,
  "diagnosis": "Appendectomy",
  "claimAmount": 50000.00,
  "submissionSource": "MEMBER",
  "date": "2024-06-15"
}

Response:
{
  "id": 1,
  "policyId": 1,
  "hospitalId": 10,
  "diagnosis": "Appendectomy",
  "claimAmount": 50000.00,
  "status": "PENDING",
  "submissionSource": "MEMBER",
  "date": "2024-06-15",
  "rejectionReason": null
}
```

**Update Claim Status (PUT `/claims/{id}/status`)**
```json
Request:
{
  "status": "APPROVED",
  "rejectionReason": null
}

Response:
{
  "id": 1,
  "policyId": 1,
  "hospitalId": 10,
  "diagnosis": "Appendectomy",
  "claimAmount": 50000.00,
  "status": "APPROVED",
  "submissionSource": "MEMBER",
  "date": "2024-06-15",
  "rejectionReason": null
}
```

---

### 6. Hospital Service
**Port:** `8105`  
**Database:** `insurance_provider_db`  
**Purpose:** Manage network hospitals and healthcare providers

#### Features:
- Register new hospitals
- Search hospitals by name
- Maintain network hospital information
- Track hospital network status

#### Endpoints:

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/hospitals` | Register new hospital | Yes |
| GET | `/hospitals` | Get all active hospitals | Yes |
| GET | `/hospitals/{id}` | Get hospital details | Yes |
| GET | `/hospitals/search?name={name}` | Search hospitals by name | Yes |

#### Request/Response Models:

**Register Hospital (POST `/hospitals`)**
```json
Request:
{
  "name": "City General Hospital",
  "address": "123 Main Street, City, State 12345",
  "contactNumber": "+1-234-567-8900",
  "email": "info@citygeneralhospital.com"
}

Response:
{
  "id": 1,
  "name": "City General Hospital",
  "address": "123 Main Street, City, State 12345",
  "contactNumber": "+1-234-567-8900",
  "email": "info@citygeneralhospital.com",
  "isNetworkHospital": true
}
```

**Search Hospitals (GET `/hospitals/search?name=City`)**
```json
Response:
[
  {
    "id": 1,
    "name": "City General Hospital",
    "address": "123 Main Street, City, State 12345",
    "contactNumber": "+1-234-567-8900",
    "email": "info@citygeneralhospital.com",
    "isNetworkHospital": true
  }
]
```

---

## 🌩️ Config Server (Spring Cloud Config)

Centralized configuration for all services.

- Service: `insurance-config-server` (port 8888)
- Modes supported: native (local files) or Git repo-backed

Client usage (in each service):
```properties
spring.application.name=INSURANCE-POLICY-SERVICE
spring.config.import=optional:configserver:http://localhost:8888
```

Example externalized config `insurance-policy-service.yml` (served by config server):
```yaml
server:
  port: 8095
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/insurance_policy_db?createDatabaseIfNotExist=true
    username: root
    password: root
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

kafka:
  bootstrap-servers: localhost:9092

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

Verify:
```bash
curl http://localhost:8888/insurance-policy-service/default
```

---

## 🛠️ Technology Stack

### Backend Framework
- **Spring Boot** 3.5.10-SNAPSHOT
- **Java** 17
- **Spring Cloud** 2025.0.1

### Microservices Components
- **Spring Cloud Netflix Eureka** - Service Discovery
- **Spring Cloud Gateway** - API Gateway
- **Spring Security** - Authentication & Authorization
- **JWT (JSON Web Tokens)** - Token-based authentication

### Database
- **MySQL** - Relational database
- **Spring Data JPA** - ORM framework
- **Hibernate** - JPA implementation

### Additional Libraries
- **Lombok** - Reduce boilerplate code
- **JaCoCo** - Code coverage
- **Maven** - Build and dependency management

---

## 🚀 Getting Started

### Prerequisites

1. **Java Development Kit (JDK) 17** or higher
2. **Maven** 3.6 or higher
3. **MySQL** 8.0 or higher
4. **Git**

### Database Setup

Create the required MySQL databases:

```sql
CREATE DATABASE insurance_identity_db;
CREATE DATABASE insurance_policy_db;
CREATE DATABASE insurance_claim_db;
CREATE DATABASE insurance_provider_db;
```

### Configuration

Update the MySQL credentials in each service's `application.properties`:

```properties
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

**Files to update:**
- `insurance-identity-service/src/main/resources/application.properties`
- `insurance-policy-service/src/main/resources/application.properties`
- `insurance-claims-service/src/main/resources/application.properties`
- `insurance-hospital-service/src/main/resources/application.properties`

### Running the Services

#### Option 1: Using the run script (Linux/Mac)
```bash
chmod +x run-services.sh
./run-services.sh
```

#### Option 2: Manual startup (Recommended order)

1. **Start Service Registry (Eureka)**
```bash
cd insurance-service-registry
mvn spring-boot:run
```
Wait for the registry to start (~15 seconds)

2. **Start Identity Service**
```bash
cd insurance-identity-service
mvn spring-boot:run
```

3. **Start Policy Service**
```bash
cd insurance-policy-service
mvn spring-boot:run
```

4. **Start Claims Service**
```bash
cd insurance-claims-service
mvn spring-boot:run
```

5. **Start Hospital Service**
```bash
cd insurance-hospital-service
mvn spring-boot:run
```

6. **Start API Gateway**
```bash
cd insurance-api-gateway
mvn spring-boot:run
```

### Verification

1. **Eureka Dashboard**: Visit `http://localhost:8761`
   - Verify all services are registered

2. **Health Check**: Test the API Gateway
```bash
curl http://localhost:9000/auth/validate?token=test
```

---

## 📡 Service Ports

| Service | Port | Database |
|---------|------|----------|
| Service Registry (Eureka) | 8761 | - |
| Identity Service | 8085 | insurance_identity_db |
| Policy Service | 8095 | insurance_policy_db |
| Claims Service | 8100 | insurance_claim_db |
| Hospital Service | 8105 | insurance_provider_db |
| API Gateway | 9000 | - |

---

## 🔒 Authentication Flow

1. **User Registration**: Register a new user via `/auth/register`
2. **Login**: Authenticate and receive JWT token via `/auth/token`
3. **Access Protected Endpoints**: Include JWT token in Authorization header
   ```
   Authorization: Bearer <your-jwt-token>
   ```
4. **API Gateway**: Validates token and routes to appropriate service

### Public Endpoints (No Authentication Required):
- POST `/auth/register`
- POST `/auth/token`
- POST `/auth/google`
- GET `/auth/validate`
- GET `/auth/users/{email}`

### Protected Endpoints (Authentication Required):
- All `/policy/**` endpoints
- All `/claims/**` endpoints
- All `/hospitals/**` endpoints

---

## 🧪 Testing

### Run Tests for Individual Services

```bash
# Policy Service
cd insurance-policy-service
mvn test

# Claims Service
cd insurance-claims-service
mvn test

# Hospital Service
cd insurance-hospital-service
mvn test

# Identity Service
cd insurance-identity-service
mvn test
```

### Generate Code Coverage Report
```bash
mvn clean test
# Reports generated in target/site/jacoco/index.html
```

---

## 📈 Code Quality & Coverage

### JaCoCo (per service)
Generate coverage reports:
```bash
mvn clean test jacoco:report
# Open target/site/jacoco/index.html
```
Enforce 90% line coverage (pom.xml snippet):
```xml
<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
  <version>0.8.11</version>
  <executions>
    <execution>
      <goals><goal>prepare-agent</goal></goals>
    </execution>
    <execution>
      <id>report</id>
      <phase>test</phase>
      <goals><goal>report</goal></goals>
    </execution>
    <execution>
      <id>check</id>
      <goals><goal>check</goal></goals>
      <configuration>
        <rules>
          <rule>
            <element>BUNDLE</element>
            <limits>
              <limit>
                <counter>LINE</counter>
                <value>COVEREDRATIO</value>
                <minimum>0.90</minimum>
              </limit>
            </limits>
          </rule>
        </rules>
      </configuration>
    </execution>
  </executions>
</plugin>
```

### SonarCloud/SonarQube
Run analysis for a service:
```bash
export SONAR_TOKEN=YOUR_TOKEN
mvn -DskipTests=false verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
  -Dsonar.projectKey=tanmaydhelia_insurance-policy-service \
  -Dsonar.organization=tanmaydhelia \
  -Dsonar.login=$SONAR_TOKEN
```
Notes:
- Policy service tests: 100% coverage on service layer; Claims service: >90%.
- Configure `sonar.organization=tanmaydhelia` in poms as needed.

---

## 📊 Database Schema

### Identity Service Database
- **Table**: `users`
  - id (PK, Auto-increment)
  - name
  - email (Unique)
  - password (Encrypted)
  - role (ENUM)

### Policy Service Database
- **Table**: `insurance_plan`
  - id (PK, Auto-increment)
  - name
  - description
  - base_premium
  - coverage_amount

- **Table**: `policy`
  - id (PK, Auto-increment)
  - policy_number (Unique, UUID)
  - start_date
  - end_date
  - premium
  - status (ENUM)
  - user_id (FK to users)
  - agent_id (FK to users)
  - plan_id (FK to insurance_plan)

### Claims Service Database
- **Table**: `claims`
  - id (PK, Auto-increment)
  - policy_id (FK to policy)
  - hospital_id (FK to hospitals)
  - diagnosis
  - claim_amount
  - status (ENUM)
  - submission_source (ENUM)
  - rejection_reason
  - date

### Hospital Service Database
- **Table**: `hospitals`
  - id (PK, Auto-increment)
  - name
  - address
  - contact_number
  - email
  - is_network_hospital (Boolean)

---

## 🔄 API Gateway Routing Details

The API Gateway uses Spring Cloud Gateway to route requests:

1. **Service Discovery**: Uses Eureka to discover service instances
2. **Load Balancing**: Automatically load balances requests using `lb://` protocol
3. **Authentication Filter**: Custom filter validates JWT tokens
4. **Route Validation**: Certain endpoints bypass authentication

---

## 🛡️ Resilience4j (Circuit Breakers, Retry, Bulkhead, TimeLimiter)

Add dependency to services calling others (Feign/RestTemplate):
```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
</dependency>
<dependency>
  <groupId>io.github.resilience4j</groupId>
  <artifactId>resilience4j-timelimiter</artifactId>
</dependency>
```

Usage examples (e.g., in Claims service calling Policy service):
```java
@CircuitBreaker(name = "policyService", fallbackMethod = "fallbackPoliciesByAgent")
public List<PolicyDTO> getPoliciesByAgent(Integer agentId) {
  return policyClient.getPoliciesByAgent(agentId);
}
private List<PolicyDTO> fallbackPoliciesByAgent(Integer agentId, Throwable t) {
  return Collections.emptyList();
}

@Retry(name = "policyServiceRetry")
public PolicyDTO getPolicy(Integer id) { return policyClient.getPolicyById(id); }

@TimeLimiter(name = "policyServiceTL")
public CompletableFuture<PolicyDTO> getPolicyAsync(Integer id) {
  return CompletableFuture.supplyAsync(() -> policyClient.getPolicyById(id));
}

@Bulkhead(name = "claimsBH", type = Bulkhead.Type.SEMAPHORE)
public ClaimResponse submit(ClaimRequest r) { /* ... */ }
```

Centralized config via Config Server:
```yaml
resilience4j:
  circuitbreaker:
    instances:
      policyService:
        failure-rate-threshold: 50
        slow-call-rate-threshold: 50
        slow-call-duration-threshold: 2s
        sliding-window-type: COUNT_BASED
        sliding-window-size: 20
        minimum-number-of-calls: 10
        wait-duration-in-open-state: 10s
        permitted-number-of-calls-in-half-open-state: 3
  retry:
    instances:
      policyServiceRetry:
        max-attempts: 3
        wait-duration: 200ms
  timelimiter:
    instances:
      policyServiceTL:
        timeout-duration: 2s
  bulkhead:
    instances:
      claimsBH:
        max-concurrent-calls: 50
        max-wait-duration: 0
```

---

## 🐛 Troubleshooting

### Service Not Registering with Eureka
- Ensure Eureka server is running on port 8761
- Check network connectivity
- Verify `spring.application.name` in application.properties

### Database Connection Issues
- Verify MySQL is running
- Check database credentials in application.properties
- Ensure databases are created

### Port Already in Use
```bash
# Find and kill process using the port
lsof -ti:8085 | xargs kill -9
```

### JWT Token Issues
- Ensure token is valid and not expired
- Include "Bearer " prefix in Authorization header
- Register and login to get a fresh token

---

## 📝 Best Practices

1. **Always start Eureka Server first** to ensure service registration works
2. **Use API Gateway** (port 9000) for all client requests instead of direct service calls
3. **Include JWT token** in Authorization header for protected endpoints
4. **Monitor Eureka Dashboard** to verify all services are healthy
5. **Check application logs** for detailed error messages

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

---

## 📄 License

This project is part of an insurance management system demonstration.

---

## 👥 Contact & Support

For issues, questions, or contributions, please refer to the project repository.

---

## 🎯 Future Enhancements

- Payment gateway integration
- Email/SMS notifications
- Document upload for claims
- Advanced reporting and analytics
- Mobile app integration
- Premium calculation engine
- Automated claim processing with AI

---

## ✉️ Notifications (HTML Emails)

Notification Service sends branded HTML emails with RestO'Sure header and privacy policy footer.
Scenarios: Welcome, Account Created, Suspended, Reactivated, Claim Submitted/Approved/Rejected.
Templates live in `insurance-notification-service` and are consumed via Kafka events.

## 🐳 Docker & Jenkins

- Build all: `./build-all.sh`
- Compose up: `docker-compose up -d --build`
- Jenkinsfile builds each service sequentially and deploys via docker-compose.

---

**Last Updated:** January 2026
