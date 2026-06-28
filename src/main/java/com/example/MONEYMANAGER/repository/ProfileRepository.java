package com.example.MONEYMANAGER.repository;

import com.example.MONEYMANAGER.entity.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<ProfileEntity, Long> {

    Optional<ProfileEntity> findByEmail(String email);

    Optional<ProfileEntity> findByActivationtoken(String activationtoken);
}
