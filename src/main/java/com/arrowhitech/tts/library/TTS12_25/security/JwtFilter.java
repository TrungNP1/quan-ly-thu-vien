package com.arrowhitech.tts.library.TTS12_25.security;

import com.arrowhitech.tts.library.TTS12_25.service.JwtService;
import com.arrowhitech.tts.library.TTS12_25.service.UserService;
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

            if (jwtService.validateToken(token)) {
                String username = jwtService.extractUsername(token);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    //Kiểm tra Redis
                    String revocationKey = "revocation:user:" + username;
                    String revocationTimestampStr = redisTemplate.opsForValue().get(revocationKey);
                    if (revocationTimestampStr != null) {
                        try {
                            long revocationTimestamp = Long.parseLong(revocationTimestampStr);
                            long tokenIat = jwtService.extractIssuedAt(token);

                            // Token được tạo trước thời điểm đổi mật khẩu sẽ bị từ chối
                            if (tokenIat < revocationTimestamp) {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.setContentType("application/json;charset=UTF-8");
                                response.getWriter().write("{\"message\": \"Phiên đăng nhập hết hạn do đổi mật khẩu\"}");
                                return;
                            }
                        } catch (RuntimeException ignored) {

                        }
                    }

                    // Tiếp tục logic xác thực nếu pass qua Redis
                    try {
                        UserDetails userDetails = userService.loadUserByUsername(username);
                        if (jwtService.validateToken(token, username)) {
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
        }
        filterChain.doFilter(request, response);
    }
}