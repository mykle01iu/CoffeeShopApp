package com.coffeeshop.client.model;

import com.coffeeshop.client.catalog.model.Product;

public final class CartItem {
    public final Product product;
    public final SelectedOptions options;
    public int quantity;

    public CartItem(Product product, int quantity) {
        this(product, SelectedOptions.defaults(), quantity);
    }

    public CartItem(Product product, SelectedOptions options, int quantity) {
        this.product = product;
        this.options = options == null ? SelectedOptions.defaults() : options;
        this.quantity = quantity;
    }

    public int getUnitPrice() {
        int price=product.price+(options.size==null?0:options.size.priceDelta);
        for(com.coffeeshop.client.model.Topping topping:options.toppings) price+=topping.price;
        return price;
    }
    public int getLineTotal() { return getUnitPrice()*quantity; }
    public String getLineKey() { return product.id+"_"+options.optionsHash(); }
    public String getOptionSummary() { return options.summary(); }
    public String getOptionSummary(android.content.Context context) {
        StringBuilder b=new StringBuilder();
        if(options.size!=null)b.append(context.getString(com.coffeeshop.client.R.string.options_summary_size,options.size.label));
        if(options.sugar!=null){append(b,context.getString(com.coffeeshop.client.R.string.options_summary_sugar,sugar(context,options.sugar)));}
        if(options.ice!=null)append(b,ice(context,options.ice));
        for(Topping t:options.toppings)append(b,"+ "+t.name);
        if(options.warmUp)append(b,context.getString(com.coffeeshop.client.R.string.options_summary_warm));
        return b.toString();
    }
    private static void append(StringBuilder b,String v){if(b.length()>0)b.append(" · ");b.append(v);}
    private static String sugar(android.content.Context c,SugarLevel s){switch(s){case ZERO:return c.getString(com.coffeeshop.client.R.string.options_sugar_0);case THIRTY:return c.getString(com.coffeeshop.client.R.string.options_sugar_30);case FIFTY:return c.getString(com.coffeeshop.client.R.string.options_sugar_50);case SEVENTY:return c.getString(com.coffeeshop.client.R.string.options_sugar_70);default:return c.getString(com.coffeeshop.client.R.string.options_sugar_100);}}
    private static String ice(android.content.Context c,IceLevel i){switch(i){case NONE:return c.getString(com.coffeeshop.client.R.string.options_ice_none);case LESS:return c.getString(com.coffeeshop.client.R.string.options_ice_less);case MORE:return c.getString(com.coffeeshop.client.R.string.options_ice_more);default:return c.getString(com.coffeeshop.client.R.string.options_ice_normal);}}
}
