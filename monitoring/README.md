# Monitoring Setup

## Quick Start

### 1. Start Monitoring Stack

```bash
# Start production application first
docker-compose -f docker-compose.prod.yml up -d

# Then start monitoring stack
docker-compose -f docker-compose.monitoring.yml up -d
```

### 2. Access Dashboards

- **Grafana**: http://localhost:3001
  - Default credentials: admin / (set in .env.production)
  
- **Prometheus**: http://localhost:9090
  
- **AlertManager**: http://localhost:9093

### 3. Configure Alerts

Edit `.env.production` to add email configuration:

```bash
# Email alerts
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-password
ALERT_EMAIL_FROM=alerts@gestaofinanceira.com
ALERT_EMAIL_TO=admin@gestaofinanceira.com

# Grafana
GRAFANA_ADMIN_PASSWORD=secure-password-here
```

## Directory Structure

```
monitoring/
├── prometheus/
│   ├── prometheus.yml    # Prometheus configuration
│   └── alerts.yml        # Alert rules
├── alertmanager/
│   └── alertmanager.yml  # AlertManager configuration
├── grafana/
│   ├── provisioning/
│   │   ├── datasources/  # Data source configuration
│   │   └── dashboards/   # Dashboard provisioning
│   └── dashboards/       # Dashboard JSON files
└── README.md
```

## Creating Custom Dashboards

### Option 1: Import Existing Dashboard

1. Go to Grafana (http://localhost:3001)
2. Click "+" → "Import"
3. Enter dashboard ID or upload JSON
4. Select Prometheus data source

### Option 2: Create New Dashboard

1. Click "+" → "Dashboard"
2. Add panels with PromQL queries
3. Save dashboard
4. Export JSON to `monitoring/grafana/dashboards/`

## Useful PromQL Queries

### Application Metrics

```promql
# Request rate
rate(http_server_requests_seconds_count[5m])

# Response time (95th percentile)
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))

# Error rate
rate(http_server_requests_seconds_count{status=~"5.."}[5m])

# Active requests
http_server_requests_active_seconds_count
```

### JVM Metrics

```promql
# Heap memory usage
jvm_memory_used_bytes{area="heap"}

# Memory usage percentage
(jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100

# GC pause time
rate(jvm_gc_pause_seconds_sum[5m])

# Thread count
jvm_threads_live_threads
```

### Database Metrics

```promql
# Active connections
hikaricp_connections_active

# Connection pool usage
(hikaricp_connections_active / hikaricp_connections_max) * 100

# Connection wait time
hikaricp_connections_acquire_seconds
```

### Business Metrics

```promql
# Transactions per minute
rate(transactions_created_total[1m]) * 60

# Budget alerts
rate(budget_alerts_total[5m])

# Authentication failures
rate(authentication_failures_total[5m])
```

## Alert Testing

Test alerts manually:

```bash
# Trigger test alert
curl -X POST http://localhost:9093/api/v1/alerts \
  -H 'Content-Type: application/json' \
  -d '[{
    "labels": {
      "alertname": "TestAlert",
      "severity": "warning"
    },
    "annotations": {
      "summary": "Test alert",
      "description": "This is a test alert"
    }
  }]'
```

## Troubleshooting

### Prometheus Not Scraping Metrics

1. Check backend is running:
   ```bash
   curl http://localhost:8080/api/actuator/health
   ```

2. Check Prometheus targets:
   - Go to http://localhost:9090/targets
   - Verify backend target is UP

3. Check network connectivity:
   ```bash
   docker exec gestao-financeira-prometheus wget -O- http://backend:8080/api/actuator/prometheus
   ```

### Grafana Can't Connect to Prometheus

1. Check Prometheus is running:
   ```bash
   docker ps | grep prometheus
   ```

2. Test connection from Grafana container:
   ```bash
   docker exec gestao-financeira-grafana wget -O- http://prometheus:9090/api/v1/query?query=up
   ```

### Alerts Not Firing

1. Check alert rules in Prometheus:
   - Go to http://localhost:9090/alerts
   - Verify rules are loaded

2. Check AlertManager:
   - Go to http://localhost:9093
   - Verify alerts are received

3. Check email configuration in AlertManager logs:
   ```bash
   docker logs gestao-financeira-alertmanager
   ```

## Maintenance

### Backup Grafana Dashboards

```bash
# Export all dashboards
docker exec gestao-financeira-grafana grafana-cli admin export-dashboard > backup.json
```

### Clean Old Metrics

```bash
# Prometheus automatically cleans old data based on retention policy
# Default: 30 days (configured in docker-compose.monitoring.yml)

# Manual cleanup if needed
docker exec gestao-financeira-prometheus promtool tsdb clean-tombstones /prometheus
```

### Update Monitoring Stack

```bash
# Pull latest images
docker-compose -f docker-compose.monitoring.yml pull

# Restart services
docker-compose -f docker-compose.monitoring.yml up -d
```

## Best Practices

1. **Regular Review**: Check dashboards weekly
2. **Alert Tuning**: Adjust thresholds based on actual usage
3. **Dashboard Organization**: Group related metrics
4. **Documentation**: Document custom metrics and alerts
5. **Testing**: Test alerts regularly
6. **Backup**: Export and backup custom dashboards
7. **Security**: Change default Grafana password
8. **Access Control**: Limit access to monitoring tools

## Additional Resources

- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer Metrics](https://micrometer.io/docs)
