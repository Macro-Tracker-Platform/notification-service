package com.olehprukhnytskyi.macrotrackernotificationservice.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterPushDeviceRequest {
    @NotBlank
    @Size(max = 512)
    private String token;
    @NotBlank
    @Size(max = 16)
    private String platform;
    @NotBlank
    @Size(max = 8)
    private String locale;
    @NotBlank
    @Size(max = 64)
    private String timeZone;
}
