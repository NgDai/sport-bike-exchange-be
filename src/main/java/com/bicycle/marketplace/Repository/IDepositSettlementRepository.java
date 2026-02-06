package com.bicycle.marketplace.Repository;

import com.bicycle.marketplace.entities.DepositSettlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDepositSettlementRepository extends JpaRepository<DepositSettlement, Integer> {
}
