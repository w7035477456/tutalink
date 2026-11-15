package com.tutamall.tutalink.service;

import com.tutamall.tutalink.config.TutalinkProperties;
import com.tutamall.tutalink.entity.Click;
import com.tutamall.tutalink.entity.Link;
import com.tutamall.tutalink.repository.ClickRepository;
import com.tutamall.tutalink.repository.LinkRepository;
import com.tutamall.tutalink.web.ClientInfoExtractor.ClientInfo;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClickService {

	private static final Logger log = LoggerFactory.getLogger(ClickService.class);

	private final ClickRepository clickRepository;
	private final LinkRepository linkRepository;
	private final String ipSalt;

	public ClickService(ClickRepository clickRepository, LinkRepository linkRepository,
			TutalinkProperties properties) {
		this.clickRepository = clickRepository;
		this.linkRepository = linkRepository;
		this.ipSalt = properties.ipSalt();
	}

	/**
	 * {@code @Async("clickExecutor")} runs on the named ThreadPoolTaskExecutor so the
	 * redirect can return 302 immediately. Writes go to the Primary.
	 * Losing a few clicks on JVM crash is acceptable for analytics.
	 */
	@Async("clickExecutor")
	@Transactional
	public void recordClickAsync(String code, ClientInfo clientInfo) {
		try {
			Link link = linkRepository.findByCode(code).orElse(null);
			if (link == null) {
				return;
			}
			Click click = new Click(
					hashIp(clientInfo.ip()),
					clientInfo.country(),
					truncate(clientInfo.referrer(), 2000),
					truncate(clientInfo.userAgent(), 2000),
					clientInfo.utmSource(),
					clientInfo.utmMedium(),
					clientInfo.utmCampaign()
			);
			link.addClick(click);
			clickRepository.save(click);
		} catch (Exception ex) {
			log.warn("Failed to record click for code={}: {}", code, ex.getMessage());
		}
	}

	private String hashIp(String ip) {
		if (ip == null || ip.isBlank()) {
			return null;
		}
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] digest = md.digest((ip + ipSalt).getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	private static String truncate(String s, int max) {
		if (s == null) {
			return null;
		}
		return s.length() <= max ? s : s.substring(0, max);
	}
}
