package com.autospend.util;

import com.autospend.model.Transaction;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SmsParser {

    // Pattern to extract amount
    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
            "(?:Rs\\.?|INR)\\s*([\\d,]+(?:\\.\\d{1,2})?)",
            Pattern.CASE_INSENSITIVE
    );

    // Pattern to detect debit
    private static final Pattern DEBIT_PATTERN = Pattern.compile(
            "(?:debited|sent|paid|deducted)",
            Pattern.CASE_INSENSITIVE
    );

    // Pattern to detect credit
    private static final Pattern CREDIT_PATTERN = Pattern.compile(
            "(?:credited|received|added)",
            Pattern.CASE_INSENSITIVE
    );

    // Pattern to extract merchant from VPA
    private static final Pattern VPA_PATTERN = Pattern.compile(
            "to\\s+VPA\\s+([\\w.]+)@[\\w]+",
            Pattern.CASE_INSENSITIVE
    );

    // Pattern to extract merchant from Info
    private static final Pattern INFO_PATTERN = Pattern.compile(
            "Info:\\s*([\\w\\s]+?)(?:\\s+UPI|$)",
            Pattern.CASE_INSENSITIVE
    );

    // Pattern to extract contact name (sent to person)
    private static final Pattern CONTACT_PATTERN = Pattern.compile(
            "(?:sent to|paid to)\\s+([A-Za-z\\s]+?)(?:\\s+on|$)",
            Pattern.CASE_INSENSITIVE
    );

    public Transaction parse(String sms) {
        Transaction transaction = new Transaction();
        transaction.setRawSms(sms);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setStatus("UNCATEGORIZED");
        transaction.setCategory("Uncategorized");

        // Extract amount
        Matcher amountMatcher = AMOUNT_PATTERN.matcher(sms);
        if (amountMatcher.find()) {
            String amountStr = amountMatcher.group(1).replace(",", "");
            transaction.setAmount(Double.parseDouble(amountStr));
        }

        // Detect type — DEBIT or CREDIT
        if (DEBIT_PATTERN.matcher(sms).find()) {
            transaction.setType("DEBIT");
        } else if (CREDIT_PATTERN.matcher(sms).find()) {
            transaction.setType("CREDIT");
        } else {
            transaction.setType("UNKNOWN");
        }

        // Extract merchant name
        Matcher vpaMatcher = VPA_PATTERN.matcher(sms);
        Matcher infoMatcher = INFO_PATTERN.matcher(sms);
        Matcher contactMatcher = CONTACT_PATTERN.matcher(sms);

        if (vpaMatcher.find()) {
            // Extract from VPA like hariombakery@okaxis
            String vpa = vpaMatcher.group(1);
            transaction.setMerchantName(cleanMerchantName(vpa));
        } else if (infoMatcher.find()) {
            // Extract from Info: DMRC METRO UPI
            transaction.setMerchantName(infoMatcher.group(1).trim());
        } else if (contactMatcher.find()) {
            // It's a person payment
            transaction.setContactName(contactMatcher.group(1).trim());
            transaction.setMerchantName(null);
        }

        return transaction;
    }

    // Clean merchant name from VPA
    // hariombakery → Hari Om Bakery (basic cleaning)
    private String cleanMerchantName(String vpa) {
        return vpa.replace(".", " ").trim();
    }
}