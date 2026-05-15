package com.trainit.backend.repository;

import com.trainit.backend.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Integer> {

	/**
	 * Wykonuje zapytanie repozytorium: `findByName`.
	 * @param name kryterium wyszukiwania
	 * @return wynik typu `Optional<Exercise>`
	 */
	Optional<Exercise> findByName(String name);

	/**
	 * Wykonuje zapytanie repozytorium: `findByMuscleGroup`.
	 * @param muscleGroup kryterium wyszukiwania
	 * @return wynik typu `List<Exercise>`
	 */
	List<Exercise> findByMuscleGroup(String muscleGroup);

	/**
	 * Wykonuje zapytanie repozytorium: `findByIsCustomTrue`.
	 * @return wynik typu `List<Exercise>`
	 */
	List<Exercise> findByIsCustomTrue();

	/**
	 * Wykonuje zapytanie repozytorium: `findByCreatedByIdAndIsCustomTrue`.
	 * @param userId kryterium wyszukiwania
	 * @return wynik typu `List<Exercise>`
	 */
	List<Exercise> findByCreatedByIdAndIsCustomTrue(Integer userId);
}
