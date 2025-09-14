package com.jpmc.midascore.service;


import ch.qos.logback.classic.Logger;
import com.jpmc.midascore.component.Incentive;
import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.User;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class TransactionService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final RestTemplate restTemplate;

    public TransactionService(UserRepository userRepository, TransactionRepository transactionRepository, RestTemplate restTemplate) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public boolean processTransaction(long senderId, long recipientId, double amount) {
        Optional<UserRecord> senderExist = userRepository.findById(senderId);
        Optional<UserRecord> recipientExist = userRepository.findById(recipientId);

        if(senderExist.isEmpty() || recipientExist.isEmpty()) {
            return false;
        }

        UserRecord sender = senderExist.get();
        UserRecord recipient = recipientExist.get();

        if(sender.getBalance() < amount) return false;

        Transaction tx = new Transaction(senderId, recipientId, (float) amount); // provided class
        Incentive incentive = restTemplate.postForObject(
                "http://localhost:8080/incentive",
                tx,
                Incentive.class
        );

        double incentiveAmount = (incentive != null) ? incentive.getAmount() : 0.0;

        sender.setBalance((float) (sender.getBalance() - amount));
        recipient.setBalance((float) (recipient.getBalance() + amount + incentiveAmount));

        userRepository.save(sender);
        userRepository.save(recipient);
        UserRecord waldorf = userRepository.findByName("waldorf")
                .orElseThrow(() -> new RuntimeException("Waldorf not found"));



        TransactionRecord record = new TransactionRecord();
        record.setSender(sender);
        record.setRecipient(recipient);
        record.setAmount(amount);
        record.setIncentive(incentiveAmount);

        transactionRepository.save(record);

        return true;
    }
}
