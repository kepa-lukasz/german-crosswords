package com.germancrosswords.germancrosswords.Classes.Security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {

    // Szukanie po loginie (używane przy klasycznym logowaniu)
    Optional<User> findByUsername(String username);

    // UserRepo.java
    List<User> findTop5ByOrderByPointsDesc();

    // Szukanie po e-mailu (używane przy logowaniu Google)
    Optional<User> findByEmail(String email);

    // Walidacja przy rejestracji
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}