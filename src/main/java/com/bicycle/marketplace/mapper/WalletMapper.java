package com.bicycle.marketplace.mapper;

import com.bicycle.marketplace.dto.response.WalletResponse;
import com.bicycle.marketplace.entities.Wallet;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WalletMapper {
    WalletResponse toWalletResponse(Wallet wallet);
}
