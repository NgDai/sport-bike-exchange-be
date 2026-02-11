package com.bicycle.marketplace.services;

import com.bicycle.marketplace.Repository.ITransactionRepository;
import com.bicycle.marketplace.dto.request.TransactionCreationRequest;
import com.bicycle.marketplace.dto.request.TransactionUpdateRequest;
import com.bicycle.marketplace.dto.response.TransactionResponse;
import com.bicycle.marketplace.entities.Transaction;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.TransactionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {
    @Autowired
    private ITransactionRepository transactionRepository;
    @Autowired
    private TransactionMapper transactionMapper;

    public TransactionResponse createTransaction(TransactionCreationRequest request) {
        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setStatus(request.getStatus());
        transaction.setCreateAt(request.getCreatedAt());
        // transaction.setCompletedAt(request.getCompletedAt()); // Field removed from
        // Entity
        return transactionMapper.toTransactionResponse(transactionRepository.save(transaction));
    }

    public TransactionResponse updateTransaction(int transactionId, TransactionUpdateRequest request) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));
        transactionMapper.updateTransaction(transaction, request);
        return transactionMapper.toTransactionResponse(transactionRepository.save(transaction));
    }

    public List<Transaction> findAllTransactions() {
        return transactionRepository.findAll();
    }

    public TransactionResponse findTransactionById(int transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));
        return transactionMapper.toTransactionResponse(transaction);
    }

    public String deleteTransaction(int transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));
        transactionRepository.delete(transaction);
        return "Transaction deleted successfully";
    }
}
