Write-Host "Генерация OpenAPI документации..." -ForegroundColor Green

# 1. Запускаем приложение
Write-Host "Запуск приложения..."
Start-Process -NoNewWindow -FilePath "mvn" -ArgumentList "spring-boot:run", "-Dspring-boot.run.jvmArguments='-Dspring.profiles.active=docs'"
Start-Sleep -Seconds 5

# 2. Ждем запуска
Write-Host "Ожидание запуска приложения..."
Start-Sleep -Seconds 25

# 3. Загружаем спецификацию
Write-Host "Загрузка OpenAPI спецификации..."
Invoke-WebRequest -Uri "http://localhost:8080/v3/api-docs" -OutFile "docs/openapi.json"

# 4. Конвертируем JSON в YAML
Write-Host "Конвертация JSON в YAML..."
if (Get-Command python -ErrorAction SilentlyContinue) {
    python -c "
import json, yaml
with open('docs/openapi.json', 'r', encoding='utf-8') as f:
    data = json.load(f)
with open('docs/openapi.yaml', 'w', encoding='utf-8') as f:
    yaml.dump(data, f, allow_unicode=True, sort_keys=False)
"
} else {
    Write-Host "Установите Python для конвертации YAML" -ForegroundColor Red
}

# 5. Удаляем временный файл
Remove-Item "docs/openapi.json"

Write-Host "✅ Документация сгенерирована: docs/openapi.yaml" -ForegroundColor Green