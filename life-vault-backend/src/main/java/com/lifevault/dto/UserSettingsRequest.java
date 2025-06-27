package com.lifevault.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserSettingsRequest {
    @NotNull
    @Min(30)
    @Max(730)
    private Integer inactivityPeriodDays;
}