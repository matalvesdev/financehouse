# Deployment Guide - Sistema de Gestão Financeira Doméstica

## Overview

This guide covers the deployment process for the Sistema de Gestão Financeira Doméstica in production environments using Docker and Docker Compose.

## Prerequisites

- Docker 20.10 or higher
- Docker Compose 2.0 or higher
- At least 2GB of available RAM
- At least 10GB of available disk space

## Architecture

The production deployment consists of three main services:

1. **PostgreSQL Database** - Persistent data storage
2. **Backend (Spring Boot)** - REST API and business logic
3. **Frontend (React + Nginx)** - User interface

All services run in isolated Docker containers and communicate through a dedicated network.

## Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd gestao-financeira-domestica
```

### 2. Create Environment File

Copy the example environment file and configure it:

```bash
cp .env.example .env.production
```

Edit `.env.production` and set the required variables (see Configuration section below).

### 3. Deploy

Make the deployment script executable and run it:

```bash
chmod +x deploy.sh
./deploy.sh deploy
```

The script will:
- Validate prerequisites
- Check environment variables
- Create a database backup (if exists)
- Build Docker images
- Start all services
- Wait for health checks
- Display service status

### 4. Access the Application

- **Frontend**: http://localhost (or configured port)
- **Backend API**: http://localhost:8080/api
- **Health Check**: http://localhost:8080/actuator/health

## Configuration

### Required Environment Variables

Create a `.env.production` file with the following variables:

```bash
# Database Configuration
POSTGRES_DB=gestao_financeira
POSTGRES_USER=postgres
POSTGRES_PASSWORD=<strong-password-here>

# JWT Configuration (CRITICAL - Must be secure!)
JWT_SECRET=<minimum-32-character-secret-key>
JWT_EXPIRATION=86400000          # 24 hours in milliseconds
JWT_REFRESH_EXPIRATION=604800000 # 7 days in milliseconds

# Encryption Configuration (CRITICAL - Must be secure!)
ENCRYPTION_KEY=<minimum-32-character-encryption-key>

# Application Ports
BACKEND_PORT=8080
FRONTEND_PORT=80

# Logging
LOG_LEVEL=INFO
APP_LOG_LEVEL=INFO

# Optional: Email Configuration (for notifications)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

### Security Best Practices

#### JWT Secret Generation

Generate a secure JWT secret:

```bash
# Using OpenSSL
openssl rand -base64 32

# Using Python
python3 -c "import secrets; print(secrets.token_urlsafe(32))"
```

#### Encryption Key Generation

Generate a secure encryption key:

```bash
# Using OpenSSL
openssl rand -base64 32

# Using Python
python3 -c "import secrets; print(secrets.token_urlsafe(32))"
```

#### Database Password

Use a strong password with:
- Minimum 16 characters
- Mix of uppercase, lowercase, numbers, and special characters
- No dictionary words

```bash
# Generate secure password
openssl rand -base64 24
```

## Deployment Commands

### Deploy Application

```bash
./deploy.sh deploy
```

### Stop Services

```bash
./deploy.sh stop
```

### Restart Services

```bash
./deploy.sh restart
```

### View Logs

```bash
# All services
./deploy.sh logs

# Specific service
./deploy.sh logs backend
./deploy.sh logs frontend
./deploy.sh logs postgres
```

### Check Status

```bash
./deploy.sh status
```

### Create Database Backup

```bash
./deploy.sh backup
```

## Manual Deployment

If you prefer manual deployment without the script:

### Build Images

```bash
docker-compose -f docker-compose.prod.yml --env-file .env.production build
```

### Start Services

```bash
docker-compose -f docker-compose.prod.yml --env-file .env.production up -d
```

### Stop Services

```bash
docker-compose -f docker-compose.prod.yml down
```

### View Logs

```bash
docker-compose -f docker-compose.prod.yml logs -f
```

## Database Management

### Create Backup

```bash
docker exec gestao-financeira-db-prod pg_dump -U postgres gestao_financeira > backup.sql
```

### Restore Backup

```bash
cat backup.sql | docker exec -i gestao-financeira-db-prod psql -U postgres gestao_financeira
```

### Access Database Console

```bash
docker exec -it gestao-financeira-db-prod psql -U postgres gestao_financeira
```

## Monitoring and Health Checks

### Health Check Endpoints

- **Backend**: `http://localhost:8080/actuator/health`
- **Frontend**: `http://localhost/health`
- **Database**: Automatic health checks via Docker

### View Service Health

```bash
docker ps --format "table {{.Names}}\t{{.Status}}"
```

### Monitor Resource Usage

```bash
docker stats
```

## Troubleshooting

### Services Won't Start

1. Check Docker daemon is running:
   ```bash
   docker info
   ```

2. Check environment variables:
   ```bash
   cat .env.production
   ```

3. Check logs:
   ```bash
   ./deploy.sh logs
   ```

### Backend Connection Issues

1. Verify database is healthy:
   ```bash
   docker exec gestao-financeira-db-prod pg_isready -U postgres
   ```

2. Check backend logs:
   ```bash
   docker logs gestao-financeira-backend-prod
   ```

3. Verify network connectivity:
   ```bash
   docker network inspect gestao-financeira-network-prod
   ```

### Frontend Can't Connect to Backend

1. Check nginx configuration:
   ```bash
   docker exec gestao-financeira-frontend-prod cat /etc/nginx/conf.d/default.conf
   ```

2. Verify backend is accessible:
   ```bash
   docker exec gestao-financeira-frontend-prod wget -O- http://backend:8080/actuator/health
   ```

### Database Connection Errors

1. Check database logs:
   ```bash
   docker logs gestao-financeira-db-prod
   ```

2. Verify credentials:
   ```bash
   docker exec gestao-financeira-db-prod psql -U postgres -c "SELECT version();"
   ```

3. Check connection from backend:
   ```bash
   docker exec gestao-financeira-backend-prod wget -O- http://postgres:5432
   ```

## Performance Tuning

### PostgreSQL Optimization

The production configuration includes optimized PostgreSQL settings:

- `max_connections=100` - Maximum concurrent connections
- `shared_buffers=256MB` - Memory for caching
- `effective_cache_size=1GB` - Estimated OS cache
- `work_mem=2621kB` - Memory per query operation

Adjust these based on your server resources.

### Backend JVM Tuning

The backend Dockerfile includes JVM optimization:

- `MaxRAMPercentage=75.0` - Use 75% of container memory
- `UseG1GC` - G1 garbage collector for better performance
- `ExitOnOutOfMemoryError` - Restart on OOM

### Resource Limits

Adjust resource limits in `docker-compose.prod.yml`:

```yaml
deploy:
  resources:
    limits:
      memory: 1G
    reservations:
      memory: 512M
```

## Security Considerations

### Network Security

- Services communicate through isolated Docker network
- Only necessary ports are exposed to host
- Database is not exposed externally

### Data Security

- All sensitive data encrypted at rest (via ENCRYPTION_KEY)
- JWT tokens for authentication
- HTTPS recommended for production (configure reverse proxy)

### Container Security

- Non-root users in containers
- Minimal base images (Alpine Linux)
- Regular security updates

### Recommended: Reverse Proxy with SSL

For production, use a reverse proxy (nginx, Traefik, Caddy) with SSL:

```nginx
server {
    listen 443 ssl http2;
    server_name gestaofinanceira.com;
    
    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;
    
    location / {
        proxy_pass http://localhost:80;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## Backup Strategy

### Automated Backups

Create a cron job for automated backups:

```bash
# Edit crontab
crontab -e

# Add daily backup at 2 AM
0 2 * * * /path/to/deploy.sh backup
```

### Backup Retention

Keep backups for at least 30 days:

```bash
# Clean old backups (keep last 30 days)
find ./database/backup -name "backup_*.sql" -mtime +30 -delete
```

## Scaling

### Horizontal Scaling

For high availability, consider:

1. **Load Balancer** - Distribute traffic across multiple backend instances
2. **Database Replication** - PostgreSQL streaming replication
3. **Shared Storage** - For file uploads and logs

### Vertical Scaling

Increase resources in `docker-compose.prod.yml`:

```yaml
deploy:
  resources:
    limits:
      cpus: '2'
      memory: 2G
```

## Updates and Maintenance

### Update Application

1. Pull latest code:
   ```bash
   git pull origin main
   ```

2. Backup database:
   ```bash
   ./deploy.sh backup
   ```

3. Rebuild and restart:
   ```bash
   ./deploy.sh deploy
   ```

### Update Dependencies

Backend:
```bash
cd backend
./mvnw versions:display-dependency-updates
```

Frontend:
```bash
cd frontend
npm outdated
```

## Support

For issues and questions:
- Check logs: `./deploy.sh logs`
- Review documentation: This file
- Check health endpoints
- Review application logs in `backend/logs/`

## License

[Your License Here]
