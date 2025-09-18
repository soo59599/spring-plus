package org.example.expert.config;

import lombok.RequiredArgsConstructor;
import org.example.expert.security.JwtAccessDeniedHandler;
import org.example.expert.security.JwtAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter JwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement((sessionManagement) ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(JwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // JWT 사용 시 불필요한 기능들 비활성화
                .formLogin(AbstractHttpConfigurer::disable)      // [SSR] 서버가 로그인 HTML 폼 렌더링
                .anonymous(AbstractHttpConfigurer::disable)      // 미인증 사용자를 익명으로 처리
                .httpBasic(AbstractHttpConfigurer::disable)      // [SSR] 인증 팝업
                .logout(AbstractHttpConfigurer::disable)         // [SSR] 서버가 세션 무효화 후 리다이렉트
                .rememberMe(AbstractHttpConfigurer::disable)     // 서버가 쿠키 발급하여 자동 로그인

                .authorizeHttpRequests((authorizeHttpRequests) ->
                        authorizeHttpRequests
                                .requestMatchers("/auth/**").permitAll()
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                .anyRequest().authenticated())

                .exceptionHandling(exceptionHandling ->exceptionHandling
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler));
        return http.build();
    }
}
