# 🚀 Deployment Guide

## Overview

This document provides instructions for deploying the MHSA Backend to different environments:
- **Development** (Local Docker)
- **Staging** (Cloud VPS)
- **Production** (Kubernetes)

---

## Development Environment (Local)

### Prerequisites
- Docker Desktop installed
- 4GB+ available RAM
- 2GB+ available disk space

### Deployment Steps

```powershell
# 1. Navigate to project directory
cd d:\StudyFiles\Thesis\thesis-backend

# 2. Start all services
docker-compose up -d

# 3. Verify all services are healthy
docker-compose ps
# Expected: All containers showing "healthy"

# 4. Run database migrations
# (Automatically done by Flyway on startup)

# 5. Test the API
curl http://localhost:8080/health
```

### Configuration Files
- `docker-compose.yml` - Service definitions
- `.env` (optional) - Environment variables
- `application-docker.properties` - Spring Boot config

### Logs
```powershell
# View all logs
docker-compose logs

# Follow specific service
docker-compose logs -f auth-service

# View errors only
docker-compose logs | Select-String "ERROR"
```

---

## Staging Environment (Cloud VPS)

### Prerequisites
- Ubuntu 20.04+ LTS
- Docker & Docker Compose installed
- Git installed
- Domain name (e.g., api.staging.mhsa.com)
- SSL certificate (Let's Encrypt)

### Setup Steps

#### 1. Deploy on VPS

```bash
#!/bin/bash
# setup-staging.sh

# Update system
sudo apt update && sudo apt upgrade -y

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.10.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Clone repository
git clone https://github.com/yourorg/thesis-backend.git
cd thesis-backend

# Create .env file
cat > .env << EOF
# Database
POSTGRES_USER=postgres
POSTGRES_PASSWORD=$(openssl rand -base64 32)

# JWT
JWT_EXPIRATION_MS=3600000
JWT_ISSUER=mhsa.backend
JWT_AUDIENCE=mhsa-api

# Service URLs
SERVICE_AUTH_URL=http://auth-service:8081
SERVICE_TRACKING_URL=http://tracking-service:8083
SERVICE_AI_URL=http://ai-service:8082
EOF

# Start services
docker-compose up -d

# Verify
docker-compose ps
```

#### 2. Setup Nginx Reverse Proxy (Cloudflare/Nginx)

```nginx
# /etc/nginx/sites-available/api.staging.mhsa.com

upstream backend {
    server 127.0.0.1:8080 max_fails=3 fail_timeout=30s;
}

server {
    listen 80;
    listen [::]:80;
    server_name api.staging.mhsa.com;

    # Redirect to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    listen [::]:443 ssl http2;
    server_name api.staging.mhsa.com;

    # SSL certificates (Let's Encrypt)
    ssl_certificate /etc/letsencrypt/live/api.staging.mhsa.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.staging.mhsa.com/privkey.pem;

    # Security headers
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-Frame-Options "SAMEORIGIN" always;

    # Rate limiting
    limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;
    limit_req zone=api burst=20 nodelay;

    location / {
        proxy_pass http://backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # Health check endpoint
    location /health {
        proxy_pass http://backend;
        access_log off;
    }
}
```

#### 3. Enable & Restart Nginx

```bash
# Enable site
sudo ln -s /etc/nginx/sites-available/api.staging.mhsa.com /etc/nginx/sites-enabled/

# Test config
sudo nginx -t

# Restart
sudo systemctl restart nginx

# Enable on boot
sudo systemctl enable nginx docker
```

#### 4. Setup SSL (Let's Encrypt)

```bash
# Install Certbot
sudo apt install certbot python3-certbot-nginx -y

# Generate certificate
sudo certbot certonly --nginx -d api.staging.mhsa.com

# Auto-renew
sudo systemctl enable certbot.timer
```

#### 5. Backup Strategy

```bash
#!/bin/bash
# backup-staging.sh

BACKUP_DIR="/backups/mhsa"
DATE=$(date +%Y%m%d_%H%M%S)

# Create backup directory
mkdir -p $BACKUP_DIR

# Backup databases
for container in postgres-auth postgres-ai postgres-tracking; do
    docker exec $container pg_dump -U postgres > "$BACKUP_DIR/${container}_${DATE}.sql"
done

# Backup volumes (optional - if MinIO has important data)
# tar -czf "$BACKUP_DIR/volumes_${DATE}.tar.gz" /var/lib/docker/volumes/

# Upload to cloud storage (S3, Google Cloud, etc.)
# aws s3 cp "$BACKUP_DIR" s3://backups/ --recursive

echo "Backup completed: $BACKUP_DIR"
```

---

## Production Environment (Kubernetes)

### Prerequisites
- Kubernetes cluster (EKS, GKE, AKS, or self-hosted)
- kubectl installed
- Helm (optional, for templating)
- Container registry (Docker Hub, ECR, GCR)
- PostgreSQL managed service (RDS, Cloud SQL)
- Redis managed service (ElastiCache, Memorystore)
- RabbitMQ managed service or operator

### 1. Build & Push Images

```bash
#!/bin/bash
# build-images.sh

REGISTRY="docker.io/yourorg"
VERSION="1.0.0"

# Build images
docker-compose build

# Tag images
docker tag thesis-backend-auth-service:latest $REGISTRY/auth-service:$VERSION
docker tag thesis-backend-ai-service:latest $REGISTRY/ai-service:$VERSION
docker tag thesis-backend-tracking-service:latest $REGISTRY/tracking-service:$VERSION
docker tag thesis-backend-dashboard-service:latest $REGISTRY/dashboard-service:$VERSION

# Push to registry
docker push $REGISTRY/auth-service:$VERSION
docker push $REGISTRY/ai-service:$VERSION
docker push $REGISTRY/tracking-service:$VERSION
docker push $REGISTRY/dashboard-service:$VERSION
```

### 2. Kubernetes Manifests

```yaml
# k8s/namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: mhsa
---

# k8s/config.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: mhsa-config
  namespace: mhsa
data:
  JWT_EXPIRATION_MS: "3600000"
  JWT_ISSUER: "mhsa.backend"
  JWT_AUDIENCE: "mhsa-api"
  SERVICE_AUTH_URL: "http://auth-service:8081"
  SERVICE_TRACKING_URL: "http://tracking-service:8083"
---

# k8s/secrets.yaml (use sealed-secrets in production)
apiVersion: v1
kind: Secret
metadata:
  name: mhsa-secrets
  namespace: mhsa
type: Opaque
stringData:
  POSTGRES_PASSWORD: "$(openssl rand -base64 32)"
  GEMINI_API_KEY: "your-key-here"
---

# k8s/auth-service-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: auth-service
  namespace: mhsa
spec:
  replicas: 2
  selector:
    matchLabels:
      app: auth-service
  template:
    metadata:
      labels:
        app: auth-service
    spec:
      containers:
      - name: auth-service
        image: docker.io/yourorg/auth-service:1.0.0
        ports:
        - containerPort: 8081
        env:
        - name: SPRING_DATASOURCE_URL
          value: "jdbc:postgresql://postgres-rds:5432/auth_db"
        - name: SPRING_DATA_REDIS_HOST
          value: "redis-cluster"
        - name: SPRING_RABBITMQ_HOST
          value: "rabbitmq-cluster"
        envFrom:
        - configMapRef:
            name: mhsa-config
        - secretRef:
            name: mhsa-secrets
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8081
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 5
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
---

# k8s/auth-service-service.yaml
apiVersion: v1
kind: Service
metadata:
  name: auth-service
  namespace: mhsa
spec:
  type: ClusterIP
  selector:
    app: auth-service
  ports:
  - protocol: TCP
    port: 8081
    targetPort: 8081
---

# k8s/ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: mhsa-ingress
  namespace: mhsa
  annotations:
    cert-manager.io/cluster-issuer: letsencrypt-prod
    nginx.ingress.kubernetes.io/rate-limit: "100"
spec:
  ingressClassName: nginx
  tls:
  - hosts:
    - api.mhsa.com
    secretName: mhsa-tls
  rules:
  - host: api.mhsa.com
    http:
      paths:
      - path: /api/v1/auth
        pathType: Prefix
        backend:
          service:
            name: auth-service
            port:
              number: 8081
      - path: /api/v1/ai
        pathType: Prefix
        backend:
          service:
            name: ai-service
            port:
              number: 8082
      # ... other services
```

### 3. Deploy to Kubernetes

```bash
# Create namespace
kubectl create namespace mhsa

# Apply manifests
kubectl apply -f k8s/

# Verify deployment
kubectl get pods -n mhsa
kubectl get svc -n mhsa
kubectl get ingress -n mhsa

# Check logs
kubectl logs -n mhsa -l app=auth-service -f

# Scale services
kubectl scale deployment auth-service --replicas=3 -n mhsa
```

### 4. Monitoring & Logging

```bash
# Install Prometheus & Grafana
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm install prometheus prometheus-community/kube-prometheus-stack -n mhsa

# Install ELK Stack (or use CloudWatch, Stackdriver)
helm repo add elastic https://helm.elastic.co
helm install elasticsearch elastic/elasticsearch -n mhsa

# View pod logs
kubectl logs -n mhsa -l app=auth-service --tail=100 -f

# Port forward for local access
kubectl port-forward -n mhsa svc/prometheus 9090:9090
kubectl port-forward -n mhsa svc/grafana 3000:3000
```

---

## Database Migration Strategy

### Using Flyway (Automatic)
- Migrations run automatically on service startup
- Files: `src/main/resources/db/migration/V*.sql`
- No manual intervention needed

### Manual Migration
```sql
-- Connect to database
psql -U postgres -h postgres-auth -d auth_db -f migration.sql

-- Verify
SELECT * FROM flyway_schema_history;
```

---

## Backup & Recovery

### Automated Backup
```bash
# Using AWS RDS
aws rds create-db-snapshot \
  --db-instance-identifier mhsa-auth-db \
  --db-snapshot-identifier mhsa-auth-backup-$(date +%Y%m%d)

# Using Google Cloud SQL
gcloud sql backups create \
  --instance=mhsa-auth-db \
  --description="Daily backup"
```

### Disaster Recovery
```bash
# 1. Restore database from backup
# 2. Deploy new instances
# 3. Run health checks
# 4. Verify data integrity
# 5. Update DNS to new endpoint
```

---

## Environment-Specific Configurations

### Development
```properties
spring.jpa.hibernate.ddl-auto=update
logging.level.root=INFO
logging.level.com.mhsa.backend=DEBUG
server.port=8081
```

### Staging
```properties
spring.jpa.hibernate.ddl-auto=validate
logging.level.root=INFO
logging.level.com.mhsa.backend=INFO
server.port=8081
server.compression.enabled=true
```

### Production
```properties
spring.jpa.hibernate.ddl-auto=validate
logging.level.root=WARN
logging.level.com.mhsa.backend=INFO
server.port=8081
server.compression.enabled=true
server.error.include-message=never
server.error.include-stacktrace=never
```

---

## Checklist

### Pre-Deployment
- [ ] Code review completed
- [ ] All tests passing
- [ ] Database migrations tested
- [ ] Security scan passed
- [ ] Performance benchmarked
- [ ] Documentation updated

### Deployment
- [ ] Backup created
- [ ] Services deployed
- [ ] Health checks passing
- [ ] Integration tests passed
- [ ] Monitoring enabled
- [ ] Logs being collected

### Post-Deployment
- [ ] Monitor error rates
- [ ] Check performance metrics
- [ ] Verify user access
- [ ] Review logs for errors
- [ ] Collect feedback

---

## Rollback Procedure

### Kubernetes
```bash
# Check rollout history
kubectl rollout history deployment/auth-service -n mhsa

# Rollback to previous version
kubectl rollout undo deployment/auth-service -n mhsa

# Rollback to specific revision
kubectl rollout undo deployment/auth-service --to-revision=2 -n mhsa
```

### Docker Compose
```bash
# Stop current services
docker-compose down

# Checkout previous version
git checkout v1.0.0

# Restart
docker-compose up -d
```

---

## Support & Troubleshooting

For deployment issues:
1. Check service logs
2. Verify environment variables
3. Check database connectivity
4. Review firewall rules
5. Monitor resource usage
6. Check SSL certificates

---

## References
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Spring Boot Deployment](https://spring.io/guides/gs/spring-boot-docker/)
