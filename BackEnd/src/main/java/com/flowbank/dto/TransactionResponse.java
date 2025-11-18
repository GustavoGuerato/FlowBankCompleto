package com.flowbank.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private String description;
    private BigDecimal amount;
    private String type;
    private LocalDateTime timestamp;
}