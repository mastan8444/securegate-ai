# Build and Merge Script for SecureGate AI

# 1. Clean old static resource directory in backend
Write-Host "Cleaning old static resources..." -ForegroundColor Cyan
$staticDir = Join-Path (Get-Item .).FullName "backend/src/main/resources/static"
if (Test-Path $staticDir) {
    Remove-Item -Recurse -Force $staticDir
}
New-Item -ItemType Directory -Path $staticDir -Force | Out-Null

# 2. Build Frontend
Write-Host "Building React Frontend..." -ForegroundColor Cyan
Push-Location frontend
npm run build
Pop-Location

# 3. Copy Frontend Build files to Spring Boot static folder
Write-Host "Merging frontend build into Spring Boot..." -ForegroundColor Cyan
$distDir = Join-Path (Get-Item .).FullName "frontend/dist"
Copy-Item -Path "$distDir\*" -Destination $staticDir -Recurse -Force

# 4. Package Backend
Write-Host "Packaging Backend into JAR..." -ForegroundColor Cyan
Push-Location backend
.\mvnw clean package -DskipTests
Pop-Location

Write-Host "Build complete! The runnable jar is available at backend/target/backend-0.0.1-SNAPSHOT.jar" -ForegroundColor Green
