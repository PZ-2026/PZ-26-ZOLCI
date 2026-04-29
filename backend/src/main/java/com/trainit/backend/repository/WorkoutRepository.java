package com.trainit.backend.repository;

import com.trainit.backend.entity.Workout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, Integer> {

	List<Workout> findByUserId(Integer userId);

	Optional<Workout> findByIdAndUserId(Integer id, Integer userId);

	List<Workout> findByUserIdAndName(Integer userId, String name);
}
