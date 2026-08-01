[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
Write-Host "Starting Docker Desktop..." -ForegroundColor Cyan

# --- AUTO-DETECT DOCKER ---
$dockerProcessName = "Docker Desktop"
$commonPaths = @(
    "C:\Program Files\Docker\Docker\Docker Desktop.exe",
    "D:\Program Files\Docker\Docker\Docker Desktop.exe",
    "E:\Program Files\Docker\Docker\Docker Desktop.exe"
)

# 1. Check if Docker is already running
if (Get-Process $dockerProcessName -ErrorAction SilentlyContinue) {
    Write-Host "Docker Desktop is already running." -ForegroundColor Green
} else {
    # 2. If not running, try to find and start it
    $started = $false
    
    # Method 1: Check common installation paths
    foreach ($path in $commonPaths) {
        if (Test-Path $path) {
            Start-Process $path
            $started = $true
            break
        }
    }

    # Method 2: Try generic command (depends on system path)
    if (-not $started) {
        try {
            Start-Process "Docker Desktop"
            $started = $true
        } catch {
            # Failed to start
        }
    }

    # Fallback: Tell user to open it manually
    if (-not $started) {
        Write-Host "Could not automatically find Docker Desktop!" -ForegroundColor Yellow
        Write-Host "Please open Docker Desktop manually and wait a moment..." -ForegroundColor White
    }
}

# --- WAIT LOOP ---
Write-Host "Waiting for Docker Engine to initialize (this may take 1-2 minutes)..." -ForegroundColor Yellow
$timeout = 0
do {
    Start-Sleep -Seconds 3
    $dockerInfo = docker info 2>&1
    if ($LASTEXITCODE -eq 0) { break } # Exit loop immediately if docker is ready

    $timeout++
    if ($timeout -gt 40) { # Wait max 2 minutes (40 * 3s)
        Write-Error "Docker took too long to start or is not running. Please check manually."
        exit
    }
} until ($LASTEXITCODE -eq 0)

Write-Host "Docker is ready!" -ForegroundColor Green

# --- START CONTAINERS ---
Write-Host "Starting Services (Postgres, Redis, RabbitMQ & pgAdmin)..." -ForegroundColor Cyan
docker-compose up -d

Write-Host "Environment is ready! You can run 'mvnw spring-boot:run' now." -ForegroundColor Green