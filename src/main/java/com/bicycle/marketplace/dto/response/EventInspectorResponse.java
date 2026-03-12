package com.bicycle.marketplace.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT)
public class EventInspectorResponse {
    int inspecId;
    int eventId;
    int inspectorId;
    String inspectorName;
    String status;
    LocalDateTime createDate;

    java.util.List<com.bicycle.marketplace.entities.EventInspector> eventInspectors;
}
