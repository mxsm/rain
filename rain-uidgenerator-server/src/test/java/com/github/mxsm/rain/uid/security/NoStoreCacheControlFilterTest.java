package com.github.mxsm.rain.uid.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class NoStoreCacheControlFilterTest {

    @Test
    void addsNoStoreHeadersForApiRequests() throws Exception {
        NoStoreCacheControlFilter filter = new NoStoreCacheControlFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/snowflake/uid");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals("no-store, no-cache, max-age=0", response.getHeader(HttpHeaders.CACHE_CONTROL));
        assertEquals("no-cache", response.getHeader(HttpHeaders.PRAGMA));
    }
}
