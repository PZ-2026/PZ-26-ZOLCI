package com.trainit.backend.repository;

import com.trainit.backend.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Integer> {

	Optional<Exercise> findByName(String name);

	List<Exercise> findByMuscleGroup(String muscleGroup);

	List<Exercise> findByIsCustomTrue();

	List<Exercise> findByCreatedByIdAndIsCustomTrue(Integer userId);
}
