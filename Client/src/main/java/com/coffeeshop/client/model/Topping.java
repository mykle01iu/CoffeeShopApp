package com.coffeeshop.client.model;
import java.io.Serializable;
public final class Topping implements Serializable { public final String id,name; public final int price; public Topping(String id,String name,int price){this.id=id;this.name=name;this.price=price;} }
