package com.trainit.backend.repository;

import com.trainit.backend.entity.Workout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, Integer> {

	/**
	 * Wykonuje zapytanie repozytorium: `findByUserId`.
	 * @param userId kryterium wyszukiwania
	 * @return wynik typu `List<Workout>`
	 */
	List<Workout> findByUserId(Integer userId);

	/**
	 * Wykonuje zapytanie repozytorium: `findByIdAndUserId`.
	 * @param id kryterium wyszukiwania
	 * @param userId kryterium wyszukiwania
	 * @return wynik typu `Optional<Workout>`
	 */
	Optional<Workout> findByIdAndUserId(Integer id, Integer userId);

	/**
	 * Wykonuje zapytanie repozytorium: `findByUserIdAndName`.
	 * @param userId kryterium wyszukiwania
	 * @param name kryterium wyszukiwania
	 * @return wynik typu `List<Workout>`
	 */
	List<Workout> findByUserIdAndName(Integer userId, String name);
}
