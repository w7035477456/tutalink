package com.tutamall.tutalink.repository;

import com.tutamall.tutalink.entity.Link;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * {@code @Repository} marks a Spring Data bean. Spring generates a proxy implementing
 * derived queries (findByCode) and @Query methods at startup.
 */
@Repository
public interface LinkRepository extends JpaRepository<Link, Long> {

	/** Derived query — Spring parses the method name into WHERE code = ? */
	Optional<Link> findByCode(String code);

	boolean existsByCode(String code);

	Page<Link> findAllByOrderByCreatedAtDesc(Pageable pageable);

	/**
	 * JPQL @Query example. Prefer this when the method name would be awkward.
	 */
	@Query("SELECT l FROM Link l WHERE l.code = :code AND l.active = true")
	Optional<Link> findActiveByCode(@Param("code") String code);

	/**
	 * N+1 PROBLEM (commented example):
	 *   for (Link l : linkRepository.findAll()) { l.getClicks().size(); }
	 * Each getClicks() fires a separate SELECT — N+1 queries.
	 *
	 * FIX: JOIN FETCH or @EntityGraph so clicks load in one query.
	 * With spring.jpa.open-in-view=false, accessing LAZY associations outside a
	 * transaction throws LazyInitializationException — load what you need inside @Transactional.
	 */
	@EntityGraph(attributePaths = "clicks")
	@Query("SELECT l FROM Link l WHERE l.code = :code")
	Optional<Link> findByCodeWithClicks(@Param("code") String code);
}
