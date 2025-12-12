package com.innowise.userservice.repository;

import com.innowise.userservice.entity.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long>,
        JpaSpecificationExecutor<PaymentCard> {


    @Query("SELECT pc FROM PaymentCard pc WHERE pc.user.id = :userId")
    List<PaymentCard> findAllByUserId(@Param("userId") long id);

    @Query(
            value = "SELECT * FROM payment_cards pc WHERE pc.number = :cardNumber",
            nativeQuery = true
    )
    Optional<PaymentCard> findByNumber(@Param("cardNumber") String number);
}
