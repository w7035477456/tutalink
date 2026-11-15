package com.tutamall.tutalink.exception;

public class LinkExpiredException extends RuntimeException {
	public LinkExpiredException(String code) {
		super("Link expired or inactive: " + code);
	}
}
