package com.gestaofinanceira.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuração de segurança do Spring Security com JWT.
 * 
 * Define:
 * - Endpoints públicos e privados
 * - Configuração CORS para frontend
 * - Integração com filtro JWT
 * - Política de sessão stateless
 * - Tratamento de erros de autenticação/autorização
 * 
 * Requirements: 1.1, 1.2, 10.2
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                         JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Desabilitar CSRF (usando JWT, não cookies)
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configurar CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Configurar política de sessão (stateless)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configurar autorização de endpoints
            .authorizeHttpRequests(authz -> authz
                // Endpoints públicos - autenticação
                .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/refresh").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/logout").permitAll()
                
                // Endpoints públicos - health check e documentação
                .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/docs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/swagger-ui/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/v3/api-docs/**").permitAll()
                
                // Endpoints públicos - recursos estáticos (se houver)
                .requestMatchers(HttpMethod.GET, "/static/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/favicon.ico").permitAll()
                
                // Todos os outros endpoints requerem autenticação
                .anyRequest().authenticated()
            )
            
            // Configurar tratamento de erros de autenticação
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            )
            
            // Adicionar filtro JWT antes do filtro de autenticação padrão
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    /**
     * Configuração CORS para permitir acesso do frontend.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Origens permitidas (em produção, especificar domínios específicos)
        configuration.setAllowedOriginPatterns(List.of(
            "http://localhost:*",  // Desenvolvimento local
            "http://127.0.0.1:*",  // Desenvolvimento local
            "https://*.vercel.app", // Deploy Vercel
            "https://*.netlify.app" // Deploy Netlify
        ));
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        
        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // Headers expostos para o frontend
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials",
            "Authorization"
        ));
        
        // Permitir credenciais (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Cache de preflight requests (OPTIONS)
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}