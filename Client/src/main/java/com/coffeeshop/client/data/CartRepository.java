package com.coffeeshop.client.data;

import android.content.Context;
import android.content.SharedPreferences;
import com.coffeeshop.client.catalog.data.ProductRepository;
import com.coffeeshop.client.catalog.model.Product;
import com.coffeeshop.client.model.*;
import com.coffeeshop.client.ui.home.CartOptions;
import com.coffeeshop.client.ui.home.DrinkProduct;
import com.coffeeshop.client.ui.home.MenuCatalog;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;

public final class CartRepository {
    private static final String PREF_NAME="moc_mien_cart", LINE="line_", META="line_meta_";
    private CartRepository(){}
    private static SharedPreferences prefs(Context c){SharedPreferences sp=c.getApplicationContext().getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);migrateHomeCart(c,sp);return sp;}
    private static synchronized void migrateHomeCart(Context c,SharedPreferences cart){SharedPreferences home=c.getApplicationContext().getSharedPreferences("moc_mien_home",Context.MODE_PRIVATE);SharedPreferences.Editor into=cart.edit(),out=home.edit();boolean changed=false;for(Map.Entry<String,?> e:home.getAll().entrySet()){String k=e.getKey();if((k.startsWith("cart_item_")||k.startsWith("cart_")||k.startsWith("qty_"))&&e.getValue() instanceof Integer){int q=(Integer)e.getValue();if(q>0)into.putInt(k,cart.getInt(k,0)+q);out.remove(k);changed=true;}}if(changed){into.apply();out.apply();}}
    public static SharedPreferences getCartPreferences(Context c){return c==null?null:prefs(c);}

    public static synchronized void add(Context c,Product p,SelectedOptions options,int quantity){if(c==null||p==null||quantity<=0)return;SharedPreferences sp=prefs(c);SelectedOptions o=options==null?SelectedOptions.defaults():options;String k=lineKey(p.id,o);int n=sp.getInt(k,0)+quantity;sp.edit().putInt(k,n).putString(META+p.id+"_"+o.optionsHash(),serialize(o)).apply();}
    public static void add(Context c,Product p){if(p!=null)add(c,p,OptionsConfig.forProduct(p).defaults(c),1);}
    public static void addSelection(Context c,DrinkProduct dp,CartOptions.Selection s){if(c==null||dp==null)return;Product p=findByStringId(dp.id);if(p==null)return; add(c,p,fromLegacySelection(c,p,s),1);}
    public static synchronized void update(Context c,CartItem old,SelectedOptions next,int quantity){if(c==null||old==null)return;remove(c,old);if(quantity>0)add(c,old.product,next,quantity);}
    public static synchronized List<CartItem> getItems(Context c){if(c==null)return Collections.emptyList();SharedPreferences sp=prefs(c);migrateLegacy(c,sp);List<CartItem> out=new ArrayList<>();for(Product p:ProductRepository.getProducts())for(String k:sp.getAll().keySet())if(k.startsWith(LINE+p.id+"_")){int qty=sp.getInt(k,0);if(qty<=0)continue;int hash;try{hash=Integer.parseInt(k.substring((LINE+p.id+"_").length()));}catch(Exception e){continue;}SelectedOptions o=deserialize(sp.getString(META+p.id+"_"+hash,null));if(o==null)o=OptionsConfig.forProduct(p).defaults(c);out.add(new CartItem(p,o,qty));}return Collections.unmodifiableList(out);}
    public static synchronized void increase(Context c,CartItem item){setQuantity(c,item,item==null?0:item.quantity+1);}
    public static synchronized void decrease(Context c,CartItem item){setQuantity(c,item,item==null?0:item.quantity-1);}
    public static synchronized void setQuantity(Context c,CartItem item,int q){if(c==null||item==null)return;SharedPreferences sp=prefs(c);String k=lineKey(item.product.id,item.options);if(q<=0)sp.edit().remove(k).remove(META+item.product.id+"_"+item.options.optionsHash()).apply();else sp.edit().putInt(k,q).apply();}
    public static synchronized void remove(Context c,CartItem item){if(c==null||item==null)return;SharedPreferences sp=prefs(c);sp.edit().remove(lineKey(item.product.id,item.options)).remove(META+item.product.id+"_"+item.options.optionsHash()).apply();}
    /** Compatibility entry points for existing callers; operate on the first matching line. */
    public static void increase(Context c,int id){CartItem i=findFirst(c,id);if(i!=null)increase(c,i);}
    public static void decrease(Context c,int id){CartItem i=findFirst(c,id);if(i!=null)decrease(c,i);}
    public static void remove(Context c,int id){for(CartItem i:new ArrayList<>(getItems(c)))if(i.product.id==id)remove(c,i);}
    public static synchronized void clear(Context c){if(c==null)return;SharedPreferences sp=prefs(c);SharedPreferences.Editor e=sp.edit();for(String k:sp.getAll().keySet())if(k.startsWith(LINE)||k.startsWith(META)||k.startsWith("cart_item_")||k.startsWith("cart_")||k.startsWith("qty_"))e.remove(k);e.apply();}
    public static int subtotal(Context c){int n=0;for(CartItem i:getItems(c))n+=i.getLineTotal();return n;}
    public static int totalQuantity(Context c){int n=0;for(CartItem i:getItems(c))n+=i.quantity;return n;}
    private static String lineKey(int id,SelectedOptions o){return LINE+id+"_"+o.optionsHash();}
    private static CartItem findFirst(Context c,int id){for(CartItem i:getItems(c))if(i.product.id==id)return i;return null;}
    private static Product findByStringId(String id){List<DrinkProduct> ds=MenuCatalog.getProducts();List<Product> ps=ProductRepository.getProducts();for(int i=0;i<ds.size()&&i<ps.size();i++)if(ds.get(i).id.equals(id))return ps.get(i);return null;}
    private static SelectedOptions fromLegacySelection(Context c,Product p,CartOptions.Selection s){if(s==null)return OptionsConfig.forProduct(p).defaults(c);OptionsConfig config=OptionsConfig.forProduct(p);Size size=Size.S;try{size=Size.valueOf(s.size);}catch(Exception ignored){}SugarLevel sugar=SugarLevel.HUNDRED;try{sugar=SugarLevel.valueOf(s.sugar);}catch(Exception ignored){if("50".equals(s.sugar))sugar=SugarLevel.FIFTY;else if("70".equals(s.sugar))sugar=SugarLevel.SEVENTY;else if("0".equals(s.sugar))sugar=SugarLevel.ZERO;}IceLevel ice=IceLevel.NORMAL;if("less".equals(s.ice))ice=IceLevel.LESS;else if("none".equals(s.ice))ice=IceLevel.NONE;Set<String> ids=new HashSet<>();for(String t:s.toppings.split(",")){if("salt".equals(t))ids.add("cheese");else if("espresso".equals(t))ids.add(t);else if("pearls".equals(t))ids.add(t);}return new SelectedOptions(config.size?size:null,config.sugar?sugar:null,config.ice?ice:null,OptionsConfig.toppings(p,ids,c),false,"");}
    private static String serialize(SelectedOptions o){try{JSONObject j=new JSONObject();j.put("size",o.size==null?"":o.size.name());j.put("sugar",o.sugar==null?"":o.sugar.name());j.put("ice",o.ice==null?"":o.ice.name());j.put("warm",o.warmUp);j.put("note",o.note);JSONArray a=new JSONArray();for(Topping t:o.toppings){JSONObject x=new JSONObject();x.put("id",t.id);x.put("name",t.name);x.put("price",t.price);a.put(x);}j.put("toppings",a);return j.toString();}catch(Exception e){return "";}}
    private static SelectedOptions deserialize(String raw){if(raw==null)return null;try{JSONObject j=new JSONObject(raw);Size s=j.optString("size").isEmpty()?null:Size.valueOf(j.getString("size"));SugarLevel g=j.optString("sugar").isEmpty()?null:SugarLevel.valueOf(j.getString("sugar"));IceLevel i=j.optString("ice").isEmpty()?null:IceLevel.valueOf(j.getString("ice"));List<Topping> ts=new ArrayList<>();JSONArray a=j.optJSONArray("toppings");if(a!=null)for(int x=0;x<a.length();x++){JSONObject t=a.getJSONObject(x);ts.add(new Topping(t.getString("id"),t.getString("name"),t.getInt("price")));}return new SelectedOptions(s,g,i,ts,j.optBoolean("warm"),j.optString("note"));}catch(Exception e){return null;}}
    private static synchronized void migrateLegacy(Context c,SharedPreferences sp){SharedPreferences.Editor e=sp.edit();boolean changed=false;Map<String,?> all=new HashMap<>(sp.getAll());List<Product> products=ProductRepository.getProducts();List<DrinkProduct> drinks=MenuCatalog.getProducts();for(Map.Entry<String,?> entry:all.entrySet()){String key=entry.getKey();if(!(entry.getValue() instanceof Integer)||((Integer)entry.getValue())<=0||key.startsWith(LINE)||key.startsWith(META))continue;int id=-1;SelectedOptions options=null;String token=null;
      if(key.startsWith("qty_")){try{id=Integer.parseInt(key.substring(4));}catch(Exception ignored){}}
      else if(key.startsWith("cart_item_")){String tail=key.substring(10);int bar=tail.indexOf('|');String rawId=bar<0?tail:tail.substring(0,bar);id=parseProductId(rawId,products,drinks);if(bar>=0)token=tail.substring(bar+1);}
      else if(key.startsWith("cart_")){String tail=key.substring(5);int underscore=tail.lastIndexOf('_');String rawId=tail;String size=null;if(underscore>0&&"SML".contains(tail.substring(underscore+1))){rawId=tail.substring(0,underscore);size=tail.substring(underscore+1);}id=parseProductId(rawId,products,drinks);if(size!=null)options=new SelectedOptions(Size.valueOf(size),SugarLevel.HUNDRED,IceLevel.NORMAL,Collections.emptyList(),false,"");}
      if(id<1||id>products.size())continue;Product p=products.get(id-1);if(token!=null){CartOptions.Selection old=CartOptions.parseToken(token);options=fromLegacySelection(c,p,old);}if(options==null)options=OptionsConfig.forProduct(p).defaults(c);String dest=lineKey(id,options);int qty=(Integer)entry.getValue();e.putInt(dest,sp.getInt(dest,0)+qty).putString(META+id+"_"+options.optionsHash(),serialize(options)).remove(key);changed=true;}
      if(changed)e.apply();}
    private static int parseProductId(String raw,List<Product> products,List<DrinkProduct> drinks){try{return Integer.parseInt(raw);}catch(Exception ignored){}for(int i=0;i<drinks.size()&&i<products.size();i++)if(drinks.get(i).id.equals(raw))return products.get(i).id;return -1;}
}
