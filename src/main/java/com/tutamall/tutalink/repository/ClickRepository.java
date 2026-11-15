package com.tutamall.tutalink.repository;

import com.tutamall.tutalink.entity.Click;
import com.tutamall.tutalink.repository.projections.DayCountProjection;
import com.tutamall.tutalink.repository.projections.NamedCountProjection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClickRepository extends JpaRepository<Click, Long> {

	long countByLinkId(Long linkId);

	/** Native aggregation for stats — last 30 days of clicks per day. */
	@Query(value = """
			SELECT to_char(c.clicked_at AT TIME ZONE 'UTC', 'YYYY-MM-DD') AS day,
			       COUNT(*) AS count
			FROM tutalink.clicks c
			WHERE c.link_id = :linkId
			  AND c.clicked_at >= NOW() - INTERVAL '30 days'
			GROUP BY day
			ORDER BY day
			""", nativeQuery = true)
	List<DayCountProjection> countPerDayLast30(@Param("linkId") Long linkId);

	@Query(value = """
			SELECT COALESCE(NULLIF(c.referrer, ''), '(direct)') AS name, COUNT(*) AS count
			FROM tutalink.clicks c
			WHERE c.link_id = :linkId
			GROUP BY name
			ORDER BY count DESC
			LIMIT 10
			""", nativeQuery = true)
	List<NamedCountProjection> topReferrers(@Param("linkId") Long linkId);

	@Query(value = """
			SELECT COALESCE(NULLIF(c.country, ''), 'ZZ') AS name, COUNT(*) AS count
			FROM tutalink.clicks c
			WHERE c.link_id = :linkId
			GROUP BY name
			ORDER BY count DESC
			LIMIT 10
			""", nativeQuery = true)
	List<NamedCountProjection> topCountries(@Param("linkId") Long linkId);

	@Query(value = """
			SELECT COALESCE(NULLIF(c.utm_campaign, ''), '(none)') AS name, COUNT(*) AS count
			FROM tutalink.clicks c
			WHERE c.link_id = :linkId
			GROUP BY name
			ORDER BY count DESC
			LIMIT 10
			""", nativeQuery = true)
	List<NamedCountProjection> topUtmCampaigns(@Param("linkId") Long linkId);
}
