package ru.sfedu.mmcs_nexus.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Value;
import ru.sfedu.mmcs_nexus.service.UserService;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserService userService;

    @Value("${BASE_URL}")
    private String baseUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .anonymous((anonymous)->anonymous.authorities("ROLE_GUEST"))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/api/v1/auth/**").hasRole("GUEST");
                    auth.requestMatchers("/api/v1/public/**").hasRole("USER");
                    auth.requestMatchers("/api/v1/admin/**").hasRole("ADMIN");
                    //auth.requestMatchers("/api/v1/admin/**").permitAll();
                    auth.requestMatchers("/api/v1/jury/**").hasRole("JURY");
                    auth.anyRequest().authenticated();
                })
                .csrf(httpSecurityCsrfConfigurer -> httpSecurityCsrfConfigurer.ignoringRequestMatchers("/api/v1/auth/**", "api/v1/admin/**", "/api/v1/jury/**"))
                .cors(Customizer.withDefaults()) // Enable CORS
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            System.out.println("wowowo");
                            response.setStatus(HttpServletResponse.SC_OK);
                        })
                        .deleteCookies("JSESSIONID")
                        .clearAuthentication(true)
                        .invalidateHttpSession(true)
                )
                .oauth2Login(httpSecurityOAuth2LoginConfigurer
                        -> httpSecurityOAuth2LoginConfigurer.successHandler(this.successHandler()))
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling
                                .authenticationEntryPoint((request, response, authException) -> {
                                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                                    response.setContentType("application/json");
                                    response.setCharacterEncoding("UTF-8");
                                    response.getWriter().write("{\"error\": \"Пользователь не авторизован.\"}");
                                })
                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                    response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
                                    response.setContentType("application/json");
                                    response.setCharacterEncoding("UTF-8");
                                    response.getWriter().write("{\"error\": \"Недостаточно прав\"}");
                                })
                )
                .formLogin(Customizer.withDefaults())
                .build();
    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return ((request, response, authentication) -> {
            DefaultOAuth2User oauthUser = (DefaultOAuth2User) authentication.getPrincipal();
            String githubLogin = oauthUser.getAttribute("login");

            if (userService.isNotFoundOrVerified(githubLogin)) {
                userService.create(githubLogin);
            }
            String roleName = userService.findByGithubLogin(githubLogin).orElseThrow().getRole().name();
            DefaultOAuth2User newUser = new DefaultOAuth2User(List.of(new SimpleGrantedAuthority(roleName)),
                    oauthUser.getAttributes(),"id");
            Authentication newAuthentication = new OAuth2AuthenticationToken(newUser, List.of(new SimpleGrantedAuthority(roleName)),"github");
            SecurityContextHolder.getContext().setAuthentication(newAuthentication);

            response.sendRedirect(baseUrl);

        });
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        String hierarchy =  "ROLE_ADMIN > ROLE_JURY \n" +
                            "ROLE_JURY > ROLE_USER \n" +
                            "ROLE_USER > ROLE_GUEST";
        return RoleHierarchyImpl.fromHierarchy(hierarchy);
    }



}
