package cl.duoc.cdy2204.formativa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Value("${app.security.roles-enabled:false}") boolean rolesEnabled,
            Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter
    ) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> {
                    if (rolesEnabled) {
                        authorize
                                .requestMatchers(HttpMethod.POST, "/cursos", "/cursos/**").hasRole("INSTRUCTOR")
                                .requestMatchers(HttpMethod.POST, "/calificaciones").hasRole("INSTRUCTOR")
                                .requestMatchers(HttpMethod.POST, "/inscripciones/*/resumenes-mq/producir").hasRole("INSTRUCTOR")
                                .requestMatchers(HttpMethod.POST, "/inscripciones/resumenes-mq/consumir").hasRole("INSTRUCTOR")
                                .requestMatchers(HttpMethod.POST, "/s3/uploadResumen").hasRole("INSTRUCTOR")
                                .requestMatchers(HttpMethod.PUT, "/s3/updateResumen").hasRole("INSTRUCTOR")
                                .requestMatchers(HttpMethod.DELETE, "/s3/deleteResumen").hasRole("INSTRUCTOR")
                                .requestMatchers(HttpMethod.GET, "/cursos", "/cursos/**").hasAnyRole("ESTUDIANTE", "INSTRUCTOR")
                                .requestMatchers(HttpMethod.POST, "/inscripciones").hasAnyRole("ESTUDIANTE", "INSTRUCTOR")
                                .requestMatchers(HttpMethod.POST, "/examenes/*/intentos").hasAnyRole("ESTUDIANTE", "INSTRUCTOR")
                                .requestMatchers(HttpMethod.POST, "/intentos/*/finalizar").hasAnyRole("ESTUDIANTE", "INSTRUCTOR")
                                .requestMatchers(HttpMethod.GET, "/inscripciones/*/intentos").hasAnyRole("ESTUDIANTE", "INSTRUCTOR")
                                .requestMatchers(HttpMethod.GET, "/inscripciones/*/resumen").hasAnyRole("ESTUDIANTE", "INSTRUCTOR")
                                .requestMatchers(HttpMethod.GET, "/calificaciones").hasAnyRole("ESTUDIANTE", "INSTRUCTOR")
                                .requestMatchers(HttpMethod.GET, "/s3/downloadResumen").hasAnyRole("ESTUDIANTE", "INSTRUCTOR")
                                .anyRequest().authenticated();
                    } else {
                        authorize.anyRequest().authenticated();
                    }
                })
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .build();
    }

    @Bean
    public Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter scopesConverter = new JwtGrantedAuthoritiesConverter();
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<GrantedAuthority> authorities = new ArrayList<>(scopesConverter.convert(jwt));
            addRoleAuthorities(authorities, jwt, "roles");
            addRoleAuthorities(authorities, jwt, "role");
            addRoleAuthorities(authorities, jwt, "extension_Rol");
            addRoleAuthorities(authorities, jwt, "extension_role");
            return authorities;
        });
        return converter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:*}") String allowedOrigins
    ) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(parseList(allowedOrigins));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setExposedHeaders(List.of("Content-Disposition"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private List<String> parseList(String values) {
        return Arrays.stream(values.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }

    private static void addRoleAuthorities(List<GrantedAuthority> authorities, Jwt jwt, String claimName) {
        Object claim = jwt.getClaims().get(claimName);
        if (claim instanceof String value) {
            addRoleAuthority(authorities, value);
        }
        if (claim instanceof Collection<?> values) {
            values.forEach(value -> addRoleAuthority(authorities, String.valueOf(value)));
        }
    }

    private static void addRoleAuthority(List<GrantedAuthority> authorities, String value) {
        String role = value.trim();
        if (role.isBlank()) {
            return;
        }
        String normalized = role.toUpperCase(Locale.ROOT);
        if (!normalized.startsWith("ROLE_")) {
            normalized = "ROLE_" + normalized;
        }
        authorities.add(new SimpleGrantedAuthority(normalized));
    }
}
