package com.bicycle.marketplace.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

import com.bicycle.marketplace.enums.CheckInStatus;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class CheckInCreationRequest {
    private CheckInStatus role;
    private Date checkInTime;
}
