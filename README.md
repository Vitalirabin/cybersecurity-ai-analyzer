# Cybersecurity AI Analyzer

**Cybersecurity AI Analyzer** — это кроссплатформенное Kotlin Multiplatform (KMP) приложение для анализа системных логов и исходного кода на наличие уязвимостей и аномалий с использованием искусственного интеллекта.

Приложение поддерживает **гибридную интеллектуальную маршрутизацию**, распределяя запросы между локальным ИИ-сервером (например, Ollama или любой другой совместимый сервис) для быстрого анализа небольших фрагментов и внешними облачными провайдерами (OpenRouter, OpenAI, Anthropic и др.) для более тяжелых задач, с возможностью автоматического отказоустойчивого резервирования (fallback).

---

## Архитектура проекта и модули

Проект разделен на следующие кроссплатформенные таргеты и модули:

* **`:shared` (Kotlin Multiplatform Module)**:
  - Содержит общую бизнес-логику, доменные модели, usecase-классы, репозитории и сетевые клиенты.
  - Компилируется под все целевые платформы.
* **`:androidApp` (Android Target)**:
  - Мобильное клиентское приложение под Android, использующее общую логику из `:shared`.
* **`:desktopApp` (Desktop Target / JVM)**:
  - Десктопное клиентское приложение (Windows, macOS, Linux) на базе Compose Desktop.
* **`:webApp` (Web Target / Wasm-JS)**:
  - Браузерное клиентское приложение на базе Compose Multiplatform for Wasm, взаимодействующее с сервером по REST API.
* **`:server` (Ktor Backend Target / JVM)**:
  - Серверная часть приложения, инкапсулирующая взаимодействие с AI-провайдерами и обрабатывающая запросы клиентов (работает на порту `8070`).

---

## Основные возможности

1. **Гибридный ИИ-анализ (Local + Cloud)**:
   - Маленькие запросы (размером до порога `SMALL_LOG_THRESHOLD`) автоматически направляются на локальный эндпоинт.
   - Крупные запросы направляются к облачному провайдеру.
2. **Отказоустойчивость и резервное копирование (Fallback)**:
   - Если основной эндпоинт или модель недоступны, система автоматически переключается на альтернативные варианты из списка резервных моделей.
3. **Кроссплатформенность**:
   - Общая логика и UI переиспользуются между Android, Desktop, Web и Server.

---

## Запуск проекта

### 1. Запуск бэкенд-сервера
Сервер запускается на `http://localhost:8070`:
```bash
./gradlew :server:run
```

### 2. Запуск клиентов
* **Android**: Откройте проект в Android Studio и запустите модуль `:androidApp` на эмуляторе или устройстве.
* **Desktop (ПК)**:
  ```bash
  ./gradlew :desktopApp:run
  ```
* **Web (Браузер)**:
  ```bash
  ./gradlew :webApp:wasmJsBrowserDevelopmentRun
  ```

---

## Конфигурация проекта

Все параметры, эндпоинты, ключи и названия моделей вынесены в конфигурационные свойства сборки. **Вы можете легко заменить их на любые собственные:**

```properties
# =====================================================================
# AI Model & API Configuration
# Вы можете заменить любые ссылки, эндпоинты и модели на собственные
# (поддерживаются любые провайдеры и модели с OpenAI-совместимым API)
# =====================================================================

# API ключ для доступа к облачному провайдеру
OPENROUTER_API_KEY=your_api_key_here

# URL базового эндпоинта облачного API
CLOUD_BASE_URL=https://openrouter.ai/api/v1/chat/completions

# Название облачной модели
CLOUD_MODEL=google/gemma-4-31b-it:free

# URL локального API (например, Ollama или другой совместимый сервер)
OLLAMA_BASE_URL=http://localhost:11434/api/chat

# Название локальной модели
OLLAMA_MODEL=qwen2.5-coder:7b

# Порог размера лога в символах (разделение между локальным и облачным анализом)
SMALL_LOG_THRESHOLD=500

# Список резервных моделей через запятую
FALLBACK_MODELS=google/gemma-4-31b-it:free,thinkingmachines/inkling-small:free,nvidia/nemotron-3-ultra-550b-a55b:free
```

---

## Взаимодействие с серверной частью через API

Серверная часть предоставляет REST API эндпоинт для анализа логов на `http://localhost:8070/api/analyze-logs`.

### Эндпоинт: `POST /api/analyze-logs`

* **Content-Type**: `application/json`
* **Успешный ответ (`200 OK`)**:
  ```json
  {
    "vulnerabilities": [
      {
        "description": "Potential SQL Injection (SQLi) pattern detected in query logs",
        "risk": "High"
      }
    ],
    "error": null
  }
  ```

---

### Примеры взаимодействия с API

Вы можете использовать любой удобный HTTP-клиент:

#### 1. cURL (Terminal)
```bash
curl -X POST http://localhost:8070/api/analyze-logs \
  -H "Content-Type: application/json" \
  -d '{
    "logs": "SELECT * FROM users WHERE name = '\''admin'\'' --"
  }'
```

#### 2. PowerShell (Windows)
```powershell
$body = @{
    logs = "SELECT * FROM users WHERE id = 1 OR 1=1;"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8070/api/analyze-logs" `
    -Method Post `
    -Body $body `
    -ContentType "application/json"
```

#### 3. Python (requests)
```python
import requests

url = "http://localhost:8070/api/analyze-logs"
payload = {
    "logs": "GET /admin/config.php?file=../../etc/passwd HTTP/1.1"
}

response = requests.post(url, json=payload)
print(response.json())
```
