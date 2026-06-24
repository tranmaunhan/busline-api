package com.busline.tranmaunhan.service.serviceImpl;

import com.busline.tranmaunhan.config.EmailNotificationProperties;
import com.busline.tranmaunhan.dto.booking.BookingResponse;
import com.busline.tranmaunhan.dto.booking.TicketResponse;
import com.busline.tranmaunhan.entity.Users;
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
            "HH:mm 'ngày' dd/MM/yyyy",
            VIETNAM_LOCALE);

    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(VIETNAM_LOCALE);

    private static final String BOOKING_LOOKUP_URL = "https://aihost.io.vn/booking-lookup";

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final EmailNotificationProperties emailNotificationProperties;

    @Override
    public void sendBookingPendingNotification(
            Users user,
            BookingResponse bookingResponse) {

        String content = """
                Xin chào %s,

                Cảm ơn bạn đã lựa chọn SaigonST BusLine.

                Chúng tôi xin xác nhận yêu cầu đặt chỗ của bạn đã được ghi nhận thành công trên hệ thống.

                ==================================================

                THÔNG TIN ĐẶT CHỖ

                ==================================================

                Mã đặt chỗ: %s

                Tuyến đường: %s → %s

                Điểm đón: %s

                Điểm trả: %s

                Thời gian khởi hành: %s

                Ghế đã chọn: %s

                Tổng tiền tạm tính: %s

                ==================================================

                TRẠNG THÁI

                ==================================================

                ⏳ Đang chờ thanh toán

                Để hoàn tất việc đặt vé, vui lòng truy cập:

                %s

                Tại đây bạn có thể:

                • Thanh toán đơn đặt chỗ
                • Kiểm tra trạng thái thanh toán
                • Tra cứu thông tin vé
                • Theo dõi tình trạng đặt vé

                Lưu ý:
                Đơn đặt chỗ chỉ được giữ trong thời gian quy định.
                Nếu không hoàn tất thanh toán đúng hạn, hệ thống có thể tự động hủy giữ chỗ.

                Cảm ơn bạn đã sử dụng dịch vụ của SaigonST BusLine.

                Trân trọng,

                SaigonST BusLine
                Hệ thống đặt vé xe khách trực tuyến
                https://aihost.io.vn
                """
                .formatted(
                        resolveDisplayName(user),
                        bookingResponse.getBookingCode(),
                        bookingResponse.getRouteOrigin(),
                        bookingResponse.getRouteDestination(),
                        bookingResponse.getPickupLocationName(),
                        bookingResponse.getDropoffLocationName(),
                        DATE_TIME_FORMATTER.format(
                                bookingResponse.getTripDepartureTime()),
                        joinSeatCodes(bookingResponse.getTickets()),
                        formatCurrency(
                                bookingResponse.getTotalAmount()),
                        BOOKING_LOOKUP_URL);

        sendEmail(
                user,
                bookingResponse,
                "🚌 Giữ chỗ thành công - " +
                        bookingResponse.getBookingCode(),
                content);
    }

    @Override
    public void sendBookingConfirmedNotification(
            Users user,
            BookingResponse bookingResponse) {

        String content = """
                Xin chào %s,

                SaigonST BusLine xin chúc mừng!

                Thanh toán của bạn đã được xác nhận thành công và vé xe đã được phát hành.

                ==================================================

                THÔNG TIN VÉ

                ==================================================

                Mã đặt vé: %s

                Tuyến đường: %s → %s

                Điểm đón: %s

                Điểm trả: %s

                Thời gian khởi hành: %s

                Ghế đã đặt: %s

                Tổng tiền đã thanh toán: %s

                ==================================================

                TRẠNG THÁI

                ==================================================

                ✅ Đặt vé thành công

                Bạn có thể tra cứu thông tin vé bất cứ lúc nào tại:

                %s

                Tại đây bạn có thể:

                • Xem lại thông tin vé
                • Kiểm tra trạng thái chuyến đi
                • Tra cứu bằng mã đặt vé hoặc số điện thoại
                • Theo dõi lịch trình khởi hành

                Vui lòng có mặt tại điểm đón trước giờ khởi hành từ 15 đến 30 phút.

                Khi lên xe, vui lòng cung cấp:

                • Mã đặt vé
                hoặc
                • Số điện thoại đặt vé

                Chúc bạn có một chuyến đi an toàn và thuận lợi.

                Trân trọng,

                SaigonST BusLine
                Hệ thống đặt vé xe khách trực tuyến
                https://aihost.io.vn
                """
                .formatted(
                        resolveDisplayName(user),
                        bookingResponse.getBookingCode(),
                        bookingResponse.getRouteOrigin(),
                        bookingResponse.getRouteDestination(),
                        bookingResponse.getPickupLocationName(),
                        bookingResponse.getDropoffLocationName(),
                        DATE_TIME_FORMATTER.format(
                                bookingResponse.getTripDepartureTime()),
                        joinSeatCodes(bookingResponse.getTickets()),
                        formatCurrency(
                                bookingResponse.getTotalAmount()),
                        BOOKING_LOOKUP_URL);

        sendEmail(
                user,
                bookingResponse,
                "🎫 Đặt vé thành công - " +
                        bookingResponse.getBookingCode(),
                content);
    }

    private void sendEmail(
            Users user,
            BookingResponse bookingResponse,
            String subject,
            String body) {

        if (!emailNotificationProperties.canSendTo(user.getEmail())) {
            log.info(
                    "Bỏ qua gửi email booking {} vì email không hợp lệ hoặc mail chưa được cấu hình",
                    bookingResponse.getBookingCode());
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();

        if (mailSender == null) {
            log.warn(
                    "JavaMailSender chưa được cấu hình");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setTo(user.getEmail());
            message.setSubject(subject);
            message.setText(body);

            if (StringUtils.hasText(
                    emailNotificationProperties.getFromAddress())) {
                message.setFrom(
                        emailNotificationProperties.getFromAddress());
            }

            mailSender.send(message);

            log.info(
                    "Đã gửi email booking {} tới {}",
                    bookingResponse.getBookingCode(),
                    user.getEmail());

        } catch (MailException ex) {

            log.warn(
                    "Gửi email thông báo booking {} thất bại: {}",
                    bookingResponse.getBookingCode(),
                    ex.getMessage());
        }
    }

    private String resolveDisplayName(Users user) {

        if (StringUtils.hasText(user.getFullName())) {
            return user.getFullName();
        }

        if (StringUtils.hasText(user.getEmail())) {
            return user.getEmail();
        }

        return "Quý khách";
    }

    private String joinSeatCodes(
            List<TicketResponse> tickets) {

        if (tickets == null || tickets.isEmpty()) {
            return "Chưa có thông tin";
        }

        return tickets.stream()
                .map(TicketResponse::getSeatCode)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(", "));
    }

    private String formatCurrency(Number amount) {

        if (amount == null) {
            return "0 ₫";
        }

        return CURRENCY_FORMATTER.format(amount);
    }

}
