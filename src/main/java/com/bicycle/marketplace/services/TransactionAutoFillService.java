package com.bicycle.marketplace.services;

import com.bicycle.marketplace.entities.*;
import com.bicycle.marketplace.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionAutoFillService {

    private final ITransactionRepository transactionRepository;
    private final IEventRepository eventRepository;
    private final IBikeListingRepository bikeListingRepository;
    private final IDepositRepository depositRepository;
    private final IReservationRepository reservationRepository;

    public static class AutoFillIds {
        public Integer depositId, reservationId, listingId, sellerId, eventId;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public AutoFillIds tryAutoFill(boolean needDeposit, boolean needReservation, boolean needListing,
                                   boolean needSeller, boolean needEvent, int buyerId) {
        AutoFillIds result = new AutoFillIds();
        if (needDeposit) {
            List<Deposit> deposits = depositRepository.findByUser_UserIdOrderByCreatedAtDesc(buyerId);
            for (Deposit d : deposits) {
                if (transactionRepository.findByDeposit_DepositId(d.getDepositId()).isEmpty()) {
                    result.depositId = d.getDepositId();
                    break;
                }
            }
        }
        if (needReservation) {
            List<Reservation> reservations = reservationRepository.findByBuyer_UserIdOrderByReservedAtDesc(buyerId);
            if (!reservations.isEmpty()) {
                result.reservationId = reservations.get(0).getReservationId();
            }
        }
        Deposit d = result.depositId != null ? depositRepository.findById(result.depositId).orElse(null) : null;
        Reservation r = result.reservationId != null ? reservationRepository.findById(result.reservationId).orElse(null) : null;
        if (needListing && d != null && d.getListing() != null) {
            result.listingId = d.getListing().getListingId();
        }
        if (needListing && result.listingId == null && r != null && r.getListing() != null) {
            result.listingId = r.getListing().getListingId();
        }
        if (needListing && result.listingId == null) {
            bikeListingRepository.findAll().stream().findFirst().ifPresent(l -> result.listingId = l.getListingId());
        }
        if (needSeller && result.listingId != null) {
            bikeListingRepository.findById(result.listingId).ifPresent(l -> {
                if (l.getSeller() != null) result.sellerId = l.getSeller().getUserId();
            });
        }
        if (needEvent) {
            eventRepository.findAllByOrderByCreateDateDesc().stream()
                    .findFirst()
                    .ifPresent(e -> result.eventId = e.getEventId());
        }
        return result;
    }
}
