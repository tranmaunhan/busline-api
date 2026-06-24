package com.busline.tranmaunhan.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "transactions",
        uniqueConstraints = @UniqueConstraint(name = "uk_transactions_sepay_id", columnNames = "sepay_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sepay_id", nullable = false, unique = true)
    private Long sepayId;

    @Column(name = "gateway")
    private String gateway;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "sub_account")
    private String subAccount;

    @Column(name = "code")
    private String code;

    @Column(name = "amount_in")
    private Long amountIn;

    @Column(name = "amount_out")
    private Long amountOut;

    @Column(name = "accumulated")
    private Long accumulated;

    @Lob
    @Column(name = "content")
    private String content;

    @Column(name = "reference_code")
    private String referenceCode;

    @Lob
    @Column(name = "body", nullable = false)
    private String body;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void setCreatedAtIfMissing() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
