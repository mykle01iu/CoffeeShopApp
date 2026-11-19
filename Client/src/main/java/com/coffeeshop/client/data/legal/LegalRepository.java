package com.coffeeshop.client.data.legal;

import com.coffeeshop.client.data.support.SupportInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Ghi chú: Nội dung dưới đây chỉ là văn bản mẫu cho ứng dụng bán đồ uống,
 * cần nhóm/pháp lý duyệt trước khi phát hành chính thức.
 */
public class LegalRepository {
    private static LegalRepository instance;

    private LegalRepository() { }

    public static synchronized LegalRepository getInstance() {
        if (instance == null) {
            instance = new LegalRepository();
        }
        return instance;
    }

    public LegalDocument getDocument(LegalType type) {
        if (type == LegalType.PRIVACY) {
            return getPrivacyPolicy();
        } else {
            return getTermsOfService();
        }
    }

    private LegalDocument getTermsOfService() {
        List<LegalSection> sections = new ArrayList<>();
        sections.add(new LegalSection(
                "1. Giới thiệu và chấp nhận điều khoản",
                "Chào mừng bạn đến với Mộc Miên Coffee. Khi tải về, truy cập hoặc sử dụng ứng dụng đặt món Mộc Miên Coffee, bạn đồng ý tuân thủ và chịu sự ràng buộc bởi các Điều khoản sử dụng này. Nếu bạn không đồng ý với bất kỳ điều khoản nào, vui lòng ngưng sử dụng dịch vụ của chúng tôi."
        ));
        sections.add(new LegalSection(
                "2. Tài khoản người dùng",
                "Bạn có trách nhiệm bảo mật thông tin đăng nhập tài khoản cá nhân và số điện thoại đã đăng ký. Mọi hoạt động được thực hiện thông qua tài khoản của bạn sẽ được coi là do chính bạn thực hiện. Hãy thông báo ngay cho Mộc Miên nếu phát hiện bất kỳ dấu hiệu truy cập trái phép nào."
        ));
        sections.add(new LegalSection(
                "3. Đặt hàng và thanh toán",
                "Khi xác nhận đặt món qua ứng dụng, đơn hàng của bạn sẽ được chuyển tới cửa hàng để chuẩn bị. Mộc Miên hỗ trợ các hình thức thanh toán trực tiếp khi nhận hàng (COD), ví điện tử MoMo/ZaloPay hoặc thẻ ngân hàng. Giá món ăn và phí giao hàng được niêm yết công khai trước khi bạn xác nhận đơn hàng."
        ));
        sections.add(new LegalSection(
                "4. Voucher và điểm tích lũy",
                "Điểm thưởng tích lũy và mã ưu đãi (Voucher) có giá trị sử dụng theo đúng thể lệ chương trình được quy định tại từng thời điểm. Điểm tích lũy và Voucher không có giá trị quy đổi thành tiền mặt và không được chuyển nhượng cho tài khoản khác."
        ));
        sections.add(new LegalSection(
                "5. Hủy đơn và hoàn tiền",
                "Bạn có thể chủ động hủy đơn hàng trực tiếp trên ứng dụng khi đơn hàng đang ở trạng thái 'Đang chuẩn bị'. Đối với các đơn hàng thanh toán trực tuyến bị hủy thành công, số tiền đã thanh toán sẽ được hệ thống tự động hoàn trả về phương thức thanh toán ban đầu của bạn trong vòng 24 giờ."
        ));
        sections.add(new LegalSection(
                "6. Liên hệ",
                "Nếu bạn có bất kỳ câu hỏi hoặc thắc mắc nào liên quan đến Điều khoản sử dụng này, vui lòng liên hệ với bộ phận CSKH của Mộc Miên qua Hotline " + SupportInfo.HOTLINE + " (" + SupportInfo.HOURS + ") hoặc Email " + SupportInfo.EMAIL + " để được hỗ trợ kịp thời."
        ));
        return new LegalDocument(LegalType.TERMS, LegalType.TERMS.getTitle(), "01/11/2026", "1.0", sections);
    }

    private LegalDocument getPrivacyPolicy() {
        List<LegalSection> sections = new ArrayList<>();
        sections.add(new LegalSection(
                "1. Thông tin chúng tôi thu thập",
                "Mộc Miên thu thập các thông tin cần thiết nhằm cung cấp dịch vụ đặt món và giao hàng tốt nhất cho bạn, bao gồm: Họ tên, số điện thoại, địa chỉ email, địa chỉ giao hàng và lịch sử đơn hàng. Chúng tôi cam kết không thu thập các thông tin nhạy cảm khi chưa có sự đồng ý của bạn."
        ));
        sections.add(new LegalSection(
                "2. Mục đích sử dụng thông tin",
                "Thông tin của bạn được sử dụng để xử lý đơn hàng, giao hàng tới tận nơi, tích lũy điểm thưởng, áp dụng mã ưu đãi và gửi thông báo cập nhật trạng thái đơn hàng. Ngoài ra, chúng tôi có thể sử dụng thông tin để cải thiện chất lượng dịch vụ và hỗ trợ giải quyết khiếu nại."
        ));
        sections.add(new LegalSection(
                "3. Chia sẻ thông tin",
                "Mộc Miên cam kết không bán, cho thuê hay chia sẻ thông tin cá nhân của bạn cho bên thứ ba vì mục đích thương mại. Thông tin giao hàng chỉ được cung cấp cho đối tác vận chuyển nhằm phục vụ việc giao món ăn tới bạn chính xác và nhanh chóng."
        ));
        sections.add(new LegalSection(
                "4. Lưu trữ và bảo mật",
                "Dữ liệu cá nhân của bạn được lưu trữ an toàn trên máy chủ bảo mật và được bảo vệ bởi các giải pháp mã hóa tiên tiến. Chúng tôi thực hiện các biện pháp kỹ thuật và tổ chức phù hợp để phòng ngừa việc truy cập, sử dụng hoặc tiết lộ trái phép thông tin cá nhân."
        ));
        sections.add(new LegalSection(
                "5. Quyền của bạn",
                "Bạn có quyền kiểm tra, cập nhật hoặc điều chỉnh thông tin cá nhân của mình bất kỳ lúc nào trong mục 'Cá nhân' trên ứng dụng. Bạn cũng có quyền yêu cầu tạm khóa hoặc xóa dữ liệu tài khoản bằng cách liên hệ với bộ phận Hỗ trợ khách hàng của Mộc Miên."
        ));
        sections.add(new LegalSection(
                "6. Liên hệ",
                "Mọi thắc mắc hoặc yêu cầu liên quan đến Chính sách bảo mật thông tin cá nhân, xin vui lòng gửi email về địa chỉ " + SupportInfo.EMAIL + " hoặc gọi tới Hotline " + SupportInfo.HOTLINE + " (" + SupportInfo.HOURS + ") để được giải đáp."
        ));
        return new LegalDocument(LegalType.PRIVACY, LegalType.PRIVACY.getTitle(), "01/11/2026", "1.0", sections);
    }
}
