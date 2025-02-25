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
import ru.sfedu.mmcs_nexus.data.user.UserService;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserService userService;


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
                            response.setStatus(HttpServletResponse.SC_OK);
                        })
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                )
                .oauth2Login(httpSecurityOAuth2LoginConfigurer
                        -> httpSecurityOAuth2LoginConfigurer.successHandler(this.successHandler()))
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling
                                .authenticationEntryPoint((request, response, authException) -> response.sendRedirect(STR."\{ApplicationConfig.CLIENT_URL}/tuda"))
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
                userService.saveUser(githubLogin);
            }
            String roleName = userService.findByGithubLogin(githubLogin).orElseThrow().getRole().name();
            DefaultOAuth2User newUser = new DefaultOAuth2User(List.of(new SimpleGrantedAuthority(roleName)),
                    oauthUser.getAttributes(),"id");
            Authentication newAuthentication = new OAuth2AuthenticationToken(newUser, List.of(new SimpleGrantedAuthority(roleName)),"github");
            System.out.println("WOWOWO " + new SimpleGrantedAuthority(roleName).toString());
            SecurityContextHolder.getContext().setAuthentication(newAuthentication);

            System.out.println("GOGOGO " + SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().findFirst().get());
            response.sendRedirect(ApplicationConfig.CLIENT_URL);

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
