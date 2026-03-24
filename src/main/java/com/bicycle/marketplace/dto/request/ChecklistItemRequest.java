package com.bicycle.marketplace.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChecklistItemRequest {
    private String name;   // VD: "Khung xe", "Phanh trước"
    private String status; // "PASS" / "FAIL" / "NOT_CHECKED"
    private String note;
}
