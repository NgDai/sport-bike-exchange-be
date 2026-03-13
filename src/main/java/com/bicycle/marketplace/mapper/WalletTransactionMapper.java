package com.bicycle.marketplace.mapper;

import com.bicycle.marketplace.dto.response.WalletTransactionResponse;
import com.bicycle.marketplace.entities.WalletTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WalletTransactionMapper {
        @Mapping(source = "wallet.user.userId", target = "userId")
        @Mapping(source = "walletTransId", target = "transactionId")
        @Mapping(source = "type", target = "transactionType")
        @Mapping(source = "balance", target = "balance")
        WalletTransactionResponse toWalletTransactionResponse(WalletTransaction walletTransaction);
}
