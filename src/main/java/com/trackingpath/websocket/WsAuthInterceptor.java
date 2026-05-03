package com.trackingpath.websocket;

import com.trackingpath.services.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Component
public class WsAuthInterceptor implements HandshakeInterceptor {
	private static final Logger log = LoggerFactory.getLogger(WsAuthInterceptor.class);

	private final JwtService jwtService;
	private final UserDetailsService userDetailsService;

	public WsAuthInterceptor(JwtService jwtService, UserDetailsService userDetailsService) {
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
	}

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Map<String, Object> attributes) {
		try {
			var params = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams();
			String token = params.getFirst("token");

			// Optional: accept via subprotocol "jwt,<token>" or just "<token>"
			var hdrs = request.getHeaders();
			var subproto = hdrs.getFirst("Sec-WebSocket-Protocol");
			if ((token == null || token.isBlank()) && subproto != null) {
				// e.g. "jwt,<token>" or just "<token>"
				var parts = subproto.split(",", 2);
				token = parts.length == 2 && "jwt".equalsIgnoreCase(parts[0].trim()) ? parts[1].trim()
						: subproto.trim();
			}

			if (token == null || token.isBlank()) {
				response.setStatusCode(HttpStatus.UNAUTHORIZED);
				log.warn("WS handshake denied: missing token");
				return false;
			}
			if (token.startsWith("Bearer "))
				token = token.substring(7);

			String username = jwtService.extractUsername(token);
			if (username == null || username.isBlank()) {
				response.setStatusCode(HttpStatus.UNAUTHORIZED);
				log.warn("WS handshake denied: could not extract username");
				return false;
			}

			UserDetails ud = userDetailsService.loadUserByUsername(username);
			if (!jwtService.isTokenValid(token, ud)) {
				response.setStatusCode(HttpStatus.UNAUTHORIZED);
				log.warn("WS handshake denied: token invalid for user={}", username);
				return false;
			}

			attributes.put("user", ud.getUsername()); // available in handler via session attributes
			return true;
		} catch (Exception ex) {
			response.setStatusCode(HttpStatus.UNAUTHORIZED);
			log.warn("WS handshake denied: {}", ex.toString());
			return false;
		}
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Exception exception) {
		// no-op
	}
}
