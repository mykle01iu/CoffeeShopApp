import 'dart:convert';

import 'package:flutter/foundation.dart';

import '../data/menu.dart';

const sizeNames = ['S', 'M', 'L'];
const sizeFees = [0, 6000, 10000];
const sugarLevels = ['0%', '50%', '70%', '100%'];
const iceLevels = ['Không đá', 'Ít đá', 'Bình thường'];
const toppingNames = ['Thêm shot espresso', 'Kem muối', 'Trân châu'];
const toppingFees = [10000, 8000, 8000];
const paymentMethods = ['Tiền mặt khi nhận hàng', 'Chuyển khoản ngân hàng', 'Ví điện tử'];

@immutable
class DrinkOptions {
  const DrinkOptions({this.size = 'S', this.sugar = '100%', this.ice = 'Bình thường', this.toppings = const [], this.note = ''});
  final String size;
  final String sugar;
  final String ice;
  final List<int> toppings;
  final String note;

  int get extra => sizeFees[sizeNames.indexOf(size)] + toppings.fold(0, (sum, i) => sum + toppingFees[i]);
  String get key => jsonEncode([size, sugar, ice, toppings.toList()..sort(), note]);
  DrinkOptions copyWith({String? size, String? sugar, String? ice, List<int>? toppings, String? note}) =>
      DrinkOptions(size: size ?? this.size, sugar: sugar ?? this.sugar, ice: ice ?? this.ice, toppings: toppings ?? this.toppings, note: note ?? this.note);
}

class CartLine {
  CartLine({required this.product, required this.options, this.quantity = 1});
  final Product product;
  DrinkOptions options;
  int quantity;
  int get unitPrice => product.price + (product.isDrink ? options.extra : 0);
  String get identity => '${product.id}|${options.key}';
}

class OrderProvider extends ChangeNotifier {
  final List<CartLine> _lines = [];
  final List<String> _orderHistory = [];
  final Map<String, String> _orderNotes = {};
  bool pickup = false;
  String paymentMethod = paymentMethods.first;
  String? voucher;
  String? _voucherCandidate;
  String? voucherError;

  List<CartLine> get lines => List.unmodifiable(_lines);
  int get itemCount => _lines.fold(0, (sum, line) => sum + line.quantity);
  int get processingOrderCount => _orderHistory.length;
  int get subtotal => _lines.fold(0, (sum, line) => sum + line.unitPrice * line.quantity);
  int get deliveryFee => pickup || voucher == 'FREESHIP' ? 0 : 15000;
  int get discount => voucher == 'GIAM10' && subtotal >= 50000 ? 10000 : 0;
  int get total {
    final amount = subtotal + deliveryFee - discount;
    return amount < 0 ? 0 : amount;
  }
  List<String> get orderHistory => List.unmodifiable(_orderHistory);
  String orderNote(String id) => _orderNotes[id] ?? '';

  void add(Product product, DrinkOptions options, {int quantity = 1}) {
    final effectiveOptions = product.isDrink ? options : options.copyWith(size: 'S', sugar: '100%', ice: 'Bình thường', toppings: const []);
    final index = _lines.indexWhere((line) => line.identity == '${product.id}|${effectiveOptions.key}');
    if (index >= 0) {
      _lines[index].quantity += quantity;
    } else {
      _lines.add(CartLine(product: product, options: effectiveOptions, quantity: quantity));
    }
    _revalidateVoucher();
    notifyListeners();
  }

  void changeQuantity(CartLine line, int delta) {
    final current = _lines.indexOf(line);
    if (current < 0) return;
    _lines[current].quantity += delta;
    if (_lines[current].quantity <= 0) _lines.removeAt(current);
    _revalidateVoucher();
    notifyListeners();
  }

  void updateOptions(CartLine line, DrinkOptions options, {int? quantity}) {
    final current = _lines.indexOf(line);
    if (current < 0) return;
    final matching = _lines.indexWhere((other) => other != line && other.product.id == line.product.id && other.options.key == options.key);
    if (matching >= 0) {
      _lines[matching].quantity += quantity ?? line.quantity;
      _lines.removeAt(current);
    } else {
      line.options = options;
      if (quantity != null) line.quantity = quantity;
    }
    _revalidateVoucher();
    notifyListeners();
  }

  void remove(CartLine line) { _lines.remove(line); _revalidateVoucher(); notifyListeners(); }
  void setPickup(bool value) { pickup = value; _revalidateVoucher(); notifyListeners(); }
  void setPayment(String value) { paymentMethod = value; notifyListeners(); }
  bool applyVoucher(String code) {
    final value = code.trim().toUpperCase();
    if (value.isEmpty) {
      _voucherCandidate = null;
      voucher = null;
      voucherError = 'Nhập mã trước khi áp dụng.';
      notifyListeners();
      return false;
    }
    _voucherCandidate = value;
    _revalidateVoucher();
    notifyListeners();
    return voucher == value;
  }

  void clearVoucher() { voucher = null; _voucherCandidate = null; voucherError = null; notifyListeners(); }

  void _revalidateVoucher() {
    if (_voucherCandidate == 'GIAM10') {
      if (subtotal < 50000) {
        voucher = null;
        voucherError = 'Mã GIAM10 cần đơn từ 50,000đ';
      } else {
        voucher = 'GIAM10';
        voucherError = null;
      }
    } else if (_voucherCandidate == 'FREESHIP') {
      voucher = 'FREESHIP';
      voucherError = null;
    } else if (_voucherCandidate != null) {
      voucher = null;
      voucherError = 'Mã không hợp lệ hoặc đã hết hạn.';
    }
  }

  String placeOrder({String note = ''}) {
    final id = 'MM${DateTime.now().millisecondsSinceEpoch.toString().substring(7)}';
    _orderHistory.insert(0, id);
    _orderNotes[id] = note.trim();
    _lines.clear(); voucher = null; _voucherCandidate = null; voucherError = null;
    notifyListeners();
    return id;
  }
}
