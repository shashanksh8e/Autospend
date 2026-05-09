package com.autospend.controller;

import com.autospend.model.Transaction;
import com.autospend.model.User;
import com.autospend.service.TransactionService;
import com.autospend.util.SmsParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sms")
public class SmsController {

    @Autowired
    private SmsParser smsParser;

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/parse")
    public ResponseEntity<Transaction> parseSms(
            @RequestParam String sms,
            @RequestParam Long userId) {

        // Parse the SMS
        Transaction transaction = smsParser.parse(sms);

        // Attach user
        User user = new User();
        user.setId(userId);
        transaction.setUser(user);

        // Save to database
        Transaction saved = transactionService.addTransaction(transaction);

        return ResponseEntity.ok(saved);
    }
}