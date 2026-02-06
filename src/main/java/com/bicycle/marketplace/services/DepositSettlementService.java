package com.bicycle.marketplace.services;

import com.bicycle.marketplace.Repository.IDepositRepository;
import com.bicycle.marketplace.Repository.IDepositSettlementRepository;
import com.bicycle.marketplace.dto.request.DepositSettlementCreationRequest;
import com.bicycle.marketplace.dto.request.DepositSettlementUpdateRequest;
import com.bicycle.marketplace.dto.response.DepositSettlementResponse;
import com.bicycle.marketplace.entities.DepositSettlement;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.DepositSettlementMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepositSettlementService {
    @Autowired
    private IDepositSettlementRepository DepositSettlementRepository;
    @Autowired
    private DepositSettlementMapper depositSettlementMapper;

    public DepositSettlementResponse createDepositSettlement(DepositSettlementCreationRequest request){
        DepositSettlement depositSettlement = new DepositSettlement();
        depositSettlement.setAmount(request.getAmount());
        depositSettlement.setReason(request.getReason());
        depositSettlement.setCreateAt(request.getCreateAt());
        return depositSettlementMapper.toDepositSettlementResponse(DepositSettlementRepository.save(depositSettlement));
    }

    public DepositSettlementResponse getDepositSettlementById(int depositSettlementId){
        DepositSettlement depositSettlement = DepositSettlementRepository.findById(depositSettlementId).orElseThrow(() -> new AppException(ErrorCode.DEPOSITSETTLEMENT_NOT_FOUND));
        return depositSettlementMapper.toDepositSettlementResponse(depositSettlement);
    }

    public List<DepositSettlement> getAllDepositSettlements(){
        return DepositSettlementRepository.findAll();
    }

    public DepositSettlementResponse updateDepositSettlement(int depositSettlementId, DepositSettlementUpdateRequest request){
        DepositSettlement depositSettlement = DepositSettlementRepository.findById(depositSettlementId).orElseThrow(() -> new AppException(ErrorCode.DEPOSITSETTLEMENT_NOT_FOUND));
        depositSettlementMapper.updateDepositSettlement(depositSettlement, request);
        return depositSettlementMapper.toDepositSettlementResponse(DepositSettlementRepository.save(depositSettlement));
    }

    public String deleteDepositSettlement(int depositSettlementId){
        DepositSettlement depositSettlement = DepositSettlementRepository.findById(depositSettlementId).orElseThrow(() -> new AppException(ErrorCode.DEPOSITSETTLEMENT_NOT_FOUND));
        DepositSettlementRepository.delete(depositSettlement);
        return "Deposit Settlement deleted successfully";
    }
}
