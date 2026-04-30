package com.trainit.backend.dto;

public record ProfileAchievementResponse(
		String key,
		String label,
		boolean unlocked
) {
}
