package com.jpmc.midascore.entity;

import com.jpmc.midascore.foundation.Transaction;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class TransactionListener {

    @KafkaListener(topics = "${midas.kafka.topic}", groupId = "midas-group")
    public void listen(Transaction transaction) {
        System.out.println("Received transaction: " + transaction);
    }
}

