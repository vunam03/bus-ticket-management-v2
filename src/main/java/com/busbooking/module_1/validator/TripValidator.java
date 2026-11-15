package com.busbooking.module_1.validator;

import com.busbooking.module_1.dto.BusTripFormDto;
import org.springframework.stereotype.Component;

/**
 * Wrapper tương thích giữ lại tên TripValidator gọi thống nhất tới BusTripValidator.
 */
@Component
public class TripValidator {

    private final BusTripValidator busTripValidator;

    public TripValidator(BusTripValidator busTripValidator) {
        this.busTripValidator = busTripValidator;
    }

    public void validateTripForm(BusTripFormDto dto) {
        busTripValidator.validateTripForm(dto);
    }
}
