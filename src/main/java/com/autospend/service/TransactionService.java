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

    @Autowired
    private GeminiService geminiService;
    // Add new transaction
    public Transaction addTransaction(Transaction transaction) {
        transaction.setCreatedAt(LocalDateTime.now());
        if(transaction.getTransactionDate() == null) {
            transaction.setTransactionDate(LocalDateTime.now());
        }

        String category = geminiService.categorizeTransaction(
                transaction.getMerchantName(),
                transaction.getContactName(),
                transaction.getRawSms()
        );
        transaction.setCategory(category);

        if(category.equals("Uncategorized")) {
            transaction.setStatus("UNCATEGORIZED");
        } else {
            transaction.setStatus("CATEGORIZED");
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
    // Export transactions to CSV
    public String exportToCsv(Long userId) {
        List<Transaction> transactions = transactionRepository.findByUserId(userId);

        StringBuilder csv = new StringBuilder();
        csv.append("Date,Merchant,Contact,Category,Type,Amount,Status\n");

        for (Transaction t : transactions) {
            csv.append(t.getTransactionDate()).append(",");
            csv.append(t.getMerchantName() != null ? t.getMerchantName() : "").append(",");
            csv.append(t.getContactName() != null ? t.getContactName() : "").append(",");
            csv.append(t.getCategory()).append(",");
            csv.append(t.getType()).append(",");
            csv.append(t.getAmount()).append(",");
            csv.append(t.getStatus()).append("\n");
        }

        return csv.toString();
    }
}