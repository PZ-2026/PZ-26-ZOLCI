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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testy jednostkowe komponentu {@link RoleInitializer}.
 *
 * <p>Weryfikują logikę „seed-if-missing" dla ról systemowych:
 * {@code USER}, {@code TRAINER} i {@code ADMIN}.
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
	@DisplayName("zapisuje trzy role systemowe gdy żadna nie istnieje")
	void run_createsSystemRolesWhenMissing() throws Exception {
		when(roleRepository.findByName("USER")).thenReturn(Optional.empty());
		when(roleRepository.findByName("TRAINER")).thenReturn(Optional.empty());
		when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());

		roleInitializer.run();

		ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
		verify(roleRepository, times(3)).save(captor.capture());
		assertThat(captor.getAllValues()).extracting(Role::getName)
				.containsExactlyInAnyOrder("USER", "TRAINER", "ADMIN");
	}

	@Test
	@DisplayName("nie zapisuje gdy wszystkie role systemowe już istnieją")
	void run_doesNotSaveWhenSystemRolesExist() throws Exception {
		Role user = new Role();
		user.setName("USER");
		Role trainer = new Role();
		trainer.setName("TRAINER");
		Role admin = new Role();
		admin.setName("ADMIN");
		when(roleRepository.findByName("USER")).thenReturn(Optional.of(user));
		when(roleRepository.findByName("TRAINER")).thenReturn(Optional.of(trainer));
		when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(admin));

		roleInitializer.run();

		verify(roleRepository, never()).save(org.mockito.ArgumentMatchers.any());
	}

	@Test
	@DisplayName("ignoruje argumenty linii poleceń przekazane do run")
	void run_ignoresCommandLineArgs() throws Exception {
		when(roleRepository.findByName("USER")).thenReturn(Optional.empty());
		when(roleRepository.findByName("TRAINER")).thenReturn(Optional.empty());
		when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());

		roleInitializer.run("--profile=dev", "--debug");

		verify(roleRepository, times(3)).save(org.mockito.ArgumentMatchers.any(Role.class));
	}

	@Test
	@DisplayName("akceptuje pustą tablicę argumentów")
	void run_acceptsEmptyArgs() throws Exception {
		when(roleRepository.findByName("USER")).thenReturn(Optional.empty());
		when(roleRepository.findByName("TRAINER")).thenReturn(Optional.empty());
		when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());

		roleInitializer.run(new String[0]);

		verify(roleRepository, times(3)).save(org.mockito.ArgumentMatchers.any(Role.class));
	}
}
