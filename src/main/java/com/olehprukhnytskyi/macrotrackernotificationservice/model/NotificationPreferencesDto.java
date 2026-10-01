package com.olehprukhnytskyi.macrotrackernotificationservice.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferencesDto {
    private boolean enabled;
    private boolean dinnerEnabled;
    @NotNull
    private LocalTime dinnerTime;
    private boolean aiLimitEnabled;
    @NotNull
    private LocalTime aiLimitTime;
    private boolean weeklyReportEnabled;
    @NotNull
    private LocalTime weeklyReportTime;
    @NotBlank
    private String locale;
    @NotBlank
    private String timeZone;
}
