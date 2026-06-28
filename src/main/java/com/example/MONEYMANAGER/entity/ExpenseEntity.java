package com.example.MONEYMANAGER.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "tbl_expense")
public class ExpenseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String expensename;

    private String icon;

    private LocalDate date;

    private BigDecimal amount;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createtime;

    @UpdateTimestamp
    private LocalDateTime updatetime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoryid", nullable = false)
    private CategoryEntity category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profileid", nullable = false)
    private ProfileEntity profile;

    @PrePersist
    public void prePersist() {
        if (this.date == null) {
            this.date = LocalDate.now();
        }
    }
}
