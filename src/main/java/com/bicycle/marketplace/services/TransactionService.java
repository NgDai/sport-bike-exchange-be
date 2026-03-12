package com.bicycle.marketplace.services;

import com.bicycle.marketplace.dto.request.TransactionCreationRequest;
import com.bicycle.marketplace.dto.request.TransactionUpdateRequest;
import com.bicycle.marketplace.dto.response.TransactionResponse;
import com.bicycle.marketplace.entities.*;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.TransactionMapper;
import com.bicycle.marketplace.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private ITransactionRepository transactionRepository;
    @Autowired
    private TransactionMapper transactionMapper;
    @Autowired
    private IEventRepository eventRepository;
    @Autowired
    private IBikeListingRepository bikeListingRepository;
    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private IDepositRepository depositRepository;
    @Autowired
    private IReservationRepository reservationRepository;

    @Transactional
    public TransactionResponse createTransaction(TransactionCreationRequest request) {
        Events event = null;
        if (request.getEventId() != null) {
            event = eventRepository.findById(request.getEventId())
                    .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        }
        BikeListing listing = null;
        if (request.getListingId() != null) {
            listing = bikeListingRepository.findById(request.getListingId())
                    .orElseThrow(() -> new AppException(ErrorCode.BIKE_LISTING_NOT_FOUND));
        }
        Users buyer = null;
        if (request.getBuyerId() != null) {
            buyer = userRepository.findById(request.getBuyerId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        }
        Users seller = null;
        if (request.getSellerId() != null) {
            seller = userRepository.findById(request.getSellerId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        }
        Deposit deposit = null;
        if (request.getDepositId() != null) {
            deposit = depositRepository.findById(request.getDepositId())
                    .orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_NOT_FOUND));
            // Một deposit chỉ được gắn với một transaction (unique constraint deposit_id)
            transactionRepository.findByDeposit_DepositId(request.getDepositId())
                    .ifPresent(existing -> {
                        throw new AppException(ErrorCode.DEPOSIT_ALREADY_HAS_TRANSACTION);
                    });
        }
        Reservation reservation = null;
        if (request.getReservationId() != null) {
            reservation = reservationRepository.findById(request.getReservationId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));
        }

        Transaction transaction = Transaction.builder()
                .event(event)
                .listing(listing)
                .buyer(buyer)
                .seller(seller)
                .deposit(deposit)
                .reservation(reservation)
                .amount(request.getAmount())
                .actualPrice(request.getActualPrice())
                .fee(request.getFee() != null ? request.getFee() : 0.0)
                .status("PENDING")  // Status do Admin duyệt qua PUT /transactions/{id}
                .build();
        transaction = transactionRepository.save(transaction);
        return transactionMapper.toTransactionResponse(transaction);
    }

    @Transactional
    public TransactionResponse updateTransaction(int transactionId, TransactionUpdateRequest request) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));

        if (request.getEventId() != null) {
            Events event = eventRepository.findById(request.getEventId())
                    .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
            transaction.setEvent(event);
        }
        if (request.getListingId() != null) {
            BikeListing listing = bikeListingRepository.findById(request.getListingId())
                    .orElseThrow(() -> new AppException(ErrorCode.BIKE_LISTING_NOT_FOUND));
            transaction.setListing(listing);
        }
        if (request.getBuyerId() != null) {
            Users buyer = userRepository.findById(request.getBuyerId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            transaction.setBuyer(buyer);
        }
        if (request.getSellerId() != null) {
            Users seller = userRepository.findById(request.getSellerId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            transaction.setSeller(seller);
        }
        if (request.getDepositId() != null) {
            Deposit deposit = depositRepository.findById(request.getDepositId())
                    .orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_NOT_FOUND));
            transaction.setDeposit(deposit);
        }
        if (request.getReservationId() != null) {
            Reservation reservation = reservationRepository.findById(request.getReservationId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));
            transaction.setReservation(reservation);
        }
        if (request.getStatus() != null) {
            transaction.setStatus(request.getStatus());
        }
        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }
        if (request.getActualPrice() != null) {
            transaction.setActualPrice(request.getActualPrice());
        }

        transaction = transactionRepository.save(transaction);
        return transactionMapper.toTransactionResponse(transaction);
    }

    public List<Transaction> findAllTransactions() {
        return transactionRepository.findAll();
    }

    public List<TransactionResponse> findAllTransactionResponses() {
        return transactionRepository.findAll().stream()
                .map(transactionMapper::toTransactionResponse)
                .toList();
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

    public List<Transaction> findTransactionsByStatus(String status) {
        return transactionRepository.findAllByStatus(status);
    }

    public List<TransactionResponse> findTransactionResponsesByStatus(String status) {
        return transactionRepository.findAllByStatus(status).stream()
                .map(transactionMapper::toTransactionResponse)
                .toList();
    }
}
