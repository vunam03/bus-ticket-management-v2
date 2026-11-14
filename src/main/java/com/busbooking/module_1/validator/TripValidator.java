package com.busbooking.module_1.validator;

import com.busbooking.module_1.dto.BusTripFormDto;
import com.busbooking.shared.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TripValidator {

    public void validateTripForm(BusTripFormDto dto) {
        if (dto.getDepartureProvince() != null && dto.getArrivalProvince() != null
                && dto.getDepartureProvince().trim().equalsIgnoreCase(dto.getArrivalProvince().trim())) {
            throw new BusinessException("INVALID_ROUTE", "Tỉnh/thành phố khởi hành và điểm đến không được trùng nhau.");
        }

        if (dto.getDepartureTime() == null || dto.getArrivalTime() == null) {
            throw new BusinessException("INVALID_TIME", "Thời gian khởi hành và thời gian đến không được để trống.");
        }

        if (!dto.getArrivalTime().isAfter(dto.getDepartureTime())) {
            throw new BusinessException("INVALID_TIME_RANGE", "Thời gian đến dự kiến phải sau thời gian khởi hành.");
        }

        if (dto.getTicketPrice() == null || dto.getTicketPrice().compareTo(new BigDecimal("10000")) < 0) {
            throw new BusinessException("INVALID_PRICE", "Giá vé phải từ 10.000 VNĐ trở lên.");
        }
    }
}
