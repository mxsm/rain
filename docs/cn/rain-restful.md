# Rain REST API

基础路径：`/api/v1`

所有接口返回统一结构：

```json
{
  "data": 123,
  "status": "SUCCESS",
  "code": "SUCCESS",
  "msg": "SUCCESS"
}
```

错误也使用同样结构，`code` 为稳定错误码，例如 `VALIDATION_ERROR`、`UNAUTHORIZED`、`BIZ_CODE_NOT_FOUND`、`UID_UNAVAILABLE`、`CLOCK_MOVED_BACKWARDS`。

## 鉴权

生产开启：

```yaml
mxsm.uid.security.enabled: true
mxsm.uid.security.tokens: ${RAIN_UID_TOKENS}
mxsm.uid.security.admin-tokens: ${RAIN_UID_ADMIN_TOKENS}
```

请求头：

```http
Authorization: Bearer <token>
```

或：

```http
X-API-Key: <token>
```

普通生成接口接受 `tokens` 或 `admin-tokens`。管理接口 `POST /api/v1/segment/rg` 只接受 `admin-tokens`。

## Segment

注册业务编码：

```bash
curl -X POST http://localhost:8080/api/v1/segment/rg \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer ${RAIN_ADMIN_TOKEN}" \
  -d '{"bizCode":"orders","step":1000}'
```

生成 UID：

```bash
curl -X POST http://localhost:8080/api/v1/segment/uid/orders \
  -H "Authorization: Bearer ${RAIN_TOKEN}"
```

批量获取号段，供 SDK 本地缓存使用：

```bash
curl 'http://localhost:8080/api/v1/segment/list/orders?segmentNum=16' \
  -H "Authorization: Bearer ${RAIN_TOKEN}"
```

## Snowflake

生成 UID：

```bash
curl -X POST http://localhost:8080/api/v1/snowflake/uid \
  -H "Authorization: Bearer ${RAIN_TOKEN}"
```

解析 UID：

```bash
curl http://localhost:8080/api/v1/snowflake/parse/{uid} \
  -H "Authorization: Bearer ${RAIN_TOKEN}"
```

## GET 兼容接口

旧版 GET 生成接口仍保留，但已废弃。生成 UID 是有副作用的操作，新接入方应使用 POST。

所有 `/api/v1/**` 响应都会带 `Cache-Control: no-store`。
