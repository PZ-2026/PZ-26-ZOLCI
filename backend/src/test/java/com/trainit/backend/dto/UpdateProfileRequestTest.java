package com.trainit.backend.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateProfileRequestTest {

	@Test
	void gettersAndSetters_work() {
		UpdateProfileRequest request = new UpdateProfileRequest();
		request.setFirstName("Jan");
		request.setLastName("Kowalski");
		request.setEmail("jan@example.com");
		request.setNewPassword("NoweHaslo123!");

		assertThat(request.getFirstName()).isEqualTo("Jan");
		assertThat(request.getLastName()).isEqualTo("Kowalski");
		assertThat(request.getEmail()).isEqualTo("jan@example.com");
		assertThat(request.getNewPassword()).isEqualTo("NoweHaslo123!");
	}
}
