package com.autospend.controller;

import com.autospend.model.Transaction;
import com.autospend.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    // Add new transaction
    @PostMapping("/add")
    public ResponseEntity<Transaction> addTransaction(
            @RequestBody Transaction transaction) {
        Transaction saved = transactionService.addTransaction(transaction);
        return ResponseEntity.ok(saved);
    }

    // Get all transactions for a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Transaction>> getAllTransactions(
            @PathVariable Long userId) {
        List<Transaction> transactions =
                transactionService.getAllTransactions(userId);
        return ResponseEntity.ok(transactions);
    }

    // Get by category
    @GetMapping("/user/{userId}/category/{category}")
    public ResponseEntity<List<Transaction>> getByCategory(
            @PathVariable Long userId,
            @PathVariable String category) {
        return ResponseEntity.ok(
                transactionService.getByCategory(userId, category));
    }

    // Get by type
    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<Transaction>> getByType(
            @PathVariable Long userId,
            @PathVariable String type) {
        return ResponseEntity.ok(
                transactionService.getByType(userId, type));
    }

    // Get uncategorized
    @GetMapping("/user/{userId}/uncategorized")
    public ResponseEntity<List<Transaction>> getUncategorized(
            @PathVariable Long userId) {
        return ResponseEntity.ok(
                transactionService.getUncategorized(userId));
    }

    // Update category manually
    @PutMapping("/{id}/category")
    public ResponseEntity<Transaction> updateCategory(
            @PathVariable Long id,
            @RequestParam String category) {
        Transaction updated = transactionService.updateCategory(id, category);
        if(updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    // Delete transaction
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTransaction(
            @PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.ok("Transaction deleted successfully!");
    }
}