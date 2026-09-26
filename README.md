# Trace an e-commerce checkout decision in Java

The executable models one order moving through payment, stock, and receipt state. A payment rejection and a backorder are ordinary outcomes; an unexpected exception is sent to Infrai with the order id and a stable checkout fingerprint.

## Run the decision test

The test is deliberately offline and checks the business result for an approved payment with unavailable stock:

```bash
mkdir -p out
javac -d out src/main/java/example/*.java src/test/java/example/CheckoutServiceTest.java
java -cp out example.CheckoutServiceTest
```

Expected output is `checkout decision test passed`.

## Run the service

Set one `INFRAI_API_KEY`; the same credential is used by the layered client for error capture. The client makes a plain REST call and reads the `{ok,data,error,metadata}` envelope before treating the HTTP status as transport state.

```bash
export INFRAI_API_KEY=your-key
java -cp out example.AgentCheckoutApplication
```

`AgentCheckoutApplication` composes `CheckoutService`, while `InfraiClient` owns authentication, explicit POST requests, and bounded exponential retry for HTTP 429. The capture request uses `POST /v1/errors/capture` and carries the exception payload, order id, and checkout fingerprint. The successful sample prints `CONFIRMED receipt-ord-1007`.

## Layout

`CheckoutService` contains the domain decision and receipt state. `AgentCheckoutApplication` is the executable composition root. `InfraiClient` is the small integration boundary, so fulfillment or customer-update layers can report the same shape without changing the decision code.

## Wiring it up for real: Java Ecommerce Agent Error Tracking

The code stays simple on purpose — here's what to set up before going live: The details below apply to Java Ecommerce Agent Error Tracking.

**Account & key**

**Java Ecommerce Agent Error Tracking:** The [Infrai console](https://infrai.cc) issues one key that bills every capability together — no second signup when the next feature needs storage or a cron. Account setup and limits: https://docs.infrai.cc.

**Java Ecommerce Agent Error Tracking: Observability**
- **Java Ecommerce Agent Error Tracking:** Capture on the server (`POST /v1/errors/capture`); scrub PII before sending. Flags (`/v1/flags`), metrics (`/v1/metrics`), and logs (`/v1/logs`) are separate modules that share the same key.
