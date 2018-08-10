package org.app.config;

import javax.sql.DataSource;

import org.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	// @Autowired
	// private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private DataSource dataSource;

	@Value("${spring.queries.users-query}")
	private String usersQuery;

	@Value("${spring.queries.roles-query}")
	private String rolesQuery;

	@Autowired
	private UserService userService;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable()
			.authorizeRequests()

				.antMatchers("/", "/*.html", "/*.ico", "/*.js", "/*.svg", "/*.woff2", "/*.woff").permitAll()

				// .antMatchers(HttpMethod.POST, "/api/pub/login").permitAll()

				.antMatchers("/api/pub/**").permitAll()
				.antMatchers("/api/admin/**").hasRole("ADMIN")
				.antMatchers("/api/user/**").hasRole("USER")

				.anyRequest().authenticated().and()
				.addFilterBefore(new LoginFilter("/api/pub/login", authenticationManager(), userService ),
						UsernamePasswordAuthenticationFilter.class);
		// And filter other requests to check the presence of JWT in header
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		// Create a default account
		
		System.out.println("auth=" + auth.toString());

		// auth.inMemoryAuthentication().withUser("lucas").password("1234").roles("USER");

		auth.jdbcAuthentication().usersByUsernameQuery(usersQuery).authoritiesByUsernameQuery(rolesQuery)
				.dataSource(dataSource)
				//.passwordEncoder(new MessageDigestPasswordEncoder("SHA-256"))
		;
	}
	 

}