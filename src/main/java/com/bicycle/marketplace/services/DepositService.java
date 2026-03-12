package com.bicycle.marketplace.services;

import com.bicycle.marketplace.dto.request.DepositCreationRequest;
import com.bicycle.marketplace.dto.request.TransactionCreationRequest;
import com.bicycle.marketplace.dto.request.DepositUpdateRequest;
import com.bicycle.marketplace.dto.response.DepositResponse;
import com.bicycle.marketplace.entities.BikeListing;
import com.bicycle.marketplace.entities.Deposit;
import com.bicycle.marketplace.entities.Users;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.DepositMapper;
import com.bicycle.marketplace.repository.IBikeListingRepository;
import com.bicycle.marketplace.repository.IDepositRepository;
import com.bicycle.marketplace.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepositService {
    @Autowired
    private IDepositRepository depositRepository;
    @Autowired
    private DepositMapper depositMapper;
    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private IBikeListingRepository bikeListingRepository;
    @Autowired
    private TransactionService transactionService;

    /**
     * Tạo đặt cọc: lưu vào bảng deposit, sau đó tạo và lưu transaction (status PENDING).
     * Khi request có listingId thì luôn tạo transaction gắn với deposit; reservationId tùy chọn (đặt chỗ).
     */
    @Transactional
    public DepositResponse createDeposit(DepositCreationRequest request) {
        Deposit deposit = depositMapper.toDeposit(request);
        Users buyer = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (request.getListingId() != null) {
            BikeListing listing = bikeListingRepository.findById(request.getListingId())
                    .orElseThrow(() -> new AppException(ErrorCode.BIKE_LISTING_NOT_FOUND));
            deposit.setUser(buyer);
            deposit.setListing(listing);
        }
        deposit = depositRepository.save(deposit);

        if (request.getListingId() != null) {
            BikeListing listing = deposit.getListing();
            Users seller = listing.getSeller();
            if (seller == null) {
                throw new AppException(ErrorCode.USER_NOT_FOUND);
            }
    }

    public DepositResponse updateDeposit(int depositId, DepositUpdateRequest request) {
        Deposit deposit = depositRepository.findById(depositId)
                .orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_NOT_FOUND));
        depositMapper.updateDeposit(deposit, request);
        return depositMapper.toDepositResponse(depositRepository.save(deposit));
    }

    public List<Deposit> findAllDeposits() {
        return depositRepository.findAll();
    }

    public DepositResponse findDepositById(int depositId) {
        Deposit deposit = depositRepository.findById(depositId)
                .orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_NOT_FOUND));
        return depositMapper.toDepositResponse(deposit);
    }

    public String deleteDeposit(int depositId) {
        Deposit deposit = depositRepository.findById(depositId)
                .orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_NOT_FOUND));
        depositRepository.delete(deposit);
        return "Deposit deleted successfully";
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<Deposit> findDepositsByStatus(String status) {
        return depositRepository.findAllByStatus(status);
    }
}
