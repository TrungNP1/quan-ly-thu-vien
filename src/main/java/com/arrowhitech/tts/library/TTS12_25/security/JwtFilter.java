package com.arrowhitech.tts.library.TTS12_25.security;

import com.arrowhitech.tts.library.TTS12_25.service.JwtService;
import com.arrowhitech.tts.library.TTS12_25.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@NullMarked
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;
    private final StringRedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // Parse token một lần duy nhất và lấy Claims
            Claims claims;
            try {
                claims = jwtService.extractAllClaims(token);
            } catch (JwtException e) {
                // Token không hợp lệ, bỏ qua và tiếp tục filter chain
                filterChain.doFilter(request, response);
                return;
            }

            // Extract username từ Claims đã parse
            String username = claims.getSubject();

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Kiểm tra Redis revocation
                String revocationKey = "revocation:user:" + username;
                String revocationTimestampStr = redisTemplate.opsForValue().get(revocationKey);

                if (revocationTimestampStr != null && claims.getIssuedAt() != null) {
                    try {
                        long revocationTimestamp = Long.parseLong(revocationTimestampStr);
                        // Extract iat từ Claims đã parse (không cần parse lại token)
                        long tokenIat = claims.getIssuedAt().getTime() / 1000;

                        // Token được tạo trước thời điểm đổi mật khẩu sẽ bị từ chối
                        if (tokenIat < revocationTimestamp) {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"message\": \"Phiên đăng nhập hết hạn do đổi mật khẩu\"}");
                            return;
                        }
                    } catch (RuntimeException e) {
                        // Log error nếu cần, nhưng không block request
                    }
                }

                // Tiếp tục logic xác thực nếu pass qua Redis
                try {
                    UserDetails userDetails = userService.loadUserByUsername(username);
                    // Validate bằng cách so sánh username từ Claims với username từ UserDetails
                    // Không cần parse lại token vì đã có Claims
                    if (username.equals(userDetails.getUsername())) {
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                } catch (UsernameNotFoundException e) {
                    // Để filter tự trôi đi, SecurityContext trống sẽ bị chặn ở SecurityConfig
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}