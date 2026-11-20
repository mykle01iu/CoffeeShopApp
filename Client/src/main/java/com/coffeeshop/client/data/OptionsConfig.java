package com.coffeeshop.client.data;
import com.coffeeshop.client.R; import com.coffeeshop.client.catalog.model.Product; import com.coffeeshop.client.model.ToppingOption; import com.coffeeshop.client.model.SelectedOptions; import java.util.*;
public final class OptionsConfig {
 public final boolean size,sugar,ice,warm; public final List<ToppingOption> toppings;
 private OptionsConfig(boolean size,boolean sugar,boolean ice,boolean warm,List<ToppingOption> toppings){this.size=size;this.sugar=sugar;this.ice=ice;this.warm=warm;this.toppings=Collections.unmodifiableList(toppings);}
 public boolean hasOptions(){return size||sugar||ice||warm||!toppings.isEmpty();}
 public SelectedOptions defaults(android.content.Context context){return new SelectedOptions(size?com.coffeeshop.client.model.Size.S:null,sugar?com.coffeeshop.client.model.SugarLevel.HUNDRED:null,ice?com.coffeeshop.client.model.IceLevel.NORMAL:null,Collections.emptyList(),false,"");}
 public static OptionsConfig forProduct(Product p){int id=p.id;String c=p.category==null?"":p.category.toLowerCase(Locale.ROOT);
  if(id==13||id==15||c.contains("bánh")&& !p.name.toLowerCase(Locale.ROOT).contains("croissant"))return new OptionsConfig(false,false,false,false,new ArrayList<>());
  if(id==14||p.name.equalsIgnoreCase("Croissant"))return new OptionsConfig(false,false,false,true,new ArrayList<>());
  List<ToppingOption> t=new ArrayList<>();boolean coffee=id>=1&&id<=3||c.contains("cà phê"), tea=id>=4&&id<=10||c.contains("trà"), frozen=id==11||c.contains("đá xay")||c.contains("sinh tố"), other=id==12||c.contains("khác");
  if(coffee){t.add(new ToppingOption("espresso",R.string.topping_espresso,10000));t.add(new ToppingOption("cheese",R.string.topping_cheese,10000));t.add(new ToppingOption("coffee_jelly",R.string.topping_coffee_jelly,5000));}
  else if(tea){t.add(new ToppingOption("pearls",R.string.topping_pearls,7000));t.add(new ToppingOption("coconut_jelly",R.string.topping_coconut_jelly,5000));t.add(new ToppingOption("fruit_jelly",R.string.topping_fruit_jelly,5000));t.add(new ToppingOption("pudding",R.string.topping_pudding,8000));}
  else if(frozen){t.add(new ToppingOption("whipped_cream",R.string.topping_whipped,5000));t.add(new ToppingOption("pearls",R.string.topping_pearls,7000));t.add(new ToppingOption("pudding",R.string.topping_pudding,8000));}
  else if(other){t.add(new ToppingOption("pearls",R.string.topping_pearls,7000));t.add(new ToppingOption("cheese",R.string.topping_cheese,10000));}
  return new OptionsConfig(true,true,!frozen,false,t);
 }
 public static String displayCategory(android.content.Context c,Product p){int id=p.id;if(id<=3)return c.getString(R.string.category_coffee_catalog);if(id<=10)return c.getString(R.string.category_tea_catalog);if(id==11)return c.getString(R.string.options_frozen);if(id==12)return c.getString(R.string.options_other);return c.getString(R.string.options_cake);}
 public static List<com.coffeeshop.client.model.Topping> toppings(Product p,Set<String> ids,android.content.Context c){List<com.coffeeshop.client.model.Topping> out=new ArrayList<>();for(ToppingOption t:forProduct(p).toppings)if(ids.contains(t.id))out.add(new com.coffeeshop.client.model.Topping(t.id,c.getString(t.nameRes),t.price));return out;}
}
