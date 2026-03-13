package com.bicycle.marketplace.dto.request;

import com.bicycle.marketplace.enums.DisputeReasonCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DisputeCreationRequest {
    @NotBlank(message = "Lý do tranh chấp không được để trống")
    private String reason;
    @NotNull(message = "Phân loại lý do tranh chấp là bắt buộc")
    private DisputeReasonCategory reasonCategory;
}
