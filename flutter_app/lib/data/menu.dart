class Category {
  const Category({required this.id, required this.name});
  final String id;
  final String name;
}

class Product {
  const Product({
    required this.id,
    required this.categoryId,
    required this.name,
    required this.description,
    required this.price,
    this.imageAsset,
    required this.isDrink,
    this.isNew = false,
    this.soldOut = false,
  });

  final String id;
  final String categoryId;
  final String name;
  final String description;
  final int price;
  final String? imageAsset;
  final bool isDrink;
  final bool isNew;
  final bool soldOut;
}

const categories = <Category>[
  Category(id: 'cafe', name: 'Cà phê'),
  Category(id: 'tra', name: 'Trà'),
  Category(id: 'them', name: 'Gọi thêm'),
];

const products = <Product>[
  Product(id: 'ca-phe-sua-da', categoryId: 'cafe', name: 'Cà phê sữa đá', description: 'Cà phê đậm vị, sữa ngọt dịu', price: 35000, imageAsset: 'assets/products/nau_da.png', isDrink: true),
  Product(id: 'bac-xiu', categoryId: 'cafe', name: 'Bạc xỉu', description: 'Nhiều sữa, cà phê thơm nhẹ', price: 39000, imageAsset: 'assets/products/bac_xiu.png', isDrink: true),
  Product(id: 'cold-brew-cam-sa', categoryId: 'cafe', name: 'Cold brew cam sả', description: 'Ủ lạnh êm dịu, thơm cam sả', price: 49000, imageAsset: null, isDrink: true, isNew: true),
  Product(id: 'tra-dao-cam-sa', categoryId: 'tra', name: 'Trà đào cam sả', description: 'Trà thanh mát cùng đào và cam sả', price: 45000, imageAsset: 'assets/products/tra_dao_cam_sa.png', isDrink: true),
  Product(id: 'tra-o-long-sua', categoryId: 'tra', name: 'Trà ô long sữa', description: 'Ô long thơm dịu, sữa béo nhẹ', price: 49000, imageAsset: null, isDrink: true, soldOut: true),
  Product(id: 'thach-tra-dao', categoryId: 'them', name: 'Thạch trà đào', description: 'Thạch trái cây giòn mát', price: 15000, imageAsset: 'assets/products/thach_tra_dao.png', isDrink: false),
  Product(id: 'banh-mi-que', categoryId: 'them', name: 'Bánh mì que', description: 'Vỏ giòn, nhân pate thơm', price: 20000, imageAsset: null, isDrink: false),
];

String money(int value) => '${value.toString().replaceAllMapped(RegExp(r'\B(?=(\d{3})+(?!\d))'), (_) => ',')}đ';
