# Rain API

Base path: `/api/v1`

All API responses use the same envelope:

```json
{
  "data": 123,
  "status": "SUCCESS",
  "code": "SUCCESS",
  "msg": "SUCCESS"
}
```

Errors use the same envelope with a stable `code` and an HTTP status. Common codes:

- `VALIDATION_ERROR`
- `UNAUTHORIZED`
- `BIZ_CODE_NOT_FOUND`
- `SEGMENT_ALLOCATE_FAILED`
- `UID_UNAVAILABLE`
- `CLOCK_MOVED_BACKWARDS`
- `WORKER_ID_UNAVAILABLE`
- `UPSTREAM_UNAVAILABLE`
- `INTERNAL_ERROR`

## Authentication

When `mxsm.uid.security.enabled=true`, send one of:

```http
Authorization: Bearer <token>
X-API-Key: <token>
```

Generation and read endpoints accept `mxsm.uid.security.tokens` or `mxsm.uid.security.admin-tokens`. The admin endpoint `POST /api/v1/segment/rg` accepts only `admin-tokens`.

`/actuator/health/**` and `/actuator/info` do not require a token.

## Segment APIs

### Register Biz Code

```http
POST /api/v1/segment/rg
Content-Type: application/json
Authorization: Bearer <admin-token>
```

```json
{
  "bizCode": "orders",
  "step": 1000
}
```

`bizCode` is limited to 128 characters. `step` must be positive.

### Generate Segment UID

```http
POST /api/v1/segment/uid/{bizCode}
Authorization: Bearer <token>
```

Legacy compatibility:

```http
GET /api/v1/segment/uid/{bizCode}
```

The GET endpoint is deprecated because ID generation has side effects. All `/api/v1/**` responses include `Cache-Control: no-store`.

### Allocate Segment List

```http
GET /api/v1/segment/list/{bizCode}?segmentNum=16
Authorization: Bearer <token>
```

This endpoint is used by the Java SDK local segment cache. `segmentNum` must be between `1` and `10000`.

## Snowflake APIs

### Generate Snowflake UID

```http
POST /api/v1/snowflake/uid
Authorization: Bearer <token>
```

Legacy compatibility:

```http
GET /api/v1/snowflake/uid
```

The GET endpoint is deprecated.

### Parse Snowflake UID

```http
GET /api/v1/snowflake/parse/{uid}
Authorization: Bearer <token>
```

Returns timestamp, machine ID, and sequence.
