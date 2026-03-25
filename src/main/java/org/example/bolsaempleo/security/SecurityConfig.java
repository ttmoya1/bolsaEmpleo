package org.example.bolsaempleo.security;



import org.example.bolsaempleo.data.UsuarioRepository;
import org.example.bolsaempleo.logic.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // ----------------------------------------------------------------
    // Bean que el Service (y Spring Security) necesitan para bcrypt
    // ----------------------------------------------------------------
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ----------------------------------------------------------------
    // Carga el usuario por correo (username = correo en este proyecto)
    // ----------------------------------------------------------------
    @Bean
    public UserDetailsService userDetailsService() {
        return correo -> {
            Usuario u = usuarioRepository.findByCorreo(correo)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correo));

            return User.builder()
                    .username(u.getCorreo())
                    .password(u.getClave())
                    .authorities(List.of(new SimpleGrantedAuthority(u.getRol())))
                    .disabled(!u.isActivo())
                    .build();
        };
    }

    // ----------------------------------------------------------------
    // Reglas de acceso (se ampliarán cuando estén los controllers)
    // ----------------------------------------------------------------
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico").permitAll()
                        .requestMatchers("/", "/login", "/acceso-denegado", "/dashboard", "/publica/**", "/registro/**").permitAll()
                        .requestMatchers("/admin/**").hasAuthority("ADM")
                        .requestMatchers("/empresa/**").hasAuthority("EMP")
                        .requestMatchers("/oferente/**").hasAuthority("OFE")
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .permitAll()
                        .defaultSuccessUrl("/dashboard", true)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/publica/inicio")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/acceso-denegado")
                )
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}