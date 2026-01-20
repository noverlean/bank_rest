package com.example.bankcards.repository;

import com.example.bankcards.entity.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {
    Page<Transfer> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT t FROM Transfer t WHERE t.user.id = :userId AND " +
            "(t.fromCard.id = :cardId OR t.toCard.id = :cardId)")
    Page<Transfer> findByUserIdAndCardId(@Param("userId") Long userId,
                                         @Param("cardId") Long cardId,
                                         Pageable pageable);

    @Query("SELECT SUM(t.amount) FROM Transfer t WHERE t.fromCard.id = :cardId " +
            "AND t.createdAt >= :startDate AND t.createdAt < :endDate")
    BigDecimal getTotalTransferredAmount(@Param("cardId") Long cardId,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);
}
