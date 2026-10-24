# Redis-Backed Rate Limiting

## Overview

This document describes the implementation of rate limiting for the User Service route using Spring Cloud Gateway and Redis.

The rate limiter controls incoming request rates at the API Gateway, preventing excessive requests from reaching the downstream service.

## Architecture

Client → Spring Cloud Gateway → Rate Limiter → User Service

Redis maintains the rate-limiting state used by the gateway.

## Technology Stack

- Spring Cloud Gateway
- Spring Boot
- Redis
- Docker with Colima for local development

## Configuration

The `RequestRateLimiter` filter is configured specifically for the User Service route in `application.yml`.

Key properties:

| Property | Description |
|---|---|
| `key-resolver` | Identifies the key used to group requests |
| `replenishRate` | Rate at which tokens are replenished |
| `burstCapacity` | Maximum token bucket capacity |
| `requestedTokens` | Tokens consumed by each request |

The current configuration is intended to allow approximately two requests per minute per resolved key.

**Note:** Spring Cloud Gateway uses a token-bucket algorithm. This configuration does not enforce a strict rolling 60-second request window.

## Redis Setup

Redis runs locally in a Docker container managed by Colima.

Start Colima:

```bash
colima start
```

Start Redis:

```bash
docker run -d \
  --name redis \
  -p 6379:6379 \
  redis:7
```

Verify Redis connectivity:

```bash
docker exec -it redis redis-cli ping
```

Expected response:

```text
PONG
```

When the gateway runs directly on the host machine, Spring Boot's default Redis connection settings use `localhost:6379`, provided no other configuration overrides them.

## Request Processing

1. A client sends a request to the API Gateway.
2. The gateway resolves the request's rate-limit key.
3. The Redis rate limiter checks token availability.
4. Allowed requests are forwarded to the User Service.
5. Requests exceeding the available token budget are rejected with HTTP `429 Too Many Requests`.

## Scope and Limitations

- Rate limiting currently applies only to the User Service route.
- Other gateway routes are unaffected by this route-specific filter.
- The effective limit depends on the `KeyResolver` and rate-limiter configuration.
- The built-in Redis rate limiter uses token-bucket semantics rather than a strict rolling time window.

## Future Enhancements

- Apply rate limiting across all gateway routes where required.
- Introduce route-specific policies for different services.
- Add monitoring for rejected requests and Redis connectivity.
- Evaluate stricter time-window policies if required by the application's use case.