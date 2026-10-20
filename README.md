# TradeDesk – Paper Trading API

A Spring Boot 3 / Java 17 backend for equity paper trading. It provides per-user cash accounts, market/limit/stop-loss orders, pre-trade checks, an auditable cash ledger, a portfolio with unrealized P&L, and live quote broadcasts over STOMP.

## Run locally

Requires Java 17+, Maven 3.8+, Docker, and Docker Compose.

```powershell
docker compose up --build
```

The API listens on `http://localhost:8080`; PostgreSQL and Redis are also exposed on their default ports. To run the API outside Docker, start PostgreSQL and Redis, then run `mvn spring-boot:run`.

Set `JWT_SECRET` to a private, random value of at least 32 bytes before deploying. The checked-in default is only for local development. Authentication expects an HS256 JWT with a `sub` claim and a `roles` claim (for example, `["TRADER"]` or `["ADMIN"]`). Public token issuance is intentionally not part of this API.

## API

All REST routes except `/actuator/health` require a bearer token. The quote WebSocket handshake is public because market quotes are public data. Account and portfolio data are scoped to the token subject.

| Method | Route | Purpose |
| --- | --- | --- |
| `POST` | `/api/v1/account` | Create an account with a $100,000 paper balance |
| `GET` | `/api/v1/account` | Read cash balance |
| `POST` | `/api/v1/orders` | Submit an order; requires `Idempotency-Key` |
| `GET` | `/api/v1/orders` | List the caller's orders |
| `DELETE` | `/api/v1/orders/{orderId}` | Cancel an open order |
| `GET` | `/api/v1/ledger` | Read the caller's cash ledger |
| `GET` | `/api/v1/portfolio` | Read positions, equity, and unrealized P&L |
| `GET` | `/api/v1/quotes/{symbol}` | Read a quote |
| `PUT` | `/api/v1/quotes/{symbol}` | Publish a quote (ADMIN role) |
| STOMP | `/ws`, `/topic/quotes/{symbol}` | Subscribe to live quote updates |

Example order body:

```json
{
  "symbol": "AAPL",
  "side": "BUY",
  "type": "LIMIT",
  "quantity": 10,
  "limitPrice": 190.00,
  "stopPrice": null
}
```

Market orders execute immediately at the latest quote. Limit orders execute when the quote reaches or improves on their limit; stop-loss orders trigger when the stop price is crossed. Orders that cannot pass risk checks when triggered are cancelled rather than left indefinitely open. The risk rules are individual Spring beans, so new pre-trade checks can be added without modifying the order service.

## Tests

```powershell
mvn test
```

The test suite uses JUnit 5 and Mockito; Testcontainers dependencies are included for integration tests that need PostgreSQL.
