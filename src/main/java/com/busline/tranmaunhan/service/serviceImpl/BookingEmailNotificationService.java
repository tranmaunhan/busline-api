package com.busline.tranmaunhan.service.serviceImpl;

import com.busline.tranmaunhan.config.EmailNotificationProperties;
import com.busline.tranmaunhan.dto.booking.BookingResponse;
import com.busline.tranmaunhan.dto.booking.TicketResponse;
import com.busline.tranmaunhan.entity.Bookings;
import com.busline.tranmaunhan.service.BookingNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.NumberFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingEmailNotificationService implements BookingNotificationService {

    private static final Locale VIETNAM_LOCALE = new Locale("vi", "VN");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
            "HH:mm 'ngay' dd/MM/yyyy",
            VIETNAM_LOCALE);
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(VIETNAM_LOCALE);
    private static final String BOOKING_LOOKUP_URL = "https://aihost.io.vn/booking-lookup";

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final EmailNotificationProperties emailNotificationProperties;

    @Override
    public void sendBookingPendingNotification(Bookings booking, BookingResponse bookingResponse) {
        String content = """
                Xin chao %s,

                Cam on ban da lua chon SaigonST BusLine.

                Yeu cau dat cho cua ban da duoc ghi nhan thanh cong tren he thong.

                ==================================================
                THONG TIN DAT CHO
                ==================================================

                Ma dat cho: %s
                Tuyen duong: %s -> %s
                Diem don: %s
                Diem tra: %s
                Thoi gian khoi hanh: %s
                Ghe da chon: %s
                Tong tien tam tinh: %s
                Nguoi lien he: %s
                So dien thoai: %s
                Email nhan ve: %s
                Han thanh toan: %s
                Ghi chu: %s

                ==================================================
                TRANG THAI
                ==================================================

                Don dang cho thanh toan.

                Ban co the tra cuu hoac tiep tuc thanh toan tai:
                %s

                Neu qua han thanh toan, he thong se tu dong xoa don va mo lai ghe.

                Tran trong,
                SaigonST BusLine
                """
                .formatted(
                        resolveDisplayName(booking),
                        bookingResponse.getBookingCode(),
                        bookingResponse.getRouteOrigin(),
                        bookingResponse.getRouteDestination(),
                        bookingResponse.getPickupLocationName(),
                        bookingResponse.getDropoffLocationName(),
                        formatDateTime(bookingResponse.getTripDepartureTime()),
                        joinSeatCodes(bookingResponse.getTickets()),
                        formatCurrency(bookingResponse.getTotalAmount()),
                        defaultText(booking.getContactName()),
                        defaultText(booking.getContactPhone()),
                        defaultText(resolveRecipientEmail(booking)),
                        formatDateTime(booking.getPaymentExpiry()),
                        defaultText(booking.getNote()),
                        BOOKING_LOOKUP_URL);

        sendEmail(
                booking,
                bookingResponse,
                "Giu cho thanh cong - " + bookingResponse.getBookingCode(),
                content);
    }

    @Override
    public void sendBookingConfirmedNotification(Bookings booking, BookingResponse bookingResponse) {
        String content = """
                Xin chao %s,

                Thanh toan cua ban da duoc xac nhan thanh cong va ve xe da duoc phat hanh.

                ==================================================
                THONG TIN VE
                ==================================================

                Ma dat ve: %s
                Tuyen duong: %s -> %s
                Diem don: %s
                Diem tra: %s
                Thoi gian khoi hanh: %s
                Ghe da dat: %s
                Tong tien da thanh toan: %s
                Nguoi lien he: %s
                So dien thoai: %s
                Email nhan ve: %s
                Ghi chu: %s

                Ban co the tra cuu thong tin ve tai:
                %s

                Tran trong,
                SaigonST BusLine
                """
                .formatted(
                        resolveDisplayName(booking),
                        bookingResponse.getBookingCode(),
                        bookingResponse.getRouteOrigin(),
                        bookingResponse.getRouteDestination(),
                        bookingResponse.getPickupLocationName(),
                        bookingResponse.getDropoffLocationName(),
                        formatDateTime(bookingResponse.getTripDepartureTime()),
                        joinSeatCodes(bookingResponse.getTickets()),
                        formatCurrency(bookingResponse.getTotalAmount()),
                        defaultText(booking.getContactName()),
                        defaultText(booking.getContactPhone()),
                        defaultText(resolveRecipientEmail(booking)),
                        defaultText(booking.getNote()),
                        BOOKING_LOOKUP_URL);

        sendEmail(
                booking,
                bookingResponse,
                "Dat ve thanh cong - " + bookingResponse.getBookingCode(),
                content);
    }

    private void sendEmail(
            Bookings booking,
            BookingResponse bookingResponse,
            String subject,
            String body
    ) {
        String recipientEmail = resolveRecipientEmail(booking);
        if (!emailNotificationProperties.canSendTo(recipientEmail)) {
            log.info(
                    "Bo qua gui email booking {} vi email khong hop le hoac mail chua duoc cau hinh",
                    bookingResponse.getBookingCode());
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("JavaMailSender chua duoc cau hinh");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipientEmail);
            message.setSubject(subject);
            message.setText(body);

            if (StringUtils.hasText(emailNotificationProperties.getFromAddress())) {
                message.setFrom(emailNotificationProperties.getFromAddress());
            }

            mailSender.send(message);
            log.info("Da gui email booking {} toi {}", bookingResponse.getBookingCode(), recipientEmail);
        } catch (MailException ex) {
            log.warn(
                    "Gui email thong bao booking {} that bai: {}",
                    bookingResponse.getBookingCode(),
                    ex.getMessage());
        }
    }

    private String resolveDisplayName(Bookings booking) {
        if (StringUtils.hasText(booking.getContactName())) {
            return booking.getContactName().trim();
        }

        if (booking.getUser() != null && StringUtils.hasText(booking.getUser().getFullName())) {
            return booking.getUser().getFullName().trim();
        }

        String recipientEmail = resolveRecipientEmail(booking);
        if (StringUtils.hasText(recipientEmail)) {
            return recipientEmail;
        }

        return "Quy khach";
    }

    private String resolveRecipientEmail(Bookings booking) {
        if (StringUtils.hasText(booking.getContactEmail())) {
            return booking.getContactEmail().trim();
        }

        if (booking.getUser() != null && StringUtils.hasText(booking.getUser().getEmail())) {
            return booking.getUser().getEmail().trim();
        }

        return null;
    }

    private String joinSeatCodes(List<TicketResponse> tickets) {
        if (tickets == null || tickets.isEmpty()) {
            return "Chua co thong tin";
        }

        return tickets.stream()
                .map(TicketResponse::getSeatCode)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(", "));
    }

    private String formatCurrency(Number amount) {
        if (amount == null) {
            return "0 VND";
        }
        return CURRENCY_FORMATTER.format(amount);
    }

    private String formatDateTime(OffsetDateTime value) {
        if (value == null) {
            return "Chua xac dinh";
        }
        return DATE_TIME_FORMATTER.format(value);
    }

    private String defaultText(String value) {
        if (!StringUtils.hasText(value)) {
            return "Khong co";
        }
        return value.trim();
    }
}
