package com.tutamall.tutalink.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "clicks", schema = "tutalink")
public class Click {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@CreationTimestamp
	@Column(name = "clicked_at", nullable = false, updatable = false)
	private Instant clickedAt;

	/** SHA-256(IP + salt) — never store the raw IP. */
	@Column(name = "ip_hash", length = 64)
	private String ipHash;

	@Column(length = 8)
	private String country;

	@Column(columnDefinition = "TEXT")
	private String referrer;

	@Column(name = "user_agent", columnDefinition = "TEXT")
	private String userAgent;

	@Column(name = "utm_source")
	private String utmSource;

	@Column(name = "utm_medium")
	private String utmMedium;

	@Column(name = "utm_campaign")
	private String utmCampaign;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "link_id", nullable = false)
	private Link link;

	protected Click() {
	}

	public Click(String ipHash, String country, String referrer, String userAgent,
			String utmSource, String utmMedium, String utmCampaign) {
		this.ipHash = ipHash;
		this.country = country;
		this.referrer = referrer;
		this.userAgent = userAgent;
		this.utmSource = utmSource;
		this.utmMedium = utmMedium;
		this.utmCampaign = utmCampaign;
	}

	public Long getId() {
		return id;
	}

	public Instant getClickedAt() {
		return clickedAt;
	}

	public String getIpHash() {
		return ipHash;
	}

	public String getCountry() {
		return country;
	}

	public String getReferrer() {
		return referrer;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public String getUtmSource() {
		return utmSource;
	}

	public String getUtmMedium() {
		return utmMedium;
	}

	public String getUtmCampaign() {
		return utmCampaign;
	}

	public Link getLink() {
		return link;
	}

	public void setLink(Link link) {
		this.link = link;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Click other)) {
			return false;
		}
		return id != null && Objects.equals(id, other.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
