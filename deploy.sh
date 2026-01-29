#!/bin/bash

# ============================================
# Production Deployment Script
# Sistema de Gestão Financeira Doméstica
# ============================================

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
COMPOSE_FILE="docker-compose.prod.yml"
ENV_FILE=".env.production"

# Functions
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_prerequisites() {
    log_info "Checking prerequisites..."
    
    # Check Docker
    if ! command -v docker &> /dev/null; then
        log_error "Docker is not installed. Please install Docker first."
        exit 1
    fi
    
    # Check Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose is not installed. Please install Docker Compose first."
        exit 1
    fi
    
    # Check environment file
    if [ ! -f "$ENV_FILE" ]; then
        log_error "Environment file $ENV_FILE not found. Please create it from .env.example"
        exit 1
    fi
    
    log_info "Prerequisites check passed ✓"
}

validate_env_vars() {
    log_info "Validating environment variables..."
    
    source "$ENV_FILE"
    
    # Required variables
    REQUIRED_VARS=(
        "POSTGRES_PASSWORD"
        "JWT_SECRET"
        "ENCRYPTION_KEY"
    )
    
    for var in "${REQUIRED_VARS[@]}"; do
        if [ -z "${!var}" ]; then
            log_error "Required environment variable $var is not set in $ENV_FILE"
            exit 1
        fi
    done
    
    # Validate JWT_SECRET length (should be at least 32 characters)
    if [ ${#JWT_SECRET} -lt 32 ]; then
        log_error "JWT_SECRET must be at least 32 characters long"
        exit 1
    fi
    
    # Validate ENCRYPTION_KEY length (should be at least 32 characters)
    if [ ${#ENCRYPTION_KEY} -lt 32 ]; then
        log_error "ENCRYPTION_KEY must be at least 32 characters long"
        exit 1
    fi
    
    log_info "Environment variables validation passed ✓"
}

backup_database() {
    log_info "Creating database backup..."
    
    BACKUP_DIR="./database/backup"
    mkdir -p "$BACKUP_DIR"
    
    BACKUP_FILE="$BACKUP_DIR/backup_$(date +%Y%m%d_%H%M%S).sql"
    
    # Check if database container is running
    if docker ps | grep -q gestao-financeira-db-prod; then
        docker exec gestao-financeira-db-prod pg_dump -U postgres gestao_financeira > "$BACKUP_FILE"
        log_info "Database backup created: $BACKUP_FILE ✓"
    else
        log_warn "Database container not running, skipping backup"
    fi
}

build_images() {
    log_info "Building Docker images..."
    
    docker-compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" build --no-cache
    
    log_info "Docker images built successfully ✓"
}

start_services() {
    log_info "Starting services..."
    
    docker-compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d
    
    log_info "Services started successfully ✓"
}

wait_for_health() {
    log_info "Waiting for services to be healthy..."
    
    # Wait for backend to be healthy (max 2 minutes)
    TIMEOUT=120
    ELAPSED=0
    
    while [ $ELAPSED -lt $TIMEOUT ]; do
        if docker exec gestao-financeira-backend-prod wget --spider -q http://localhost:8080/actuator/health 2>/dev/null; then
            log_info "Backend is healthy ✓"
            break
        fi
        
        sleep 5
        ELAPSED=$((ELAPSED + 5))
        echo -n "."
    done
    
    if [ $ELAPSED -ge $TIMEOUT ]; then
        log_error "Backend failed to become healthy within $TIMEOUT seconds"
        docker-compose -f "$COMPOSE_FILE" logs backend
        exit 1
    fi
    
    log_info "All services are healthy ✓"
}

show_status() {
    log_info "Service Status:"
    docker-compose -f "$COMPOSE_FILE" ps
    
    echo ""
    log_info "Application URLs:"
    echo "  Frontend: http://localhost:${FRONTEND_PORT:-80}"
    echo "  Backend API: http://localhost:${BACKEND_PORT:-8080}/api"
    echo "  Health Check: http://localhost:${BACKEND_PORT:-8080}/actuator/health"
}

cleanup_old_images() {
    log_info "Cleaning up old Docker images..."
    
    docker image prune -f
    
    log_info "Cleanup completed ✓"
}

# Main deployment flow
main() {
    log_info "Starting production deployment..."
    echo ""
    
    check_prerequisites
    validate_env_vars
    
    # Ask for confirmation
    read -p "Do you want to proceed with deployment? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_warn "Deployment cancelled by user"
        exit 0
    fi
    
    backup_database
    build_images
    start_services
    wait_for_health
    cleanup_old_images
    
    echo ""
    log_info "Deployment completed successfully! 🚀"
    echo ""
    show_status
}

# Handle script arguments
case "${1:-deploy}" in
    deploy)
        main
        ;;
    stop)
        log_info "Stopping services..."
        docker-compose -f "$COMPOSE_FILE" down
        log_info "Services stopped ✓"
        ;;
    restart)
        log_info "Restarting services..."
        docker-compose -f "$COMPOSE_FILE" restart
        log_info "Services restarted ✓"
        ;;
    logs)
        docker-compose -f "$COMPOSE_FILE" logs -f "${2:-}"
        ;;
    status)
        show_status
        ;;
    backup)
        backup_database
        ;;
    *)
        echo "Usage: $0 {deploy|stop|restart|logs|status|backup}"
        echo ""
        echo "Commands:"
        echo "  deploy  - Deploy the application (default)"
        echo "  stop    - Stop all services"
        echo "  restart - Restart all services"
        echo "  logs    - Show logs (optionally specify service name)"
        echo "  status  - Show service status"
        echo "  backup  - Create database backup"
        exit 1
        ;;
esac
