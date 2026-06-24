package com.busline.tranmaunhan.service.serviceImpl;

import com.busline.tranmaunhan.config.EmailNotificationProperties;
import com.busline.tranmaunhan.dto.booking.BookingResponse;
import com.busline.tranmaunhan.dto.booking.TicketResponse;
import com.busline.tranmaunhan.entity.Users;
import com.busline.tranmaunhan.service.BookingNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingEmailNotificationService implements BookingNotificationService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", new Locale("vi", "VN"));

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final EmailNotificationProperties emailNotificationProperties;

    @Override
    public void sendBookingPendingNotification(Users user, BookingResponse bookingResponse) {
        sendEmail(
                user,
                bookingResponse,
                "Thong bao giu cho thanh cong - " + bookingResponse.getBookingCode(),
                """
                        Xin chao %s,

                        Ban da giu cho thanh cong tren he thong SaigonST BusLine.

                        Ma dat cho: %s
                        Tuyen duong: %s -> %s
                        Diem don: %s
                        Diem tra: %s
                        Gio khoi hanh: %s
                        Ghe da chon: %s
                        Tong tien tam tinh: %s

                        Trang thai hien tai: Dang giu cho cho thanh toan/xac nhan.

                        Cam on ban da su dung dich vu.
                        """.formatted(
                        resolveDisplayName(user),
                        bookingResponse.getBookingCode(),
                        bookingResponse.getRouteOrigin(),
                        bookingResponse.getRouteDestination(),
                        bookingResponse.getPickupLocationName(),
                        bookingResponse.getDropoffLocationName(),
                        DATE_TIME_FORMATTER.format(bookingResponse.getTripDepartureTime()),
                        joinSeatCodes(bookingResponse.getTickets()),
                        bookingResponse.getTotalAmount().toPlainString()
                )
        );
    }

    @Override
    public void sendBookingConfirmedNotification(Users user, BookingResponse bookingResponse) {
        sendEmail(
                user,
                bookingResponse,
                "Thong bao dat ve thanh cong - " + bookingResponse.getBookingCode(),
                """
                        Xin chao %s,

                        Ve xe cua ban da duoc xac nhan thanh cong.

                        Ma dat ve: %s
                        Tuyen duong: %s -> %s
                        Diem don: %s
                        Diem tra: %s
                        Gio khoi hanh: %s
                        Ghe da dat: %s
                        Tong tien: %s

                        Trang thai hien tai: Dat ve thanh cong.

                        Chuc ban co chuyen di an toan va thuan loi.
                        """.formatted(
                        resolveDisplayName(user),
                        bookingResponse.getBookingCode(),
                        bookingResponse.getRouteOrigin(),
                        bookingResponse.getRouteDestination(),
                        bookingResponse.getPickupLocationName(),
                        bookingResponse.getDropoffLocationName(),
                        DATE_TIME_FORMATTER.format(bookingResponse.getTripDepartureTime()),
                        joinSeatCodes(bookingResponse.getTickets()),
                        bookingResponse.getTotalAmount().toPlainString()
                )
        );
    }

    private void sendEmail(
            Users user,
            BookingResponse bookingResponse,
            String subject,
            String body
    ) {
        if (!emailNotificationProperties.canSendTo(user.getEmail())) {
            log.info(
                    "Bo qua gui email booking {} vi cau hinh mail chua san sang hoac user khong co email",
                    bookingResponse.getBookingCode()
            );
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.info(
                    "Bo qua gui email booking {} vi JavaMailSender chua duoc cau hinh",
                    bookingResponse.getBookingCode()
            );
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject(subject);
            message.setText(body);

            if (StringUtils.hasText(emailNotificationProperties.getFromAddress())) {
                message.setFrom(emailNotificationProperties.getFromAddress());
            }

            mailSender.send(message);
        } catch (MailException ex) {
            log.warn(
                    "Gui email thong bao booking {} that bai: {}",
                    bookingResponse.getBookingCode(),
                    ex.getMessage()
            );
        }
    }

    private String resolveDisplayName(Users user) {
        if (StringUtils.hasText(user.getFullName())) {
            return user.getFullName();
        }
        if (StringUtils.hasText(user.getEmail())) {
            return user.getEmail();
        }
        return "ban";
    }

    private String joinSeatCodes(List<TicketResponse> tickets) {
        return tickets.stream()
                .map(TicketResponse::getSeatCode)
                .collect(Collectors.joining(", "));
    }
}
