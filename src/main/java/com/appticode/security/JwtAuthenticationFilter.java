// src/main/java/com/appticode/security/JwtAuthenticationFilter.java
package com.appticode.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getServletPath();
        String method = request.getMethod();
        
        logger.debug("📝 Request: {} {}", method, path);
        
        // Skip authentication for public endpoints
        if (shouldSkipAuthentication(path)) {
            logger.debug("✅ Skipping authentication for public path: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        // Handle OPTIONS requests (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(method)) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        
        try {
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt)) {
                logger.debug("🔍 JWT present for: {}", path);
                
                if (tokenProvider.validateToken(jwt)) {
                    String username = tokenProvider.getUsernameFromToken(jwt);
                    logger.info("✅ Valid token for user: {}", username);

                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.info("✅ Authenticated user: {}", username);
                    
                    filterChain.doFilter(request, response);
                    return;
                } else {
                    logger.warn("❌ Invalid JWT token for request: {}", path);
                }
            } else {
                logger.debug("ℹ️ No JWT token found for request: {}", path);
            }
        } catch (Exception ex) {
            logger.error("❌ Authentication error: {}", ex.getMessage());
        }

        // For public paths, continue even without authentication
        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private boolean shouldSkipAuthentication(String path) {
        // Public paths
        String[] publicPaths = {
            "/auth/", "/public/", "/pages/", "/css/", "/js/", "/images/",
            "/favicon.ico", "/error", "/index.html", "/login.html", "/register.html"
        };
        
        for (String publicPath : publicPaths) {
            if (path.startsWith(publicPath)) {
                return true;
            }
        }
        return false;
    }
}