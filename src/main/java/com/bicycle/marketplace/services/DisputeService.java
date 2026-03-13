package com.bicycle.marketplace.services;

import com.bicycle.marketplace.dto.request.DisputeCreationRequest;
import com.bicycle.marketplace.dto.request.DisputeUpdateRequest;
import com.bicycle.marketplace.dto.response.DisputeResponse;
import com.bicycle.marketplace.entities.Dispute;
import com.bicycle.marketplace.entities.Transaction;
import com.bicycle.marketplace.entities.Users;
import com.bicycle.marketplace.enums.DisputeStatus;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.DisputeMapper;
import com.bicycle.marketplace.repository.IDisputeRepository;
import com.bicycle.marketplace.repository.ITransactionRepository;
import com.bicycle.marketplace.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DisputeService {
    @Autowired
    private IDisputeRepository disputeRepository;
    @Autowired
    private DisputeMapper disputeMapper;
    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private ITransactionRepository transactionRepository;

    /**
     * Tạo tranh chấp. Chỉ buyer hoặc seller của giao dịch mới được tạo.
     */
    public DisputeResponse createDispute(int transactionId, DisputeCreationRequest request) {
        Users user = getCurrentUser();
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));
        if (transaction.getBuyer() == null || transaction.getSeller() == null) {
            throw new AppException(ErrorCode.TRANSACTION_NOT_FOUND);
        }
        int buyerId = transaction.getBuyer().getUserId();
        int sellerId = transaction.getSeller().getUserId();
        int userId = user.getUserId();
        if (userId != buyerId && userId != sellerId) {
            throw new AppException(ErrorCode.NOT_BUYER_OR_SELLER);
        }
        Dispute dispute = disputeMapper.toDispute(request);
        dispute.setRaisedBy(user);
        dispute.setTransaction(transaction);
        dispute.setStatus(DisputeStatus.OPEN);
        return disputeMapper.toDisputeResponse(disputeRepository.save(dispute));
    }

    public DisputeResponse updateDispute(int disputeId, DisputeUpdateRequest request) {
        getCurrentUser();
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new AppException(ErrorCode.DISPUTE_NOT_FOUND));
        disputeMapper.updateDispute(dispute, request);
        return disputeMapper.toDisputeResponse(disputeRepository.save(dispute));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<DisputeResponse> findAllDisputes() {
        return disputeRepository.findAll().stream()
                .map(disputeMapper::toDisputeResponse)
                .collect(Collectors.toList());
    }

    public DisputeResponse findDisputeById(int disputeId) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new AppException(ErrorCode.DISPUTE_NOT_FOUND));
        return disputeMapper.toDisputeResponse(dispute);
    }

    /** Tranh chấp mà user hiện tại tham gia (người tạo, buyer hoặc seller). */
    public List<DisputeResponse> findMyDisputes() {
        Users user = getCurrentUser();
        List<Dispute> list = disputeRepository.findByRaisedBy_UserIdOrTransaction_Buyer_UserIdOrTransaction_Seller_UserId(
                user.getUserId(), user.getUserId(), user.getUserId());
        return list.stream().map(disputeMapper::toDisputeResponse).collect(Collectors.toList());
    }

    /** Tranh chấp được gán cho inspector hiện tại. */
    @PreAuthorize("hasRole('INSPECTOR') or hasRole('ADMIN')")
    public List<DisputeResponse> findDisputesAssignedToMe() {
        Users user = getCurrentUser();
        List<Dispute> list = disputeRepository.findByAssignedInspector_UserId(user.getUserId());
        return list.stream().map(disputeMapper::toDisputeResponse).collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public DisputeResponse assignInspector(int disputeId, int inspectorId) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new AppException(ErrorCode.DISPUTE_NOT_FOUND));
        if (dispute.getStatus() == DisputeStatus.RESOLVED || dispute.getStatus() == DisputeStatus.CLOSED) {
            throw new AppException(ErrorCode.DISPUTE_ALREADY_RESOLVED);
        }
        Users inspector = userRepository.findById(inspectorId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!"INSPECTOR".equalsIgnoreCase(inspector.getRole())) {
            throw new AppException(ErrorCode.USER_IS_NOT_INSPECTOR);
        }
        dispute.setAssignedInspector(inspector);
        dispute.setStatus(DisputeStatus.ASSIGNED);
        return disputeMapper.toDisputeResponse(disputeRepository.save(dispute));
    }

    /** Cập nhật trạng thái dispute sang RESOLVED (gọi từ InspectionReportService khi có báo cáo cuối). */
    public void resolveDispute(int disputeId) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new AppException(ErrorCode.DISPUTE_NOT_FOUND));
        dispute.setStatus(DisputeStatus.RESOLVED);
        disputeRepository.save(dispute);
    }

    public String deleteDispute(int disputeId) {
        getCurrentUser();
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new AppException(ErrorCode.DISPUTE_NOT_FOUND));
        disputeRepository.delete(dispute);
        return "Dispute deleted successfully";
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<DisputeResponse> findDisputesByStatus(DisputeStatus status) {
        return disputeRepository.findAllByStatus(status).stream()
                .map(disputeMapper::toDisputeResponse)
                .collect(Collectors.toList());
    }

    private Users getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
