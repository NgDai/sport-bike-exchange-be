package com.bicycle.marketplace.repository;

import com.bicycle.marketplace.entities.Dispute;
import com.bicycle.marketplace.enums.DisputeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IDisputeRepository extends JpaRepository<Dispute, Integer> {

    List<Dispute> findAllByStatus(DisputeStatus status);

    List<Dispute> findByAssignedInspector_UserId(int inspectorUserId);

    List<Dispute> findByRaisedBy_UserId(int userId);

    /** Tranh chấp mà user tham gia (là người tạo, buyer hoặc seller). */
    List<Dispute> findByRaisedBy_UserIdOrTransaction_Buyer_UserIdOrTransaction_Seller_UserId(
            int raisedByUserId, int buyerUserId, int sellerUserId);
}
