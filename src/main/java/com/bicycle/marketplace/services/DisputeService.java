package com.bicycle.marketplace.services;

import com.bicycle.marketplace.entities.Transaction;
import com.bicycle.marketplace.entities.Users;
import com.bicycle.marketplace.repository.IDisputeRepository;
import com.bicycle.marketplace.dto.request.DisputeCreationRequest;
import com.bicycle.marketplace.dto.request.DisputeUpdateRequest;
import com.bicycle.marketplace.dto.response.DisputeResponse;
import com.bicycle.marketplace.entities.Dispute;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.DisputeMapper;
import com.bicycle.marketplace.repository.ITransactionRepository;
import com.bicycle.marketplace.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public DisputeResponse createDispute(int transactionId, DisputeCreationRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String username = authentication.getName();
        Users user = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));
        Dispute dispute = disputeMapper.toDispute(request);
        dispute.setRaisedBy(user);
        dispute.setTransaction(transaction);

        return disputeMapper.toDisputeResponse(disputeRepository.save(dispute));
    }

    public DisputeResponse updateDispute(int disputeId, DisputeUpdateRequest request) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new AppException(ErrorCode.DISPUTE_NOT_FOUND));
        disputeMapper.updateDispute(dispute, request);
        return disputeMapper.toDisputeResponse(disputeRepository.save(dispute));
    }

    public List<Dispute> findAllDisputes() {
        return disputeRepository.findAll();
    }

    public DisputeResponse findDisputeById(int disputeId) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new AppException(ErrorCode.DISPUTE_NOT_FOUND));
        return disputeMapper.toDisputeResponse(dispute);
    }

    public String deleteDispute(int disputeId) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new AppException(ErrorCode.DISPUTE_NOT_FOUND));
        disputeRepository.delete(dispute);
        return "Dispute deleted successfully";
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<Dispute> findDisputesByStatus(String status) {
        return disputeRepository.findAllByStatus(status);
    }
}
