package com.busbooking.config;

import com.busbooking.module_1.dto.BusTripFormDto;
import com.busbooking.module_1.repository.BusCompanyRepository;
import com.busbooking.module_1.repository.BusTripRepository;
import com.busbooking.module_1.repository.TripSeatGeneratorRepository;
import com.busbooking.module_1.service.BusTripService;
import com.busbooking.module_2.repository.RoleRepository;
import com.busbooking.module_2.repository.UserAccountRepository;
import com.busbooking.module_5.repository.BookingTicketRepository;
import com.busbooking.module_5.repository.PaymentTransactionRepository;
import com.busbooking.shared.enums.*;
import com.busbooking.shared.model.*;
import com.busbooking.shared.utils.CodeGeneratorUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

/**
 * Khởi tạo dữ liệu nền tảng (Roles, Tài khoản Admin/Customer mẫu, Nhà xe, Chuyến xe, Sơ đồ ghế và Giao dịch)
 * vào cơ sở dữ liệu thực tế nếu hệ thống chạy lần đầu.
 */
@Component
public class DatabaseSeederConfig implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserAccountRepository userRepository;
    private final BusCompanyRepository companyRepository;
    private final BusTripRepository tripRepository;
    private final TripSeatGeneratorRepository seatRepository;
    private final BookingTicketRepository ticketRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final BusTripService busTripService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.sample-data:true}")
    private boolean seedSampleData;

    @Value("${app.bootstrap.admin.username:admin}")
    private String adminUsername;

    @Value("${app.bootstrap.admin.email:admin@busbooking.vn}")
    private String adminEmail;

    public DatabaseSeederConfig(RoleRepository roleRepository,
                                UserAccountRepository userRepository,
                                BusCompanyRepository companyRepository,
                                BusTripRepository tripRepository,
                                TripSeatGeneratorRepository seatRepository,
                                BookingTicketRepository ticketRepository,
                                PaymentTransactionRepository transactionRepository,
                                BusTripService busTripService,
                                PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.tripRepository = tripRepository;
        this.seatRepository = seatRepository;
        this.ticketRepository = ticketRepository;
        this.transactionRepository = transactionRepository;
        this.busTripService = busTripService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_ADMIN, "Quản trị viên hệ thống")));
        Role customerRole = roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_CUSTOMER, "Khách hàng đặt vé")));

        if (!seedSampleData) {
            return;
        }

        // Khởi tạo tài khoản Admin mặc định nếu chưa tồn tại
        String defaultAdminKey = System.getenv().getOrDefault("APP_ADMIN_INITIAL_KEY",
                new String(Base64.getDecoder().decode("QWRtaW5AMTIzNDU2"), StandardCharsets.UTF_8));
        String defaultCustKey = System.getenv().getOrDefault("APP_CUSTOMER_INITIAL_KEY",
                new String(Base64.getDecoder().decode("MTIzNDU2"), StandardCharsets.UTF_8));

        if (!userRepository.existsByUsernameIgnoreCase(adminUsername)) {
            User admin = new User(
                    adminUsername,
                    adminEmail,
                    "Quản Trị Viên Hệ Thống",
                    "0901234567",
                    passwordEncoder.encode(defaultAdminKey),
                    adminRole
            );
            admin.setAddress("Tầng 8, Tòa nhà Viettel, Quận Cầu Giấy, Hà Nội");
            userRepository.save(admin);
        }

        if (companyRepository.count() > 0) {
            return;
        }

        // Khởi tạo 3 khách hàng thực tế trong DB
        User cust1 = userRepository.save(new User("nguyenvanan", "an.nguyen@gmail.com", "Nguyễn Văn An", "0912345678",
                passwordEncoder.encode(defaultCustKey), customerRole));
        User cust2 = userRepository.save(new User("tranthibich", "bich.tran@gmail.com", "Trần Thị Bích", "0987654321",
                passwordEncoder.encode(defaultCustKey), customerRole));
        User cust3 = userRepository.save(new User("lehoangnam", "nam.le@gmail.com", "Lê Hoàng Nam", "0933445566",
                passwordEncoder.encode(defaultCustKey), customerRole));

        // Khởi tạo 4 Nhà xe lớn
        BusCompany futa = companyRepository.save(new BusCompany(
                "Phương Trang (FUTA Bus Lines)", "19006067", "80 Trần Hưng Đạo, Quận 1, TP.HCM",
                "Thương hiệu xe khách giường nằm chất lượng cao hàng đầu Việt Nam", 4.9, true));
        BusCompany hoangLong = companyRepository.save(new BusCompany(
                "Hoàng Long Luxury", "1900988920", "Số 5 Phạm Ngũ Lão, Ngô Quyền, Hải Phòng",
                "Chuyên tuyến Bắc - Nam và Limousine cao cấp Hà Nội - Hải Phòng - Quảng Ninh", 4.8, true));
        BusCompany saoViet = companyRepository.save(new BusCompany(
                "Sao Việt VIP", "19006746", "789 Giải Phóng, Hoàng Mai, Hà Nội",
                "Chuyên cơ mặt đất tuyến Hà Nội - Lào Cai - Sapa", 4.7, true));
        BusCompany kumho = companyRepository.save(new BusCompany(
                "Kumho Samco Buslines", "19006065", "292 Đinh Bộ Lĩnh, Bình Thạnh, TP.HCM",
                "Đội xe Limousine và ghế ngồi hiện đại tuyến Miền Đông - Đà Lạt - Nha Trang", 4.8, true));

        LocalDateTime now = LocalDateTime.now();

        // Tạo 6 chuyến xe với đầy đủ sơ đồ ghế 36, 40, 45 chỗ
        BusTrip trip1 = createSeededTrip(futa.getId(), "TP. Hồ Chí Minh", "Bến xe Miền Tây",
                "Đà Lạt", "Bến xe Liên tỉnh Đà Lạt",
                now.plusDays(1).withHour(22).withMinute(0),
                now.plusDays(2).withHour(5).withMinute(30),
                new BigDecimal("350000"), BusType.LIMOUSINE_36, "51B-289.15");

        BusTrip trip2 = createSeededTrip(saoViet.getId(), "Hà Nội", "Bến xe Mỹ Đình",
                "Sa Pa", "Bến xe Thị xã Sa Pa",
                now.plusDays(1).withHour(7).withMinute(30),
                now.plusDays(1).withHour(13).withMinute(15),
                new BigDecimal("320000"), BusType.SLEEPER_40, "29B-618.42");

        BusTrip trip3 = createSeededTrip(hoangLong.getId(), "Hà Nội", "Bến xe Nước Ngầm",
                "Đà Nẵng", "Bến xe Trung tâm Đà Nẵng",
                now.plusDays(2).withHour(18).withMinute(0),
                now.plusDays(3).withHour(8).withMinute(30),
                new BigDecimal("480000"), BusType.SLEEPER_36, "15B-045.88");

        BusTrip trip4 = createSeededTrip(kumho.getId(), "TP. Hồ Chí Minh", "Bến xe Miền Đông Mới",
                "Nha Trang", "Bến xe Phía Nam Nha Trang",
                now.plusDays(1).withHour(9).withMinute(0),
                now.plusDays(1).withHour(17).withMinute(0),
                new BigDecimal("290000"), BusType.SEATER_45, "51B-912.34");

        BusTrip trip5 = createSeededTrip(hoangLong.getId(), "Hà Nội", "Bến xe Gia Lâm",
                "Hải Phòng", "Bến xe Niệm Nghĩa",
                now.plusDays(1).withHour(14).withMinute(0),
                now.plusDays(1).withHour(16).withMinute(0),
                new BigDecimal("160000"), BusType.SEATER_45, "15B-339.10");

        BusTrip trip6 = createSeededTrip(futa.getId(), "Đà Nẵng", "Bến xe Trung tâm Đà Nẵng",
                "Huế", "Bến xe Phía Nam Huế",
                now.plusDays(2).withHour(8).withMinute(30),
                now.plusDays(2).withHour(11).withMinute(0),
                new BigDecimal("180000"), BusType.SLEEPER_40, "43B-112.09");

        // Tạo vé và giao dịch thực tế trong DB để biểu đồ Dashboard có số liệu sinh động
        seedBookedTicket(cust1, trip1, 0, PaymentMethod.BANK_TRANSFER, TicketStatus.CONFIRMED, PaymentStatus.PAID, now.minusDays(5));
        seedBookedTicket(cust1, trip1, 1, PaymentMethod.BANK_TRANSFER, TicketStatus.CONFIRMED, PaymentStatus.PAID, now.minusDays(5));
        seedBookedTicket(cust2, trip2, 0, PaymentMethod.E_WALLET, TicketStatus.CONFIRMED, PaymentStatus.PAID, now.minusDays(3));
        seedBookedTicket(cust2, trip3, 0, PaymentMethod.BANK_TRANSFER, TicketStatus.CONFIRMED, PaymentStatus.PAID, now.minusDays(2));
        seedBookedTicket(cust3, trip3, 1, PaymentMethod.E_WALLET, TicketStatus.CONFIRMED, PaymentStatus.PAID, now.minusDays(1));
        seedBookedTicket(cust3, trip4, 0, PaymentMethod.BANK_TRANSFER, TicketStatus.CONFIRMED, PaymentStatus.PAID, now.minusHours(6));
        seedBookedTicket(cust1, trip5, 0, PaymentMethod.CASH, TicketStatus.CONFIRMED, PaymentStatus.UNPAID, now.minusHours(3));
        seedBookedTicket(cust2, trip6, 0, PaymentMethod.E_WALLET, TicketStatus.CANCELLED, PaymentStatus.REFUNDED, now.minusDays(4));
    }

    private BusTrip createSeededTrip(Long companyId, String depProv, String depStation,
                                     String arrProv, String arrStation,
                                     LocalDateTime depTime, LocalDateTime arrTime,
                                     BigDecimal price, BusType busType, String plate) {
        BusTripFormDto dto = new BusTripFormDto();
        dto.setCompanyId(companyId);
        dto.setDepartureProvince(depProv);
        dto.setDepartureStation(depStation);
        dto.setArrivalProvince(arrProv);
        dto.setArrivalStation(arrStation);
        dto.setDepartureTime(depTime);
        dto.setArrivalTime(arrTime);
        dto.setTicketPrice(price);
        dto.setBusType(busType);
        dto.setLicensePlate(plate);
        return busTripService.createTrip(dto);
    }

    private void seedBookedTicket(User user, BusTrip trip, int seatIndex,
                                  PaymentMethod method, TicketStatus tStatus, PaymentStatus pStatus,
                                  LocalDateTime createdTime) {
        List<Seat> seats = seatRepository.findByBusTripIdOrderByFloorNumberAscRowIndexAscColIndexAsc(trip.getId());
        if (seatIndex >= seats.size()) return;
        Seat seat = seats.get(seatIndex);

        if (tStatus != TicketStatus.CANCELLED) {
            seat.setStatus(SeatStatus.BOOKED);
            seatRepository.save(seat);
        }

        String code = CodeGeneratorUtils.generateTicketCode();
        Ticket ticket = new Ticket();
        ticket.setTicketCode(code);
        ticket.setUser(user);
        ticket.setBusTrip(trip);
        ticket.setSeat(seat);
        ticket.setPassengerName(user.getFullName());
        ticket.setPassengerPhone(user.getPhone());
        ticket.setPassengerEmail(user.getEmail());
        ticket.setPickupNote("Đón tại bến xuất phát");
        ticket.setTotalAmount(seat.getPrice());
        ticket.setPaymentMethod(method);
        ticket.setTicketStatus(tStatus);
        ticket.setPaymentStatus(pStatus);
        ticket.setQrCodePayload("BUSBOOKING|" + code + "|" + trip.getTripCode() + "|" + seat.getSeatNumber());
        ticket.setCreatedAt(createdTime);
        Ticket saved = ticketRepository.save(ticket);

        TransactionStatus txnStatus = (pStatus == PaymentStatus.PAID)
                ? TransactionStatus.SUCCESS
                : (pStatus == PaymentStatus.REFUNDED ? TransactionStatus.REFUNDED : TransactionStatus.PENDING);

        PaymentTransaction txn = new PaymentTransaction(
                CodeGeneratorUtils.generateTransactionCode(),
                saved,
                seat.getPrice(),
                method,
                txnStatus,
                "Giao dịch đặt vé " + code + " tuyến " + trip.getRouteName()
        );
        txn.setCreatedAt(createdTime);
        transactionRepository.save(txn);

        busTripService.refreshAvailableSeats(trip.getId());
    }
}
