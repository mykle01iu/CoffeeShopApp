package com.coffeeshop.client.model;
public enum Size {
    S("S","360ml",0), M("M","480ml",6000), L("L","600ml",10000);
    public final String label, volume; public final int priceDelta;
    Size(String label,String volume,int priceDelta){this.label=label;this.volume=volume;this.priceDelta=priceDelta;}
}
