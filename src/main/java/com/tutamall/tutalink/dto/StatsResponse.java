package com.tutamall.tutalink.dto;

import java.util.List;

public record StatsResponse(
		String code,
		long totalClicks,
		List<DayCount> clicksPerDay,
		List<NamedCount> topReferrers,
		List<NamedCount> topCountries,
		List<NamedCount> topUtmCampaigns,
		List<NamedCount> topUtmSources
) {
	public record DayCount(String day, long count) {
	}

	public record NamedCount(String name, long count) {
	}
}
