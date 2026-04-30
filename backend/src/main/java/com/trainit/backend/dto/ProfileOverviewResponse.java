package com.trainit.backend.dto;

import java.util.List;

public record ProfileOverviewResponse(
		String profileName,
		String memberSinceText,
		String workoutsText,
		String totalHoursText,
		String streakText,
		List<Float> weeklyHours,
		List<ProfileRecordResponse> personalRecords,
		List<ProfileAchievementResponse> achievements,
		List<FeatureItemResponse> summaryItems
) {
}
