package com.coffeeshop.client.data.support;

import java.util.ArrayList;
import java.util.List;

public class FaqItem {
    private final String question;
    private final String answer;

    public FaqItem(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public static List<FaqItem> getDefaultFaqs() {
        List<FaqItem> faqs = new ArrayList<>();
        faqs.add(new FaqItem(
                "Hủy hoặc đổi món sau khi đã đặt hàng?",
                "Bạn có thể hủy đơn khi đơn ở trạng thái 'Đang chuẩn bị' trực tiếp tại màn Chi tiết đơn hàng, hoặc gọi ngay cho Hotline 1900 6868 để được hỗ trợ hỗ trợ điều chỉnh món kịp thời."
        ));
        faqs.add(new FaqItem(
                "Phí giao hàng được tính như thế nào?",
                "Mộc Miên áp dụng phí giao hàng cố định 15.000đ cho bán kính 3km. Đơn hàng từ 50.000đ có thể áp dụng mã FREESHIP trong danh sách Voucher để nhận ưu đãi miễn phí giao hàng."
        ));
        faqs.add(new FaqItem(
                "Cách tích điểm và đổi quà tặng ưu đãi?",
                "Với mỗi 10.000đ hoàn tất cho đơn hàng, bạn sẽ nhận được 1 điểm tích lũy tự động. Số điểm này dùng để đổi lấy các Voucher giảm giá ở mục 'Đổi quà' trong trang Cá nhân."
        ));
        faqs.add(new FaqItem(
                "Thanh toán trực tuyến không thành công?",
                "Nếu giao dịch qua Ví điện tử/Thẻ ngân hàng gặp sự cố, hệ thống sẽ hoàn tiền tự động trong 24h. Bạn có thể chọn phương thức Thanh toán khi nhận hàng (COD) để tiếp tục mua sắm."
        ));
        return faqs;
    }
}
