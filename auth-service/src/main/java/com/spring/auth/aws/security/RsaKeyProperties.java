package com.spring.auth.aws.security;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class RsaKeyProperties {

	private final PrivateKey privateKey;

	public RsaKeyProperties() throws Exception {
		String privateKeyPEM = loadKeyFromFile("keys/private_key.pem");

		this.privateKey = loadPrivateKey(privateKeyPEM);

	}

	private String loadKeyFromFile(String path) throws Exception {
		System.out.println("<<<<<<<<<<<<<<<< loadKeyFromFile <<<<<<<<<<<<");

		InputStream inputStream = new ClassPathResource(path).getInputStream();
		return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
	}

	private PrivateKey loadPrivateKey(String key) throws Exception {
		String privateKeyPEM = key.replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "")
				.replaceAll("\\s+", "");
		byte[] keyBytes = Base64.getDecoder().decode(privateKeyPEM);
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePrivate(spec);
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}

}
