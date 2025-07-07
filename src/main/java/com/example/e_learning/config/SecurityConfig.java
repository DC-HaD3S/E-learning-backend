	package com.example.e_learning.config;
	
	import org.springframework.beans.factory.annotation.Autowired;
	import org.springframework.context.annotation.Bean;
	import org.springframework.context.annotation.Configuration;
	import org.springframework.context.annotation.Lazy;
	import org.springframework.http.HttpMethod;
	import org.springframework.scheduling.annotation.EnableScheduling;
	import org.springframework.scheduling.annotation.Scheduled;
	import org.springframework.security.authentication.AuthenticationManager;
	import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
	import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
	import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
	import org.springframework.security.config.http.SessionCreationPolicy;
	import org.springframework.security.config.annotation.web.builders.HttpSecurity;
	import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
	import org.springframework.security.web.SecurityFilterChain;
	import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
	import org.springframework.web.cors.CorsConfiguration;
	import org.springframework.web.cors.CorsConfigurationSource;
	import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
	import org.springframework.stereotype.Component;
	import jakarta.persistence.EntityManager;
	import jakarta.persistence.PersistenceContext;
	import java.util.Arrays;
	
	@Configuration
	@EnableWebSecurity
	@EnableMethodSecurity(prePostEnabled = true)
	@EnableScheduling
	public class SecurityConfig {
	
	    @Autowired
	    @Lazy
	    private JwtAuthFilter jwtAuthFilter;
	
	    @Bean
	    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
	        http
	            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
	            .csrf(csrf -> csrf.disable())
	            .authorizeHttpRequests(auth -> auth
	                .requestMatchers("/auth/login", "/auth/signup", "/feedback/course/{courseId}", 
	                                "/feedback/course/{courseId}/average-rating", "/feedback/all", 
	                                "/courses", "/auth/check-username", "/auth/check-email", 
	                                "/swagger-ui/**", "/v3/api-docs/**").permitAll()
	                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
	                .anyRequest().authenticated()
	            )
	            .sessionManagement(session -> session
	                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
	            )
	            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
	
	        return http.build();
	    }
	
	    @Bean
	    public CorsConfigurationSource corsConfigurationSource() {
	        CorsConfiguration configuration = new CorsConfiguration();
	        configuration.setAllowedOriginPatterns(Arrays.asList("*")); 
	        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
	        configuration.setAllowedHeaders(Arrays.asList("*"));
	        configuration.setAllowCredentials(false);
	        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	        source.registerCorsConfiguration("/**", configuration);
	        return source;
	    }
	
	    @Bean
	    public BCryptPasswordEncoder bCryptPasswordEncoder() {
	        return new BCryptPasswordEncoder();
	    }
	
	    @Bean
	    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
	        return authConfig.getAuthenticationManager();
	    }
	
	    @Component
	    public class KeepAliveTask {
	        @PersistenceContext
	        private EntityManager entityManager;
	
	        @Scheduled(fixedRate = 600000) // Every 10 minutes
	        public void keepDatabaseAlive() {
	            entityManager.createNativeQuery("SELECT 1").getSingleResult();
	        }
	    }
	}