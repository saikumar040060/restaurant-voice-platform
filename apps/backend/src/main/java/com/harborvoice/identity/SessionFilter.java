package com.harborvoice.identity;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

final class SessionFilter extends OncePerRequestFilter {
    private final SessionService sessions;

    SessionFilter(SessionService sessions) {
        this.sessions = sessions;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        UUID correlation = UUID.randomUUID();
        request.setAttribute("correlationId", correlation);
        response.setHeader("X-Correlation-ID", correlation.toString());
        response.setHeader("Cache-Control", "no-store");
        MDC.put("correlationId", correlation.toString());
        try {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                try {
                    sessions.authenticate(header.substring(7)).ifPresent(actor -> {
                        var context = SecurityContextHolder.createEmptyContext();
                        context.setAuthentication(new UsernamePasswordAuthenticationToken(actor, null, List.of()));
                        SecurityContextHolder.setContext(context);
                    });
                } catch (DataAccessException unavailable) {
                    response.setStatus(503);
                    return;
                }
            }
            chain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
            MDC.remove("correlationId");
        }
    }
}
