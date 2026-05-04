# Rain Performance Checks

Run these checks against a deployed service with a registered `bizCode`.

```bash
export RAIN_BASE_URL=http://localhost:8080
export RAIN_TOKEN=change-me
export RAIN_BIZ_CODE=mxsm

wrk -t4 -c128 -d60s -H "Authorization: Bearer ${RAIN_TOKEN}" \
  "${RAIN_BASE_URL}/api/v1/snowflake/uid"

wrk -t4 -c128 -d60s -H "Authorization: Bearer ${RAIN_TOKEN}" \
  "${RAIN_BASE_URL}/api/v1/segment/uid/${RAIN_BIZ_CODE}"
```

Balanced production target: cached segment and snowflake p99 latency below 10ms under expected service load.
