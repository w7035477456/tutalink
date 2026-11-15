package com.tutamall.tutalink.exception;

public class LinkNotFoundException extends RuntimeException {
	public LinkNotFoundException(String code) {
		super("Link not found: " + code);
	}
}
