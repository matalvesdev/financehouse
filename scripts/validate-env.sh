#!/bin/bash

# ============================================
# Environment Validation Script
# Sistema de Gestão Financeira Doméstica
# ============================================
# This script validates environment configuration
# ============================================

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# Configuration
ENV_FILE="${1:-.env.production}"
ERRORS=0
WARNINGS=0

echo -e "${GREEN}================================${NC}"
echo -e "${GREEN}Environment Validation${NC}"
echo -e "${GREEN}================================${NC}"
echo ""
echo "Validating: $ENV_FILE"
echo ""

# Check if file exists
if [ ! -f "$ENV_FILE" ]; then
    echo -e "${RED}Error: $ENV_FILE not found${NC}"
    exit 1
fi

# Load environment file
source "$ENV_FILE"

# Validation functions
validate_required() {
    local var_name=$1
    local var_value=${!var_name}
    
    if [ -z "$var_value" ]; then
        echo -e "${RED}✗ $var_name is not set${NC}"
        ((ERRORS++))
        return 1
    else
        echo -e "${GREEN}✓ $var_name is set${NC}"
        return 0
    fi
}

validate_length() {
    local var_name=$1
    local min_length=$2
    local var_value=${!var_name}
    local actual_length=${#var_value}
    
    if [ $actual_length -lt $min_length ]; then
        echo -e "${RED}✗ $var_name is too short ($actual_length chars, minimum $min_length)${NC}"
        ((ERRORS++))
        return 1
    else
        echo -e "${GREEN}✓ $var_name length is valid ($actual_length chars)${NC}"
        return 0
    fi
}

validate_not_default() {
    local var_name=$1
    local var_value=${!var_name}
    
    if [[ "$var_value" == *"CHANGE_THIS"* ]]; then
        echo -e "${RED}✗ $var_name still contains default value${NC}"
        ((ERRORS++))
        return 1
    else
        echo -e "${GREEN}✓ $var_name has been changed from default${NC}"
        return 0
    fi
}

validate_port() {
    local var_name=$1
    local var_value=${!var_name}
    
    if ! [[ "$var_value" =~ ^[0-9]+$ ]] || [ "$var_value" -lt 1 ] || [ "$var_value" -gt 65535 ]; then
        echo -e "${RED}✗ $var_name is not a valid port number${NC}"
        ((ERRORS++))
        return 1
    else
        echo -e "${GREEN}✓ $var_name is a valid port ($var_value)${NC}"
        return 0
    fi
}

warn_if_weak() {
    local var_name=$1
    local var_value=${!var_name}
    local min_entropy=40
    
    # Simple entropy check (not cryptographically accurate, but good enough)
    local unique_chars=$(echo -n "$var_value" | grep -o . | sort -u | wc -l)
    local length=${#var_value}
    local entropy=$((unique_chars * length / 10))
    
    if [ $entropy -lt $min_entropy ]; then
        echo -e "${YELLOW}⚠ $var_name may be weak (low entropy)${NC}"
        ((WARNINGS++))
    fi
}

# Validate required variables
echo -e "${YELLOW}Checking required variables...${NC}"
echo ""

validate_required "POSTGRES_DB"
validate_required "POSTGRES_USER"
validate_required "POSTGRES_PASSWORD"
validate_required "JWT_SECRET"
validate_required "ENCRYPTION_KEY"

echo ""

# Validate secret lengths
echo -e "${YELLOW}Checking secret lengths...${NC}"
echo ""

validate_length "JWT_SECRET" 32
validate_length "ENCRYPTION_KEY" 32
validate_length "POSTGRES_PASSWORD" 16

echo ""

# Validate not using defaults
echo -e "${YELLOW}Checking for default values...${NC}"
echo ""

validate_not_default "POSTGRES_PASSWORD"
validate_not_default "JWT_SECRET"
validate_not_default "ENCRYPTION_KEY"

echo ""

# Validate ports
echo -e "${YELLOW}Checking port configuration...${NC}"
echo ""

if [ -n "$BACKEND_PORT" ]; then
    validate_port "BACKEND_PORT"
fi

if [ -n "$FRONTEND_PORT" ]; then
    validate_port "FRONTEND_PORT"
fi

echo ""

# Check for weak secrets
echo -e "${YELLOW}Checking secret strength...${NC}"
echo ""

warn_if_weak "JWT_SECRET"
warn_if_weak "ENCRYPTION_KEY"
warn_if_weak "POSTGRES_PASSWORD"

echo ""

# Validate JWT configuration
echo -e "${YELLOW}Checking JWT configuration...${NC}"
echo ""

if [ -n "$JWT_ACCESS_TOKEN_VALIDITY" ]; then
    if [ "$JWT_ACCESS_TOKEN_VALIDITY" -gt 3600 ]; then
        echo -e "${YELLOW}⚠ JWT_ACCESS_TOKEN_VALIDITY is high (${JWT_ACCESS_TOKEN_VALIDITY}s). Consider shorter expiration for security.${NC}"
        ((WARNINGS++))
    else
        echo -e "${GREEN}✓ JWT_ACCESS_TOKEN_VALIDITY is reasonable${NC}"
    fi
fi

if [ -n "$JWT_REFRESH_TOKEN_VALIDITY" ]; then
    if [ "$JWT_REFRESH_TOKEN_VALIDITY" -gt 2592000 ]; then
        echo -e "${YELLOW}⚠ JWT_REFRESH_TOKEN_VALIDITY is very high (${JWT_REFRESH_TOKEN_VALIDITY}s). Consider shorter expiration.${NC}"
        ((WARNINGS++))
    else
        echo -e "${GREEN}✓ JWT_REFRESH_TOKEN_VALIDITY is reasonable${NC}"
    fi
fi

echo ""

# Check logging configuration
echo -e "${YELLOW}Checking logging configuration...${NC}"
echo ""

if [ -n "$LOG_LEVEL" ]; then
    case "$LOG_LEVEL" in
        TRACE|DEBUG|INFO|WARN|ERROR)
            echo -e "${GREEN}✓ LOG_LEVEL is valid ($LOG_LEVEL)${NC}"
            
            if [ "$LOG_LEVEL" == "DEBUG" ] || [ "$LOG_LEVEL" == "TRACE" ]; then
                echo -e "${YELLOW}⚠ LOG_LEVEL is set to $LOG_LEVEL. This may expose sensitive information in production.${NC}"
                ((WARNINGS++))
            fi
            ;;
        *)
            echo -e "${RED}✗ LOG_LEVEL has invalid value ($LOG_LEVEL)${NC}"
            ((ERRORS++))
            ;;
    esac
fi

echo ""

# Check optional but recommended variables
echo -e "${YELLOW}Checking optional variables...${NC}"
echo ""

if [ -z "$SPRING_PROFILES_ACTIVE" ]; then
    echo -e "${YELLOW}⚠ SPRING_PROFILES_ACTIVE is not set. Defaulting to 'production'.${NC}"
    ((WARNINGS++))
else
    echo -e "${GREEN}✓ SPRING_PROFILES_ACTIVE is set ($SPRING_PROFILES_ACTIVE)${NC}"
fi

echo ""

# Security recommendations
echo -e "${YELLOW}Security recommendations:${NC}"
echo ""
echo "1. Rotate secrets every 90 days"
echo "2. Use different secrets for each environment"
echo "3. Store secrets in a secure vault (AWS Secrets Manager, HashiCorp Vault)"
echo "4. Never commit .env files to version control"
echo "5. Back up encryption keys separately from database"
echo "6. Enable HTTPS in production with valid SSL certificates"
echo "7. Implement rate limiting and DDoS protection"
echo "8. Enable audit logging for all financial operations"
echo "9. Regular security audits and penetration testing"
echo "10. Monitor for unauthorized access attempts"

echo ""

# Summary
echo -e "${GREEN}================================${NC}"
echo -e "${GREEN}Validation Summary${NC}"
echo -e "${GREEN}================================${NC}"
echo ""

if [ $ERRORS -eq 0 ] && [ $WARNINGS -eq 0 ]; then
    echo -e "${GREEN}✓ All checks passed!${NC}"
    echo -e "${GREEN}Environment configuration is valid.${NC}"
    exit 0
elif [ $ERRORS -eq 0 ]; then
    echo -e "${YELLOW}⚠ Validation completed with $WARNINGS warning(s)${NC}"
    echo -e "${YELLOW}Review warnings above and consider addressing them.${NC}"
    exit 0
else
    echo -e "${RED}✗ Validation failed with $ERRORS error(s) and $WARNINGS warning(s)${NC}"
    echo -e "${RED}Fix errors before deploying to production.${NC}"
    exit 1
fi
