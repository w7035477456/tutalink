package com.tutamall.tutalink.exception;

public class CodeTakenException extends RuntimeException {
	public CodeTakenException(String code) {
		super("Short code already taken: " + code);
	}
}
