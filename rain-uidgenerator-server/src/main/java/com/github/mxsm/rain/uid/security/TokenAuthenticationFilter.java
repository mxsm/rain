package com.github.mxsm.rain.uid.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mxsm.rain.uid.config.UidSecurityProperties;
import com.github.mxsm.rain.uid.core.common.ErrorCode;
import com.github.mxsm.rain.uid.core.common.Result;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";

    private final UidSecurityProperties properties;

    private final ObjectMapper objectMapper;

    public TokenAuthenticationFilter(UidSecurityProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !properties.isEnabled()
            || path.startsWith("/actuator/health")
            || path.startsWith("/actuator/info");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        Set<String> allowedTokens = new HashSet<>(properties.getTokens());
        String token = resolveToken(request);
        if (allowedTokens.isEmpty() || !StringUtils.hasText(token) || !allowedTokens.contains(token)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getOutputStream(),
                Result.buildError(null, ErrorCode.UNAUTHORIZED, "Unauthorized"));
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            return authorization.substring("Bearer ".length());
        }
        return request.getHeader(API_KEY_HEADER);
    }
}
