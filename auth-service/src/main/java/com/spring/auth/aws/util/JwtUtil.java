package com.spring.auth.aws.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.spring.auth.aws.model.AppUser;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtil {

	private PrivateKey privateKey;

	public JwtUtil() {
		try {
			this.privateKey = loadPrivateKey();
		} catch (Exception e) {
			throw new RuntimeException("Failed to load private key", e);
		}
	}

	private PrivateKey loadPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		// Load key from classpath
		ClassPathResource resource = new ClassPathResource("keys/private_key.pem");
		String keyPem;
		try (InputStream is = resource.getInputStream()) {
			keyPem = new String(is.readAllBytes());
		}

		keyPem = keyPem.replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "")
				.replaceAll("\\s+", "");

		byte[] decoded = Base64.getDecoder().decode(keyPem);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return keyFactory.generatePrivate(keySpec);
	}

	public String generateToken(AppUser user) {
		return Jwts.builder().setSubject(user.getId().toString()).claim("role", user.getRole()).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day
				.signWith(SignatureAlgorithm.RS256, privateKey).compact();
	}
}
