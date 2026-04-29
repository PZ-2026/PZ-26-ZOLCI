package com.trainit.backend.service;

import com.trainit.backend.dto.CompleteWorkoutSessionRequest;
import com.trainit.backend.dto.CreateWorkoutSessionRequest;
import com.trainit.backend.dto.ExerciseResultDto;
import com.trainit.backend.dto.ExerciseResultRequest;
import com.trainit.backend.dto.WorkoutSessionDto;
import com.trainit.backend.entity.Exercise;
import com.trainit.backend.entity.ExerciseResult;
import com.trainit.backend.entity.User;
import com.trainit.backend.entity.Workout;
import com.trainit.backend.entity.WorkoutSession;
import com.trainit.backend.repository.ExerciseRepository;
import com.trainit.backend.repository.ExerciseResultRepository;
import com.trainit.backend.repository.UserRepository;
import com.trainit.backend.repository.WorkoutRepository;
import com.trainit.backend.repository.WorkoutSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class WorkoutSessionService {

	private final WorkoutSessionRepository workoutSessionRepository;
	private final WorkoutRepository workoutRepository;
	private final UserRepository userRepository;
	private final ExerciseRepository exerciseRepository;
	private final ExerciseResultRepository exerciseResultRepository;

	public WorkoutSessionService(WorkoutSessionRepository workoutSessionRepository,
								 WorkoutRepository workoutRepository,
								 UserRepository userRepository,
								 ExerciseRepository exerciseRepository,
								 ExerciseResultRepository exerciseResultRepository) {
		this.workoutSessionRepository = workoutSessionRepository;
		this.workoutRepository = workoutRepository;
		this.userRepository = userRepository;
		this.exerciseRepository = exerciseRepository;
		this.exerciseResultRepository = exerciseResultRepository;
	}

	public WorkoutSessionDto createSession(Integer userId, CreateWorkoutSessionRequest request) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("User not found"));

		Workout workout = workoutRepository.findById(request.getWorkoutId())
				.orElseThrow(() -> new RuntimeException("Workout not found"));

		WorkoutSession session = new WorkoutSession();
		session.setUser(user);
		session.setWorkout(workout);
		session.setPlannedDate(request.getPlannedDate());
		session.setStatus("PLANNED");

		WorkoutSession saved = workoutSessionRepository.save(session);
		return mapToDto(saved);
	}

	public WorkoutSessionDto getSession(Integer sessionId, Integer userId) {
		WorkoutSession session = workoutSessionRepository.findByIdAndUserId(sessionId, userId)
				.orElseThrow(() -> new RuntimeException("Workout session not found"));
		return mapToDto(session);
	}

	public List<WorkoutSessionDto> getUserSessions(Integer userId) {
		List<WorkoutSession> sessions = workoutSessionRepository.findByUserId(userId);
		return sessions.stream()
				.map(this::mapToDto)
				.collect(Collectors.toList());
	}

	public List<WorkoutSessionDto> getUserSessionsByStatus(Integer userId, String status) {
		List<WorkoutSession> sessions = workoutSessionRepository.findByUserIdAndStatus(userId, status);
		return sessions.stream()
				.map(this::mapToDto)
				.collect(Collectors.toList());
	}

	public List<WorkoutSessionDto> getSessionsBetweenDates(Integer userId, LocalDateTime startDate, LocalDateTime endDate) {
		List<WorkoutSession> sessions = workoutSessionRepository.findByUserIdAndPlannedDateBetween(userId, startDate, endDate);
		return sessions.stream()
				.map(this::mapToDto)
				.collect(Collectors.toList());
	}

	public WorkoutSessionDto completeSession(Integer sessionId, Integer userId, CompleteWorkoutSessionRequest request) {
		WorkoutSession session = workoutSessionRepository.findByIdAndUserId(sessionId, userId)
				.orElseThrow(() -> new RuntimeException("Workout session not found"));

		session.setStatus("COMPLETED");
		session.setCompletedDate(LocalDateTime.now());
		session.setDuration(request.getDuration());

		if (request.getExerciseResults() != null) {
			for (ExerciseResultRequest resultRequest : request.getExerciseResults()) {
				Exercise exercise = exerciseRepository.findById(resultRequest.getExerciseId())
						.orElseThrow(() -> new RuntimeException("Exercise not found"));

				ExerciseResult result = new ExerciseResult();
				result.setSession(session);
				result.setExercise(exercise);
				result.setSetsDone(resultRequest.getSetsDone());
				result.setRepsDone(resultRequest.getRepsDone());
				result.setWeightUsed(resultRequest.getWeightUsed());
				result.setDuration(resultRequest.getDuration());
				result.setNotes(resultRequest.getNotes());

				exerciseResultRepository.save(result);
			}
		}

		WorkoutSession updated = workoutSessionRepository.save(session);
		return mapToDto(updated);
	}

	public void cancelSession(Integer sessionId, Integer userId) {
		WorkoutSession session = workoutSessionRepository.findByIdAndUserId(sessionId, userId)
				.orElseThrow(() -> new RuntimeException("Workout session not found"));

		session.setStatus("CANCELLED");
		workoutSessionRepository.save(session);
	}

	public void deleteSession(Integer sessionId, Integer userId) {
		WorkoutSession session = workoutSessionRepository.findByIdAndUserId(sessionId, userId)
				.orElseThrow(() -> new RuntimeException("Workout session not found"));

		exerciseResultRepository.deleteBySessionId(sessionId);
		workoutSessionRepository.delete(session);
	}

	private WorkoutSessionDto mapToDto(WorkoutSession session) {
		WorkoutSessionDto dto = new WorkoutSessionDto();
		dto.setId(session.getId());
		dto.setWorkoutId(session.getWorkout().getId());
		dto.setWorkoutName(session.getWorkout().getName());
		dto.setPlannedDate(session.getPlannedDate());
		dto.setCompletedDate(session.getCompletedDate());
		dto.setStatus(session.getStatus());
		dto.setDuration(session.getDuration());

		List<ExerciseResult> results = exerciseResultRepository.findBySessionId(session.getId());
		dto.setExerciseResults(results.stream()
				.map(this::mapExerciseResultToDto)
				.collect(Collectors.toList()));

		return dto;
	}

	private ExerciseResultDto mapExerciseResultToDto(ExerciseResult result) {
		ExerciseResultDto dto = new ExerciseResultDto();
		dto.setId(result.getId());
		dto.setExerciseId(result.getExercise().getId());
		dto.setExerciseName(result.getExercise().getName());
		dto.setSetsDone(result.getSetsDone());
		dto.setRepsDone(result.getRepsDone());
		dto.setWeightUsed(result.getWeightUsed());
		dto.setDuration(result.getDuration());
		dto.setNotes(result.getNotes());
		return dto;
	}
}
