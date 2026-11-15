package com.busbooking.module_4.controller;

import com.busbooking.common.exception.BusinessException;
import com.busbooking.common.exception.ResourceNotFoundException;
import com.busbooking.module_1.model.BusTrip;
import com.busbooking.module_1.service.BusTripService;
import com.busbooking.module_4.dto.SeatHoldResultDto;
import com.busbooking.module_4.dto.SeatLayoutResponse;
import com.busbooking.module_4.dto.SeatSelectRequest;
import com.busbooking.module_4.exception.SeatAlreadyBookedException;
import com.busbooking.module_4.service.SeatLockService;
import com.busbooking.module_4.service.SeatSelectionService;
import com.busbooking.module_4.service.SeatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller chuẩn hóa API REST cho sơ đồ ghế thời gian thực, gợi ý chỗ ngồi,
 * giữ/đổi/hủy giữ ghế và gia hạn thời gian giữ chỗ (Module 4).
 */
@Controller
public class SeatController {

    private final SeatService seatService;
    private final SeatLockService seatLockService;
    private final SeatSelectionService seatSelectionService;
    private final BusTripService busTripService;

    public SeatController(SeatService seatService,
                          SeatLockService seatLockService,
                          SeatSelectionService seatSelectionService,
                          BusTripService busTripService) {
        this.seatService = seatService;
        this.seatLockService = seatLockService;
        this.seatSelectionService = seatSelectionService;
        this.busTripService = busTripService;
    }

    /**
     * Đường dẫn hiển thị sơ đồ ghế kèm tóm tắt cấu trúc tầng mở rộng (/booking/trip/{tripId}/layout).
     */
    @GetMapping("/booking/trip/{tripId}/layout")
    public String viewDetailedSeatLayout(@PathVariable Long tripId, Authentication auth, Model model) {
        String username = extractAuthenticatedUsername(auth);
        BusTrip trip = busTripService.getTripById(tripId);
        var allSeats = seatSelectionService.getSeatsForTrip(tripId);
        SeatLayoutResponse layout = seatService.getSeatLayoutForTrip(tripId, username);

        model.addAttribute("trip", trip);
        model.addAttribute("allSeats", allSeats);
        model.addAttribute("floor1Seats", allSeats.stream().filter(s -> s.getFloorNumber() == 1).toList());
        model.addAttribute("floor2Seats", allSeats.stream().filter(s -> s.getFloorNumber() == 2).toList());
        model.addAttribute("hasTwoFloors", trip.getBusType().getFloors() == 2);
        model.addAttribute("seatLayout", layout);
        return "module_4/seat-selection";
    }

    /**
     * API JSON lấy toàn bộ sơ đồ ghế và số liệu ghế trống/đang giữ/đã đặt/không khả dụng theo thời gian thực.
     */
    @GetMapping("/api/seats/trip/{tripId}")
    @ResponseBody
    public ResponseEntity<SeatLayoutResponse> getSeatLayoutApi(@PathVariable Long tripId, Authentication auth) {
        String username = extractAuthenticatedUsername(auth);
        return ResponseEntity.ok(seatService.getSeatLayoutForTrip(tripId, username));
    }

    /**
     * API JSON gợi ý tự động N ghế trống liền kề ưu tiên cùng hàng và cùng tầng.
     */
    @GetMapping("/api/seats/trip/{tripId}/suggest")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> suggestBestSeatsApi(
            @PathVariable Long tripId,
            @RequestParam(value = "count", defaultValue = "2") int count,
            @RequestParam(value = "floor", required = false) Integer floor,
            @RequestParam(value = "preferredFloor", required = false) Integer preferredFloor) {
        try {
            Integer chosenFloor = preferredFloor != null ? preferredFloor : floor;
            return ResponseEntity.ok(seatService.suggestSmartSeats(tripId, count, chosenFloor));
        } catch (BusinessException ex) {
            return buildErrorResponse(ex);
        }
    }

    /**
     * API JSON kiểm tra tính hợp lệ và tính tổng tiền tạm tính cho danh sách ghế đang chọn.
     */
    @PostMapping("/api/seats/calculate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> calculateSeatsApi(@RequestBody SeatSelectRequest request,
                                                                 Authentication auth) {
        try {
            if (request == null || request.getTripId() == null) {
                throw new BusinessException("INVALID_TRIP_SELECTION", "Thiếu thông tin chuyến xe.");
            }
            String username = extractAuthenticatedUsername(auth);
            return ResponseEntity.ok(
                    seatService.calculateSelectionTotalForUser(request.getTripId(), request.getSeatIds(), username)
            );
        } catch (BusinessException ex) {
            return buildErrorResponse(ex);
        }
    }

    /**
     * API JSON giữ ghế tạm thời (10 phút) chống đặt trùng đồng thời.
     */
    @PostMapping("/api/seats/trip/{tripId}/hold")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> holdSeatsApi(@PathVariable Long tripId,
                                                            @RequestBody(required = false) SeatSelectRequest request,
                                                            Authentication auth) {
        String username = extractAuthenticatedUsername(auth);
        if (username == null) {
            return unauthorizedResponse("Vui lòng đăng nhập để giữ ghế.");
        }
        try {
            List<Long> seatIds = request != null ? request.getSeatIds() : null;
            SeatHoldResultDto result = seatSelectionService.holdSeats(tripId, seatIds, username);
            return ResponseEntity.ok(toHoldPayload(result));
        } catch (BusinessException ex) {
            return buildErrorResponse(ex);
        }
    }

    /**
     * API JSON thay đổi danh sách ghế đang giữ của một phiên holdCode hiện tại trong cùng giao dịch.
     */
    @PutMapping("/api/seats/hold/{holdCode}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateHeldSeatsApi(@PathVariable String holdCode,
                                                                  @RequestBody(required = false) SeatSelectRequest request,
                                                                  Authentication auth) {
        String username = extractAuthenticatedUsername(auth);
        if (username == null) {
            return unauthorizedResponse("Vui lòng đăng nhập để thay đổi danh sách ghế giữ.");
        }
        try {
            List<Long> seatIds = request != null ? request.getSeatIds() : null;
            SeatHoldResultDto result = seatSelectionService.changeHeldSeats(holdCode, seatIds, username);
            return ResponseEntity.ok(toHoldPayload(result));
        } catch (BusinessException ex) {
            return buildErrorResponse(ex);
        }
    }

    /**
     * API JSON hủy chủ động phiên giữ ghế và trả ghế HELD về AVAILABLE.
     */
    @DeleteMapping("/api/seats/hold/{holdCode}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> releaseHoldApi(@PathVariable String holdCode, Authentication auth) {
        String username = extractAuthenticatedUsername(auth);
        if (username == null) {
            return unauthorizedResponse("Vui lòng đăng nhập để hủy phiên giữ ghế.");
        }
        try {
            seatSelectionService.releaseHoldByCode(holdCode, username);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("success", true);
            body.put("holdCode", holdCode);
            body.put("message", "Đã hủy phiên giữ ghế và trả ghế về trạng thái trống.");
            return ResponseEntity.ok(body);
        } catch (BusinessException ex) {
            return buildErrorResponse(ex);
        }
    }

    /**
     * API JSON kiểm tra số giây giữ ghế còn lại và khả năng gia hạn của phiên giữ chỗ.
     */
    @GetMapping("/api/seats/hold/{holdCode}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getHoldStatusApi(@PathVariable String holdCode, Authentication auth) {
        String username = extractAuthenticatedUsername(auth);
        if (username == null) {
            return unauthorizedResponse("Vui lòng đăng nhập để kiểm tra phiên giữ ghế.");
        }
        try {
            return ResponseEntity.ok(seatLockService.getHoldStatusSummary(holdCode, username));
        } catch (BusinessException ex) {
            return buildErrorResponse(ex);
        }
    }

    /**
     * API JSON gia hạn thêm thời gian giữ ghế (tổng thời gian tối đa 15 phút kể từ lúc giữ).
     * Hỗ trợ cả query parameter lẫn JSON request body.
     */
    @PostMapping("/api/seats/hold/extend")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> extendHoldApi(
            @RequestParam(value = "holdCode", required = false) String holdCodeParam,
            @RequestParam(value = "minutes", defaultValue = "3") int extraMinutesParam,
            @RequestBody(required = false) SeatSelectRequest bodyRequest,
            Authentication auth) {
        String username = extractAuthenticatedUsername(auth);
        if (username == null) {
            return unauthorizedResponse("Vui lòng đăng nhập để gia hạn giữ ghế.");
        }
        String holdCode = (holdCodeParam != null && !holdCodeParam.isBlank())
                ? holdCodeParam
                : (bodyRequest != null ? bodyRequest.getHoldCode() : null);
        int extraMinutes = (bodyRequest != null && bodyRequest.getExtendMinutes() != null && holdCodeParam == null)
                ? bodyRequest.getExtendMinutes()
                : extraMinutesParam;
        try {
            SeatHoldResultDto extended = seatLockService.extendHold(holdCode, username, extraMinutes);
            return ResponseEntity.ok(toHoldPayload(extended));
        } catch (BusinessException ex) {
            return buildErrorResponse(ex);
        }
    }

    private String extractAuthenticatedUsername(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        String name = auth.getName();
        return (name == null || name.isBlank() || "anonymousUser".equalsIgnoreCase(name)) ? null : name;
    }

    private ResponseEntity<Map<String, Object>> unauthorizedResponse(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("error", "UNAUTHORIZED");
        body.put("message", message);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(BusinessException ex) {
        HttpStatus status = resolveHttpStatus(ex);
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("success", false);
        error.put("error", ex.getErrorCode());
        error.put("errorCode", ex.getErrorCode());
        error.put("message", ex.getMessage());
        return ResponseEntity.status(status).body(error);
    }

    private HttpStatus resolveHttpStatus(BusinessException ex) {
        if (ex instanceof ResourceNotFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        String code = ex.getErrorCode() != null ? ex.getErrorCode() : "";
        if ("UNAUTHORIZED_HOLD".equals(code)) {
            return HttpStatus.FORBIDDEN;
        }
        if ("HOLD_EXPIRED".equals(code)) {
            return HttpStatus.GONE;
        }
        if (ex instanceof SeatAlreadyBookedException
                || "SEAT_ALREADY_BOOKED".equals(code)
                || "SEAT_BEING_HELD".equals(code)
                || "SEAT_CONCURRENT_CONFLICT".equals(code)
                || "HOLD_ALREADY_BOOKED".equals(code)) {
            return HttpStatus.CONFLICT;
        }
        return HttpStatus.BAD_REQUEST;
    }

    private Map<String, Object> toHoldPayload(SeatHoldResultDto dto) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("success", true);
        payload.put("holdCode", dto.getHoldCode());
        payload.put("tripId", dto.getTripId());
        payload.put("seatIds", dto.getSeatIds());
        payload.put("seatNumbers", dto.getSeatNumbers());
        payload.put("estimatedTotal", dto.getEstimatedTotal());
        payload.put("heldAt", dto.getHeldAt());
        payload.put("expiresAt", dto.getExpiresAt());
        payload.put("maxExpiresAt", dto.getMaxExpiresAt());
        payload.put("remainingSeconds", dto.getRemainingSeconds());
        payload.put("canExtend", dto.isCanExtend());
        payload.put("message", dto.getMessage());
        return payload;
    }
}
