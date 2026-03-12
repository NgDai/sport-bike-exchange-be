package com.bicycle.marketplace.repository;

import com.bicycle.marketplace.entities.Deposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IDepositRepository extends JpaRepository<Deposit, Integer> {
    List<Deposit> findAllByStatus(String status);

    /** Lấy danh sách deposit của user (buyer), mới nhất trước */
    List<Deposit> findByUser_UserIdOrderByCreatedAtDesc(Integer userId);
}
