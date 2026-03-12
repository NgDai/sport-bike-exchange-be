package com.bicycle.marketplace.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventInspector {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int inspecId;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Events event;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users inspector;

    private String status = "pending"; // pending, approved, rejected

    @CreationTimestamp
    LocalDateTime createDate;


}
