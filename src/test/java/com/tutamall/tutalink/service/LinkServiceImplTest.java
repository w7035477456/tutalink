package com.tutamall.tutalink.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tutamall.tutalink.dto.CreateLinkRequest;
import com.tutamall.tutalink.dto.LinkResponse;
import com.tutamall.tutalink.entity.Link;
import com.tutamall.tutalink.exception.CodeTakenException;
import com.tutamall.tutalink.exception.LinkNotFoundException;
import com.tutamall.tutalink.mapper.LinkMapper;
import com.tutamall.tutalink.repository.LinkRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LinkServiceImplTest {

	@Mock LinkRepository linkRepository;
	@Mock CodeGenerator codeGenerator;
	@Mock LinkMapper linkMapper;
	@InjectMocks LinkServiceImpl linkService;

	@Test
	void createWithCustomCodePersists() {
		var req = new CreateLinkRequest("https://tutamall.com/mall", "fb3", null);
		when(linkRepository.existsByCode("fb3")).thenReturn(false);
		Link saved = new Link("fb3", req.url(), null);
		when(linkRepository.save(any(Link.class))).thenReturn(saved);
		when(linkMapper.toResponse(saved)).thenReturn(
				new LinkResponse(1L, "fb3", req.url(), "http://localhost:4100/l/fb3", null, null, true));

		LinkResponse resp = linkService.create(req);

		assertThat(resp.code()).isEqualTo("fb3");
		verify(linkRepository).save(any(Link.class));
	}

	@Test
	void createWithTakenCustomCodeThrows409() {
		var req = new CreateLinkRequest("https://tutamall.com", "taken", null);
		when(linkRepository.existsByCode("taken")).thenReturn(true);
		assertThatThrownBy(() -> linkService.create(req)).isInstanceOf(CodeTakenException.class);
	}

	@Test
	void getUnknownCodeThrows404() {
		when(linkRepository.findByCode("nope")).thenReturn(Optional.empty());
		assertThatThrownBy(() -> linkService.getByCode("nope")).isInstanceOf(LinkNotFoundException.class);
	}
}
