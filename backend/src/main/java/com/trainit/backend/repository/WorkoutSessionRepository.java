package com.trainit.backend.repository;

import com.trainit.backend.entity.WorkoutSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, Integer> {

	/**
	 * Wykonuje zapytanie repozytorium: `findByUserId`.
	 * @param userId kryterium wyszukiwania
	 * @return wynik typu `List<WorkoutSession>`
	 */
	List<WorkoutSession> findByUserId(Integer userId);

	/**
	 * Wykonuje zapytanie repozytorium: `findByUserIdAndStatus`.
	 * @param userId kryterium wyszukiwania
	 * @param status kryterium wyszukiwania
	 * @return wynik typu `List<WorkoutSession>`
	 */
	List<WorkoutSession> findByUserIdAndStatus(Integer userId, String status);

	/**
	 * Wykonuje zapytanie repozytorium: `findByIdAndUserId`.
	 * @param id kryterium wyszukiwania
	 * @param userId kryterium wyszukiwania
	 * @return wynik typu `Optional<WorkoutSession>`
	 */
	Optional<WorkoutSession> findByIdAndUserId(Integer id, Integer userId);

	/**
	 * Wykonuje zapytanie repozytorium: `findByWorkoutId`.
	 * @param workoutId kryterium wyszukiwania
	 * @return wynik typu `List<WorkoutSession>`
	 */
	List<WorkoutSession> findByWorkoutId(Integer workoutId);

	/**
	 * Wykonuje zapytanie repozytorium: `findByUserIdAndPlannedDateBetween`.
	 * @param userId kryterium wyszukiwania
	 * @param startDate kryterium wyszukiwania
	 * @param endDate kryterium wyszukiwania
	 * @return wynik typu `List<WorkoutSession>`
	 */
	List<WorkoutSession> findByUserIdAndPlannedDateBetween(Integer userId, LocalDateTime startDate, LocalDateTime endDate);

	/**
	 * Wykonuje zapytanie repozytorium: `findByUserIdAndCompletedDateBetween`.
	 * @param userId kryterium wyszukiwania
	 * @param startDate kryterium wyszukiwania
	 * @param endDate kryterium wyszukiwania
	 * @return wynik typu `List<WorkoutSession>`
	 */
	List<WorkoutSession> findByUserIdAndCompletedDateBetween(Integer userId, LocalDateTime startDate, LocalDateTime endDate);
}
