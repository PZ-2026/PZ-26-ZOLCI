package com.trainit.backend.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

	@AfterEach
	void clearContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void doFilter_withValidToken_setsAuthentication() throws Exception {
		JwtService jwtService = mock(JwtService.class);
		JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
		FilterChain chain = mock(FilterChain.class);
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer abc");

		when(jwtService.parseToken("abc")).thenReturn(new JwtPrincipal(2, "u@t.com", "USER"));

		filter.doFilter(request, response, chain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
		verify(chain).doFilter(request, response);
	}

	@Test
	void doFilter_withInvalidToken_clearsAuthentication() throws Exception {
		JwtService jwtService = mock(JwtService.class);
		JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
		FilterChain chain = mock(FilterChain.class);
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer bad");

		doThrow(new IllegalArgumentException("bad token")).when(jwtService).parseToken("bad");

		filter.doFilter(request, response, chain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verify(chain).doFilter(request, response);
	}

	@Test
	void doFilter_withoutAuthorizationHeader_passesThrough() throws Exception {
		JwtService jwtService = mock(JwtService.class);
		JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
		FilterChain chain = mock(FilterChain.class);
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, chain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verify(chain).doFilter(request, response);
	}
}
