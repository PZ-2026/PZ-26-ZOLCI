package com.trainit.backend.repository;

import com.trainit.backend.entity.ExerciseResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseResultRepository extends JpaRepository<ExerciseResult, Integer> {

	/**
	 * Wykonuje zapytanie repozytorium: `findBySessionId`.
	 * @param sessionId kryterium wyszukiwania
	 * @return wynik typu `List<ExerciseResult>`
	 */
	List<ExerciseResult> findBySessionId(Integer sessionId);

	/**
	 * Wykonuje zapytanie repozytorium: `findByExerciseId`.
	 * @param exerciseId kryterium wyszukiwania
	 * @return wynik typu `List<ExerciseResult>`
	 */
	List<ExerciseResult> findByExerciseId(Integer exerciseId);

	/**
	 * Wykonuje zapytanie repozytorium: `deleteBySessionId`.
	 * @param sessionId kryterium wyszukiwania
	 * @return wynik typu `void`
	 */
	void deleteBySessionId(Integer sessionId);
}
