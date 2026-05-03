// src/main/java/com/trackingpath/configs/JwtAuthenticationFilter.java
package com.trackingpath.configs;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.trackingpath.security.CustomDriverUserPrincipal;
import com.trackingpath.security.CustomUserPrincipal;
import com.trackingpath.security.DriverUserDetailsService;
import com.trackingpath.security.JwtDriverService;
import com.trackingpath.security.JwtParentService;
import com.trackingpath.security.ParentUserDetailsService;
import com.trackingpath.services.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final HandlerExceptionResolver handlerExceptionResolver;
	private final JwtService jwtService;
	private final ParentUserDetailsService parentUserDetailsService;
	private final JwtParentService jwtParentService;
	private final UserDetailsService userDetailsService;
	private final DriverUserDetailsService driverUserDetailsService;
	private final JwtDriverService jwtDriverService;
	

	public JwtAuthenticationFilter(JwtService jwtService, JwtParentService jwtParentService,JwtDriverService jwtDriverService,
			DriverUserDetailsService driverUserDetailsService,
			ParentUserDetailsService parentUserDetailsService, UserDetailsService userDetailsService,
			@Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver) {
		this.jwtService = jwtService;
		this.jwtParentService = jwtParentService;
		this.parentUserDetailsService = parentUserDetailsService;
		this.userDetailsService = userDetailsService;
		this.handlerExceptionResolver = handlerExceptionResolver;
		this.jwtDriverService = jwtDriverService;
		this.driverUserDetailsService = driverUserDetailsService;
	}

	// Routes that bypass JWT filter (public pages, static, WS, hooks, media)
	private static final RequestMatcher SKIP = new OrRequestMatcher(new AntPathRequestMatcher("/"),
			new AntPathRequestMatcher("/login"), new AntPathRequestMatcher("/dashboard"),
			new AntPathRequestMatcher("/css/**"), new AntPathRequestMatcher("/js/**"),
			new AntPathRequestMatcher("/images/**"), new AntPathRequestMatcher("/webjars/**"),
			new AntPathRequestMatcher("/favicon.ico"), new AntPathRequestMatcher("/error"),
			new AntPathRequestMatcher("/auth/**"), new AntPathRequestMatcher("/hook/**"),
			new AntPathRequestMatcher("/ws/**"));

	@Override
	protected boolean shouldNotFilter(HttpServletRequest req) {
		if ("OPTIONS".equalsIgnoreCase(req.getMethod()))
			return true;
		return SKIP.matches(req);
	}

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest req, @NonNull HttpServletResponse res,
			@NonNull FilterChain chain) throws ServletException, IOException {

		final String auth = req.getHeader("Authorization");

		// No token → pass through; Security rules determine if path is public
		if (auth == null || !auth.startsWith("Bearer ")) {
			chain.doFilter(req, res);
			return;
		}

		String username;
		UserDetails userDetails;

		try {
			final String jwt = auth.substring(7);
			String role = null;

			try {
			    role = jwtParentService.extractClaim(jwt, claims -> claims.get("role", String.class));
			} catch (Exception e) {
			    try {
			        role = jwtService.extractClaim(jwt, claims -> claims.get("role", String.class));
			    } catch (Exception ex) {
			        role = null;
			    }
			}

			System.out.println("role----" + role);
			username = jwtService.extractUsername(jwt);
			System.out.println("role----" + role);
			if ("ROLE_PARENT".equals(role)) {

				username = jwtParentService.extractUsername(jwt);
				userDetails = parentUserDetailsService.loadUserByUsername(username);

				if (!jwtParentService.isTokenValid(jwt, (CustomUserPrincipal) userDetails)) {
					chain.doFilter(req, res);
					return;
				}
				System.out.println("username----" + username);
			} else if ("ROLE_DRIVER".equals(role)) {

			    username = jwtDriverService.extractUsername(jwt);
			    userDetails = driverUserDetailsService.loadUserByUsername(username);

			    if (!jwtDriverService.isTokenValid(jwt, (CustomDriverUserPrincipal) userDetails)) {
			        chain.doFilter(req, res);
			        return;
			    }

			    System.out.println("DRIVER username----" + username);

			
			} else {

				username = jwtService.extractUsername(jwt);
				userDetails = userDetailsService.loadUserByUsername(username);

				if (!jwtService.isTokenValid(jwt, userDetails)) {
					chain.doFilter(req, res);
					return;
				}
			}
			
			if (SecurityContextHolder.getContext().getAuthentication() == null) {

			    UsernamePasswordAuthenticationToken authToken =
			            new UsernamePasswordAuthenticationToken(
			                    userDetails,
			                    null,
			                    userDetails.getAuthorities()
			            );

			    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));

			    SecurityContextHolder.getContext().setAuthentication(authToken);
			}

			chain.doFilter(req, res);
		} catch (Exception ex) {
			handlerExceptionResolver.resolveException(req, res, null, ex);
		}
	}
}
