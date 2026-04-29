package com.trainit.backend.repository;

import com.trainit.backend.entity.ExerciseResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseResultRepository extends JpaRepository<ExerciseResult, Integer> {

	List<ExerciseResult> findBySessionId(Integer sessionId);

	List<ExerciseResult> findByExerciseId(Integer exerciseId);

	void deleteBySessionId(Integer sessionId);
}
