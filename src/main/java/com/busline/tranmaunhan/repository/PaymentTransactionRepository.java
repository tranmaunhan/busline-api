package com.busline.tranmaunhan.repository;

import com.busline.tranmaunhan.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    boolean existsBySepayId(Long sepayId);
}
