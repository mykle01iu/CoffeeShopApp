package com.coffeeshop.client.catalog.viewmodel;

import com.coffeeshop.client.catalog.data.ProductRepository;
import com.coffeeshop.client.catalog.model.Product;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class ProductListViewModel {
    public interface Observer { void onStateChanged(UiState state); }
    public static final class UiState {
        public final String query, selectedCategory;
        public final List<Product> products;
        UiState(String query, String category, List<Product> products) {
            this.query=query; this.selectedCategory=category;
            this.products=Collections.unmodifiableList(products);
        }
    }
    private String query="", category="Tất cả";
    private Observer observer;
    public UiState getState() { return new UiState(query, category, filter()); }
    public void observe(Observer observer) { this.observer=observer; observer.onStateChanged(getState()); }
    public void setQuery(String value) { query=value == null ? "" : value; publish(); }
    public void setCategory(String value) { category=value; publish(); }
    public void clearFilters() { query=""; category="Tất cả"; publish(); }
    private void publish() { if(observer!=null) observer.onStateChanged(getState()); }
    private List<Product> filter() {
        String q=normalize(query); List<Product> result=new ArrayList<>();
        for(Product p:ProductRepository.getProducts())
            if((category.equals("Tất cả") || p.category.equals(category)) && (q.isEmpty() || normalize(p.name).contains(q))) result.add(p);
        return result;
    }
    private static String normalize(String value) {
        return Normalizer.normalize(value.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").replace('đ','d').trim();
    }
}
