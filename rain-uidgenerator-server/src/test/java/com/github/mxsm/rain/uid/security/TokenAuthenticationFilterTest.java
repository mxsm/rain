package com.github.mxsm.rain.uid.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mxsm.rain.uid.config.UidSecurityProperties;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class TokenAuthenticationFilterTest {

    @Test
    void skipsAuthenticationWhenDisabled() throws Exception {
        UidSecurityProperties properties = new UidSecurityProperties();
        properties.setEnabled(false);
        TokenAuthenticationFilter filter = new TokenAuthenticationFilter(properties, new ObjectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/snowflake/uid");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(200, response.getStatus());
    }

    @Test
    void rejectsMissingTokenWhenEnabled() throws Exception {
        UidSecurityProperties properties = new UidSecurityProperties();
        properties.setEnabled(true);
        properties.setTokens(List.of("secret"));
        TokenAuthenticationFilter filter = new TokenAuthenticationFilter(properties, new ObjectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/snowflake/uid");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(401, response.getStatus());
    }

    @Test
    void acceptsBearerTokenWhenEnabled() throws Exception {
        UidSecurityProperties properties = new UidSecurityProperties();
        properties.setEnabled(true);
        properties.setTokens(List.of("secret"));
        TokenAuthenticationFilter filter = new TokenAuthenticationFilter(properties, new ObjectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/snowflake/uid");
        request.addHeader("Authorization", "Bearer secret");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(200, response.getStatus());
    }
}
