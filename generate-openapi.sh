#!/bin/bash

echo "🚀 Генерация OpenAPI 3.0 документации..."

# Запускаем приложение
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=8081" &
APP_PID=$!

echo "⏳ Ожидание запуска приложения..."
sleep 30

# Скачиваем OpenAPI 3.0 спецификацию
echo "📥 Загрузка OpenAPI 3.0 спецификации..."
curl -s http://localhost:8081/v3/api-docs -o docs/openapi.json

# Конвертируем в YAML
echo "🔄 Конвертация в YAML..."
python3 -c "
import json, yaml
with open('docs/openapi.json', 'r', encoding='utf-8') as f:
    data = json.load(f)
with open('docs/openapi.yaml', 'w', encoding='utf-8') as f:
    yaml.dump(data, f, allow_unicode=True, sort_keys=False, default_flow_style=False)
"

# Останавливаем приложение
kill $APP_PID

echo "✅ Документация сгенерирована!"
echo "📄 JSON: docs/openapi.json"
echo "📄 YAML: docs/openapi.yaml"