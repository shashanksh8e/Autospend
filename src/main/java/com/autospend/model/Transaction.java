package com.autospend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String type; // DEBIT or CREDIT

    @Column(nullable = false)
    private String category; // Food, Travel, Friends etc

    @Column(name = "merchant_name")
    private String merchantName;

    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "raw_sms")
    private String rawSms;

    @Column(nullable = false)
    private String status; // CATEGORIZED or UNCATEGORIZED

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}