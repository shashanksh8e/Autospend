package com.autospend.service;

import com.autospend.model.Transaction;
import com.autospend.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    // Add new transaction
    public Transaction addTransaction(Transaction transaction) {
        transaction.setCreatedAt(LocalDateTime.now());
        if(transaction.getTransactionDate() == null) {
            transaction.setTransactionDate(LocalDateTime.now());
        }
        if(transaction.getStatus() == null) {
            transaction.setStatus("UNCATEGORIZED");
        }
        return transactionRepository.save(transaction);
    }

    // Get all transactions by user
    public List<Transaction> getAllTransactions(Long userId) {
        return transactionRepository.findByUserId(userId);
    }

    // Get by category
    public List<Transaction> getByCategory(Long userId, String category) {
        return transactionRepository.findByUserIdAndCategory(userId, category);
    }

    // Get by type (DEBIT or CREDIT)
    public List<Transaction> getByType(Long userId, String type) {
        return transactionRepository.findByUserIdAndType(userId, type);
    }

    // Get uncategorized transactions
    public List<Transaction> getUncategorized(Long userId) {
        return transactionRepository.findByUserIdAndStatus(userId, "UNCATEGORIZED");
    }

    // Update transaction category
    public Transaction updateCategory(Long id, String category) {
        Optional<Transaction> optional = transactionRepository.findById(id);
        if(optional.isPresent()) {
            Transaction transaction = optional.get();
            transaction.setCategory(category);
            transaction.setStatus("CATEGORIZED");
            return transactionRepository.save(transaction);
        }
        return null;
    }

    // Delete transaction
    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
    }
}