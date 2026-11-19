package com.coffeeshop.client.catalog.data;

import com.coffeeshop.client.catalog.model.Product;
import com.coffeeshop.client.catalog.model.ProductReview;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ProductReviewRepository {
    private ProductReviewRepository() { }

    private static class Seed {
        final int count;
        final List<String> positive;
        final List<String> mixed;

        Seed(int count, List<String> positive, List<String> mixed) {
            this.count = count;
            this.positive = positive;
            this.mixed = mixed;
        }
    }

    private static final String[] LAST_NAMES = {
            "Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Vũ", "Đặng", "Bùi", "Đỗ", "Ngô"
    };

    private static final String[] GIVEN_NAMES = {
            "Minh Anh", "Quốc Huy", "Thu Hà", "Văn Đạt", "Mỹ Linh", "Hoài Nam", "Ngọc Lan",
            "Đức Anh", "Phương Linh", "Thanh Tùng", "Bảo Ngọc", "Bảo Châu", "Tuấn Kiệt",
            "Khánh Vy", "Gia Bảo", "Mai Trang", "Lan Hương", "Việt Hoàng", "Thảo Nguyên",
            "Hải Đăng", "Yến Nhi", "Thanh Mai", "Minh Tâm", "Đình Phúc", "Hà Phương",
            "Trung Kiên", "Cát Tường", "Hồng Nhung", "Trọng Nghĩa", "Bích Ngọc", "Diệu Linh",
            "Cẩm Tú", "Nhật Minh", "Thùy Dung", "Gia Hân", "Đăng Khoa", "Phương Thảo",
            "Khải Hoàn", "Thu Trang", "Tú Anh", "Hữu Thắng", "Bình An", "Ngân Hà",
            "Quang Vinh", "Thu Thủy", "Hà My", "Xuân Bách", "Ái Vy", "Thiên Kim", "Phúc Lâm"
    };

    private static final String[] SUFFIXES = {
            "Sẽ quay lại ủng hộ quán.",
            "Nhân viên phục vụ nhiệt tình.",
            "Giao hàng nhanh, đóng gói cẩn thận.",
            "Rất đáng thử nhé mọi người.",
            "Giá hợp lý so với chất lượng.",
            "Quán sạch sẽ, không gian dễ chịu.",
            "Mình đã giới thiệu cho bạn bè.",
            "Sẽ gọi lại lần sau."
    };

    private static final Map<Integer, Seed> SEEDS = createSeeds();

    private static Map<Integer, Seed> createSeeds() {
        Map<Integer, Seed> map = new HashMap<>();
        map.put(1, new Seed(35,
                Arrays.asList(
                        "Cà phê đậm vị, sữa vừa phải, uống sáng tỉnh cả người.",
                        "Đúng vị nâu đá truyền thống, giá lại rẻ.",
                        "Thơm, đậm, không bị chua. Rất hợp đi làm sáng.",
                        "Ngày nào cũng ghé mua một ly, chưa bao giờ thất vọng.",
                        "Ly nâu đá làm mình nhớ cà phê Hà Nội xưa.",
                        "Cà phê rang đậm, hậu vị ngọt nhẹ rất dễ chịu.",
                        "Uống lạnh vào buổi trưa nắng thì quá hợp."
                ),
                Arrays.asList(
                        "Ngon nhưng mình thích ít đá hơn một chút.",
                        "Hơi ngọt với khẩu vị của mình, nhìn chung vẫn ổn.",
                        "Cà phê ngon, chỉ tiếc ly hơi nhiều đá."
                )
        ));
        map.put(2, new Seed(28,
                Arrays.asList(
                        "Ngọt béo dễ uống, hợp người không quen cà phê đắng.",
                        "Bạc xỉu thơm sữa, uống mát lạnh rất đã.",
                        "Cà phê vừa đủ, sữa béo mịn, rất dễ nghiện.",
                        "Món yêu thích của mình ở quán này.",
                        "Màu đẹp, vị cân bằng giữa cà phê và sữa.",
                        "Uống xong thấy nhẹ nhàng, không bị hồi hộp.",
                        "Ăn kèm croissant thì hết sảy."
                ),
                Arrays.asList(
                        "Hơi ngọt với mình, lần sau sẽ gọi ít đường.",
                        "Ngon nhưng vị cà phê hơi nhạt.",
                        "Ổn, nhưng mình thích sữa ít hơn một chút."
                )
        ));
        map.put(3, new Seed(22,
                Arrays.asList(
                        "Phin chuẩn vị, thơm đậm, nhỏ giọt chậm đúng kiểu.",
                        "Cà phê đậm đà, hậu vị ngọt nhẹ. Rất đáng tiền.",
                        "Ngồi chờ phin nhỏ giọt cũng là một thú vui.",
                        "Hương cà phê thơm nức, uống một ngụm là tỉnh.",
                        "Đúng vị cà phê phin Việt Nam, không pha tạp.",
                        "Ngồi làm việc cả buổi với một phin cà phê là quá hợp.",
                        "Đắng đậm nhưng hậu ngọt, rất cuốn."
                ),
                Arrays.asList(
                        "Hơi đắng với người mới uống nhưng mình thích.",
                        "Cà phê ngon, nhưng phin ra hơi chậm.",
                        "Đậm quá so với mình, lần sau thêm sữa."
                )
        ));
        map.put(4, new Seed(48,
                Arrays.asList(
                        "Chua ngọt hài hòa, nhiều topping xoài, uống mùa hè rất hợp.",
                        "Thơm mùi chanh leo, tươi mát, sẽ quay lại.",
                        "Xoài ngọt, chanh leo thơm, uống rất sảng khoái.",
                        "Hạt chanh leo giòn giòn, xoài miếng to, quá đã.",
                        "Màu vàng nhìn bắt mắt, uống mát lạnh cả người.",
                        "Vị trà nền nhẹ nên không bị át mùi trái cây.",
                        "Một trong những món trà trái cây ngon nhất mình từng uống."
                ),
                Arrays.asList(
                        "Ngon nhưng hơi chua, nên gọi ít chanh leo.",
                        "Xoài nhiều nhưng mình thấy hơi ngọt.",
                        "Uống ổn, đá tan nhanh nên loãng hơi sớm."
                )
        ));
        map.put(5, new Seed(42,
                Arrays.asList(
                        "Hương sen thanh nhẹ, hạt sen mềm bùi, rất tinh tế.",
                        "Món này uống là nghiện, vị trà thơm dịu.",
                        "Hạt sen bùi, thạch giòn, uống no mà không ngấy.",
                        "Trà thơm hương sen tự nhiên, không bị hắc.",
                        "Uống chiều chiều ngồi nói chuyện thì thật thư thái.",
                        "Vị thanh, ngọt dịu, hợp cả người lớn tuổi.",
                        "Topping đầy đặn, đáng đồng tiền."
                ),
                Arrays.asList(
                        "Ngon, giá hơi cao một chút nhưng xứng đáng.",
                        "Vị trà nhẹ, mình thích đậm hơn.",
                        "Ổn, nhưng hương sen hơi nhạt hôm mình uống."
                )
        ));
        map.put(6, new Seed(16,
                Arrays.asList(
                        "Chua chua mặn mặn lạ miệng, uống giải nhiệt tuyệt vời.",
                        "Cóc tươi giòn, không bị ngọt gắt.",
                        "Xí muội thơm, vị lạ mà cuốn.",
                        "Uống buổi trưa nóng thì không gì bằng.",
                        "Chua nhẹ, hậu mặn mặn rất đặc biệt.",
                        "Món lạ miệng, mình gọi cả tuần liền.",
                        "Giải ngấy cực tốt sau bữa ăn nhiều dầu mỡ."
                ),
                Arrays.asList(
                        "Vị lạ nhưng cuốn, ai thích chua nên thử.",
                        "Hơi chua với mình, nên bớt cóc.",
                        "Lần đầu uống chưa quen vị xí muội."
                )
        ));
        map.put(7, new Seed(32,
                Arrays.asList(
                        "Best seller có lý do, đào giòn, sả thơm, cam tươi.",
                        "Uống mãi không chán, mùi sả rất dễ chịu.",
                        "Nhiều đào, vị cân bằng, đáng gọi mỗi lần ghé.",
                        "Cả nhóm 4 người gọi 4 ly trà đào cam sả luôn.",
                        "Thơm sả, đào ngọt tự nhiên, rất đáng thử.",
                        "Trà đậm vừa, cam vắt tươi, rất sảng khoái.",
                        "Món quốc dân, quán nào mình cũng gọi mà đây là ngon nhất."
                ),
                Arrays.asList(
                        "Ngon, chỉ tiếc đá hơi nhiều làm loãng nhanh.",
                        "Đào hơi ít hôm mình gọi, nhưng vị vẫn ổn.",
                        "Ngọt hơn mình nghĩ, lần sau gọi ít đường."
                )
        ));
        map.put(8, new Seed(14,
                Arrays.asList(
                        "Thơm mùi dưa lưới, ngọt thanh, màu xanh nhìn rất mát.",
                        "Dưa lưới thơm, uống nhẹ nhàng, hợp buổi chiều.",
                        "Vị dưa lưới tự nhiên, không bị gắt hương liệu.",
                        "Uống mát, dịu, hợp mùa nóng.",
                        "Topping dưa lưới miếng mềm ngọt, thích lắm.",
                        "Màu đẹp để chụp ảnh sống ảo.",
                        "Nhẹ nhàng, thanh mát, dễ uống."
                ),
                Arrays.asList(
                        "Ngon nhưng mình muốn có thêm topping thạch.",
                        "Hơi ngọt, nhưng thơm dưa lưới.",
                        "Ổn, chưa quá ấn tượng."
                )
        ));
        map.put(9, new Seed(19,
                Arrays.asList(
                        "Thạch đào dai giòn, trà thơm, ngọt vừa.",
                        "Mình hay gọi món này khi đi làm, rất dễ uống.",
                        "Đào thơm, thạch nhiều, uống đã miệng.",
                        "Trà đen đậm vừa, hòa quyện với vị đào.",
                        "Giá hợp lý, topping hào phóng.",
                        "Ngon, đúng kiểu trà đào truyền thống.",
                        "Uống lạnh thì tuyệt, thạch giòn sần sật."
                ),
                Arrays.asList(
                        "Uống ổn, topping nhiều. Trà hơi nhạt so với mình.",
                        "Hơi ngọt, nhưng ngon.",
                        "Thạch hơi nhiều so với trà."
                )
        ));
        map.put(10, new Seed(20,
                Arrays.asList(
                        "Vải ngọt thơm, trà hoa hồng nhẹ nhàng, rất đẹp mắt.",
                        "Mát lạnh, thơm, bạn bè mình ai cũng thích.",
                        "Vải nhiều, ngọt thanh, uống không bị ngán.",
                        "Hương trà thơm dịu, vải tươi bóc sẵn.",
                        "Món này mình gọi nhiều nhất ở quán.",
                        "Thơm mùi vải, uống rất nhẹ bụng.",
                        "Ly nhìn xinh xắn, vị cũng ngon."
                ),
                Arrays.asList(
                        "Ngon, vải nhiều. Hơi ngọt một chút.",
                        "Trà hơi nhạt, vải thì ngon.",
                        "Ổn, giá hơi cao với phần ly này."
                )
        ));
        map.put(11, new Seed(26,
                Arrays.asList(
                        "Đậm vị cacao, kem tươi béo, bé nhà mình mê lắm.",
                        "Sô cô la đậm, đá xay mịn, không bị tách nước.",
                        "Uống xong thấy vui hẳn, đúng kiểu món ngọt giải stress.",
                        "Kem tươi béo, cacao thơm, hợp trẻ em.",
                        "Đá xay mịn, uống tới cuối vẫn ngon.",
                        "Ngọt vừa phải, cacao đậm và thơm.",
                        "Gọi một ly, bạn bè xin uống thử hết nửa."
                ),
                Arrays.asList(
                        "Ngon nhưng khá ngọt, nên uống khi còn lạnh.",
                        "Ngon, mình sẽ bớt kem lần sau.",
                        "Đá xay hơi nhanh tan khi trời nóng."
                )
        ));
        map.put(12, new Seed(18,
                Arrays.asList(
                        "Matcha thơm, hơi đắng nhẹ, sữa béo cân bằng rất tốt.",
                        "Màu đẹp, uống mát lạnh, phù hợp ngồi làm việc.",
                        "Matcha xanh mướt, không bị chát.",
                        "Uống êm, vị trà xanh rõ, sữa béo vừa.",
                        "Mê matcha thì nhất định phải thử.",
                        "Ly latte phân lớp đẹp, vị cũng ngon.",
                        "Vị đắng dịu, hậu ngọt, rất dễ chịu."
                ),
                Arrays.asList(
                        "Ngon, chỉ mong matcha đậm hơn một chút.",
                        "Hơi ngọt, mình thích matcha đắng hơn.",
                        "Ổn, giá hơi cao so với ly."
                )
        ));
        map.put(13, new Seed(15,
                Arrays.asList(
                        "Mềm mịn, béo vừa, đế bánh giòn thơm bơ.",
                        "Ăn kèm cà phê đen rất hợp, không bị ngấy.",
                        "Phô mai thơm, mặt bánh nướng vàng đều.",
                        "Miếng bánh vừa ăn, vị chua nhẹ rất cuốn.",
                        "Một trong những cheese cake ngon nhất mình từng ăn.",
                        "Đế bánh giòn, nhân mịn, ngọt vừa phải.",
                        "Ăn trưa nhẹ với một miếng bánh và ly trà là đủ."
                ),
                Arrays.asList(
                        "Bánh ngon, phần hơi nhỏ so với giá.",
                        "Hơi béo, nên ăn chung với đồ uống không ngọt.",
                        "Ổn, mong bánh được để mềm hơn một chút."
                )
        ));
        map.put(14, new Seed(12,
                Arrays.asList(
                        "Vỏ giòn rụm, nhiều lớp, thơm mùi bơ. Ăn sáng quá hợp.",
                        "Nướng nóng giòn tan, ăn cùng bạc xỉu thì tuyệt.",
                        "Ruột mềm, vỏ giòn, thơm bơ nức mũi.",
                        "Bánh to, ăn no, giá lại hợp lý.",
                        "Ăn kèm cà phê đen thì hết ý.",
                        "Nhiều lớp, tơi xốp, đúng chuẩn.",
                        "Mua mang đi ăn vặt cũng tiện."
                ),
                Arrays.asList(
                        "Ngon nhưng nên ăn ngay, để lâu bị mềm.",
                        "Hơi ngấy bơ với mình.",
                        "Ổn, nhưng hôm mình ăn bánh hơi nguội."
                )
        ));
        map.put(15, new Seed(17,
                Arrays.asList(
                        "Vị cà phê và mascarpone rất hài hòa, phủ cacao thơm.",
                        "Mềm xốp, ngọt vừa, mình gọi lần nào cũng hết sạch.",
                        "Lớp kem mịn, bánh ngấm cà phê rất vừa.",
                        "Tiramisu chuẩn vị Ý, đáng thử.",
                        "Phần bánh vừa vặn, ăn kèm trà là hợp.",
                        "Cacao phủ dày, thơm, không bị đắng.",
                        "Ăn tráng miệng sau bữa trưa thì quá hợp."
                ),
                Arrays.asList(
                        "Ngon, nhưng hơi nhiều rượu rum với người không quen.",
                        "Ngọt hơn mình nghĩ.",
                        "Ổn, mong phần bánh lớn hơn."
                )
        ));
        return map;
    }

    private static final Map<Integer, List<ProductReview>> ALL_REVIEWS = buildAllReviews();

    private static Map<Integer, List<ProductReview>> buildAllReviews() {
        Map<Integer, List<ProductReview>> map = new HashMap<>();
        for (Map.Entry<Integer, Seed> entry : SEEDS.entrySet()) {
            int productId = entry.getKey();
            Seed seed = entry.getValue();
            List<ProductReview> list = new ArrayList<>();
            for (int i = 0; i < seed.count; i++) {
                boolean isMixed = (i % 8 == 5);
                float rating = isMixed ? 4f : 5f;
                String comment = isMixed ?
                        seed.mixed.get((i / 8) % seed.mixed.size()) :
                        seed.positive.get((i * 3 + productId) % seed.positive.size());
                if (i % 3 != 1) {
                    comment += " " + SUFFIXES[(i * 5 + productId) % SUFFIXES.length];
                }
                String userName = LAST_NAMES[(i * 3 + productId) % LAST_NAMES.length] + " " +
                        GIVEN_NAMES[(i * 7 + productId * 5) % GIVEN_NAMES.length];
                String date = dateAgo(i * 2 + productId % 3);
                list.add(new ProductReview(userName, rating, comment, date));
            }
            map.put(productId, Collections.unmodifiableList(list));
        }
        return map;
    }

    private static String dateAgo(int daysAgo) {
        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.NOVEMBER, 8);
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo);
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.getTime());
    }

    public static List<ProductReview> getReviews(Product product) {
        if (product == null) return Collections.emptyList();
        List<ProductReview> list = ALL_REVIEWS.get(product.id);
        return list != null ? list : Collections.emptyList();
    }

    public static int getReviewCount(int productId) {
        List<ProductReview> list = ALL_REVIEWS.get(productId);
        return list != null ? list.size() : 0;
    }
}
