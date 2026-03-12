package com.bicycle.marketplace.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WalletTransactionResponse {
    int transactionId;
    int userId;
    double amount;
    String transactionType;
    String description;
    double balance;
}
