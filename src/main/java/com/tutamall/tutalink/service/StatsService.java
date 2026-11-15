package com.tutamall.tutalink.service;

import com.tutamall.tutalink.dto.StatsResponse;
import com.tutamall.tutalink.entity.Link;
import com.tutamall.tutalink.exception.LinkNotFoundException;
import com.tutamall.tutalink.repository.ClickRepository;
import com.tutamall.tutalink.repository.LinkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatsService {

	private final LinkRepository linkRepository;
	private final ClickRepository clickRepository;

	public StatsService(LinkRepository linkRepository, ClickRepository clickRepository) {
		this.linkRepository = linkRepository;
		this.clickRepository = clickRepository;
	}

	@Transactional(readOnly = true)
	public StatsResponse statsFor(String code) {
		Link link = linkRepository.findByCode(code)
				.orElseThrow(() -> new LinkNotFoundException(code));
		Long linkId = link.getId();
		return new StatsResponse(
				code,
				clickRepository.countByLinkId(linkId),
				clickRepository.countPerDayLast30(linkId).stream()
						.map(p -> new StatsResponse.DayCount(p.getDay(), p.getCount()))
						.toList(),
				clickRepository.topReferrers(linkId).stream()
						.map(p -> new StatsResponse.NamedCount(p.getName(), p.getCount()))
						.toList(),
				clickRepository.topCountries(linkId).stream()
						.map(p -> new StatsResponse.NamedCount(p.getName(), p.getCount()))
						.toList(),
				clickRepository.topUtmCampaigns(linkId).stream()
						.map(p -> new StatsResponse.NamedCount(p.getName(), p.getCount()))
						.toList(),
				clickRepository.topUtmSources(linkId).stream()
						.map(p -> new StatsResponse.NamedCount(p.getName(), p.getCount()))
						.toList()
		);
	}
}
