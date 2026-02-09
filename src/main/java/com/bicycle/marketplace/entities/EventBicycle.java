package com.bicycle.marketplace.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventBicycle {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    int eventBikeId;
    @ManyToOne
    Events eventId;
    @OneToOne
    Bicycle bicycleId;
    @CreationTimestamp
    LocalDate createDate;
}
