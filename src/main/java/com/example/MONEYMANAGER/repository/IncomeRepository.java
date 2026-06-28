package com.example.MONEYMANAGER.repository;

import com.example.MONEYMANAGER.entity.IncomeEntity;
import com.example.MONEYMANAGER.entity.ProfileEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface IncomeRepository extends JpaRepository<IncomeEntity, Long> {

    List<IncomeEntity> findByProfileIdOrderByDateDesc(Long profileId);

    List<IncomeEntity> findTop5ByProfileIdOrderByDateDesc(Long profileId);

    @Query("SELECT SUM(e.amount) FROM IncomeEntity e WHERE e.profile.id = :profileId")
    BigDecimal findTotalIncomeByProfileId(@Param("profileId") Long profileId);

    List<IncomeEntity> findByProfileIdAndDateBetweenAndIncomenameContainingIgnoreCase(
            Long profileId, LocalDate startDate, LocalDate endDate, String keyword, Sort sort);

    List<IncomeEntity> findByProfileAndDateBetween(ProfileEntity profile, LocalDate startDate, LocalDate endDate);

    Page<IncomeEntity> findByProfileId(Long profileId, Pageable pageable);
}
