package com.flowbank.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.flowbank.dto.TransferRequest;
import com.flowbank.model.User;
import com.flowbank.repository.UserRepository;
import com.flowbank.service.BankService;
import com.flowbank.util.JwtUtil;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/bank")
@CrossOrigin(origins = "*") // Permite conexão do HTML
public class BankController {

    @Autowired
    private BankService bankService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    // Endpoint para transferir PIX
    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(
            @RequestHeader("Authorization") String auth,
            @RequestBody TransferRequest req) {

        try {
            // 1. Autenticação: Remove "Bearer " e valida token
            String token = auth.replace("Bearer ", "");
            String emailSender = jwtUtil.extractUsername(token);

            if (emailSender == null || !jwtUtil.validateToken(token, emailSender)) {
                return ResponseEntity.status(401).body("Token inválido");
            }

            // 2. Busca o usuário que está enviando (Remetente)
            User sender = userRepository.findByEmail(emailSender).orElse(null);
            if (sender == null) {
                return ResponseEntity.status(404).body("Remetente não encontrado");
            }

            // 3. VERIFICAÇÃO: A Chave Pix (Destinatário) existe?
            // Assumindo que req.getToEmail() é a chave pix
            Optional<User> recipientOpt = userRepository.findByEmail(req.getToEmail());
            if (!recipientOpt.isPresent()) {
                return ResponseEntity.status(404).body("Chave Pix inválida ou usuário inexistente");
            }

            // Opcional: Impedir transferir para si mesmo
            if (emailSender.equals(req.getToEmail())) {
                return ResponseEntity.status(400).body("Não é possível transferir para si mesmo");
            }

            // 4. VERIFICAÇÃO: Saldo insuficiente (Retornar 401)
            // Verifica se o valor do envio é maior que o saldo atual
            if (sender.getBalance().compareTo(req.getAmount()) < 0) {
                return ResponseEntity.status(401).body("Saldo insuficiente");
            }

            // 5. Realiza transferência (Se passou por todas as validações)
            // Nota: O service deve cuidar de atualizar os saldos no banco
            String result = bankService.transfer(emailSender, req);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro na transferência: " + e.getMessage());
        }
    }

    // Endpoint para consultar saldo
    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(@RequestHeader("Authorization") String auth) {
        try {
            String token = auth.replace("Bearer ", "");
            String email = jwtUtil.extractUsername(token);

            if (email == null || !jwtUtil.validateToken(token, email)) {
                return ResponseEntity.status(401).body("Token inválido");
            }

            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null)
                return ResponseEntity.status(404).body("Usuário não encontrado");

            return ResponseEntity.ok(Map.of("balance", user.getBalance()));

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao carregar saldo: " + e.getMessage());
        }
    }

    @GetMapping("/extract")
    public ResponseEntity<?> getExtract(@RequestHeader("Authorization") String auth) {
        try {
            String token = auth.replace("Bearer ", "");
            String email = jwtUtil.extractUsername(token);

            if (email == null || !jwtUtil.validateToken(token, email)) {
                return ResponseEntity.status(401).body("Token inválido");
            }

            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null)
                return ResponseEntity.status(404).body("Usuário não encontrado");

            return ResponseEntity.ok(bankService.getExtract(user));

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao carregar extrato: " + e.getMessage());
        }
    }

}