package com.tutamall.tutalink.service;

import com.tutamall.tutalink.config.TutalinkProperties;
import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class CodeGenerator {

	private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	private final SecureRandom random = new SecureRandom();
	private final int codeLength;

	public CodeGenerator(TutalinkProperties properties) {
		this.codeLength = properties.codeLength();
	}

	public String generate() {
		StringBuilder sb = new StringBuilder(codeLength);
		for (int i = 0; i < codeLength; i++) {
			sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
		}
		return sb.toString();
	}
}
