package com.trainit.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtr bezpieczeństwa odczytujący token JWT z nagłówka Authorization.
 *
 * <p>Jeśli token jest poprawny, filtr zapisuje obiekt {@link JwtPrincipal}
 * w kontekście bezpieczeństwa jako uwierzytelnionego użytkownika.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	/** Prefiks nagłówka Authorization używany do tokenów Bearer. */
	private static final String BEARER_PREFIX = "Bearer ";

	/** Serwis walidacji i dekodowania tokenów JWT. */
	private final JwtService jwtService;

	/**
	 * Tworzy filtr z zależnością do serwisu JWT.
	 *
	 * @param jwtService serwis obsługi tokenów
	 */
	public JwtAuthenticationFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
			String token = authHeader.substring(BEARER_PREFIX.length());
			try {
				JwtPrincipal principal = jwtService.parseToken(token);
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
						principal,
						null,
						List.of(new SimpleGrantedAuthority("ROLE_" + principal.role()))
				);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} catch (IllegalArgumentException ignored) {
				SecurityContextHolder.clearContext();
			}
		}
		filterChain.doFilter(request, response);
	}
}
