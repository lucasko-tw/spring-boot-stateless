package org.app.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.app.service.UserService;
import org.json.JSONObject;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

public class LoginFilter extends AbstractAuthenticationProcessingFilter {

	public static final String TOKEN_PREFIX = "Bearer ";
	public static final String HEADER_STRING = "Authorization";
	public static final String SECRET = "secretkey";
	public static final long EXPIRATION_TIME = 864_000_000; // 10 days

	private UserService userservice;

	public LoginFilter(String url, AuthenticationManager authManager, UserService userService ) {
		super(new AntPathRequestMatcher(url));
		setAuthenticationManager(authManager);
		this.userservice = userService;

	}

	@Override
	public Authentication attemptAuthentication(javax.servlet.http.HttpServletRequest req, HttpServletResponse arg1)
			throws AuthenticationException, IOException, ServletException {

		BufferedReader br = req.getReader();
		StringBuffer bf = new StringBuffer();
		String str1;
		while ((str1 = br.readLine()) != null)
			bf.append(str1);
		String body = bf.toString();
		JSONObject json = null;
		String account = null;
		String password = null;

		try {
			json = new JSONObject(body);
			account = json.getString("account");
			password = json.getString("password");
		} catch (Exception e) {
			throw new ServletException("Invalid JSON Format");
		}
		if (account == null || password == null) {
			throw new ServletException("Please fill in account and password");
		}

		org.app.model.User user = userservice.findByAccount(account);

		if (user == null) {
			throw new ServletException("Invalid Account or Password");
		}

		if (!user.getPassword().equals(password)) {
			throw new ServletException("Invalid Password");
		}

		List<Role> roles = new ArrayList<Role>();
		roles.add(Role.valueOf(user.getRole()));

		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(account, password,
				roles);

		return this.getAuthenticationManager().authenticate(token);

	}

	@Override
	protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse response, FilterChain chain,
			Authentication auth) throws IOException, ServletException {
		System.out.println(">>> successfulAuthentication");

		String username = ((User) auth.getPrincipal()).getUsername();
		String role = userservice.findByAccount(username).getRole();

		// res.addHeader(HEADER_STRING, TOKEN_PREFIX + jwttoken);

		if (auth != null) {
			List<GrantedAuthority> updatedAuthorities = new ArrayList<GrantedAuthority>();
			updatedAuthorities.add(new SimpleGrantedAuthority(role));
			User user = new User(auth.getName(), "", true, true, true, true, updatedAuthorities);
			Authentication newAuth = new UsernamePasswordAuthenticationToken(user, auth.getCredentials(),
					updatedAuthorities);
			SecurityContext securityContext = SecurityContextHolder.getContext();
			securityContext.setAuthentication(newAuth);
		}

		JSONObject json = new JSONObject();
		json.put("success", true);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json.toString());

	}

}
