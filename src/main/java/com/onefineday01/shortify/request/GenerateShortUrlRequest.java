package com.onefineday01.shortify.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class GenerateShortUrlRequest {

    @NotEmpty(message = "longUrl is mandatory")
    @URL(message = "LongUrl must be a valid Url")
    private String longUrl;

    @Min(value = 15, message = "Expiry can not be lesser than 15 seconds")
    @Max(value =  31536000, message = "Expiry can not be greater than 3,15,36,000 seconds (1 Year)")
    private int expiry = 77760000;
}
