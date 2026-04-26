package com.trainit.backend.config;

import com.trainit.backend.entity.Role;
import com.trainit.backend.repository.RoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testy jednostkowe komponentu {@link RoleInitializer}.
 *
 * <p>Weryfikują logikę „seed-if-missing": gdy rola {@code USER} nie istnieje — zapisujemy ją;
 * w przeciwnym razie nie wykonujemy żadnej operacji zapisu.
 *
 * @see RoleInitializer
 */
@ExtendWith(MockitoExtension.class)
class RoleInitializerTest {

	@Mock
	private RoleRepository roleRepository;

	@InjectMocks
	private RoleInitializer roleInitializer;

	@Test
	@DisplayName("zapisuje rolę USER gdy nie istnieje w bazie")
	void run_createsUserRoleWhenMissing() throws Exception {
		when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

		roleInitializer.run();

		ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
		verify(roleRepository).save(captor.capture());
		assertThat(captor.getValue().getName()).isEqualTo("USER");
	}

	@Test
	@DisplayName("nie zapisuje gdy rola USER już istnieje")
	void run_doesNotSaveWhenUserRoleExists() throws Exception {
		Role existing = new Role();
		existing.setName("USER");
		when(roleRepository.findByName("USER")).thenReturn(Optional.of(existing));

		roleInitializer.run();

		verify(roleRepository, never()).save(org.mockito.ArgumentMatchers.any());
	}

	@Test
	@DisplayName("ignoruje argumenty linii poleceń przekazane do run")
	void run_ignoresCommandLineArgs() throws Exception {
		when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

		roleInitializer.run("--profile=dev", "--debug");

		verify(roleRepository).save(org.mockito.ArgumentMatchers.any(Role.class));
	}

	@Test
	@DisplayName("akceptuje pustą tablicę argumentów")
	void run_acceptsEmptyArgs() throws Exception {
		when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

		roleInitializer.run(new String[0]);

		verify(roleRepository).save(org.mockito.ArgumentMatchers.any(Role.class));
	}
}
