package com.trainit.backend.repository;

import com.trainit.backend.entity.WorkoutSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, Integer> {

	List<WorkoutSession> findByUserId(Integer userId);

	List<WorkoutSession> findByUserIdAndStatus(Integer userId, String status);

	Optional<WorkoutSession> findByIdAndUserId(Integer id, Integer userId);

	List<WorkoutSession> findByWorkoutId(Integer workoutId);

	List<WorkoutSession> findByUserIdAndPlannedDateBetween(Integer userId, LocalDateTime startDate, LocalDateTime endDate);

	List<WorkoutSession> findByUserIdAndCompletedDateBetween(Integer userId, LocalDateTime startDate, LocalDateTime endDate);
}
