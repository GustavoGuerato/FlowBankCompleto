package com.flowbank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.flowbank.dto.TransactionResponse;
import com.flowbank.dto.TransferRequest;
import com.flowbank.model.Transaction;
import com.flowbank.model.User;
import com.flowbank.repository.TransactionRepository;
import com.flowbank.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BankService {

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private TransactionRepository txRepo;

    public String transfer(String fromEmail, TransferRequest req) {
        User from = userRepo.findByEmail(fromEmail).orElseThrow();
        User to = userRepo.findByEmail(req.getToEmail()).orElseThrow();

        if (from.getBalance().compareTo(req.getAmount()) < 0) {
            return "Saldo insuficiente";
        }

        from.setBalance(from.getBalance().subtract(req.getAmount()));
        to.setBalance(to.getBalance().add(req.getAmount()));

        Transaction tx = Transaction.builder()
                .fromUser(from)
                .toUser(to)
                .amount(req.getAmount())
                .build();

        txRepo.save(tx);
        userRepo.save(from);
        userRepo.save(to);

        return "Transferência realizada com sucesso";
    }

    public List<TransactionResponse> getExtract(User user) {

        List<Transaction> list = txRepo.findByFromUserOrToUserOrderByTimestampDesc(user, user);

        return list.stream().map(tx -> {
            boolean entrada = tx.getToUser().getId().equals(user.getId());

            return TransactionResponse.builder()
                    .description(entrada
                            ? "Pix Recebido - " + tx.getFromUser().getName()
                            : "Pix Enviado - " + tx.getToUser().getName())
                    .amount(tx.getAmount())
                    .type(entrada ? "entrada" : "saida")
                    .timestamp(tx.getTimestamp())
                    .build();

        }).toList();
    }
}