package com.coffeeshop.client.model;

public final class Address implements java.io.Serializable {
    public enum AddressLabel { HOME, OFFICE, OTHER }
    public final String id, receiverName, phone, street, ward, district, city;
    public final AddressLabel label;
    public final boolean isDefault;
    public Address(String id, String receiverName, String phone, String street, String ward, String district, String city, AddressLabel label, boolean isDefault) {
        this.id=id; this.receiverName=receiverName; this.phone=phone; this.street=street; this.ward=ward; this.district=district; this.city=city;
        this.label=label == null ? AddressLabel.HOME : label; this.isDefault=isDefault;
    }
    public String getFullAddress() { return street + ", " + ward + ", " + district + ", " + city; }
    public Address withDefault(boolean value) { return new Address(id,receiverName,phone,street,ward,district,city,label,value); }
}
