package com.trainit.backend.repository;

import com.trainit.backend.entity.WorkoutExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutExerciseRepository extends JpaRepository<WorkoutExercise, Integer> {

	/**
	 * Wykonuje zapytanie repozytorium: `findByWorkoutId`.
	 * @param workoutId kryterium wyszukiwania
	 * @return wynik typu `List<WorkoutExercise>`
	 */
	List<WorkoutExercise> findByWorkoutId(Integer workoutId);

	/**
	 * Wykonuje zapytanie repozytorium: `deleteByWorkoutId`.
	 * @param workoutId kryterium wyszukiwania
	 * @return wynik typu `void`
	 */
	void deleteByWorkoutId(Integer workoutId);
}
