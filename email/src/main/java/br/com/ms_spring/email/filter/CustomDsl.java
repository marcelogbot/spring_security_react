package br.com.ms_spring.email.filter;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

public class CustomDsl extends AbstractHttpConfigurer<CustomDsl, HttpSecurity> {

    //private boolean flag;

	@Override
	public void init(HttpSecurity http) throws Exception {
		
		 final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
         final CorsConfiguration               config = new CorsConfiguration();

         config.addAllowedOrigin("http://localhost:3000");
         config.addAllowedHeader("*");
         //config.addAllowedMethod("GET");
         config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);

		http.csrf().disable();
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.authorizeRequests().antMatchers("/api/v1/registration/**").permitAll();
		http.authorizeRequests().antMatchers("/api/v1/registration").permitAll();
        http.authorizeRequests().antMatchers("/api/login/**", "/api/token/refresh/**").permitAll();
		http.authorizeRequests().antMatchers(HttpMethod.GET, "/api/user/**").hasAnyAuthority("ROLE_USER","ROLE_ADMIN");
    	http.authorizeRequests().antMatchers(HttpMethod.POST, "/api/user/**").hasAnyAuthority("ROLE_ADMIN");
		http.authorizeRequests().antMatchers(HttpMethod.PUT, "/api/user/**").hasAnyAuthority("ROLE_ADMIN");
		http.authorizeRequests().anyRequest().authenticated();
		// http.formLogin();
		 http
		// 	// Adding spring-web CORS filter
		 	.addFilterBefore(new CorsFilter(source), LogoutFilter.class);

			// Adding custom REST Authentication filter
			//.addFilterBefore(new RestAuthenticationFilter(authenticationManager()), LogoutFilter.class);
		http.logout(logout -> logout                                                
			.logoutUrl("/api/logout")                                            
			.logoutSuccessUrl("/")                                      
			.logoutSuccessHandler(new SimpleUrlLogoutSuccessHandler())                         
			.invalidateHttpSession(true)                                        
			//.addLogoutHandler(logoutHandler)                                    
			.deleteCookies("SessionID")                                  
		);
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {

		AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
		CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter(authenticationManager);
		customAuthenticationFilter.setFilterProcessesUrl("/api/login");
		http.addFilter(customAuthenticationFilter);
		http.addFilterBefore(new CustomAuthorizationFilter(),UsernamePasswordAuthenticationFilter.class);

	}

	/* public CustomDsl flag(boolean value) {
		this.flag = value;
		return this;
	} */

	public static CustomDsl customDsl() {
		return new CustomDsl();
	}
    
}