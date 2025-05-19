package com.example.project.config;

import com.example.project.entity.Usuarios;
import com.example.project.repository.UsuariosRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;

import javax.sql.DataSource;
import java.util.Optional;

@EnableWebSecurity
@Configuration
public class WebSecurityConfig {

    private final DataSource dataSource;

    private final UsuariosRepository usuariosRepository;

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Autowired
    public WebSecurityConfig(DataSource dataSource, UsuariosRepository usuariosRepository) {
        this.dataSource = dataSource;
        this.usuariosRepository = usuariosRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                Optional<Usuarios> usuarioOpt = Optional.empty();

                if (username != null) {
                    try {
                        int dni = Integer.parseInt(username);
                        usuarioOpt = usuariosRepository.findByDni(dni);
                    } catch (NumberFormatException e) {
                    }
                }

                if (usuarioOpt.isEmpty() && username != null) {
                    usuarioOpt = usuariosRepository.findByCorreo(username);
                }

                if (usuarioOpt.isPresent()) {
                    Usuarios usuario = usuarioOpt.get();
                    boolean enabled = (usuario.getEstado() != null && usuario.getEstado().getIdEstado() == 1);
                    String role = (usuario.getRol() != null && usuario.getRol().getRol() != null) ? usuario.getRol().getRol() : "ROLE_USER";

                    return User.withUsername(username)
                            .password(usuario.getContrasena())
                            .disabled(!enabled)
                            .authorities(new SimpleGrantedAuthority(role))
                            .build();
                } else {
                    throw new UsernameNotFoundException("Usuario no encontrado con identificador: " + username);
                }
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .formLogin(formLogin ->
                        formLogin
                                .permitAll()
                                .loginPage("/login")
                                .loginProcessingUrl("/login")
                                .usernameParameter("username")
                                .passwordParameter("password")
                                .successHandler(new AuthenticationSuccessHandler() {
                                    @Override
                                    public void onAuthenticationSuccess(HttpServletRequest request,
                                                                        HttpServletResponse response,
                                                                        Authentication authentication) throws java.io.IOException {

                                        DefaultSavedRequest defaultSavedRequest =
                                                (DefaultSavedRequest) request.getSession().getAttribute("SPRING_SECURITY_SAVED_REQUEST");

                                        String authenticatedUsername = authentication.getName();

                                        Optional<Usuarios> optUser = Optional.empty();
                                        if (authenticatedUsername != null) {
                                            try {
                                                int dni = Integer.parseInt(authenticatedUsername);
                                                optUser = usuariosRepository.findByDni(dni);
                                            } catch (NumberFormatException e) {
                                            }
                                        }

                                        if (optUser.isEmpty() && authenticatedUsername != null) {
                                            optUser = usuariosRepository.findByCorreo(authenticatedUsername);
                                        }

                                        if (optUser.isPresent()) {
                                            Usuarios loggedUser = optUser.get();
                                            request.getSession().setAttribute("usuario", loggedUser);

                                            if (defaultSavedRequest != null) {
                                                String targetURL = defaultSavedRequest.getRedirectUrl();
                                                redirectStrategy.sendRedirect(request, response, targetURL);
                                            } else {
                                                String rolName = loggedUser.getRol().getRol();

                                                if (rolName.equals("SuperAdmin")) {
                                                    redirectStrategy.sendRedirect(request, response, "/superadmin/home");
                                                } else if (rolName.equals("Administrador")) {
                                                    redirectStrategy.sendRedirect(request, response, "/admin/mi_cuenta");
                                                } else if (rolName.equals("Coordinador")) {
                                                    redirectStrategy.sendRedirect(request, response, "/coordinador/perfil");
                                                } else if (rolName.equals("Usuario final")) {
                                                    redirectStrategy.sendRedirect(request, response, "/vecino/home");
                                                } else {
                                                    redirectStrategy.sendRedirect(request, response, "/");
                                                }
                                            }
                                        } else {
                                            redirectStrategy.sendRedirect(request, response, "/login?error=auth_failed");
                                        }
                                    }
                                })
                )

                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/admin","/admin/**").hasAuthority("Administrador")
                                .requestMatchers("/coordinador","/coordinador/**").hasAuthority("Coordinador")
                                .requestMatchers("/superadmin","/superadmin/**").hasAuthority("SuperAdmin")
                                .requestMatchers("/vecino", "/vecino/**").hasAuthority("Usuario final")
                                .requestMatchers("/login", "/principal", "/registro/**", "/registro" ,"/olvidoContrasena","/olvido",
                                        "/confirmoContrasena","/renovarContrasena","/logout","/css/**","/js/**","/renovarContrasena/**"
                                        , "/img/**","/vendor/**").permitAll()
                                .anyRequest().permitAll()
                )

                .logout(logout ->
                        logout
                                .logoutUrl("/logout")
                                .logoutSuccessUrl("/login?logout")
                                .invalidateHttpSession(true)
                                .deleteCookies("JSESSIONID")
                )

        ;

        return http.build();
    }

    // ELIMINADO: Ya no usamos JdbcUserDetailsManager
    // @Bean
    // public UserDetailsManager users(DataSource dataSource) { ... }
}

