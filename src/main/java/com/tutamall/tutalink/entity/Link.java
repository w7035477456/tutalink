package com.tutamall.tutalink.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hibernate.annotations.CreationTimestamp;

/**
 * {@code @Entity} marks a JPA-managed persistent class.
 * {@code @Table(schema = "tutalink")} keeps us out of the {@code public} schema.
 */
@Entity
@Table(name = "links", schema = "tutalink")
public class Link {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 32)
	private String code;

	@Column(name = "original_url", nullable = false, columnDefinition = "TEXT")
	private String originalUrl;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "expires_at")
	private Instant expiresAt;

	@Column(nullable = false)
	private boolean active = true;

	/** Optimistic locking — Hibernate increments on each update. */
	@Version
	private Long version;

	/**
	 * LAZY: clicks are not loaded until accessed. With {@code open-in-view=false},
	 * accessing this outside a transaction throws LazyInitializationException.
	 * Prefer JOIN FETCH / @EntityGraph when you need clicks (see LinkRepository).
	 */
	@OneToMany(mappedBy = "link", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<Click> clicks = new ArrayList<>();

	protected Link() {
		// JPA requires a no-arg constructor
	}

	public Link(String code, String originalUrl, Instant expiresAt) {
		this.code = code;
		this.originalUrl = originalUrl;
		this.expiresAt = expiresAt;
	}

	/** Keep both sides of the bidirectional association in sync. */
	public void addClick(Click click) {
		clicks.add(click);
		click.setLink(this);
	}

	public void removeClick(Click click) {
		clicks.remove(click);
		click.setLink(null);
	}

	public boolean isExpired() {
		return expiresAt != null && Instant.now().isAfter(expiresAt);
	}

	public Long getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getOriginalUrl() {
		return originalUrl;
	}

	public void setOriginalUrl(String originalUrl) {
		this.originalUrl = originalUrl;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(Instant expiresAt) {
		this.expiresAt = expiresAt;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Long getVersion() {
		return version;
	}

	public List<Click> getClicks() {
		return clicks;
	}

	/** equals/hashCode on business key (id) — never use Lombok @Data on entities. */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Link other)) {
			return false;
		}
		return id != null && Objects.equals(id, other.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
