package com.bicycle.marketplace.mapper;

import com.bicycle.marketplace.dto.request.TransactionUpdateRequest;
import com.bicycle.marketplace.dto.response.TransactionResponse;
import com.bicycle.marketplace.entities.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toTransactionResponse(Transaction t) {
        if (t == null) return null;
        TransactionResponse r = new TransactionResponse();
        r.setTransactionId(t.getTransactionId());
        r.setBuyerId(t.getBuyer() != null ? t.getBuyer().getUserId() : null);
        r.setSellerId(t.getSeller() != null ? t.getSeller().getUserId() : null);
        r.setEventId(t.getEvent() != null ? t.getEvent().getEventId() : null);
        r.setListingId(t.getListing() != null ? t.getListing().getListingId() : null);
        r.setDepositId(t.getDeposit() != null ? t.getDeposit().getDepositId() : null);
        r.setReservationId(t.getReservation() != null ? t.getReservation().getReservationId() : null);
        r.setAmount(t.getAmount());
        r.setActualPrice(t.getActualPrice());
        r.setCreatedAt(t.getCreateAt());
        r.setUpdatedAt(t.getUpdateAt());
        r.setStatus(t.getStatus());
        return r;
    }

    public void updateTransaction(Transaction transaction, TransactionUpdateRequest request) {
        if (request == null) return;
        if (request.getStatus() != null) transaction.setStatus(request.getStatus());
        if (request.getAmount() != null) transaction.setAmount(request.getAmount());
        if (request.getActualPrice() != null) transaction.setActualPrice(request.getActualPrice());
    }
}
