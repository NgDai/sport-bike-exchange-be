package com.bicycle.marketplace.services;

import com.bicycle.marketplace.Repository.IDisputeRepository;
import com.bicycle.marketplace.dto.request.DisputeCreationRequest;
import com.bicycle.marketplace.dto.request.DisputeUpdateRequest;
import com.bicycle.marketplace.dto.response.DisputeResponse;
import com.bicycle.marketplace.entities.Dispute;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.DisputeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DisputeService {
    @Autowired
    private IDisputeRepository disputeRepository;
    @Autowired
    private DisputeMapper disputeMapper;

    public DisputeResponse createDispute(DisputeCreationRequest request) {
        Dispute dispute = disputeMapper.toDispute(request);
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
}
