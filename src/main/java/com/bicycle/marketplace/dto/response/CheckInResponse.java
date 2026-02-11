package com.bicycle.marketplace.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckInResponse {
    private int checkInId;
    private int userId;
    private int eventId;
    private String roleId;
    private String status;
    private Date checkInTime;
}
