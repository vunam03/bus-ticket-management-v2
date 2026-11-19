package com.busbooking.module_6.service;

import com.busbooking.module_5.repository.PaymentTransactionRepository;
import com.busbooking.module_6.dto.*;
import com.busbooking.module_6.repository.DashboardCustomerRepository;
import com.busbooking.module_6.repository.DashboardTicketRepository;
import com.busbooking.module_6.repository.DashboardTripRepository;
import com.busbooking.common.enums.*;
import com.busbooking.module_4.enums.SeatStatus;
import com.busbooking.module_1.model.BusTrip;
import com.busbooking.module_5.model.PaymentTransaction;
import com.busbooking.module_5.model.Ticket;
import com.busbooking.module_2.model.User;
import com.busbooking.common.utils.DateTimeUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Module 6 – Dịch vụ tổng hợp thống kê KPI, biểu đồ doanh thu, tỷ lệ lấp đầy ghế và phân tích khách hàng.
 * Chỉ tính doanh thu đối với các vé có trạng thái CONFIRMED và PAID.
 */
@Service
public class StatisticService {

    private final DashboardTicketRepository ticketRepository;
    private final DashboardTripRepository tripRepository;
    private final DashboardCustomerRepository customerRepository;
    private final PaymentTransactionRepository transactionRepository;

    private static final DateTimeFormatter SHORT_DAY_FMT = DateTimeFormatter.ofPattern("dd/MM");

    public StatisticService(DashboardTicketRepository ticketRepository,
                            DashboardTripRepository tripRepository,
                            DashboardCustomerRepository customerRepository,
                            PaymentTransactionRepository transactionRepository) {
        this.ticketRepository = ticketRepository;
        this.tripRepository = tripRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryDto getDashboardSummary(Integer yearParam) {
        int year = (yearParam != null && yearParam >= 2020 && yearParam <= 2035)
                ? yearParam
                : LocalDate.now().getYear();

        DashboardSummaryDto summary = new DashboardSummaryDto();
        summary.setSelectedYear(year);

        // 1. Bốn thẻ KPI chính
        BigDecimal totalRevenue = ticketRepository.sumConfirmedPaidRevenue(TicketStatus.CONFIRMED, PaymentStatus.PAID);
        summary.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        summary.setTotalTrips(tripRepository.count());
        summary.setTotalPaidTickets(ticketRepository.countByTicketStatusAndPaymentStatus(TicketStatus.CONFIRMED, PaymentStatus.PAID));
        summary.setTotalCustomers(customerRepository.countByRoleName(RoleName.ROLE_CUSTOMER));

        // 2. Thống kê vé theo trạng thái
        summary.setPaidTicketsCount(summary.getTotalPaidTickets());
        summary.setUnpaidTicketsCount(ticketRepository.countByPaymentStatus(PaymentStatus.UNPAID));
        summary.setCancelledTicketsCount(ticketRepository.countByTicketStatus(TicketStatus.CANCELLED));
        summary.setRefundedTicketsCount(ticketRepository.countByPaymentStatus(PaymentStatus.REFUNDED));

        // 3. Thống kê ghế và tỷ lệ lấp đầy
        long totalCapacity = tripRepository.sumTotalSeatsCapacity();
        long bookedSeats = tripRepository.countSeatsByStatus(SeatStatus.BOOKED);
        long heldSeats = tripRepository.countSeatsByStatus(SeatStatus.HELD);
        long availableSeats = tripRepository.countSeatsByStatus(SeatStatus.AVAILABLE);

        summary.setTotalSeatsCapacity(totalCapacity);
        summary.setBookedSeatsCount(bookedSeats);
        summary.setHeldSeatsCount(heldSeats);
        summary.setAvailableSeatsCount(availableSeats);
        if (totalCapacity > 0) {
            double rate = BigDecimal.valueOf(bookedSeats * 100.0 / totalCapacity)
                    .setScale(1, RoundingMode.HALF_UP).doubleValue();
            summary.setOverallOccupancyRate(rate);
        }

        // 4. Biểu đồ doanh thu 12 tháng trong năm
        List<MonthlyReportDto> monthlyBreakdown = getMonthlyRevenueBreakdown(year);
        summary.setMonthlyBreakdown(monthlyBreakdown);

        List<String> mLabels = new ArrayList<>();
        List<BigDecimal> mRevenues = new ArrayList<>();
        for (MonthlyReportDto m : monthlyBreakdown) {
            mLabels.add(m.getMonthLabel());
            mRevenues.add(m.getRevenue());
        }
        summary.setMonthlyLabels(mLabels);
        summary.setMonthlyRevenues(mRevenues);

        // 5. Biểu đồ đường doanh thu 14 ngày gần nhất
        LocalDate today = LocalDate.now();
        LocalDate start14Days = today.minusDays(13);
        List<RevenueStatisticDto> recentDaily = getDailyRevenueStats(start14Days, today, null, null);
        List<String> dLabels = new ArrayList<>();
        List<BigDecimal> dRevenues = new ArrayList<>();
        for (RevenueStatisticDto d : recentDaily) {
            dLabels.add(d.getPeriodLabel());
            dRevenues.add(d.getTotalRevenue());
        }
        summary.setDailyLabels(dLabels);
        summary.setDailyRevenues(dRevenues);

        // 6. Doanh thu theo nhà xe & tuyến đường
        summary.setCompanySalesStats(getSalesByCompany());
        summary.setRouteSalesStats(getSalesByRoute());

        // 7. Tỷ lệ lấp đầy ghế theo từng chuyến xe
        summary.setTripOccupancies(getTripOccupancyStats(12));

        // 8. Bảng giao dịch gần đây
        summary.setRecentTransactions(getRecentTransactions(15));

        return summary;
    }

    @Transactional(readOnly = true)
    public List<MonthlyReportDto> getMonthlyRevenueBreakdown(int year) {
        LocalDateTime startOfYear = LocalDateTime.of(year, 1, 1, 0, 0, 0);
        LocalDateTime endOfYear = LocalDateTime.of(year, 12, 31, 23, 59, 59);

        List<Ticket> allYearTickets = ticketRepository.findAllTicketsBetween(startOfYear, endOfYear);

        Map<Integer, BigDecimal> revenueByMonth = new HashMap<>();
        Map<Integer, Long> paidCountByMonth = new HashMap<>();
        Map<Integer, Long> cancelledCountByMonth = new HashMap<>();

        for (int m = 1; m <= 12; m++) {
            revenueByMonth.put(m, BigDecimal.ZERO);
            paidCountByMonth.put(m, 0L);
            cancelledCountByMonth.put(m, 0L);
        }

        if (allYearTickets != null) {
            for (Ticket t : allYearTickets) {
                if (t.getCreatedAt() == null) continue;
                int m = t.getCreatedAt().getMonthValue();
                if (t.getTicketStatus() == TicketStatus.CONFIRMED && t.getPaymentStatus() == PaymentStatus.PAID) {
                    revenueByMonth.put(m, revenueByMonth.get(m).add(t.getTotalAmount()));
                    paidCountByMonth.put(m, paidCountByMonth.get(m) + 1);
                } else if (t.getTicketStatus() == TicketStatus.CANCELLED) {
                    cancelledCountByMonth.put(m, cancelledCountByMonth.get(m) + 1);
                }
            }
        }

        List<MonthlyReportDto> result = new ArrayList<>(12);
        BigDecimal prevRevenue = BigDecimal.ZERO;
        for (int m = 1; m <= 12; m++) {
            BigDecimal currentRev = revenueByMonth.get(m);
            double growth = 0.0;
            if (prevRevenue.compareTo(BigDecimal.ZERO) > 0) {
                growth = currentRev.subtract(prevRevenue)
                        .multiply(new BigDecimal("100"))
                        .divide(prevRevenue, 1, RoundingMode.HALF_UP)
                        .doubleValue();
            } else if (currentRev.compareTo(BigDecimal.ZERO) > 0 && m > 1) {
                growth = 100.0;
            }
            result.add(new MonthlyReportDto(
                    m,
                    year,
                    "Tháng " + m,
                    currentRev,
                    paidCountByMonth.get(m),
                    cancelledCountByMonth.get(m),
                    growth
            ));
            prevRevenue = currentRev;
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<RevenueStatisticDto> getDailyRevenueStats(LocalDate startDate,
                                                           LocalDate endDate,
                                                           Long companyId,
                                                           String routeName) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Ticket> paidTickets = ticketRepository.findPaidTicketsBetween(
                TicketStatus.CONFIRMED, PaymentStatus.PAID, startDateTime, endDateTime);

        Map<LocalDate, BigDecimal> revMap = new LinkedHashMap<>();
        Map<LocalDate, Long> countMap = new LinkedHashMap<>();

        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            revMap.put(cursor, BigDecimal.ZERO);
            countMap.put(cursor, 0L);
            cursor = cursor.plusDays(1);
        }

        BigDecimal grandTotal = BigDecimal.ZERO;
        if (paidTickets != null) {
            for (Ticket t : paidTickets) {
                if (companyId != null && !t.getBusTrip().getBusCompany().getId().equals(companyId)) {
                    continue;
                }
                if (routeName != null && !routeName.isBlank()
                        && !t.getBusTrip().getRouteName().equalsIgnoreCase(routeName.trim())) {
                    continue;
                }
                LocalDate day = t.getCreatedAt().toLocalDate();
                if (revMap.containsKey(day)) {
                    revMap.put(day, revMap.get(day).add(t.getTotalAmount()));
                    countMap.put(day, countMap.get(day) + 1);
                    grandTotal = grandTotal.add(t.getTotalAmount());
                }
            }
        }

        List<RevenueStatisticDto> list = new ArrayList<>();
        for (Map.Entry<LocalDate, BigDecimal> entry : revMap.entrySet()) {
            LocalDate day = entry.getKey();
            BigDecimal rev = entry.getValue();
            long count = countMap.getOrDefault(day, 0L);
            BigDecimal avg = count > 0
                    ? rev.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            double pct = grandTotal.compareTo(BigDecimal.ZERO) > 0
                    ? rev.multiply(new BigDecimal("100")).divide(grandTotal, 1, RoundingMode.HALF_UP).doubleValue()
                    : 0.0;
            list.add(new RevenueStatisticDto(day.format(SHORT_DAY_FMT), rev, count, avg, pct));
        }
        return list;
    }

    @Transactional(readOnly = true)
    public List<RevenueStatisticDto> getYearlyRevenueStats(int startYear, int endYear) {
        List<RevenueStatisticDto> result = new ArrayList<>();
        BigDecimal grandTotal = BigDecimal.ZERO;
        Map<Integer, BigDecimal> revByYear = new LinkedHashMap<>();
        Map<Integer, Long> countByYear = new LinkedHashMap<>();

        for (int y = startYear; y <= endYear; y++) {
            LocalDateTime start = LocalDateTime.of(y, 1, 1, 0, 0, 0);
            LocalDateTime end = LocalDateTime.of(y, 12, 31, 23, 59, 59);
            List<Ticket> paid = ticketRepository.findPaidTicketsBetween(TicketStatus.CONFIRMED, PaymentStatus.PAID, start, end);
            BigDecimal rev = BigDecimal.ZERO;
            long count = 0;
            if (paid != null) {
                for (Ticket t : paid) {
                    rev = rev.add(t.getTotalAmount());
                    count++;
                }
            }
            revByYear.put(y, rev);
            countByYear.put(y, count);
            grandTotal = grandTotal.add(rev);
        }

        for (int y = startYear; y <= endYear; y++) {
            BigDecimal rev = revByYear.get(y);
            long count = countByYear.get(y);
            BigDecimal avg = count > 0 ? rev.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            double pct = grandTotal.compareTo(BigDecimal.ZERO) > 0
                    ? rev.multiply(new BigDecimal("100")).divide(grandTotal, 1, RoundingMode.HALF_UP).doubleValue()
                    : 0.0;
            result.add(new RevenueStatisticDto("Năm " + y, rev, count, avg, pct));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<TicketSalesStatDto> getSalesByCompany() {
        List<Object[]> rows = ticketRepository.aggregateSalesByCompany(TicketStatus.CONFIRMED, PaymentStatus.PAID);
        return convertAggregatedRows(rows);
    }

    @Transactional(readOnly = true)
    public List<TicketSalesStatDto> getSalesByRoute() {
        List<Object[]> rows = ticketRepository.aggregateSalesByRoute(TicketStatus.CONFIRMED, PaymentStatus.PAID);
        return convertAggregatedRows(rows);
    }

    private List<TicketSalesStatDto> convertAggregatedRows(List<Object[]> rows) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (Object[] row : rows) {
            BigDecimal rev = (row[3] instanceof BigDecimal) ? (BigDecimal) row[3] : BigDecimal.ZERO;
            totalRevenue = totalRevenue.add(rev);
        }

        List<TicketSalesStatDto> list = new ArrayList<>();
        for (Object[] row : rows) {
            String name = row[0] != null ? row[0].toString() : "N/A";
            String secondary = row[1] != null ? row[1].toString() : "";
            long count = (row[2] instanceof Number) ? ((Number) row[2]).longValue() : 0L;
            BigDecimal rev = (row[3] instanceof BigDecimal) ? (BigDecimal) row[3] : BigDecimal.ZERO;
            double pct = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                    ? rev.multiply(new BigDecimal("100")).divide(totalRevenue, 1, RoundingMode.HALF_UP).doubleValue()
                    : 0.0;
            list.add(new TicketSalesStatDto(name, secondary, count, rev, pct));
        }
        return list;
    }

    @Transactional(readOnly = true)
    public List<SeatOccupancyDto> getTripOccupancyStats(int limit) {
        List<BusTrip> trips = tripRepository.findAllByOrderByDepartureTimeDesc();
        if (trips == null || trips.isEmpty()) {
            return Collections.emptyList();
        }
        List<SeatOccupancyDto> result = new ArrayList<>();
        int count = 0;
        for (BusTrip trip : trips) {
            if (count >= limit) break;
            int total = trip.getTotalSeats() != null ? trip.getTotalSeats() : 0;
            int booked = tripRepository.countTripSeatsByStatus(trip.getId(), SeatStatus.BOOKED);
            int held = tripRepository.countTripSeatsByStatus(trip.getId(), SeatStatus.HELD);
            int avail = Math.max(0, total - booked - held);
            double rate = total > 0
                    ? BigDecimal.valueOf(booked * 100.0 / total).setScale(1, RoundingMode.HALF_UP).doubleValue()
                    : 0.0;
            result.add(new SeatOccupancyDto(
                    trip.getId(),
                    trip.getTripCode(),
                    trip.getRouteName(),
                    trip.getBusCompany() != null ? trip.getBusCompany().getName() : "N/A",
                    trip.getDepartureTime(),
                    total,
                    booked,
                    held,
                    avail,
                    rate
            ));
            count++;
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<RecentTransactionDto> getRecentTransactions(int limit) {
        List<PaymentTransaction> allTxns = transactionRepository.findAllByOrderByCreatedAtDesc();
        List<RecentTransactionDto> result = new ArrayList<>();

        if (allTxns != null && !allTxns.isEmpty()) {
            int count = 0;
            for (PaymentTransaction pt : allTxns) {
                if (count >= limit) break;
                Ticket t = pt.getTicket();
                RecentTransactionDto dto = new RecentTransactionDto(
                        pt.getTransactionCode(),
                        t != null ? t.getTicketCode() : "N/A",
                        t != null ? t.getPassengerName() : "N/A",
                        t != null ? t.getPassengerPhone() : "",
                        (t != null && t.getBusTrip() != null) ? t.getBusTrip().getRouteName() : "N/A",
                        (t != null && t.getBusTrip() != null && t.getBusTrip().getBusCompany() != null)
                                ? t.getBusTrip().getBusCompany().getName() : "N/A",
                        pt.getAmount(),
                        pt.getPaymentMethod() != null ? pt.getPaymentMethod().getDisplayName() : "N/A",
                        pt.getStatus() != null ? pt.getStatus().name() : "PENDING",
                        DateTimeUtils.formatDateTime(pt.getCreatedAt())
                );
                if (t != null) {
                    dto.setTicketId(t.getId());
                    dto.setSeatNumber(t.getSeat() != null ? t.getSeat().getSeatNumber() : "-");
                    dto.setTicketStatus(t.getTicketStatus() != null ? t.getTicketStatus().name() : "PENDING");
                    dto.setPaymentStatus(t.getPaymentStatus() != null ? t.getPaymentStatus().name() : "UNPAID");
                }
                dto.setBookingTime(pt.getCreatedAt());
                result.add(dto);
                count++;
            }
            return result;
        }

        List<Ticket> recentTickets = ticketRepository.findRecentTickets(PageRequest.of(0, limit));
        if (recentTickets != null) {
            for (Ticket t : recentTickets) {
                RecentTransactionDto dto = new RecentTransactionDto(
                        "TXN-" + t.getId(),
                        t.getTicketCode(),
                        t.getPassengerName(),
                        t.getPassengerPhone(),
                        t.getBusTrip() != null ? t.getBusTrip().getRouteName() : "N/A",
                        (t.getBusTrip() != null && t.getBusTrip().getBusCompany() != null)
                                ? t.getBusTrip().getBusCompany().getName() : "N/A",
                        t.getTotalAmount(),
                        t.getPaymentMethod() != null ? t.getPaymentMethod().getDisplayName() : "N/A",
                        t.getPaymentStatus().name(),
                        DateTimeUtils.formatDateTime(t.getCreatedAt())
                );
                dto.setTicketId(t.getId());
                dto.setSeatNumber(t.getSeat() != null ? t.getSeat().getSeatNumber() : "-");
                dto.setTicketStatus(t.getTicketStatus() != null ? t.getTicketStatus().name() : "PENDING");
                dto.setPaymentStatus(t.getPaymentStatus() != null ? t.getPaymentStatus().name() : "UNPAID");
                dto.setBookingTime(t.getCreatedAt());
                result.add(dto);
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Page<CustomerAnalyticsDto> getCustomerAnalyticsPage(String keyword, Pageable pageable) {
        Page<User> userPage = customerRepository.searchCustomers(RoleName.ROLE_CUSTOMER, keyword, pageable);
        if (userPage == null || !userPage.hasContent()) {
            return Page.empty(pageable);
        }

        List<CustomerAnalyticsDto> dtoList = new ArrayList<>();
        for (User u : userPage.getContent()) {
            long totalBooked = ticketRepository.countTotalTicketsByUser(u.getId());
            long paidCount = ticketRepository.countPaidTicketsByUser(u.getId(), TicketStatus.CONFIRMED, PaymentStatus.PAID);
            BigDecimal totalSpent = ticketRepository.sumTotalSpentByUser(u.getId(), TicketStatus.CONFIRMED, PaymentStatus.PAID);
            dtoList.add(new CustomerAnalyticsDto(
                    u.getId(),
                    u.getUsername(),
                    u.getFullName(),
                    u.getEmail(),
                    u.getPhone(),
                    u.isEnabled(),
                    totalBooked,
                    paidCount,
                    totalSpent,
                    u.getCreatedAt()
            ));
        }
        return new PageImpl<>(dtoList, pageable, userPage.getTotalElements());
    }
}
