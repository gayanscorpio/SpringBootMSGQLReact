package com.spring.book.aws.security;

import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtVerificationFilter extends OncePerRequestFilter {

	private PublicKey publicKey;

	public JwtVerificationFilter() throws Exception {
		ClassPathResource resource = new ClassPathResource("public.key");
		try (InputStream is = resource.getInputStream()) {
			byte[] keyBytes = is.readAllBytes();
			// Remove PEM headers if present
			String pem = new String(keyBytes).replace("-----BEGIN PUBLIC KEY-----", "")
					.replace("-----END PUBLIC KEY-----", "").replaceAll("\\s", "");
			byte[] decoded = Base64.getDecoder().decode(pem);

			X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			publicKey = kf.generatePublic(spec);
		}
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		// Get Authorization header
		String authHeader = request.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			System.out.println("<<<<<<<<<<<<<< authHeader :" + authHeader);
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		String token = authHeader.substring(7);
		try {
			Jws<Claims> jwsClaims;
			try {
				// Use PublicKey object
				jwsClaims = Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(token);
			} catch (Exception e) {
				// Token invalid or expired
				throw new RuntimeException("JWT verification failed", e);
			}

			Claims claims = jwsClaims.getBody();
			String userId = claims.getSubject(); // "sub"
			List<String> roles;

			// ✅ Check for single role
			String singleRole = claims.get("role", String.class);

			if (singleRole != null) {
				roles = List.of(singleRole);
			} else {
				// ✅ Check for multiple roles
				roles = claims.get("roles", List.class);
			}
			if (roles == null) {
				roles = Collections.emptyList();
			}

			// Convert roles to GrantedAuthority
			// Spring Security convention
			List<GrantedAuthority> authorities = roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role))
					.collect(Collectors.toList());
			Authentication auth = new UsernamePasswordAuthenticationToken(userId, null, authorities);

			// Injects the authenticated user and role into Spring Security context.
			SecurityContextHolder.getContext().setAuthentication(auth);

		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		filterChain.doFilter(request, response);
	}

}
