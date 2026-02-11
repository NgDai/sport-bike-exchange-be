package com.bicycle.marketplace.services;

import com.bicycle.marketplace.Repository.IDepositRepository;
import com.bicycle.marketplace.dto.request.DepositCreationRequest;
import com.bicycle.marketplace.dto.request.DepositUpdateRequest;
import com.bicycle.marketplace.dto.response.DepositResponse;
import com.bicycle.marketplace.entities.Deposit;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.DepositMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepositService {
    @Autowired
    private IDepositRepository depositRepository;
    @Autowired
    private DepositMapper depositMapper;

    public DepositResponse createDeposit(DepositCreationRequest request) {
        Deposit deposit = depositMapper.toDeposit(request);
        return depositMapper.toDepositResponse(depositRepository.save(deposit));
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
}
