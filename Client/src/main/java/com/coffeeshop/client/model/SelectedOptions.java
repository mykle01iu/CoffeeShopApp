package com.coffeeshop.client.model;
import java.io.Serializable; import java.util.ArrayList; import java.util.Collections; import java.util.List;
public final class SelectedOptions implements Serializable {
 public final Size size; public final SugarLevel sugar; public final IceLevel ice; public final List<Topping> toppings; public final boolean warmUp; public final String note;
 public SelectedOptions(Size size,SugarLevel sugar,IceLevel ice,List<Topping> toppings,boolean warmUp,String note){this.size=size;this.sugar=sugar;this.ice=ice;this.toppings=Collections.unmodifiableList(new ArrayList<>(toppings==null?Collections.emptyList():toppings));this.warmUp=warmUp;this.note=note==null?"":note;}
 public static SelectedOptions defaults(){return new SelectedOptions(Size.S,SugarLevel.HUNDRED,IceLevel.NORMAL,Collections.emptyList(),false,"");}
 public int optionsHash(){int h=1;h=31*h+(size==null?0:size.hashCode());h=31*h+(sugar==null?0:sugar.hashCode());h=31*h+(ice==null?0:ice.hashCode());for(Topping t:toppings)h=31*h+t.id.hashCode();h=31*h+(warmUp?1:0);return h;}
 public String keyToken(){StringBuilder b=new StringBuilder(size==null?"-":size.name()).append('_').append(sugar==null?"-":sugar.name()).append('_').append(ice==null?"-":ice.name()).append('_').append(warmUp?"1":"0");for(Topping t:toppings)b.append('_').append(t.id);return b.toString();}
 public String summary(){StringBuilder b=new StringBuilder();if(size!=null)b.append("Size ").append(size.label);if(sugar!=null){if(b.length()>0)b.append(" · ");b.append(sugar.label).append(" đường");}if(ice!=null){if(b.length()>0)b.append(" · ");b.append(ice.label);}for(Topping t:toppings){if(b.length()>0)b.append(" · ");b.append("+ ").append(t.name);}if(warmUp){if(b.length()>0)b.append(" · ");b.append("Hâm nóng");}return b.toString();}
}
