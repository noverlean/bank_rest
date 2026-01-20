package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.util.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    Page<Card> findByUserId(Long userId, Pageable pageable);
    List<Card> findByUserId(Long userId);
    Optional<Card> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT c FROM Card c WHERE c.user.id = :userId AND " +
            "(LOWER(c.owner) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "c.maskedNumber LIKE CONCAT('%', :query, '%'))")
    Page<Card> searchByUser(@Param("userId") Long userId,
                            @Param("query") String query,
                            Pageable pageable);

    List<Card> findByExpiryDateBeforeAndStatus(LocalDate date, CardStatus status);

    @Modifying
    @Query("UPDATE Card c SET c.balance = c.balance + :amount WHERE c.id = :cardId")
    void updateBalance(@Param("cardId") Long cardId, @Param("amount") BigDecimal amount);

    long countByUserId(Long userId);
}