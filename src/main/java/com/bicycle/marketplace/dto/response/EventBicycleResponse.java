package com.bicycle.marketplace.dto.response;

import com.bicycle.marketplace.entities.BikeListing;
import com.bicycle.marketplace.entities.Events;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventBicycleResponse {
    private int eventBikeId;
    private int eventId;
    private int listingId;
    private String sellerName;
    private String type;
    private LocalDate createDate;
}
