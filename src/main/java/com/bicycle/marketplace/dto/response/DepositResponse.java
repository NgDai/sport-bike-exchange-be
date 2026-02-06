package com.bicycle.marketplace.dto.response;

import com.bicycle.marketplace.entities.BikeListing;
import com.bicycle.marketplace.entities.Users;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepositResponse {
    private int depositId;
    private int userId;
    private int listingId;
    private String type;
    private double amount;
    private String nvarchar;
    private Date createAt;
}
