#!/bin/bash

# ============================================
# Secret Generation Script
# Sistema de Gestão Financeira Doméstica
# ============================================
# This script generates secure secrets for the application
# ============================================

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${GREEN}================================${NC}"
echo -e "${GREEN}Secret Generation Script${NC}"
echo -e "${GREEN}================================${NC}"
echo ""

# Check for required tools
if ! command -v openssl &> /dev/null; then
    echo -e "${RED}Error: openssl is not installed${NC}"
    exit 1
fi

# Function to generate secret
generate_secret() {
    local length=$1
    openssl rand -base64 $length
}

# Generate secrets
echo -e "${YELLOW}Generating secrets...${NC}"
echo ""

JWT_SECRET=$(generate_secret 64)
ENCRYPTION_KEY=$(generate_secret 32)
POSTGRES_PASSWORD=$(generate_secret 24)

# Display secrets
echo -e "${GREEN}Generated Secrets:${NC}"
echo ""
echo "# ============================================"
echo "# CRITICAL SECRETS - KEEP SECURE!"
echo "# Generated on: $(date)"
echo "# ============================================"
echo ""
echo "# JWT Secret (64 bytes base64)"
echo "JWT_SECRET=$JWT_SECRET"
echo ""
echo "# Encryption Key (32 bytes base64)"
echo "ENCRYPTION_KEY=$ENCRYPTION_KEY"
echo ""
echo "# Database Password (24 bytes base64)"
echo "POSTGRES_PASSWORD=$POSTGRES_PASSWORD"
echo ""
echo "# ============================================"
echo ""

# Validate secret lengths
echo -e "${YELLOW}Validating secrets...${NC}"
echo ""

JWT_LENGTH=$(echo -n "$JWT_SECRET" | wc -c)
ENC_LENGTH=$(echo -n "$ENCRYPTION_KEY" | wc -c)
DB_LENGTH=$(echo -n "$POSTGRES_PASSWORD" | wc -c)

if [ $JWT_LENGTH -ge 64 ]; then
    echo -e "${GREEN}✓ JWT_SECRET length: $JWT_LENGTH chars (valid)${NC}"
else
    echo -e "${RED}✗ JWT_SECRET length: $JWT_LENGTH chars (too short!)${NC}"
fi

if [ $ENC_LENGTH -ge 32 ]; then
    echo -e "${GREEN}✓ ENCRYPTION_KEY length: $ENC_LENGTH chars (valid)${NC}"
else
    echo -e "${RED}✗ ENCRYPTION_KEY length: $ENC_LENGTH chars (too short!)${NC}"
fi

if [ $DB_LENGTH -ge 16 ]; then
    echo -e "${GREEN}✓ POSTGRES_PASSWORD length: $DB_LENGTH chars (valid)${NC}"
else
    echo -e "${RED}✗ POSTGRES_PASSWORD length: $DB_LENGTH chars (too short!)${NC}"
fi

echo ""

# Ask if user wants to save to file
read -p "Save secrets to .env.production? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    # Check if file exists
    if [ -f ".env.production" ]; then
        echo -e "${YELLOW}Warning: .env.production already exists${NC}"
        read -p "Overwrite? (y/n) " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            echo -e "${YELLOW}Cancelled. Secrets not saved.${NC}"
            exit 0
        fi
    fi
    
    # Create .env.production from template
    if [ -f ".env.example" ]; then
        cp .env.example .env.production
        
        # Replace placeholder values
        sed -i.bak "s|POSTGRES_PASSWORD=.*|POSTGRES_PASSWORD=$POSTGRES_PASSWORD|" .env.production
        sed -i.bak "s|JWT_SECRET=.*|JWT_SECRET=$JWT_SECRET|" .env.production
        sed -i.bak "s|ENCRYPTION_KEY=.*|ENCRYPTION_KEY=$ENCRYPTION_KEY|" .env.production
        
        # Remove backup file
        rm .env.production.bak
        
        echo -e "${GREEN}✓ Secrets saved to .env.production${NC}"
        echo -e "${YELLOW}⚠ Remember to:${NC}"
        echo "  1. Review and configure other variables in .env.production"
        echo "  2. Never commit .env.production to version control"
        echo "  3. Back up these secrets in a secure location"
        echo "  4. Rotate secrets regularly (every 90 days)"
    else
        echo -e "${RED}Error: .env.example not found${NC}"
        exit 1
    fi
else
    echo -e "${YELLOW}Secrets not saved. Copy them manually if needed.${NC}"
fi

echo ""
echo -e "${GREEN}================================${NC}"
echo -e "${GREEN}Secret generation complete!${NC}"
echo -e "${GREEN}================================${NC}"
