import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'data/menu.dart';
import 'state/order_provider.dart';

const coral = Color(0xFFEE6B6B);
const priceRed = Color(0xFFC93F3A);
const beige = Color(0xFFF1E6D8);
const ink = Color(0xFF1F1A17);
const muted = Color(0xFF6B625C);
const coffee = Color(0xFF2B2523);

void main() => runApp(ChangeNotifierProvider(create: (_) => OrderProvider(), child: const MocMienApp()));

class MocMienApp extends StatelessWidget {
  const MocMienApp({super.key});
  @override
  Widget build(BuildContext context) => MaterialApp(
        debugShowCheckedModeBanner: false,
        title: 'Mộc Miên Coffee',
        theme: ThemeData(
          useMaterial3: true,
          scaffoldBackgroundColor: Colors.white,
          colorScheme: ColorScheme.fromSeed(seedColor: coral, primary: coral, surface: Colors.white),
          textTheme: GoogleFonts.beVietnamProTextTheme().apply(bodyColor: ink, displayColor: ink),
          appBarTheme: const AppBarTheme(backgroundColor: Colors.white, foregroundColor: ink, elevation: 0, centerTitle: false),
        ),
        home: const HomeScreen(),
      );
}

class OrderShell extends StatelessWidget {
  const OrderShell({super.key, required this.child, this.title, this.showBack = false, this.historyAction = false, this.bottom});
  final Widget child;
  final String? title;
  final bool showBack;
  final bool historyAction;
  final Widget? bottom;

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(
          automaticallyImplyLeading: false,
          toolbarHeight: 70,
          titleSpacing: 20,
          title: Row(children: [
            if (showBack) ...[
              IconButton(onPressed: () => Navigator.maybePop(context), padding: EdgeInsets.zero, constraints: const BoxConstraints.tightFor(width: 38, height: 42), icon: const Icon(Icons.arrow_back_ios_new_rounded, size: 19)),
              const SizedBox(width: 8),
            ],
            Expanded(child: Text(title ?? 'Đặt hàng', style: const TextStyle(fontSize: 26, fontWeight: FontWeight.w800))),
          ]),
          actions: [
            if (historyAction)
              Padding(padding: const EdgeInsets.only(right: 16), child: Consumer<OrderProvider>(builder: (context, order, _) => Stack(clipBehavior: Clip.none, children: [
                Material(color: beige, shape: const CircleBorder(), child: InkWell(customBorder: const CircleBorder(), onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const MyOrdersScreen())), child: const SizedBox(width: 46, height: 46, child: Icon(Icons.receipt_long_rounded, color: ink, size: 22)))),
                if (order.processingOrderCount > 0) Positioned(right: -3, top: -4, child: Container(minWidth: 18, height: 18, padding: const EdgeInsets.symmetric(horizontal: 4), alignment: Alignment.center, decoration: BoxDecoration(color: const Color(0xFFD7262C), border: Border.all(color: Colors.white, width: 1.5), borderRadius: BorderRadius.circular(12)), child: Text('${order.processingOrderCount}', style: const TextStyle(color: Colors.white, fontSize: 9, fontWeight: FontWeight.w800)))),
              ]))),
          ],
        ),
        body: child,
        bottomNavigationBar: bottom ?? const OrderBottomBar(),
      );
}

class GradientBody extends StatelessWidget {
  const GradientBody({super.key, required this.child});
  final Widget child;
  @override
  Widget build(BuildContext context) => DecoratedBox(
        decoration: const BoxDecoration(gradient: LinearGradient(begin: Alignment.topCenter, end: Alignment.bottomCenter, colors: [Color(0xFFE9DCCB), Color(0xFFF3EBDF), Color(0xFFFBF8F3)])),
        child: child,
      );
}

class OrderBottomBar extends StatelessWidget {
  const OrderBottomBar({super.key, this.activeHome = false});
  final bool activeHome;

  void _comingSoon(BuildContext context) => ScaffoldMessenger.of(context)
    ..hideCurrentSnackBar()
    ..showSnackBar(const SnackBar(content: Text('Tính năng đang được hoàn thiện')));

  @override
  Widget build(BuildContext context) => SafeArea(top: false, child: SizedBox(
    height: 91,
    child: Stack(clipBehavior: Clip.none, alignment: Alignment.topCenter, children: [
      Positioned(left: 0, right: 0, bottom: 0, child: Container(
        height: 72,
        decoration: BoxDecoration(color: Colors.white, borderRadius: const BorderRadius.vertical(top: Radius.circular(22)), boxShadow: [BoxShadow(color: Colors.black.withOpacity(.09), blurRadius: 14, offset: const Offset(0, -3))]),
        child: Row(children: [
          _navItem(context, Icons.home_outlined, Icons.home_rounded, 'Trang ch\u1ee7', active: activeHome),
          _navItem(context, Icons.local_cafe_outlined, Icons.local_cafe_rounded, '\u0110\u1eb7t h\u00e0ng', active: !activeHome),
          const SizedBox(width: 70),
          _navItem(context, Icons.local_offer_outlined, Icons.local_offer_rounded, '\u01afu\u0111\u00e3i'),
          _navItem(context, Icons.more_horiz_rounded, Icons.more_horiz_rounded, 'Kh\u00e1c'),
        ]),
      )),
      Positioned(top: -1, child: GestureDetector(onTap: () => _comingSoon(context), child: Container(width: 60, height: 60, decoration: BoxDecoration(color: coffee, shape: BoxShape.circle, border: Border.all(color: Colors.white, width: 5)), child: const Icon(Icons.qr_code_2_rounded, color: Color(0xFFF3A09D), size: 30)))),
    ]),
  ));

  Widget _navItem(BuildContext context, IconData outline, IconData filled, String label, {bool active = false}) =>
    Expanded(child: InkWell(
      onTap: active ? null : () {
        if (outline == Icons.home_outlined) {
          Navigator.pushReplacement(context, MaterialPageRoute(builder: (_) => const HomeScreen()));
        } else if (outline == Icons.local_cafe_outlined) {
          Navigator.pushReplacement(context, MaterialPageRoute(builder: (_) => const MenuScreen()));
        } else {
          _comingSoon(context);
        }
      },
      child: SizedBox(height: 72, child: Column(mainAxisAlignment: MainAxisAlignment.center, children: [
        Icon(active ? filled : outline, color: active ? coral : muted, size: 22),
        const SizedBox(height: 3),
        Text(label, maxLines: 1, style: TextStyle(color: active ? coral : muted, fontSize: 10, fontWeight: active ? FontWeight.w700 : FontWeight.w500)),
      ])),
    ));
}

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) => OrderShell(
    title: 'Trang ch\u1ee7',
    historyAction: true,
    bottom: const OrderBottomBar(activeHome: true),
    child: GradientBody(child: ListView(padding: const EdgeInsets.fromLTRB(16, 12, 16, 24), children: [
      const Text('Ch\u00e0o b\u1ea1n, h\u00f4m nay u\u1ed1ng g\u00ec?', style: TextStyle(fontSize: 22, fontWeight: FontWeight.w800)),
      const SizedBox(height: 6),
      const Text('C\u00e0 ph\u00ea v\u00e0 tr\u00e0 pha t\u01b0\u01a1i m\u1ed7i ng\u00e0y.', style: TextStyle(color: muted, fontSize: 13)),
      const SizedBox(height: 16),
      Container(padding: const EdgeInsets.all(18), decoration: BoxDecoration(color: coffee, borderRadius: BorderRadius.circular(18)), child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        const Text('MOC MIEN COFFEE', style: TextStyle(color: Color(0xFFF3A09D), fontSize: 11, fontWeight: FontWeight.w800, letterSpacing: 1)),
        const SizedBox(height: 8),
        const Text('Ch\u1eadm m\u1ed9t nh\u1ecbp. \u0110\u1eadm m\u1ed9t h\u01b0\u01a1ng.', style: TextStyle(color: Colors.white, fontSize: 19, fontWeight: FontWeight.w700)),
        const SizedBox(height: 14),
        FilledButton(onPressed: () => Navigator.pushReplacement(context, MaterialPageRoute(builder: (_) => const MenuScreen())), child: const Text('Kh\u00e1m ph\u00e1 th\u1ef1c \u0111\u01a1n')),
      ])),
      const Padding(padding: EdgeInsets.fromLTRB(2, 22, 2, 10), child: Text('M\u00f3n n\u1ed5i b\u1eadt', style: TextStyle(fontSize: 19, fontWeight: FontWeight.w800))),
      for (final product in products.where((item) => !item.soldOut).take(4)) ProductTile(product: product),
    ])),
  );
}

class MenuScreen extends StatefulWidget {
  const MenuScreen({super.key});
  @override
  State<MenuScreen> createState() => _MenuScreenState();
}

class _MenuScreenState extends State<MenuScreen> {
  final _scroll = ScrollController();
  final _sectionKeys = {for (final category in categories) category.id: GlobalKey()};
  String _active = 'cafe';

  @override
  void dispose() {
    _scroll.dispose();
    super.dispose();
  }

  void _jump(String id) {
    setState(() => _active = id);
    final context = _sectionKeys[id]?.currentContext;
    if (context != null) Scrollable.ensureVisible(context, duration: const Duration(milliseconds: 350), alignment: .03);
  }

  @override
  Widget build(BuildContext context) => OrderShell(
        historyAction: true,
        child: GradientBody(child: Column(children: [
          SizedBox(height: 58, child: ListView.separated(scrollDirection: Axis.horizontal, padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 7), itemCount: categories.length, separatorBuilder: (_, __) => const SizedBox(width: 9), itemBuilder: (_, i) {
            final category = categories[i]; final selected = _active == category.id;
            return InkWell(onTap: () => _jump(category.id), borderRadius: BorderRadius.circular(12), child: Container(height: 44, padding: const EdgeInsets.symmetric(horizontal: 14), alignment: Alignment.center, decoration: BoxDecoration(color: selected ? coral : Colors.white, border: Border.all(color: selected ? coral : const Color(0xFFE6D8C6)), borderRadius: BorderRadius.circular(12)), child: Text(category.name, style: TextStyle(color: selected ? Colors.white : muted, fontSize: 13, fontWeight: FontWeight.w700))));
          })),
          Consumer<OrderProvider>(builder: (context, order, _) => order.itemCount == 0 ? const SizedBox.shrink() : Padding(padding: const EdgeInsets.fromLTRB(16, 2, 16, 4), child: SizedBox(height: 56, child: Material(color: coffee, borderRadius: BorderRadius.circular(14), child: InkWell(borderRadius: BorderRadius.circular(14), onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const CartScreen())), child: Padding(padding: const EdgeInsets.symmetric(horizontal: 14), child: Row(children: [Container(width: 27, height: 27, alignment: Alignment.center, decoration: const BoxDecoration(color: coral, shape: BoxShape.circle), child: Text('${order.itemCount}', style: const TextStyle(color: Colors.white, fontSize: 11, fontWeight: FontWeight.w800))), const SizedBox(width: 10), const Expanded(child: Text('Xem giỏ hàng', style: TextStyle(color: Colors.white, fontWeight: FontWeight.w700, fontSize: 13))), Text(money(order.subtotal), style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w800, fontSize: 13))]))))))),
          Expanded(child: NotificationListener<ScrollNotification>(onNotification: (event) {
            if (event is ScrollUpdateNotification || event is ScrollEndNotification) {
              for (final category in categories.reversed) {
                final c = _sectionKeys[category.id]?.currentContext;
                if (c != null) {
                  final box = c.findRenderObject() as RenderBox?;
                  if (box != null && box.localToGlobal(Offset.zero).dy < 330) { if (_active != category.id) setState(() => _active = category.id); break; }
                }
              }
            }
            return false;
          }, child: ListView(controller: _scroll, padding: const EdgeInsets.fromLTRB(16, 8, 16, 24), children: [
            for (final category in categories) ...[
              Padding(key: _sectionKeys[category.id], padding: const EdgeInsets.fromLTRB(2, 14, 2, 9), child: Text(category.name, style: const TextStyle(fontSize: 22, fontWeight: FontWeight.w800))),
              for (final product in products.where((item) => item.categoryId == category.id)) ProductTile(product: product),
            ],
          ]))),
        ])),
      );
}

Widget _productImage(String? asset, double size, double radius) => ClipRRect(
  borderRadius: BorderRadius.circular(radius),
  child: SizedBox(
    width: size,
    height: size,
    child: asset == null
        ? Container(color: beige, child: const Icon(Icons.local_cafe_rounded, color: coffee, size: 34))
        : Image.asset(asset, fit: BoxFit.cover, errorBuilder: (_, __, ___) => Container(color: beige, child: const Icon(Icons.local_cafe_rounded, color: coffee, size: 34))),
  ),
);

class ProductTile extends StatelessWidget {
  const ProductTile({super.key, required this.product});
  final Product product;
  @override
  Widget build(BuildContext context) {
    final soldOut = product.soldOut;
    return Opacity(opacity: soldOut ? .45 : 1, child: Material(color: Colors.white, borderRadius: BorderRadius.circular(14), child: InkWell(onTap: soldOut ? null : () => showProductOptions(context, product), borderRadius: BorderRadius.circular(14), child: Container(margin: const EdgeInsets.only(bottom: 11), padding: const EdgeInsets.all(14), child: Row(children: [
      Stack(children: [_productImage(product.imageAsset, 108, 11), if (product.isNew) Positioned(left: 5, top: 5, child: Container(padding: const EdgeInsets.symmetric(horizontal: 7, vertical: 4), decoration: BoxDecoration(color: const Color(0xFFD7262C), borderRadius: BorderRadius.circular(8)), child: const Text('MỚI', style: TextStyle(color: Colors.white, fontSize: 9, fontWeight: FontWeight.w800))))]),
      const SizedBox(width: 12),
      Expanded(child: SizedBox(height: 108, child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        Text(product.name, maxLines: 1, overflow: TextOverflow.ellipsis, style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w600)),
        const SizedBox(height: 4), Expanded(child: Text(soldOut ? 'Tạm hết hàng' : product.description, maxLines: 3, overflow: TextOverflow.ellipsis, style: const TextStyle(color: muted, fontSize: 13, height: 1.35))),
        Row(children: [Expanded(child: Text(money(product.price), style: const TextStyle(color: priceRed, fontSize: 14, fontWeight: FontWeight.w800))), if (!soldOut) Material(color: beige, shape: const CircleBorder(), child: InkWell(customBorder: const CircleBorder(), onTap: () => showProductOptions(context, product), child: const SizedBox(width: 36, height: 36, child: Icon(Icons.add_rounded, color: coral, size: 23))))]),
      ]))),
    ])))));
  }
}

Future<void> showProductOptions(BuildContext context, Product product, {CartLine? editing}) async {
  final result = await showModalBottomSheet<_ProductSheetResult>(
    context: context,
    isScrollControlled: true,
    isDismissible: true,
    enableDrag: true,
    backgroundColor: Colors.transparent,
    builder: (_) => ProductSheet(product: product, editing: editing),
  );
  if (result == null || !context.mounted) return;
  final order = context.read<OrderProvider>();
  if (editing == null) {
    order.add(product, result.options, quantity: result.quantity);
    showToast(context, 'Đã thêm vào giỏ');
  } else {
    order.updateOptions(editing, result.options, quantity: result.quantity);
    showToast(context, 'Đã cập nhật giỏ hàng');
  }
}

class _ProductSheetResult {
  const _ProductSheetResult(this.options, this.quantity);
  final DrinkOptions options;
  final int quantity;
}

class ProductSheet extends StatefulWidget {
  const ProductSheet({super.key, required this.product, this.editing});
  final Product product;
  final CartLine? editing;
  @override
  State<ProductSheet> createState() => _ProductSheetState();
}

class _ProductSheetState extends State<ProductSheet> {
  late int _size;
  late int _sugar;
  late int _ice;
  late int _quantity;
  late Set<int> _toppings;
  late final TextEditingController _note;

  @override
  void initState() {
    super.initState();
    final old = widget.editing;
    _size = sizeNames.indexOf(old?.options.size ?? 'S');
    _sugar = sugarLevels.indexOf(old?.options.sugar ?? '100%');
    _ice = iceLevels.indexOf(old?.options.ice ?? 'Bình thường');
    _quantity = old?.quantity ?? 1;
    _toppings = {...?old?.options.toppings};
    _note = TextEditingController(text: old?.options.note ?? '');
  }

  @override
  void dispose() { _note.dispose(); super.dispose(); }

  DrinkOptions get _options => DrinkOptions(
    size: widget.product.isDrink ? sizeNames[_size] : 'S',
    sugar: widget.product.isDrink ? sugarLevels[_sugar] : '100%',
    ice: widget.product.isDrink ? iceLevels[_ice] : 'Bình thường',
    toppings: widget.product.isDrink ? (_toppings.toList()..sort()) : const [],
    note: _note.text.trim(),
  );
  int get _unitPrice => widget.product.price + (widget.product.isDrink ? _options.extra : 0);
  int get _total => _unitPrice * _quantity;

  @override
  Widget build(BuildContext context) {
    final maxHeight = MediaQuery.sizeOf(context).height * .9;
    return Align(alignment: Alignment.bottomCenter, child: ConstrainedBox(constraints: BoxConstraints(maxHeight: maxHeight), child: Container(
      decoration: const BoxDecoration(color: Colors.white, borderRadius: BorderRadius.vertical(top: Radius.circular(22))),
      child: SafeArea(top: false, child: Column(mainAxisSize: MainAxisSize.min, children: [
        const SizedBox(height: 10), Container(width: 38, height: 4, decoration: BoxDecoration(color: beige, borderRadius: BorderRadius.circular(4))),
        Padding(padding: const EdgeInsets.fromLTRB(18, 12, 12, 10), child: Row(children: [
          _productImage(widget.product.imageAsset, 60, 10),
          const SizedBox(width: 12), Expanded(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [Text(widget.product.name, maxLines: 2, overflow: TextOverflow.ellipsis, style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w700)), const SizedBox(height: 4), Text(money(widget.product.price), style: const TextStyle(color: priceRed, fontSize: 14, fontWeight: FontWeight.w800))])),
          IconButton(tooltip: 'Đóng', onPressed: () => Navigator.pop(context), icon: const Icon(Icons.close_rounded)),
        ])),
        const Divider(height: 1),
        Expanded(child: ListView(padding: const EdgeInsets.fromLTRB(18, 8, 18, 10), children: [
          if (widget.product.isDrink) ...[
            _sectionTitle('Size'),
            Row(children: [for (var i = 0; i < sizeNames.length; i++) Expanded(child: Padding(padding: EdgeInsets.only(right: i == sizeNames.length - 1 ? 0 : 8), child: _choiceButton('${sizeNames[i]}${i == 0 ? '' : ' +${sizeFees[i] ~/ 1000}k'}', _size == i, () => setState(() => _size = i))))]),
            _sectionTitle('Đường'),
            Row(children: [for (var i = 0; i < sugarLevels.length; i++) Expanded(child: Padding(padding: EdgeInsets.only(right: i == sugarLevels.length - 1 ? 0 : 6), child: _choiceButton(sugarLevels[i], _sugar == i, () => setState(() => _sugar = i), compact: true)))]),
            _sectionTitle('Đá'),
            Row(children: [for (var i = 0; i < iceLevels.length; i++) Expanded(child: Padding(padding: EdgeInsets.only(right: i == iceLevels.length - 1 ? 0 : 7), child: _choiceButton(iceLevels[i], _ice == i, () => setState(() => _ice = i), compact: true)))]),
            _sectionTitle('Topping'),
            for (var i = 0; i < toppingNames.length; i++) CheckboxListTile(contentPadding: EdgeInsets.zero, dense: true, activeColor: coral, controlAffinity: ListTileControlAffinity.leading, value: _toppings.contains(i), title: Text(toppingNames[i], style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w500)), secondary: Text('+${money(toppingFees[i])}', style: const TextStyle(color: priceRed, fontSize: 12, fontWeight: FontWeight.w700)), onChanged: (value) => setState(() => value == true ? _toppings.add(i) : _toppings.remove(i))),
          ],
          _sectionTitle('Ghi chú cho quán'),
          TextField(controller: _note, maxLength: 80, textCapitalization: TextCapitalization.sentences, decoration: InputDecoration(hintText: 'VD: ít ngọt hơn', counterText: '', contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 12), enabledBorder: OutlineInputBorder(borderRadius: BorderRadius.circular(10), borderSide: const BorderSide(color: Color(0xFFECE3E2), width: 1.5)), focusedBorder: OutlineInputBorder(borderRadius: BorderRadius.circular(10), borderSide: const BorderSide(color: coral, width: 1.5))), onChanged: (_) => setState(() {})),
        ])),
        Container(padding: const EdgeInsets.fromLTRB(18, 10, 18, 12), decoration: BoxDecoration(color: Colors.white, boxShadow: [BoxShadow(color: Colors.black.withOpacity(.06), blurRadius: 10, offset: const Offset(0, -2))]), child: Row(children: [
          Container(height: 48, decoration: BoxDecoration(color: beige, borderRadius: BorderRadius.circular(12)), child: Row(children: [IconButton(onPressed: _quantity > 1 ? () => setState(() => _quantity--) : null, icon: const Icon(Icons.remove_rounded, color: coral)), Padding(padding: const EdgeInsets.symmetric(horizontal: 4), child: Text('$_quantity', style: const TextStyle(fontWeight: FontWeight.w800))), IconButton(onPressed: () => setState(() => _quantity++), icon: const Icon(Icons.add_rounded, color: coral))])),
          const SizedBox(width: 10), Expanded(child: SizedBox(height: 52, child: FilledButton(style: FilledButton.styleFrom(backgroundColor: coral, shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12))), onPressed: () => Navigator.pop(context, _ProductSheetResult(_options, _quantity)), child: Text('${widget.editing == null ? 'THÊM VÀO GIỎ' : 'CẬP NHẬT'} · ${money(_total)}', maxLines: 1, overflow: TextOverflow.ellipsis, style: const TextStyle(color: Colors.white, fontSize: 12, fontWeight: FontWeight.w800))))),
        ])),
      ])),
    )));
  }
}

Widget _sectionTitle(String value) => Padding(padding: const EdgeInsets.only(top: 13, bottom: 7), child: Text(value, style: const TextStyle(fontSize: 14, fontWeight: FontWeight.w800)));
Widget _choiceButton(String label, bool selected, VoidCallback onTap, {bool compact = false}) => SizedBox(
  height: compact ? 44 : 48,
  child: OutlinedButton(
    onPressed: onTap,
    style: OutlinedButton.styleFrom(
      foregroundColor: ink,
      backgroundColor: selected ? beige : Colors.white,
      side: BorderSide(color: selected ? coral : const Color(0xFFE6D8C6), width: selected ? 1.8 : 1),
      padding: const EdgeInsets.symmetric(horizontal: 3),
      minimumSize: Size.zero,
      tapTargetSize: MaterialTapTargetSize.shrinkWrap,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
    ),
    child: Text(label, maxLines: 1, overflow: TextOverflow.ellipsis, style: TextStyle(color: ink, fontSize: compact ? 11 : 13, fontWeight: selected ? FontWeight.w800 : FontWeight.w500)),
  ),
);

class CartScreen extends StatefulWidget {
  const CartScreen({super.key});
  @override
  State<CartScreen> createState() => _CartScreenState();
}

class _CartScreenState extends State<CartScreen> {
  final _name = TextEditingController();
  final _phone = TextEditingController();
  final _address = TextEditingController();
  final _orderNote = TextEditingController();
  SharedPreferences? _preferences;

  @override
  void initState() {
    super.initState();
    SharedPreferences.getInstance().then((prefs) {
      if (!mounted) return;
      _preferences = prefs;
      _name.text = prefs.getString('checkout_name') ?? '';
      _phone.text = prefs.getString('checkout_phone') ?? '';
      _address.text = prefs.getString('checkout_address') ?? '';
      setState(() {});
    });
  }

  @override
  void dispose() {
    _name.dispose(); _phone.dispose(); _address.dispose(); _orderNote.dispose();
    super.dispose();
  }

  bool _valid(OrderProvider order) => _name.text.trim().isNotEmpty && RegExp(r'^\d{10}$').hasMatch(_phone.text) && (order.pickup || _address.text.trim().isNotEmpty);
  void _save(String key, String value) {
    _preferences?.setString(key, value);
    if (mounted) setState(() {});
  }

  void _placeOrder(OrderProvider order) {
    if (!_valid(order)) {
      showToast(context, 'Vui lòng nhập đủ thông tin nhận hàng');
      return;
    }
    final id = order.placeOrder(note: _orderNote.text);
    Navigator.push(context, MaterialPageRoute(builder: (_) => OrderTrackingScreen(orderId: id)));
  }

  @override
  Widget build(BuildContext context) => Consumer<OrderProvider>(builder: (context, order, _) => OrderShell(
    showBack: true,
    title: 'Giỏ hàng (${order.itemCount})',
    bottom: order.itemCount == 0 ? const OrderBottomBar() : _CheckoutBottom(enabled: _valid(order), onPlaceOrder: () => _placeOrder(order)),
    child: GradientBody(child: order.lines.isEmpty ? const _EmptyCart() : ListView(padding: const EdgeInsets.fromLTRB(16, 14, 16, 20), children: [
      _whiteCard(Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        const Text('Món đã chọn', style: TextStyle(fontWeight: FontWeight.w800, fontSize: 15)),
        const SizedBox(height: 6),
        for (var i = 0; i < order.lines.length; i++) ...[
          _CartLineCard(line: order.lines[i]),
          if (i < order.lines.length - 1) const Divider(height: 1),
        ],
        Align(alignment: Alignment.centerLeft, child: TextButton.icon(onPressed: () => Navigator.pop(context), icon: const Icon(Icons.add, size: 18), label: const Text('Thêm món'))),
      ])),
      _whiteCard(Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        const Text('Nhận hàng', style: TextStyle(fontWeight: FontWeight.w800, fontSize: 15)), const SizedBox(height: 10),
        SegmentedButton<bool>(segments: const [ButtonSegment(value: false, label: Text('Giao tận nơi')), ButtonSegment(value: true, label: Text('Tự đến lấy'))], selected: {order.pickup}, onSelectionChanged: (value) => order.setPickup(value.first), style: ButtonStyle(selectedBackgroundColor: const MaterialStatePropertyAll(coral), selectedForegroundColor: const MaterialStatePropertyAll(Colors.white))),
        const SizedBox(height: 12),
        _formField('Tên người nhận *', _name, onChanged: (v) => _save('checkout_name', v)),
        const SizedBox(height: 9),
        _formField('Số điện thoại *', _phone, keyboard: TextInputType.phone, maxLength: 10, formatters: [FilteringTextInputFormatter.digitsOnly], onChanged: (v) => _save('checkout_phone', v)),
        if (!order.pickup) ...[const SizedBox(height: 9), _formField('Địa chỉ giao hàng *', _address, onChanged: (v) => _save('checkout_address', v))] else const Padding(padding: EdgeInsets.only(top: 12), child: Text('Lấy tại: Mộc Miên Coffee · sẵn sàng sau khoảng 15 phút', style: TextStyle(color: muted, fontSize: 12))),
        const SizedBox(height: 9),
        _formField('Ghi chú đơn hàng', _orderNote, maxLength: 120),
      ])),
      _PaymentVoucherCard(order: order),
      _whiteCard(Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        const Text('Tiền', style: TextStyle(fontWeight: FontWeight.w800, fontSize: 15)), const SizedBox(height: 8),
        _moneyRow('Tạm tính', order.subtotal),
        _moneyRow('Phí giao hàng', order.deliveryFee),
        _moneyRow('Giảm giá', -order.discount, green: order.discount > 0),
      ])),
    ])),
  ));
}

class _EmptyCart extends StatelessWidget {
  const _EmptyCart();
  @override
  Widget build(BuildContext context) => Center(child: Column(mainAxisSize: MainAxisSize.min, children: [const Icon(Icons.shopping_bag_outlined, size: 54, color: muted), const SizedBox(height: 12), const Text('Giỏ hàng đang trống.', style: TextStyle(fontWeight: FontWeight.w800, fontSize: 17)), const SizedBox(height: 8), OutlinedButton(onPressed: () => Navigator.pop(context), child: const Text('XEM MENU'))]));
}

class _CartLineCard extends StatelessWidget {
  const _CartLineCard({required this.line});
  final CartLine line;
  @override
  Widget build(BuildContext context) {
    final toppingText = line.options.toppings.map((i) => toppingNames[i]).join(', ');
    final details = line.product.isDrink
        ? 'Size ${line.options.size} · ${line.options.sugar} đường · ${line.options.ice}${toppingText.isEmpty ? '' : ' · $toppingText'}${line.options.note.isEmpty ? '' : ' · "${line.options.note}"'}'
        : (line.options.note.isEmpty ? '' : '"${line.options.note}"');
    return Padding(padding: const EdgeInsets.symmetric(vertical: 9), child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
      Row(children: [Expanded(child: Text(line.product.name, style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 14))), TextButton(onPressed: () => showProductOptions(context, line.product, editing: line), style: TextButton.styleFrom(minimumSize: Size.zero, padding: const EdgeInsets.symmetric(horizontal: 4, vertical: 3), tapTargetSize: MaterialTapTargetSize.shrinkWrap), child: const Text('Sửa', style: TextStyle(color: priceRed, fontSize: 12, fontWeight: FontWeight.w700)))]),
      if (details.isNotEmpty) Padding(padding: const EdgeInsets.only(top: 1), child: Text(details, style: const TextStyle(color: muted, fontSize: 11, height: 1.4))),
      const SizedBox(height: 7),
      Row(children: [Expanded(child: Text(money(line.unitPrice * line.quantity), style: const TextStyle(color: priceRed, fontSize: 13, fontWeight: FontWeight.w800))), _qtyButton(Icons.remove, () { if (line.quantity == 1) { showToast(context, 'Đã xoá món khỏi giỏ'); context.read<OrderProvider>().remove(line); } else { context.read<OrderProvider>().changeQuantity(line, -1); } }), Padding(padding: const EdgeInsets.symmetric(horizontal: 13), child: Text('${line.quantity}', style: const TextStyle(fontWeight: FontWeight.w800))), _qtyButton(Icons.add, () => context.read<OrderProvider>().changeQuantity(line, 1))]),
    ]));
  }
}

class _PaymentVoucherCard extends StatefulWidget {
  const _PaymentVoucherCard({required this.order});
  final OrderProvider order;
  @override
  State<_PaymentVoucherCard> createState() => _PaymentVoucherCardState();
}
class _PaymentVoucherCardState extends State<_PaymentVoucherCard> {
  final _voucher = TextEditingController();
  @override
  void initState() {
    super.initState();
    _voucher.text = widget.order.voucher ?? '';
  }
  @override
  void dispose() { _voucher.dispose(); super.dispose(); }
  @override
  Widget build(BuildContext context) => _whiteCard(Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
    const Text('Thanh toán', style: TextStyle(fontWeight: FontWeight.w800, fontSize: 15)), const SizedBox(height: 4),
    for (final method in paymentMethods) RadioListTile<String>(dense: true, contentPadding: EdgeInsets.zero, activeColor: coral, value: method, groupValue: widget.order.paymentMethod, title: Text(method, style: const TextStyle(fontSize: 12)), onChanged: (value) { if (value != null) widget.order.setPayment(value); }),
    const Divider(height: 18),
    const Text('Mã giảm giá', style: TextStyle(fontWeight: FontWeight.w800, fontSize: 14)), const SizedBox(height: 8),
    Row(children: [Expanded(child: TextField(controller: _voucher, textCapitalization: TextCapitalization.characters, inputFormatters: [UpperCaseTextFormatter()], enabled: widget.order.voucher == null, onChanged: (_) => setState(() {}), decoration: InputDecoration(hintText: 'Nhập mã giảm giá', contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 12), border: OutlineInputBorder(borderRadius: BorderRadius.circular(10), borderSide: const BorderSide(color: Color(0xFFECE3E2), width: 1.5)), enabledBorder: OutlineInputBorder(borderRadius: BorderRadius.circular(10), borderSide: const BorderSide(color: Color(0xFFECE3E2), width: 1.5)), focusedBorder: OutlineInputBorder(borderRadius: BorderRadius.circular(10), borderSide: const BorderSide(color: coral, width: 1.5)))), const SizedBox(width: 8), OutlinedButton(onPressed: () { if (widget.order.voucher != null) { widget.order.clearVoucher(); _voucher.clear(); } else { widget.order.applyVoucher(_voucher.text); } setState(() {}); }, child: Text(widget.order.voucher == null ? 'ÁP DỤNG' : 'BỎ MÃ'))]),
    if (widget.order.voucherError != null) Padding(padding: const EdgeInsets.only(top: 6), child: Text(widget.order.voucherError!, style: const TextStyle(color: Color(0xFFD7262C), fontSize: 12.5))),
    if (widget.order.voucher == 'GIAM10') const Padding(padding: EdgeInsets.only(top: 6), child: Text('Giảm 10,000đ', style: TextStyle(color: Color(0xFF27834A), fontSize: 12.5, fontWeight: FontWeight.w700))),
    if (widget.order.voucher == 'FREESHIP') const Padding(padding: EdgeInsets.only(top: 6), child: Text('Miễn phí giao hàng', style: TextStyle(color: Color(0xFF27834A), fontSize: 12.5, fontWeight: FontWeight.w700))),
  ]));
}

class _CheckoutBottom extends StatelessWidget {
  const _CheckoutBottom({required this.enabled, required this.onPlaceOrder});
  final bool enabled;
  final VoidCallback onPlaceOrder;
  @override
  Widget build(BuildContext context) => Column(mainAxisSize: MainAxisSize.min, children: [
    Consumer<OrderProvider>(builder: (context, order, _) => SafeArea(top: true, bottom: false, child: Container(padding: const EdgeInsets.fromLTRB(16, 9, 16, 9), decoration: BoxDecoration(color: Colors.white, boxShadow: [BoxShadow(color: Colors.black.withOpacity(.07), blurRadius: 12, offset: const Offset(0, -2))]), child: Row(children: [Expanded(child: Column(mainAxisSize: MainAxisSize.min, crossAxisAlignment: CrossAxisAlignment.start, children: [const Text('Tổng cộng', style: TextStyle(color: muted, fontSize: 11)), Text(money(order.total), style: const TextStyle(color: priceRed, fontWeight: FontWeight.w800, fontSize: 18))])), SizedBox(height: 52, child: FilledButton(style: FilledButton.styleFrom(backgroundColor: enabled ? coral : const Color(0xFFD9D7D5), disabledBackgroundColor: const Color(0xFFD9D7D5), disabledForegroundColor: const Color(0xFF76726F), shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12))), onPressed: enabled ? onPlaceOrder : null, child: const Text('ĐẶT HÀNG', style: TextStyle(fontWeight: FontWeight.w800))))]))),
    const OrderBottomBar(),
  ]);
}

class OrderTrackingScreen extends StatelessWidget {
  const OrderTrackingScreen({super.key, required this.orderId});
  final String orderId;
  @override
  Widget build(BuildContext context) {
    final orderNote = context.watch<OrderProvider>().orderNote(orderId);
    return OrderShell(title: 'Theo dõi đơn', showBack: true, child: GradientBody(child: ListView(padding: const EdgeInsets.all(16), children: [
    _whiteCard(Column(crossAxisAlignment: CrossAxisAlignment.start, children: [Text('Đơn hàng #$orderId', style: const TextStyle(fontSize: 17, fontWeight: FontWeight.w800)), const SizedBox(height: 6), const Text('Đơn hàng đã được ghi nhận', style: TextStyle(color: muted, fontSize: 12))])),
    if (orderNote.isNotEmpty) _whiteCard(Text('Ghi chú đơn hàng: $orderNote', style: const TextStyle(color: muted, fontSize: 12))),
    const SizedBox(height: 14),
    _whiteCard(Column(children: [for (var i = 0; i < 4; i++) _trackingStep(i, ['Đã nhận đơn', 'Đang pha chế', 'Đang giao', 'Hoàn thành'][i], ['Quán đã nhận được đơn hàng của bạn', 'Barista đang chuẩn bị món', 'Đơn hàng đang trên đường đến bạn', 'Chúc bạn ngon miệng!'][i])])),
    const SizedBox(height: 14), OutlinedButton(onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const MyOrdersScreen())), child: const Text('ĐƠN HÀNG CỦA TÔI')),
  ])));
  }
}

Widget _trackingStep(int index, String title, String subtitle) => Padding(padding: const EdgeInsets.symmetric(vertical: 10), child: Row(crossAxisAlignment: CrossAxisAlignment.start, children: [Container(width: 28, height: 28, decoration: BoxDecoration(color: index == 0 ? coral : beige, shape: BoxShape.circle), child: Icon(index == 0 ? Icons.check : Icons.circle, color: index == 0 ? Colors.white : muted, size: 14)), const SizedBox(width: 12), Expanded(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [Text(title, style: const TextStyle(fontWeight: FontWeight.w800)), const SizedBox(height: 3), Text(subtitle, style: const TextStyle(color: muted, fontSize: 11))]))]));

class MyOrdersScreen extends StatelessWidget {
  const MyOrdersScreen({super.key});
  @override
  Widget build(BuildContext context) => OrderShell(title: 'Đơn hàng của tôi', showBack: true, child: GradientBody(child: Consumer<OrderProvider>(builder: (context, order, _) => order.orderHistory.isEmpty ? const Center(child: Text('Bạn chưa có đơn hàng nào', style: TextStyle(color: muted))) : ListView(padding: const EdgeInsets.all(16), children: [for (final id in order.orderHistory) _whiteCard(Row(children: [const Icon(Icons.receipt_long_rounded, color: coral), const SizedBox(width: 12), Expanded(child: Text('Đơn hàng #$id', style: const TextStyle(fontWeight: FontWeight.w800))), TextButton(onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (_) => OrderTrackingScreen(orderId: id))), child: const Text('THEO DÕI'))]))]))));
}

Widget _whiteCard(Widget child) => Container(margin: const EdgeInsets.only(bottom: 10), padding: const EdgeInsets.all(14), decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(14)), child: child);
Widget _qtyButton(IconData icon, VoidCallback onTap) => Material(color: beige, shape: const CircleBorder(), child: InkWell(customBorder: const CircleBorder(), onTap: onTap, child: SizedBox(width: 32, height: 32, child: Icon(icon, color: coral, size: 18))));
Widget _moneyRow(String label, int amount, {bool strong = false, bool green = false}) => Padding(padding: const EdgeInsets.symmetric(vertical: 4), child: Row(children: [Expanded(child: Text(label, style: TextStyle(color: strong ? ink : muted, fontWeight: strong ? FontWeight.w800 : FontWeight.w500))), Text('${amount < 0 ? '-' : ''}${money(amount.abs())}', style: TextStyle(color: green ? const Color(0xFF27834A) : (amount < 0 ? priceRed : (strong ? ink : muted)), fontWeight: strong ? FontWeight.w800 : FontWeight.w600))]));
void showToast(BuildContext context, String message) => ScaffoldMessenger.of(context)..hideCurrentSnackBar()..showSnackBar(SnackBar(content: Text(message), duration: const Duration(milliseconds: 2200), behavior: SnackBarBehavior.floating, backgroundColor: coffee, shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)), margin: const EdgeInsets.fromLTRB(20, 0, 20, 18)));

Widget _formField(String label, TextEditingController controller, {int? maxLength, TextInputType? keyboard, List<TextInputFormatter> formatters = const [], ValueChanged<String>? onChanged}) => TextField(
  controller: controller,
  keyboardType: keyboard,
  inputFormatters: formatters,
  maxLength: maxLength,
  onChanged: onChanged,
  decoration: InputDecoration(
    labelText: label,
    counterText: '',
    isDense: true,
    contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 14),
    enabledBorder: OutlineInputBorder(borderRadius: BorderRadius.circular(10), borderSide: const BorderSide(color: Color(0xFFECE3E2), width: 1.5)),
    focusedBorder: OutlineInputBorder(borderRadius: BorderRadius.circular(10), borderSide: const BorderSide(color: coral, width: 1.5)),
  ),
);

class UpperCaseTextFormatter extends TextInputFormatter {
  @override
  TextEditingValue formatEditUpdate(TextEditingValue oldValue, TextEditingValue newValue) =>
      newValue.copyWith(text: newValue.text.toUpperCase(), selection: newValue.selection, composing: TextRange.empty);
}
