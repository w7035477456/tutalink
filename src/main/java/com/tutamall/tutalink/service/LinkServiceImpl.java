package com.tutamall.tutalink.service;

import com.tutamall.tutalink.dto.CreateLinkRequest;
import com.tutamall.tutalink.dto.LinkResponse;
import com.tutamall.tutalink.dto.UpdateLinkRequest;
import com.tutamall.tutalink.entity.Link;
import com.tutamall.tutalink.exception.CodeTakenException;
import com.tutamall.tutalink.exception.LinkExpiredException;
import com.tutamall.tutalink.exception.LinkNotFoundException;
import com.tutamall.tutalink.mapper.LinkMapper;
import com.tutamall.tutalink.repository.LinkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@code @Service} stereotypes a business-logic bean (specialized {@code @Component}).
 * Constructor injection only — no field {@code @Autowired}.
 *
 * {@code @Transactional} works via a Spring AOP proxy around this bean. Calling one
 * transactional method from another method *on the same class* (self-invocation)
 * bypasses the proxy, so the second method does NOT start/join a transaction.
 */
@Service
public class LinkServiceImpl implements LinkService {

	private static final Logger log = LoggerFactory.getLogger(LinkServiceImpl.class);
	private static final int MAX_CODE_RETRIES = 5;

	private final LinkRepository linkRepository;
	private final CodeGenerator codeGenerator;
	private final LinkMapper linkMapper;

	public LinkServiceImpl(LinkRepository linkRepository, CodeGenerator codeGenerator, LinkMapper linkMapper) {
		this.linkRepository = linkRepository;
		this.codeGenerator = codeGenerator;
		this.linkMapper = linkMapper;
	}

	/**
	 * Write → Primary (readOnly=false). Return the created link from Primary
	 * (read-your-writes — do not immediately re-read from the Replica).
	 */
	@Override
	@Transactional
	public LinkResponse create(CreateLinkRequest request) {
		if (request.customCode() != null && !request.customCode().isBlank()) {
			if (linkRepository.existsByCode(request.customCode())) {
				throw new CodeTakenException(request.customCode());
			}
			Link link = new Link(request.customCode(), request.url(), request.expiresAt());
			return linkMapper.toResponse(linkRepository.save(link));
		}

		DataIntegrityViolationException last = null;
		for (int i = 0; i < MAX_CODE_RETRIES; i++) {
			String code = codeGenerator.generate();
			try {
				Link link = new Link(code, request.url(), request.expiresAt());
				return linkMapper.toResponse(linkRepository.saveAndFlush(link));
			} catch (DataIntegrityViolationException ex) {
				last = ex;
				log.warn("Short code collision on '{}', retrying ({}/{})", code, i + 1, MAX_CODE_RETRIES);
			}
		}
		throw new IllegalStateException("Could not generate a unique short code after " + MAX_CODE_RETRIES + " attempts", last);
	}

	/** Read → Replica via AbstractRoutingDataSource when readOnly=true. */
	@Override
	@Transactional(readOnly = true)
	@Cacheable(cacheNames = "links", key = "'tutalink:link:' + #code")
	public LinkResponse getByCode(String code) {
		Link link = linkRepository.findByCode(code)
				.orElseThrow(() -> new LinkNotFoundException(code));
		return linkMapper.toResponse(link);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<LinkResponse> list(Pageable pageable) {
		return linkRepository.findAllByOrderByCreatedAtDesc(pageable).map(linkMapper::toResponse);
	}

	@Override
	@Transactional
	@CacheEvict(cacheNames = "links", key = "'tutalink:link:' + #code")
	public LinkResponse update(String code, UpdateLinkRequest request) {
		Link link = linkRepository.findByCode(code)
				.orElseThrow(() -> new LinkNotFoundException(code));
		if (request.active() != null) {
			link.setActive(request.active());
		}
		if (request.expiresAt() != null) {
			link.setExpiresAt(request.expiresAt());
		}
		return linkMapper.toResponse(linkRepository.save(link));
	}

	@Override
	@Transactional
	@CacheEvict(cacheNames = "links", key = "'tutalink:link:' + #code")
	public void delete(String code) {
		Link link = linkRepository.findByCode(code)
				.orElseThrow(() -> new LinkNotFoundException(code));
		linkRepository.delete(link);
	}

	/**
	 * Cached redirect lookup. Must stay on Primary-or-replica read path;
	 * click recording is async separately.
	 */
	@Override
	@Transactional(readOnly = true)
	@Cacheable(cacheNames = "redirects", key = "'tutalink:redirect:' + #code")
	public String resolveForRedirect(String code) {
		Link link = linkRepository.findByCode(code)
				.orElseThrow(() -> new LinkNotFoundException(code));
		if (!link.isActive() || link.isExpired()) {
			throw new LinkExpiredException(code);
		}
		return link.getOriginalUrl();
	}
}
