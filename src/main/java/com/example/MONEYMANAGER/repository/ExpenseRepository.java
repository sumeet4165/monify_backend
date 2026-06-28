package com.example.MONEYMANAGER.repository;

import com.example.MONEYMANAGER.entity.ExpenseEntity;
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
public interface ExpenseRepository extends JpaRepository<ExpenseEntity, Long> {

    List<ExpenseEntity> findByProfileIdOrderByDateDesc(Long profileId);

    List<ExpenseEntity> findTop5ByProfileIdOrderByDateDesc(Long profileId);

    @Query("SELECT SUM(e.amount) FROM ExpenseEntity e WHERE e.profile.id = :profileId")
    BigDecimal findTotalExpenseByProfileId(@Param("profileId") Long profileId);

    List<ExpenseEntity> findByProfileIdAndDateBetweenAndExpensenameContainingIgnoreCase(
            Long profileId, LocalDate startDate, LocalDate endDate, String keyword, Sort sort);

    List<ExpenseEntity> findByProfileAndDateBetween(ProfileEntity profile, LocalDate startDate, LocalDate endDate);

    List<ExpenseEntity> findByProfileIdAndDate(Long profileId, LocalDate date);

    Page<ExpenseEntity> findByProfileId(Long profileId, Pageable pageable);
}
