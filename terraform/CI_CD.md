# CI/CD para OCI com GitHub Actions

Este guia mostra como configurar CI/CD automatizado para deploy na Oracle Cloud Infrastructure usando GitHub Actions.

## Visão Geral

O pipeline automatiza:
- ✅ Build do backend (Maven)
- ✅ Build do frontend (npm)
- ✅ Testes unitários e de integração
- ✅ Build de imagens Docker
- ✅ Deploy na instância OCI
- ✅ Health checks pós-deploy

## Configuração

### 1. Secrets do GitHub

Adicione os seguintes secrets no repositório:

**GitHub → Settings → Secrets and variables → Actions → New repository secret**

```
OCI_SSH_PRIVATE_KEY       # Conteúdo de ~/.ssh/oci_key
OCI_INSTANCE_IP           # IP público da instância
DB_HOST                   # Host do banco de dados
DB_NAME                   # Nome do banco
DB_USERNAME               # Usuário do banco
DB_PASSWORD               # Senha do banco
JWT_SECRET                # Secret JWT
ENCRYPTION_KEY            # Chave de criptografia
```

### 2. Workflow do GitHub Actions

Crie `.github/workflows/deploy.yml`:

```yaml
name: Deploy to OCI

on:
  push:
    branches: [ main, production ]
  pull_request:
    branches: [ main ]
  workflow_dispatch:

env:
  JAVA_VERSION: '17'
  NODE_VERSION: '18'

jobs:
  test-backend:
    name: Test Backend
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK
        uses: actions/setup-java@v3
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: maven
      
      - name: Run tests
        run: |
          cd backend
          mvn clean test
      
      - name: Run integration tests
        run: |
          cd backend
          mvn verify -P integration-tests

  test-frontend:
    name: Test Frontend
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up Node.js
        uses: actions/setup-node@v3
        with:
          node-version: ${{ env.NODE_VERSION }}
          cache: 'npm'
          cache-dependency-path: frontend/package-lock.json
      
      - name: Install dependencies
        run: |
          cd frontend
          npm ci
      
      - name: Run tests
        run: |
          cd frontend
          npm test -- --run
      
      - name: Build
        run: |
          cd frontend
          npm run build

  build-and-push:
    name: Build Docker Images
    needs: [test-backend, test-frontend]
    runs-on: ubuntu-latest
    if: github.event_name == 'push' && github.ref == 'refs/heads/main'
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v2
      
      - name: Build backend image
        run: |
          cd backend
          docker build -t gestao-financeira-backend:latest .
      
      - name: Build frontend image
        run: |
          cd frontend
          docker build -t gestao-financeira-frontend:latest .
      
      - name: Save images
        run: |
          docker save gestao-financeira-backend:latest | gzip > backend-image.tar.gz
          docker save gestao-financeira-frontend:latest | gzip > frontend-image.tar.gz
      
      - name: Upload artifacts
        uses: actions/upload-artifact@v3
        with:
          name: docker-images
          path: |
            backend-image.tar.gz
            frontend-image.tar.gz
          retention-days: 1

  deploy:
    name: Deploy to OCI
    needs: build-and-push
    runs-on: ubuntu-latest
    if: github.event_name == 'push' && github.ref == 'refs/heads/main'
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Download artifacts
        uses: actions/download-artifact@v3
        with:
          name: docker-images
      
      - name: Configure SSH
        run: |
          mkdir -p ~/.ssh
          echo "${{ secrets.OCI_SSH_PRIVATE_KEY }}" > ~/.ssh/oci_key
          chmod 600 ~/.ssh/oci_key
          ssh-keyscan -H ${{ secrets.OCI_INSTANCE_IP }} >> ~/.ssh/known_hosts
      
      - name: Copy images to server
        run: |
          scp -i ~/.ssh/oci_key backend-image.tar.gz opc@${{ secrets.OCI_INSTANCE_IP }}:/tmp/
          scp -i ~/.ssh/oci_key frontend-image.tar.gz opc@${{ secrets.OCI_INSTANCE_IP }}:/tmp/
      
      - name: Deploy application
        run: |
          ssh -i ~/.ssh/oci_key opc@${{ secrets.OCI_INSTANCE_IP }} << 'EOF'
            set -e
            
            # Load images
            docker load < /tmp/backend-image.tar.gz
            docker load < /tmp/frontend-image.tar.gz
            
            # Update environment variables
            cat > /opt/gestao-financeira/.env << ENVEOF
            DB_HOST=${{ secrets.DB_HOST }}
            DB_NAME=${{ secrets.DB_NAME }}
            DB_USERNAME=${{ secrets.DB_USERNAME }}
            DB_PASSWORD=${{ secrets.DB_PASSWORD }}
            JWT_SECRET=${{ secrets.JWT_SECRET }}
            ENCRYPTION_KEY=${{ secrets.ENCRYPTION_KEY }}
            SPRING_PROFILES_ACTIVE=production
            SERVER_PORT=8080
            LOGGING_LEVEL_ROOT=INFO
            ENVEOF
            
            # Deploy with docker-compose
            cd /opt/gestao-financeira/app
            docker-compose -f docker-compose.prod.yml down
            docker-compose -f docker-compose.prod.yml up -d
            
            # Cleanup
            rm -f /tmp/backend-image.tar.gz /tmp/frontend-image.tar.gz
            docker image prune -f
          EOF
      
      - name: Health check
        run: |
          sleep 30
          for i in {1..10}; do
            if curl -f http://${{ secrets.OCI_INSTANCE_IP }}:8080/actuator/health; then
              echo "Application is healthy!"
              exit 0
            fi
            echo "Waiting for application to be ready... ($i/10)"
            sleep 10
          done
          echo "Application failed to start!"
          exit 1
      
      - name: Notify success
        if: success()
        run: |
          echo "✅ Deploy successful!"
          echo "Application URL: http://${{ secrets.OCI_INSTANCE_IP }}"
      
      - name: Notify failure
        if: failure()
        run: |
          echo "❌ Deploy failed!"
          ssh -i ~/.ssh/oci_key opc@${{ secrets.OCI_INSTANCE_IP }} \
            "docker-compose -f /opt/gestao-financeira/app/docker-compose.prod.yml logs --tail=100"
```

### 3. Workflow de Rollback

Crie `.github/workflows/rollback.yml`:

```yaml
name: Rollback Deployment

on:
  workflow_dispatch:
    inputs:
      version:
        description: 'Version to rollback to (commit SHA or tag)'
        required: true
        type: string

jobs:
  rollback:
    name: Rollback to Previous Version
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
        with:
          ref: ${{ inputs.version }}
      
      - name: Configure SSH
        run: |
          mkdir -p ~/.ssh
          echo "${{ secrets.OCI_SSH_PRIVATE_KEY }}" > ~/.ssh/oci_key
          chmod 600 ~/.ssh/oci_key
          ssh-keyscan -H ${{ secrets.OCI_INSTANCE_IP }} >> ~/.ssh/known_hosts
      
      - name: Rollback application
        run: |
          ssh -i ~/.ssh/oci_key opc@${{ secrets.OCI_INSTANCE_IP }} << 'EOF'
            cd /opt/gestao-financeira/app
            git fetch --all
            git checkout ${{ inputs.version }}
            docker-compose -f docker-compose.prod.yml down
            docker-compose -f docker-compose.prod.yml build
            docker-compose -f docker-compose.prod.yml up -d
          EOF
      
      - name: Health check
        run: |
          sleep 30
          curl -f http://${{ secrets.OCI_INSTANCE_IP }}:8080/actuator/health
```

## Configuração Avançada

### 1. Deploy por Ambiente

Crie workflows separados para staging e production:

**.github/workflows/deploy-staging.yml**:
```yaml
name: Deploy to Staging

on:
  push:
    branches: [ develop ]

# ... similar ao deploy.yml mas com secrets diferentes
```

**.github/workflows/deploy-production.yml**:
```yaml
name: Deploy to Production

on:
  push:
    tags:
      - 'v*'

# ... similar ao deploy.yml com aprovação manual
```

### 2. Aprovação Manual para Production

Adicione environment protection rules:

1. GitHub → Settings → Environments → New environment
2. Nome: `production`
3. Configure protection rules:
   - ✅ Required reviewers (adicione revisores)
   - ✅ Wait timer (opcional, ex: 5 minutos)

No workflow, adicione:
```yaml
deploy:
  environment: production
  # ... resto do job
```

### 3. Notificações

#### Slack

Adicione ao final de cada job:

```yaml
- name: Notify Slack
  if: always()
  uses: 8398a7/action-slack@v3
  with:
    status: ${{ job.status }}
    text: 'Deploy to OCI ${{ job.status }}'
    webhook_url: ${{ secrets.SLACK_WEBHOOK }}
```

#### Discord

```yaml
- name: Notify Discord
  if: always()
  uses: sarisia/actions-status-discord@v1
  with:
    webhook: ${{ secrets.DISCORD_WEBHOOK }}
    status: ${{ job.status }}
    title: "Deploy to OCI"
```

### 4. Testes de Carga

Adicione job de performance testing:

```yaml
performance-test:
  name: Performance Test
  needs: deploy
  runs-on: ubuntu-latest
  
  steps:
    - name: Run k6 load test
      uses: grafana/k6-action@v0.3.0
      with:
        filename: tests/load-test.js
      env:
        K6_CLOUD_TOKEN: ${{ secrets.K6_CLOUD_TOKEN }}
```

### 5. Análise de Segurança

```yaml
security-scan:
  name: Security Scan
  runs-on: ubuntu-latest
  
  steps:
    - uses: actions/checkout@v3
    
    - name: Run Trivy vulnerability scanner
      uses: aquasecurity/trivy-action@master
      with:
        scan-type: 'fs'
        scan-ref: '.'
        format: 'sarif'
        output: 'trivy-results.sarif'
    
    - name: Upload Trivy results to GitHub Security
      uses: github/codeql-action/upload-sarif@v2
      with:
        sarif_file: 'trivy-results.sarif'
```

## Monitoramento de Deploys

### 1. Deploy Tracking

Adicione tags às releases:

```yaml
- name: Create release
  uses: actions/create-release@v1
  env:
    GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
  with:
    tag_name: ${{ github.ref }}
    release_name: Release ${{ github.ref }}
    body: |
      Changes in this release:
      ${{ github.event.head_commit.message }}
```

### 2. Métricas de Deploy

Use GitHub Deployments API:

```yaml
- name: Create deployment
  uses: chrnorm/deployment-action@v2
  with:
    token: ${{ secrets.GITHUB_TOKEN }}
    environment: production
    ref: ${{ github.sha }}
```

## Troubleshooting CI/CD

### Deploy falha no health check

```bash
# Verificar logs
ssh -i ~/.ssh/oci_key opc@<IP> \
  "docker-compose -f /opt/gestao-financeira/app/docker-compose.prod.yml logs"

# Verificar status dos containers
ssh -i ~/.ssh/oci_key opc@<IP> \
  "docker-compose -f /opt/gestao-financeira/app/docker-compose.prod.yml ps"
```

### Problemas de SSH

```bash
# Testar conexão SSH
ssh -i ~/.ssh/oci_key -v opc@<IP>

# Verificar fingerprint
ssh-keyscan <IP>
```

### Imagens Docker muito grandes

Otimize os Dockerfiles:

```dockerfile
# Use multi-stage builds
FROM maven:3.8-openjdk-17 AS build
# ... build stage

FROM openjdk:17-jre-slim
# ... runtime stage (menor)
```

## Boas Práticas

### 1. Versionamento

Use semantic versioning:
- `v1.0.0` - Major release
- `v1.1.0` - Minor release (features)
- `v1.1.1` - Patch release (bugfixes)

### 2. Branches

Estratégia GitFlow:
- `main` - Production
- `develop` - Staging
- `feature/*` - Features
- `hotfix/*` - Hotfixes

### 3. Testes

Sempre rode testes antes de deploy:
- ✅ Unit tests
- ✅ Integration tests
- ✅ Property-based tests
- ✅ E2E tests (opcional)

### 4. Rollback

Sempre tenha um plano de rollback:
- Tag cada release
- Mantenha imagens antigas
- Teste o processo de rollback

### 5. Secrets

Nunca commite secrets:
- Use GitHub Secrets
- Rotacione regularmente
- Use diferentes secrets por ambiente

## Custos

GitHub Actions:
- ✅ **Grátis** para repositórios públicos
- ✅ **2000 minutos/mês grátis** para privados
- Minutos adicionais: ~$0.008/minuto

Este pipeline usa ~10-15 minutos por deploy.

## Referências

- [GitHub Actions Docs](https://docs.github.com/en/actions)
- [Docker Build Push Action](https://github.com/docker/build-push-action)
- [SSH Action](https://github.com/appleboy/ssh-action)
- [Deployment Action](https://github.com/chrnorm/deployment-action)

---

Última atualização: Janeiro 2026
