# Dynamic Payment Gateway Routing System

A sophisticated payment gateway routing system built with Spring Boot that intelligently distributes transactions across multiple payment gateways based on weighted load balancing and real-time health monitoring.

## Live Deployment

The application is deployed and available at: **https://payment-gateway-router.onrender.com**

### Quick Test Endpoints

You can test the API endpoints using the following curl commands:

#### 1. Initiate a Transaction
```bash
curl -X POST https://payment-gateway-router-aub0.onrender.com/transactions/initiate \
  -H "Content-Type: application/json" \
  -d '{
    "order_id": "ORD123",
    "amount": 499.0,
    "payment_instrument": {
      "type": "card",
      "card_number": "1234123412341234",
      "expiry": "12/25",
      "cvv": "123",
      "holder_name": "John Doe"
    }
  }'
```

#### 2. Send Transaction Callback
```bash
curl -X https://payment-gateway-router-aub0.onrender.com/transactions/callback \
  -H "Content-Type: application/json" \
  -d '{
    "order_id": "ORD123",
    "status": "success",
    "gateway": "razorpay",
    "reason": "Payment Successful"
  }'
```

#### 3. Check Gateway Health
```bash
curl https://payment-gateway-router-aub0.onrender.com/monitoring/health
```

#### 4. Application Health Check
```bash
curl https://payment-gateway-router-aub0.onrender.com/actuator/health
```

---

## Features

- **Intelligent Routing**: Weighted load balancing across multiple payment gateways
- **Health Monitoring**: Real-time gateway health tracking with automatic failover
- **Fault Tolerance**: Automatic gateway disabling when success rates drop below threshold
- **RESTful APIs**: Clean, well-documented APIs for transaction processing
- **Comprehensive Testing**: Unit and integration tests with high coverage
- **Docker Support**: Ready for containerized deployment

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **H2 Database** (for demo purposes)
- **Maven**
- **Docker**
- **JUnit 5**
- **Mockito**

## API Endpoints

### 1. Initiate Transaction
```http
POST /transactions/initiate
Content-Type: application/json

{
  "order_id": "ORD123",
  "amount": 499.0,
  "payment_instrument": {
    "type": "card",
    "card_number": "1234123412341234",
    "expiry": "12/25",
    "cvv": "123",
    "holder_name": "John Doe"
  }
}
```

### 2. Transaction Callback
```http
POST /transactions/callback
Content-Type: application/json

{
  "order_id": "ORD123",
  "status": "success",
  "gateway": "razorpay",
  "reason": "Customer Cancelled"
}
```

### 3. Gateway Health Monitoring
```http
GET /monitoring/health
```

## Configuration

The system supports multiple payment gateways with configurable weights:

```yaml
payment:
  gateways:
    - name: "razorpay"
      weight: 40
      enabled: true
    - name: "payu"
      weight: 35
      enabled: true
    - name: "cashfree"
      weight: 25
      enabled: true
  
  health:
    success-rate-threshold: 90.0
    monitoring-window-minutes: 15
    disable-duration-minutes: 30
```

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- Docker (optional)

### Local Development

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd payment-gateway-router
   ```

2. **Build the application**
   ```bash
   mvn clean compile
   ```

3. **Run tests**
   ```bash
   mvn test
   ```

4. **Package the application**
   ```bash
   mvn package
   ```

5. **Run the application**
   ```bash
   java -jar target/payment-gateway-router-1.0.0.jar
   ```

The application will start on port 8080.

### Docker Deployment

1. **Build Docker image**
   ```bash
   docker build -t payment-gateway-router .
   ```

2. **Run with Docker Compose**
   ```bash
   docker-compose up -d
   ```

3. **Check application health**
   ```bash
   curl http://localhost:8080/actuator/health
   ```

## Architecture

### Core Components

1. **GatewayRoutingService**: Implements weighted load balancing logic
2. **GatewayHealthService**: Monitors gateway health and manages failover
3. **TransactionService**: Handles transaction lifecycle management
4. **PaymentGatewayService**: Simulates gateway interactions

### Key Features

- **Weighted Load Balancing**: Distributes load based on configured gateway weights
- **Health Check Monitoring**: Tracks success rates over configurable time windows
- **Automatic Failover**: Temporarily disables unhealthy gateways
- **Comprehensive Logging**: Detailed logging for monitoring and debugging
- **Validation**: Input validation with meaningful error messages

## Testing

Run the test suite:
```bash
mvn test
```

The project includes:
- Unit tests for service layer components
- Integration tests for API endpoints
- Mock implementations for external dependencies

## Monitoring

- **Health Endpoint**: `/actuator/health`
- **Gateway Status**: `/monitoring/health`
- **Application Metrics**: Available via Spring Boot Actuator

## Configuration Options

### Gateway Configuration
- `weight`: Percentage weight for load distribution
- `enabled`: Enable/disable gateway
- `name`: Gateway identifier

### Health Monitoring
- `success-rate-threshold`: Minimum success rate (%)
- `monitoring-window-minutes`: Time window for health calculation
- `disable-duration-minutes`: How long to disable unhealthy gateways

## Production Considerations

1. **Database**: Replace H2 with production database (PostgreSQL, MySQL)
2. **Security**: Add authentication and authorization
3. **Monitoring**: Integrate with APM tools (New Relic, Datadog)
4. **Logging**: Configure centralized logging (ELK stack)
5. **Caching**: Add Redis for session management
6. **Load Balancing**: Use external load balancer for high availability

