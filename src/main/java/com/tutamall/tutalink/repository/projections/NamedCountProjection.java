package com.tutamall.tutalink.repository.projections;

/**
 * Spring Data interface projection — Hibernate maps query columns to getters by name.
 */
public interface NamedCountProjection {
	String getName();
	long getCount();
}
