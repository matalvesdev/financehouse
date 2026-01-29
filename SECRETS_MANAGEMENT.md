# Secrets Management Guide

## Overview

This guide covers best practices for managing secrets and sensitive configuration in the Sistema de Gestão Financeira Doméstica.

## Critical Secrets

The application requires the following critical secrets:

1. **JWT_SECRET** - Signs authentication tokens
2. **ENCRYPTION_KEY** - Encrypts sensitive financial data at rest
3. **POSTGRES_PASSWORD** - Database access credentials

**WARNING**: Losing these secrets can result in:
- Loss of access to encrypted data (ENCRYPTION_KEY)
- Security vulnerabilities (JWT_SECRET)
- Database access issues (POSTGRES_PASSWORD)

## Secret Generation

### JWT Secret

Generate a secure JWT secret (minimum 64 characters):

```bash
# Using OpenSSL (recommended)
openssl rand -base64 64

# Using Python
python3 -c "import secrets; print(secrets.token_urlsafe(64))"

# Using Node.js
node -e "console.log(require('crypto').randomBytes(64).toString('base64'))"
```

### Encryption Key

Generate a secure AES-256 encryption key (32 bytes = 256 bits):

```bash
# Using OpenSSL (recommended)
openssl rand -base64 32

# Using Python
python3 -c "import secrets; print(secrets.token_urlsafe(32))"

# Using Node.js
node -e "console.log(require('crypto').randomBytes(32).toString('base64'))"
```

### Database Password

Generate a strong database password:

```bash
# Using OpenSSL
openssl rand -base64 24

# Using Python
python3 -c "import secrets; print(secrets.token_urlsafe(24))"
```

**Requirements**:
- Minimum 16 characters
- Mix of uppercase, lowercase, numbers, and special characters
- No dictionary words or common patterns

## Environment-Specific Configuration

### Development Environment

Create `.env.local` for local development:

```bash
cp .env.example .env.local
```

Use development-friendly values (still secure, but can be shared within team):

```bash
POSTGRES_PASSWORD=dev_password_2024
JWT_SECRET=dev_jwt_secret_key_for_local_development_only_min_64_chars
ENCRYPTION_KEY=dev_encryption_key_32_chars_min
```

### Production Environment

Create `.env.production` for production:

```bash
cp .env.example .env.production
```

Use cryptographically secure, randomly generated values:

```bash
# Generate all secrets
POSTGRES_PASSWORD=$(openssl rand -base64 24)
JWT_SECRET=$(openssl rand -base64 64)
ENCRYPTION_KEY=$(openssl rand -base64 32)

# Save to .env.production
echo "POSTGRES_PASSWORD=$POSTGRES_PASSWORD" >> .env.production
echo "JWT_SECRET=$JWT_SECRET" >> .env.production
echo "ENCRYPTION_KEY=$ENCRYPTION_KEY" >> .env.production
```

## Secret Storage

### Local Development

For local development, store secrets in `.env.local`:

```bash
# .gitignore already includes
.env
.env.*
!.env.example
```

### Production Deployment

**Option 1: Environment Variables (Simple)**

Set environment variables directly on the server:

```bash
export POSTGRES_PASSWORD="your-secure-password"
export JWT_SECRET="your-secure-jwt-secret"
export ENCRYPTION_KEY="your-secure-encryption-key"
```

**Option 2: Docker Secrets (Recommended for Docker Swarm)**

```bash
# Create secrets
echo "your-secure-password" | docker secret create postgres_password -
echo "your-secure-jwt-secret" | docker secret create jwt_secret -
echo "your-secure-encryption-key" | docker secret create encryption_key -

# Reference in docker-compose.yml
secrets:
  postgres_password:
    external: true
  jwt_secret:
    external: true
  encryption_key:
    external: true
```

**Option 3: AWS Secrets Manager (Recommended for AWS)**

```bash
# Store secrets
aws secretsmanager create-secret \
    --name gestao-financeira/postgres-password \
    --secret-string "your-secure-password"

aws secretsmanager create-secret \
    --name gestao-financeira/jwt-secret \
    --secret-string "your-secure-jwt-secret"

aws secretsmanager create-secret \
    --name gestao-financeira/encryption-key \
    --secret-string "your-secure-encryption-key"

# Retrieve in application startup script
export POSTGRES_PASSWORD=$(aws secretsmanager get-secret-value \
    --secret-id gestao-financeira/postgres-password \
    --query SecretString --output text)
```

**Option 4: HashiCorp Vault (Enterprise)**

```bash
# Store secrets
vault kv put secret/gestao-financeira \
    postgres_password="your-secure-password" \
    jwt_secret="your-secure-jwt-secret" \
    encryption_key="your-secure-encryption-key"

# Retrieve in application
vault kv get -field=postgres_password secret/gestao-financeira
```

**Option 5: Kubernetes Secrets (For Kubernetes)**

```bash
# Create secrets
kubectl create secret generic gestao-financeira-secrets \
    --from-literal=postgres-password='your-secure-password' \
    --from-literal=jwt-secret='your-secure-jwt-secret' \
    --from-literal=encryption-key='your-secure-encryption-key'

# Reference in deployment
env:
  - name: POSTGRES_PASSWORD
    valueFrom:
      secretKeyRef:
        name: gestao-financeira-secrets
        key: postgres-password
```

## Secret Rotation

### Why Rotate Secrets?

- Reduce impact of potential compromises
- Comply with security policies
- Best practice for long-running systems

### Rotation Schedule

- **JWT_SECRET**: Every 90 days
- **ENCRYPTION_KEY**: Every 180 days (requires data re-encryption)
- **POSTGRES_PASSWORD**: Every 90 days
- **API Keys**: Every 30-90 days

### JWT Secret Rotation

JWT secret rotation requires careful planning to avoid service disruption:

```bash
# Step 1: Generate new secret
NEW_JWT_SECRET=$(openssl rand -base64 64)

# Step 2: Configure application to accept both old and new secrets
# (Requires code changes to support multiple secrets)

# Step 3: Deploy with both secrets active

# Step 4: Wait for all old tokens to expire (7 days for refresh tokens)

# Step 5: Remove old secret from configuration

# Step 6: Deploy with only new secret
```

### Encryption Key Rotation

**WARNING**: Encryption key rotation requires re-encrypting all data!

```bash
# Step 1: Generate new encryption key
NEW_ENCRYPTION_KEY=$(openssl rand -base64 32)

# Step 2: Create migration script to re-encrypt data
# (See backend/scripts/rotate-encryption-key.sh)

# Step 3: Schedule maintenance window

# Step 4: Run migration script
./backend/scripts/rotate-encryption-key.sh \
    --old-key "$OLD_ENCRYPTION_KEY" \
    --new-key "$NEW_ENCRYPTION_KEY"

# Step 5: Update environment configuration

# Step 6: Restart application
```

### Database Password Rotation

```bash
# Step 1: Generate new password
NEW_PASSWORD=$(openssl rand -base64 24)

# Step 2: Update database user password
docker exec gestao-financeira-db-prod psql -U postgres -c \
    "ALTER USER postgres WITH PASSWORD '$NEW_PASSWORD';"

# Step 3: Update environment configuration
# Edit .env.production and update POSTGRES_PASSWORD

# Step 4: Restart backend service
docker-compose -f docker-compose.prod.yml restart backend
```

## Secret Backup

### Backup Strategy

1. **Secure Storage**: Store secrets in encrypted backup
2. **Separate Location**: Keep separate from database backups
3. **Access Control**: Limit access to authorized personnel only
4. **Regular Testing**: Test secret recovery procedures

### Backup Procedure

```bash
# Create encrypted backup of secrets
tar czf secrets-backup.tar.gz .env.production
openssl enc -aes-256-cbc -salt -in secrets-backup.tar.gz \
    -out secrets-backup.tar.gz.enc -k "backup-password"

# Store in secure location
# - Encrypted USB drive
# - Password manager (1Password, LastPass)
# - Secure cloud storage (encrypted)
# - Physical safe

# Delete unencrypted backup
rm secrets-backup.tar.gz
```

### Recovery Procedure

```bash
# Decrypt backup
openssl enc -aes-256-cbc -d -in secrets-backup.tar.gz.enc \
    -out secrets-backup.tar.gz -k "backup-password"

# Extract secrets
tar xzf secrets-backup.tar.gz

# Restore to production
cp .env.production /path/to/production/

# Restart services
./deploy.sh restart
```

## Security Best Practices

### 1. Never Commit Secrets to Git

```bash
# Verify .gitignore includes
.env
.env.*
!.env.example

# Check for accidentally committed secrets
git log -p | grep -i "password\|secret\|key"

# Remove secrets from Git history if found
git filter-branch --force --index-filter \
    "git rm --cached --ignore-unmatch .env" \
    --prune-empty --tag-name-filter cat -- --all
```

### 2. Use Different Secrets Per Environment

```
Development:  .env.local
Staging:      .env.staging
Production:   .env.production
```

Never reuse production secrets in other environments!

### 3. Limit Secret Access

- Use principle of least privilege
- Only authorized personnel should access production secrets
- Use audit logs to track secret access
- Implement multi-factor authentication for secret access

### 4. Monitor for Secret Exposure

```bash
# Check for secrets in logs
grep -r "JWT_SECRET\|ENCRYPTION_KEY\|POSTGRES_PASSWORD" logs/

# Scan for secrets in code
git secrets --scan

# Use automated tools
trufflehog --regex --entropy=True .
```

### 5. Implement Secret Scanning

Add pre-commit hook to prevent secret commits:

```bash
# .git/hooks/pre-commit
#!/bin/bash

# Check for potential secrets
if git diff --cached | grep -E "(password|secret|key|token).*=.*['\"].*['\"]"; then
    echo "ERROR: Potential secret detected in commit!"
    echo "Please remove secrets and use environment variables instead."
    exit 1
fi
```

## Compliance and Auditing

### Audit Log

Maintain an audit log of secret operations:

```
Date       | Operation        | User    | Secret Type      | Notes
-----------|------------------|---------|------------------|------------------
2024-01-15 | Generated        | admin   | JWT_SECRET       | Initial setup
2024-01-15 | Generated        | admin   | ENCRYPTION_KEY   | Initial setup
2024-04-15 | Rotated          | admin   | JWT_SECRET       | Scheduled rotation
2024-07-15 | Rotated          | admin   | POSTGRES_PASSWORD| Scheduled rotation
```

### Compliance Requirements

For LGPD (Lei Geral de Proteção de Dados) compliance:

1. **Data Encryption**: All sensitive data must be encrypted at rest
2. **Access Control**: Implement role-based access control
3. **Audit Trail**: Maintain logs of all data access
4. **Data Retention**: Define and enforce data retention policies
5. **Breach Notification**: Procedures for security incident response

## Troubleshooting

### Secret Validation Failed

```bash
# Check secret length
echo -n "$JWT_SECRET" | wc -c  # Should be >= 32

# Check secret format
echo "$ENCRYPTION_KEY" | base64 -d | wc -c  # Should be 32 bytes

# Test database connection
docker exec gestao-financeira-backend-prod \
    psql -h postgres -U postgres -d gestao_financeira -c "SELECT 1"
```

### Application Won't Start

```bash
# Check environment variables are set
docker exec gestao-financeira-backend-prod env | grep -E "JWT_SECRET|ENCRYPTION_KEY|POSTGRES_PASSWORD"

# Check logs for secret-related errors
docker logs gestao-financeira-backend-prod | grep -i "secret\|encryption\|password"
```

### Data Decryption Failed

```bash
# Verify encryption key hasn't changed
# Compare with backup

# Check database for encrypted data
docker exec gestao-financeira-db-prod psql -U postgres gestao_financeira \
    -c "SELECT id, descricao FROM transacoes LIMIT 1"

# If key is lost, data cannot be recovered!
```

## Emergency Procedures

### Secret Compromise

If a secret is compromised:

1. **Immediate Actions**:
   - Rotate the compromised secret immediately
   - Invalidate all active sessions (for JWT_SECRET)
   - Review access logs for unauthorized access
   - Notify security team

2. **Investigation**:
   - Determine scope of compromise
   - Identify affected systems and data
   - Document timeline of events

3. **Remediation**:
   - Rotate all related secrets
   - Update security procedures
   - Implement additional monitoring
   - Conduct security audit

4. **Communication**:
   - Notify affected users (if required by LGPD)
   - Document incident for compliance
   - Update security documentation

### Lost Encryption Key

**WARNING**: If encryption key is lost, encrypted data CANNOT be recovered!

Prevention:
- Maintain secure backups of encryption key
- Store in multiple secure locations
- Document recovery procedures
- Test recovery procedures regularly

## Additional Resources

- [OWASP Secrets Management Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html)
- [NIST Guidelines for Password Management](https://pages.nist.gov/800-63-3/)
- [AWS Secrets Manager Best Practices](https://docs.aws.amazon.com/secretsmanager/latest/userguide/best-practices.html)
- [HashiCorp Vault Documentation](https://www.vaultproject.io/docs)

## Support

For security-related questions or incidents:
- Email: security@gestaofinanceira.com
- Emergency: [Emergency Contact]
- Documentation: This file and DEPLOYMENT.md
