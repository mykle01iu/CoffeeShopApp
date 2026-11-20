package com.coffeeshop.client.data;
import com.coffeeshop.client.R;
import com.coffeeshop.client.catalog.model.Product;
import com.coffeeshop.client.model.SelectedOptions;
import com.coffeeshop.client.model.Topping;
import com.coffeeshop.client.model.ToppingOption;
import java.text.Normalizer;
import java.util.*;
public final class OptionsConfig {
 public final boolean size,sugar,ice,warm; public final List<ToppingOption> toppings;
 private OptionsConfig(boolean size,boolean sugar,boolean ice,boolean warm,List<ToppingOption> toppings){this.size=size;this.sugar=sugar;this.ice=ice;this.warm=warm;this.toppings=Collections.unmodifiableList(toppings);}
 public boolean hasOptions(){return size||sugar||ice||warm||!toppings.isEmpty();}
 public SelectedOptions defaults(android.content.Context c){return new SelectedOptions(size?com.coffeeshop.client.model.Size.S:null,sugar?com.coffeeshop.client.model.SugarLevel.HUNDRED:null,ice?com.coffeeshop.client.model.IceLevel.NORMAL:null,Collections.emptyList(),false,"");}
 public static OptionsConfig forProduct(Product p){
  String c=normalize(p.category),n=normalize(p.name);boolean bakery=c.contains("banh")||c.contains("cake")||c.contains("snack")||c.contains("bakery")||n.contains("croissant")||n.contains("tiramisu")||n.contains("cheese cake");
  if(bakery)return new OptionsConfig(false,false,false,n.contains("croissant"),new ArrayList<>());
  boolean coffee=c.contains("coffee")||c.contains("ca phe")||n.contains("nau da")||n.contains("bac xiu")||n.contains("phin")||n.contains("americano"),tea=c.contains("tea")||c.contains("tra"),frozen=c.contains("da xay")||c.contains("sinh to")||c.contains("smoothie")||n.contains("cacao da xay"),other=c.contains("khac")||c.contains("other")||c.contains("latte");
  List<ToppingOption> t=new ArrayList<>();
  if(coffee){t.add(new ToppingOption("espresso",R.string.topping_espresso,10000));t.add(new ToppingOption("cheese",R.string.topping_cheese,10000));t.add(new ToppingOption("coffee_jelly",R.string.topping_coffee_jelly,5000));}
  else if(tea){t.add(new ToppingOption("pearls",R.string.topping_pearls,7000));t.add(new ToppingOption("coconut_jelly",R.string.topping_coconut_jelly,5000));t.add(new ToppingOption("fruit_jelly",R.string.topping_fruit_jelly,5000));t.add(new ToppingOption("pudding",R.string.topping_pudding,8000));}
  else if(frozen){t.add(new ToppingOption("whipped_cream",R.string.topping_whipped,5000));t.add(new ToppingOption("pearls",R.string.topping_pearls,7000));t.add(new ToppingOption("pudding",R.string.topping_pudding,8000));}
  else if(other){t.add(new ToppingOption("pearls",R.string.topping_pearls,7000));t.add(new ToppingOption("cheese",R.string.topping_cheese,10000));}
  return new OptionsConfig(true,true,true,false,t);
 }
 public static String displayCategory(android.content.Context c,Product p){String cat=normalize(p.category);if(cat.contains("coffee")||cat.contains("ca phe"))return c.getString(R.string.category_coffee_catalog);if(cat.contains("tea")||cat.contains("tra"))return c.getString(R.string.category_tea_catalog);if(cat.contains("da xay")||cat.contains("smoothie"))return c.getString(R.string.options_frozen);if(cat.contains("khac")||cat.contains("other")||cat.contains("latte"))return c.getString(R.string.options_other);return c.getString(R.string.options_cake);}
 public static List<Topping> toppings(Product p,Set<String> ids,android.content.Context c){List<Topping> out=new ArrayList<>();for(ToppingOption t:forProduct(p).toppings)if(ids.contains(t.id))out.add(new Topping(t.id,c.getString(t.nameRes),t.price));return out;}
 private static String normalize(String value){String s=Normalizer.normalize(value==null?"":value.toLowerCase(Locale.ROOT),Normalizer.Form.NFD);return s.replaceAll("\\p{InCombiningDiacriticalMarks}+","").replace('\u0111','d');}
}
