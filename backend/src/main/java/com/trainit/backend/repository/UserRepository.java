package com.trainit.backend.repository;

import com.trainit.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

	boolean existsByEmail(String email);
	boolean existsByEmailAndIdNot(String email, Integer id);

	Optional<User> findByEmail(String email);
}
