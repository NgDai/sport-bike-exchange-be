package com.bicycle.marketplace.services;

import com.bicycle.marketplace.dto.request.TransactionCreationRequest;
import com.bicycle.marketplace.dto.request.TransactionUpdateRequest;
import com.bicycle.marketplace.dto.response.TransactionResponse;
import com.bicycle.marketplace.entities.*;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.TransactionMapper;
import org.springframework.dao.DataIntegrityViolationException;
import com.bicycle.marketplace.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.PersistenceException;
import java.util.Date;
import java.util.List;

@Slf4j
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
    @Autowired
    private TransactionAutoFillService transactionAutoFillService;

    @Transactional
    public TransactionResponse createTransaction(TransactionCreationRequest request) {
        Users buyer = null;
        if (request.getBuyerId() != null) {
            buyer = userRepository.findById(request.getBuyerId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        } else {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
            String username = auth.getName();
            buyer = userRepository.findByUsername(username)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        }

        Deposit deposit = null;
        Reservation reservation = null;
        BikeListing listing = null;
        Users seller = null;
        Events event = null;

        if (request.getDepositId() != null) {
            deposit = depositRepository.findById(request.getDepositId())
                    .orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_NOT_FOUND));
            if (transactionRepository.findByDeposit_DepositId(request.getDepositId()).isPresent()) {
                throw new AppException(ErrorCode.DEPOSIT_ALREADY_HAS_TRANSACTION);
            }
        }
        if (request.getReservationId() != null) {
            reservation = reservationRepository.findById(request.getReservationId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));
        }
        if (request.getListingId() != null) {
            listing = bikeListingRepository.findById(request.getListingId())
                    .orElseThrow(() -> new AppException(ErrorCode.BIKE_LISTING_NOT_FOUND));
        }
        if (request.getSellerId() != null) {
            seller = userRepository.findById(request.getSellerId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        }
        if (request.getEventId() != null) {
            event = eventRepository.findById(request.getEventId())
                    .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        }

        final boolean needDeposit = (deposit == null);
        final boolean needReservation = (reservation == null);
        final boolean needListing = (listing == null);
        final boolean needSeller = (seller == null);
        final boolean needEvent = (event == null);
        if (needDeposit || needReservation || needListing || needSeller || needEvent) {
            try {
                TransactionAutoFillService.AutoFillIds ids = transactionAutoFillService.tryAutoFill(
                        needDeposit, needReservation, needListing, needSeller, needEvent, buyer.getUserId());
                if (ids != null) {
                    if (ids.depositId != null) deposit = depositRepository.findById(ids.depositId).orElse(null);
                    if (ids.reservationId != null) reservation = reservationRepository.findById(ids.reservationId).orElse(null);
                    if (ids.listingId != null) listing = bikeListingRepository.findById(ids.listingId).orElse(null);
                    if (ids.sellerId != null) seller = userRepository.findById(ids.sellerId).orElse(null);
                    if (ids.eventId != null) event = eventRepository.findById(ids.eventId).orElse(null);
                }
            } catch (Exception e) {
                log.debug("Auto-fill skipped or failed: {}", e.getMessage());
            }
        }

        if (deposit == null) {
            var depositOpt = depositRepository.findByUser_UserIdOrderByCreatedAtDesc(buyer.getUserId()).stream()
                    .filter(d -> transactionRepository.findByDeposit_DepositId(d.getDepositId()).isEmpty())
                    .findFirst();
            if (depositOpt.isPresent()) deposit = depositOpt.get();
        }
        if (event == null) {
            try {
                var eventOpt = eventRepository.findAllByOrderByCreateDateDesc().stream().findFirst();
                if (eventOpt.isPresent()) event = eventOpt.get();
            } catch (Exception ignored) { }
            if (event == null) {
                var eventOpt = eventRepository.findAll().stream().findFirst();
                if (eventOpt.isPresent()) event = eventOpt.get();
            }
        }
        if (reservation == null) {
            var reservationOpt = reservationRepository.findByBuyer_UserIdOrderByReservedAtDesc(buyer.getUserId()).stream()
                    .filter(r -> transactionRepository.findByReservation_ReservationId(r.getReservationId()).isEmpty())
                    .findFirst();
            if (reservationOpt.isPresent()) reservation = reservationOpt.get();
        }

        if (listing == null && reservation != null && reservation.getListing() != null) {
            listing = reservation.getListing();
        }
        if (listing == null && deposit != null && deposit.getListing() != null) {
            listing = deposit.getListing();
        }
        if (listing == null) {
            var listingOpt = bikeListingRepository.findAll().stream().findFirst();
            if (listingOpt.isPresent()) listing = listingOpt.get();
        }
        if (listing != null && seller == null && listing.getSeller() != null) {
            seller = listing.getSeller();
        }

        String status = "PENDING";
        if (deposit != null && deposit.getStatus() != null && !deposit.getStatus().isBlank()) {
            status = deposit.getStatus();
        } else if (reservation != null && reservation.getStatus() != null && !reservation.getStatus().isBlank()) {
            status = reservation.getStatus();
        }

        String type = inferTransactionType(deposit, listing, reservation);
        String description = buildTransactionDescription(type, request.getAmount(), deposit, listing);

        Date now = new Date();
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
                .status(status)
                .description(description)
                .type(type)
                .build();
        transaction.setCreateAt(now);
        transaction.setUpdateAt(now);
        try {
            transaction = transactionRepository.saveAndFlush(transaction);
        } catch (DataIntegrityViolationException e) {
            String msg = e.getCause() != null ? e.getCause().getMessage() : (e.getMessage() != null ? e.getMessage() : "");
            log.warn("Transaction save failed (constraint): {}", msg);
            if (msg != null && msg.contains("reservation_id") && msg.contains("already exists")) {
                throw new AppException(ErrorCode.RESERVATION_ALREADY_HAS_TRANSACTION);
            }
            if (msg != null && msg.contains("duplicate key") && msg.contains("reservation_id")) {
                throw new AppException(ErrorCode.RESERVATION_ALREADY_HAS_TRANSACTION);
            }
            throw new AppException(ErrorCode.TRANSACTION_SAVE_FAILED);
        } catch (PersistenceException | JpaSystemException e) {
            String msg = e.getCause() != null ? e.getCause().getMessage() : (e.getMessage() != null ? e.getMessage() : "");
            log.warn("Transaction save failed (persistence): {}", msg);
            if (msg != null && (msg.contains("reservation_id") && msg.contains("already exists") || (msg.contains("duplicate key") && msg.contains("reservation_id")))) {
                throw new AppException(ErrorCode.RESERVATION_ALREADY_HAS_TRANSACTION);
            }
            throw new AppException(ErrorCode.TRANSACTION_SAVE_FAILED);
        } catch (Exception e) {
            log.warn("Transaction save failed: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.TRANSACTION_SAVE_FAILED);
        }
        return toTransactionResponseSafe(transaction);
    }

    private static String inferTransactionType(Deposit deposit, BikeListing listing, Reservation reservation) {
        if (deposit != null) return "Deposit";
        if (listing != null && reservation != null) return "Sale";
        if (listing != null) return "ListingFee";
        if (reservation != null) return "Reservation";
        return "Fee";
    }

    private static String buildTransactionDescription(String type, double amount, Deposit deposit, BikeListing listing) {
        if (deposit != null) return "Nạp tiền / Deposit";
        if (listing != null) return "Giao dịch liên quan bài đăng xe #" + (listing.getListingId());
        return "Giao dịch " + type + " - " + String.format("%.0f", amount);
    }

    private TransactionResponse toTransactionResponseSafe(Transaction t) {
        TransactionResponse r = new TransactionResponse();
        r.setTransactionId(t.getTransactionId());
        r.setBuyerId(t.getBuyer() != null ? t.getBuyer().getUserId() : 0);
        r.setSellerId(t.getSeller() != null ? t.getSeller().getUserId() : 0);
        r.setEventId(t.getEvent() != null ? t.getEvent().getEventId() : 0);
        r.setListingId(t.getListing() != null ? t.getListing().getListingId() : 0);
        r.setDepositId(t.getDeposit() != null ? t.getDeposit().getDepositId() : 0);
        r.setReservationId(t.getReservation() != null ? t.getReservation().getReservationId() : 0);
        r.setAmount(t.getAmount());
        r.setActualPrice(t.getActualPrice());
        r.setFee(t.getFee());
        r.setDescription(t.getDescription() != null ? t.getDescription() : "");
        r.setType(t.getType() != null ? t.getType() : "");
        r.setStatus(t.getStatus() != null ? t.getStatus() : "");
        r.setCreatedAt(t.getCreateAt() != null ? t.getCreateAt() : new Date(0));
        r.setUpdatedAt(t.getUpdateAt() != null ? t.getUpdateAt() : new Date(0));
        return r;
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
        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }
        if (request.getActualPrice() != null) {
            transaction.setActualPrice(request.getActualPrice());
        }
        if (request.getFee() != null) {
            transaction.setFee(request.getFee());
        }
        transaction = transactionRepository.save(transaction);
        return toTransactionResponseSafe(transaction);
    }

    public List<Transaction> findAllTransactions() {
        return transactionRepository.findAll();
    }

    public List<TransactionResponse> findAllTransactionResponses() {
        return transactionRepository.findAll().stream()
                .map(this::toTransactionResponseSafe)
                .toList();
    }

    public TransactionResponse findTransactionById(int transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));
        return toTransactionResponseSafe(transaction);
    }

    public TransactionResponse findTransactionByIdForCurrentUser(int transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
        boolean isBuyer = transaction.getBuyer() != null && transaction.getBuyer().getUserId() == currentUser.getUserId();
        boolean isSeller = transaction.getSeller() != null && transaction.getSeller().getUserId() == currentUser.getUserId();
        if (!isAdmin && !isBuyer && !isSeller) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return toTransactionResponseSafe(transaction);
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
                .map(this::toTransactionResponseSafe)
                .toList();
    }

}
