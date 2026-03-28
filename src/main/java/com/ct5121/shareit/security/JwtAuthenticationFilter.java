package com.ct5121.shareit.security;

import com.ct5121.shareit.exception.JwtAuthenticationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String BEARER_PREFIX = "Bearer ";
    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/error",
            "/auth/register",
            "/auth/login",
            "/swagger-ui.html",
            "/v3/api-docs",
            "/actuator/health",
            "/actuator/info"
    );

    private final JwtService jwtService;
    private final ShareItUserDetailsService userDetailsService;

    @Qualifier("handlerExceptionResolver")
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return HttpMethod.OPTIONS.matches(request.getMethod()) || isPublicEndpoint(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authorizationHeader == null || !hasBearerPrefix(authorizationHeader)) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
            if (token.isEmpty()) {
                throw new JwtAuthenticationException("JWT token is missing");
            }
            String email = jwtService.extractEmail(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                ShareItUserDetails userDetails;
                try {
                    userDetails = (ShareItUserDetails) userDetailsService.loadUserByUsername(email);
                } catch (UsernameNotFoundException ex) {
                    throw new JwtAuthenticationException("Invalid JWT token", ex);
                }

                if (!jwtService.isTokenValid(token, userDetails)) {
                    throw new JwtAuthenticationException("Invalid JWT token");
                }

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }

            filterChain.doFilter(request, response);
        } catch (JwtAuthenticationException ex) {
            SecurityContextHolder.clearContext();
            handlerExceptionResolver.resolveException(request, response, null, ex);
        }
    }

    private boolean hasBearerPrefix(String authorizationHeader) {
        return authorizationHeader.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length());
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        if (PUBLIC_PATHS.contains(path)) {
            return true;
        }
        if (path.startsWith("/swagger-ui/") || path.startsWith("/v3/api-docs/")) {
            return true;
        }
        if (path.equals("/actuator/health") || path.startsWith("/actuator/health/")) {
            return true;
        }
        return HttpMethod.GET.matches(request.getMethod()) && "/items/search".equals(path);
    }
}
