package com.tutamall.tutalink.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * {@code @RestControllerAdvice} = {@code @ControllerAdvice} + {@code @ResponseBody}.
 * Central place for {@code @ExceptionHandler} methods returning RFC 7807 ProblemDetail.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(LinkNotFoundException.class)
	public ProblemDetail handleNotFound(LinkNotFoundException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(LinkExpiredException.class)
	public ProblemDetail handleExpired(LinkExpiredException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.GONE, ex.getMessage());
	}

	@ExceptionHandler(CodeTakenException.class)
	public ProblemDetail handleTaken(CodeTakenException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
		ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
		FieldError first = ex.getBindingResult().getFieldError();
		if (first != null) {
			pd.setProperty("field", first.getField());
			pd.setProperty("rejected", first.getDefaultMessage());
		}
		return pd;
	}

	@ExceptionHandler(Exception.class)
	public ProblemDetail handleGeneric(Exception ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
	}
}
