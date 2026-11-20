import 'package:flutter_test/flutter_test.dart';
import 'package:moc_mien_ordering/data/menu.dart';
import 'package:moc_mien_ordering/state/order_provider.dart';

void main() {
  group('OrderProvider cart', () {
    final coffee = products.firstWhere((product) => product.id == 'ca-phe-sua-da');

    test('uses size S as base, adds size and topping fees', () {
      const base = DrinkOptions();
      expect(base.size, 'S');
      expect(base.sugar, '100%');
      expect(base.ice, 'Bình thường');
      expect(coffee.price + base.extra, 35000);
      const largeWithToppings = DrinkOptions(size: 'L', toppings: [0, 1]);
      expect(coffee.price + largeWithToppings.extra, 63000);
    });

    test('merges identical choices and keeps different choices separate', () {
      final order = OrderProvider();
      order.add(coffee, const DrinkOptions());
      order.add(coffee, const DrinkOptions());
      order.add(coffee, const DrinkOptions(size: 'M'));
      expect(order.lines, hasLength(2));
      expect(order.itemCount, 3);
      expect(order.subtotal, 111000);
    });

    test('uses the note as part of a cart line identity', () {
      final order = OrderProvider();
      order.add(coffee, const DrinkOptions(note: 'ít ngọt hơn'), quantity: 2);
      order.add(coffee, const DrinkOptions(note: 'ít ngọt hơn'), quantity: 3);
      order.add(coffee, const DrinkOptions(note: 'không đá'));
      expect(order.lines, hasLength(2));
      expect(order.itemCount, 6);
      expect(order.lines.first.quantity, 5);
    });

    test('applies voucher thresholds and pickup delivery fee', () {
      final order = OrderProvider();
      order.add(coffee, const DrinkOptions());
      expect(order.applyVoucher('GIAM10'), isFalse);
      order.add(products.firstWhere((product) => product.id == 'bac-xiu'), const DrinkOptions());
      expect(order.applyVoucher('GIAM10'), isTrue);
      expect(order.deliveryFee, 15000);
      expect(order.discount, 10000);
      order.setPickup(true);
      expect(order.total, order.subtotal - 10000);
      order.applyVoucher('FREESHIP');
      expect(order.deliveryFee, 0);
    });

    test('revalidates GIAM10 when cart total drops below its minimum', () {
      final order = OrderProvider();
      order.add(coffee, const DrinkOptions(), quantity: 2);
      expect(order.applyVoucher('GIAM10'), isTrue);
      order.changeQuantity(order.lines.first, -1);
      expect(order.voucher, isNull);
      expect(order.voucherError, 'Mã GIAM10 cần đơn từ 50,000đ');
    });

    test('rechecks a submitted GIAM10 code after cart reaches its minimum', () {
      final order = OrderProvider();
      order.add(coffee, const DrinkOptions());
      expect(order.applyVoucher('GIAM10'), isFalse);
      order.add(products.firstWhere((product) => product.id == 'bac-xiu'), const DrinkOptions());
      expect(order.voucher, 'GIAM10');
      expect(order.discount, 10000);
      expect(order.voucherError, isNull);
    });
  });
}
