# Monitoring and Observability Guide

## Overview

This guide covers monitoring, logging, and observability for the Sistema de Gestão Financeira Doméstica.

## Architecture

The monitoring stack consists of:

1. **Spring Boot Actuator** - Application health and metrics
2. **Prometheus** - Metrics collection and storage
3. **Grafana** - Metrics visualization and dashboards
4. **Logback** - Structured logging
5. **ELK Stack (Optional)** - Log aggregation and analysis

## Spring Boot Actuator

### Endpoints

The application exposes the following actuator endpoints:

#### Health Check
```
GET /api/actuator/health
```

Response:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 499963174912,
        "free": 336889909248,
        "threshold": 10485760
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

#### Metrics
```
GET /api/actuator/metrics
```

Available metrics:
- `jvm.memory.used` - JVM memory usage
- `jvm.threads.live` - Active thread count
- `process.cpu.usage` - CPU usage
- `http.server.requests` - HTTP request metrics
- `jdbc.connections.active` - Database connection pool
- Custom business metrics

#### Prometheus Metrics
```
GET /api/actuator/prometheus
```

Prometheus-formatted metrics for scraping.

#### Info
```
GET /api/actuator/info
```

Application information (version, build, etc.)

### Configuration

Actuator is configured in `application-production.yml`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
      probes:
        enabled: true
  health:
    db:
      enabled: true
    diskspace:
      enabled: true
    livenessState:
      enabled: true
    readinessState:
      enabled: true
```

## Prometheus Setup

### Installation with Docker

Add Prometheus to `docker-compose.monitoring.yml`:

```yaml
version: '3.8'

services:
  prometheus:
    image: prom/prometheus:latest
    container_name: gestao-financeira-prometheus
    restart: unless-stopped
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--storage.tsdb.retention.time=30d'
    ports:
      - "9090:9090"
    networks:
      - gestao-financeira-network-prod

volumes:
  prometheus_data:
    driver: local
```

### Prometheus Configuration

Create `monitoring/prometheus.yml`:

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s
  external_labels:
    monitor: 'gestao-financeira'
    environment: 'production'

scrape_configs:
  - job_name: 'spring-boot-backend'
    metrics_path: '/api/actuator/prometheus'
    static_configs:
      - targets: ['backend:8080']
        labels:
          application: 'gestao-financeira-backend'
          
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

# Alerting rules
rule_files:
  - 'alerts.yml'

# Alert manager configuration
alerting:
  alertmanagers:
    - static_configs:
        - targets: ['alertmanager:9093']
```

### Alert Rules

Create `monitoring/alerts.yml`:

```yaml
groups:
  - name: application_alerts
    interval: 30s
    rules:
      # High error rate
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High error rate detected"
          description: "Error rate is {{ $value }} errors/sec"
      
      # High response time
      - alert: HighResponseTime
        expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 2
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High response time detected"
          description: "95th percentile response time is {{ $value }}s"
      
      # Database connection pool exhaustion
      - alert: DatabaseConnectionPoolExhausted
        expr: hikaricp_connections_active / hikaricp_connections_max > 0.9
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "Database connection pool nearly exhausted"
          description: "{{ $value }}% of connections in use"
      
      # High memory usage
      - alert: HighMemoryUsage
        expr: jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} > 0.9
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High JVM memory usage"
          description: "Heap memory usage is {{ $value }}%"
      
      # Application down
      - alert: ApplicationDown
        expr: up{job="spring-boot-backend"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Application is down"
          description: "Backend application is not responding"
```

## Grafana Setup

### Installation with Docker

Add Grafana to `docker-compose.monitoring.yml`:

```yaml
  grafana:
    image: grafana/grafana:latest
    container_name: gestao-financeira-grafana
    restart: unless-stopped
    environment:
      - GF_SECURITY_ADMIN_USER=${GRAFANA_ADMIN_USER:-admin}
      - GF_SECURITY_ADMIN_PASSWORD=${GRAFANA_ADMIN_PASSWORD:-admin}
      - GF_INSTALL_PLUGINS=grafana-piechart-panel
    volumes:
      - grafana_data:/var/lib/grafana
      - ./monitoring/grafana/provisioning:/etc/grafana/provisioning
      - ./monitoring/grafana/dashboards:/var/lib/grafana/dashboards
    ports:
      - "3001:3000"
    depends_on:
      - prometheus
    networks:
      - gestao-financeira-network-prod

volumes:
  grafana_data:
    driver: local
```

### Grafana Data Source

Create `monitoring/grafana/provisioning/datasources/prometheus.yml`:

```yaml
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
    editable: false
```

### Grafana Dashboard

Create `monitoring/grafana/provisioning/dashboards/dashboard.yml`:

```yaml
apiVersion: 1

providers:
  - name: 'Gestão Financeira'
    orgId: 1
    folder: ''
    type: file
    disableDeletion: false
    updateIntervalSeconds: 10
    allowUiUpdates: true
    options:
      path: /var/lib/grafana/dashboards
```

### Dashboard JSON

Key metrics to monitor:

1. **Application Health**
   - Uptime
   - Health status
   - Version info

2. **HTTP Metrics**
   - Request rate
   - Response time (p50, p95, p99)
   - Error rate
   - Status code distribution

3. **JVM Metrics**
   - Heap memory usage
   - Non-heap memory usage
   - GC pause time
   - Thread count

4. **Database Metrics**
   - Connection pool usage
   - Query execution time
   - Active connections
   - Transaction rate

5. **Business Metrics**
   - Transactions created per minute
   - Budget alerts triggered
   - User authentication rate
   - Failed login attempts

## Logging

### Log Levels

- **TRACE**: Very detailed information, typically only for debugging
- **DEBUG**: Detailed information for debugging
- **INFO**: General informational messages
- **WARN**: Warning messages for potentially harmful situations
- **ERROR**: Error events that might still allow the application to continue

### Log Files

The application generates the following log files:

1. **gestao-financeira.log** - General application logs
2. **gestao-financeira-audit.log** - Audit trail for financial operations
3. **gestao-financeira-security.log** - Security events and authentication
4. **gestao-financeira-error.log** - Error logs only

### Log Rotation

Logs are automatically rotated:
- **Max file size**: 10MB
- **Retention**: 30 days for general logs, 90 days for audit/security
- **Total size cap**: 500MB for general, 1GB for audit/security
- **Compression**: Rotated logs are gzip compressed

### Structured Logging

Audit and security logs use JSON format (Logstash encoder):

```json
{
  "@timestamp": "2024-01-28T10:30:45.123Z",
  "level": "INFO",
  "logger": "com.gestaofinanceira.audit",
  "message": "Financial operation",
  "operation": "CREATE_TRANSACTION",
  "entityType": "TRANSACTION",
  "entityId": "123e4567-e89b-12d3-a456-426614174000",
  "userId": "user-123",
  "amount": "100.00",
  "category": "ALIMENTACAO",
  "timestamp": "2024-01-28T10:30:45"
}
```

### Audit Logging

Use the `AuditLogger` component for audit trail:

```java
@Autowired
private AuditLogger auditLogger;

// Log transaction creation
auditLogger.logTransactionCreated(
    transactionId,
    userId,
    amount.toString(),
    category
);

// Log authentication
auditLogger.logAuthentication(
    userId,
    email,
    true,
    ipAddress
);

// Log security event
auditLogger.logSecurityEvent(
    "SUSPICIOUS_ACTIVITY",
    userId,
    "Multiple failed login attempts",
    Map.of("attempts", 5, "ip", ipAddress)
);
```

## ELK Stack (Optional)

For advanced log analysis, integrate with ELK stack:

### Elasticsearch

```yaml
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    container_name: gestao-financeira-elasticsearch
    environment:
      - discovery.type=single-node
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
      - xpack.security.enabled=false
    volumes:
      - elasticsearch_data:/usr/share/elasticsearch/data
    ports:
      - "9200:9200"
    networks:
      - gestao-financeira-network-prod
```

### Logstash

```yaml
  logstash:
    image: docker.elastic.co/logstash/logstash:8.11.0
    container_name: gestao-financeira-logstash
    volumes:
      - ./monitoring/logstash/pipeline:/usr/share/logstash/pipeline
      - ./backend/logs:/logs
    ports:
      - "5000:5000"
    depends_on:
      - elasticsearch
    networks:
      - gestao-financeira-network-prod
```

### Kibana

```yaml
  kibana:
    image: docker.elastic.co/kibana/kibana:8.11.0
    container_name: gestao-financeira-kibana
    environment:
      - ELASTICSEARCH_HOSTS=http://elasticsearch:9200
    ports:
      - "5601:5601"
    depends_on:
      - elasticsearch
    networks:
      - gestao-financeira-network-prod
```

## Monitoring Best Practices

### 1. Define SLOs (Service Level Objectives)

- **Availability**: 99.9% uptime
- **Response Time**: 95% of requests < 500ms
- **Error Rate**: < 0.1% of requests
- **Data Durability**: 99.999% (no data loss)

### 2. Set Up Alerts

Configure alerts for:
- Application down
- High error rate
- High response time
- Database connection issues
- Memory/CPU exhaustion
- Disk space low
- Security events

### 3. Monitor Business Metrics

Track:
- Daily active users
- Transactions per day
- Budget alerts triggered
- Goals achieved
- Import success rate
- Authentication failures

### 4. Regular Review

- Review dashboards weekly
- Analyze trends monthly
- Update alerts as needed
- Optimize based on metrics

### 5. Incident Response

1. **Detection**: Automated alerts
2. **Triage**: Check dashboards and logs
3. **Diagnosis**: Analyze metrics and traces
4. **Resolution**: Fix and deploy
5. **Post-mortem**: Document and improve

## Performance Monitoring

### Key Metrics

1. **Throughput**: Requests per second
2. **Latency**: Response time percentiles
3. **Error Rate**: Failed requests percentage
4. **Saturation**: Resource utilization

### Database Performance

Monitor:
- Query execution time
- Slow query log
- Connection pool usage
- Lock contention
- Index usage

### JVM Performance

Monitor:
- Heap usage
- GC frequency and duration
- Thread count
- CPU usage

## Security Monitoring

### Events to Monitor

1. **Authentication**
   - Failed login attempts
   - Successful logins
   - Password changes
   - Account lockouts

2. **Authorization**
   - Access denied events
   - Privilege escalation attempts
   - Unauthorized resource access

3. **Data Access**
   - Sensitive data queries
   - Bulk data exports
   - Unusual access patterns

4. **System Events**
   - Configuration changes
   - Service restarts
   - Deployment events

### SIEM Integration

For enterprise security, integrate with SIEM:
- Splunk
- IBM QRadar
- Azure Sentinel
- AWS Security Hub

## Compliance and Auditing

### LGPD Compliance

Maintain audit logs for:
- Data access (who, what, when)
- Data modifications
- Data exports
- User consent
- Data deletion requests

### Retention Policies

- **Audit logs**: 90 days minimum
- **Security logs**: 90 days minimum
- **Application logs**: 30 days
- **Metrics**: 30 days

### Log Protection

- Encrypt logs at rest
- Restrict access to authorized personnel
- Implement log integrity checks
- Regular backup of audit logs

## Troubleshooting

### High Memory Usage

```bash
# Check JVM memory
docker exec gestao-financeira-backend-prod jmap -heap 1

# Generate heap dump
docker exec gestao-financeira-backend-prod jmap -dump:live,format=b,file=/tmp/heap.bin 1
```

### High CPU Usage

```bash
# Check thread dump
docker exec gestao-financeira-backend-prod jstack 1

# Profile CPU usage
docker stats gestao-financeira-backend-prod
```

### Database Issues

```bash
# Check active connections
docker exec gestao-financeira-db-prod psql -U postgres -c "SELECT count(*) FROM pg_stat_activity;"

# Check slow queries
docker exec gestao-financeira-db-prod psql -U postgres -c "SELECT query, calls, total_time FROM pg_stat_statements ORDER BY total_time DESC LIMIT 10;"
```

## Additional Resources

- [Spring Boot Actuator Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
- [Logback Documentation](https://logback.qos.ch/documentation.html)
- [Micrometer Documentation](https://micrometer.io/docs)
