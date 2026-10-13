package com.xy177.solwarly.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import java.io.File;
import java.util.*;

/** Shared, commented TOML storage for dynamic food and spice configuration entries. */
public final class Configuration {
    private final CommentedFileConfig data;
    public Configuration(File file) {
        file.getParentFile().mkdirs();
        data = CommentedFileConfig.builder(file).sync().preserveInsertionOrder().build();
    }
    public void load() { data.load(); }
    public void save() { data.save(); }
    public void setCategoryComment(String category, String comment) { data.setComment(category, comment); }
    public Category getCategory(String name) { return new Category(name); }
    public Property get(String category, String key, Object defaultValue, Object... options) {
        List<String> path = Arrays.asList(category, key);
        if (!data.contains(path)) data.set(path, listValue(defaultValue));
        if (options.length > 0 && options[0] instanceof String comment) data.setComment(path, comment);
        double min = options.length > 1 && options[1] instanceof Number n ? n.doubleValue() : -Double.MAX_VALUE;
        double max = options.length > 2 && options[2] instanceof Number n ? n.doubleValue() : Double.MAX_VALUE;
        return new Property(path, defaultValue, min, max);
    }
    public int getInt(String key, String category, int value, int min, int max, String comment) {
        return (int) get(category,key,value,comment,min,max).getDouble(value);
    }
    public float getFloat(String key, String category, float value, float min, float max, String comment) {
        return (float) get(category,key,value,comment,min,max).getDouble(value);
    }
    public boolean getBoolean(String key, String category, boolean value, String comment) {
        Object found = get(category,key,value,comment).value();
        return found instanceof Boolean b ? b : value;
    }
    public String getString(String key, String category, String value, String comment) {
        Object found = get(category,key,value,comment).value();
        return found instanceof String s ? s : value;
    }
    private static Object listValue(Object value) {
        if (value instanceof double[] array) return Arrays.stream(array).boxed().toList();
        if (value instanceof int[] array) return Arrays.stream(array).boxed().toList();
        if (value instanceof String[] array) return Arrays.asList(array);
        return value;
    }
    public final class Category {
        private final String name;
        Category(String name) { this.name = name; }
        public boolean containsKey(String key) { return data.contains(Arrays.asList(name,key)); }
        public Property get(String key) { return containsKey(key) ? new Property(Arrays.asList(name,key),0.0,-Double.MAX_VALUE,Double.MAX_VALUE) : null; }
        public void remove(String key) { data.remove(Arrays.asList(name,key)); }
        public Collection<Property> values() {
            CommentedConfig category = data.get(name);
            return category == null ? List.of() : category.valueMap().keySet().stream().map(this::get).toList();
        }
    }
    public final class Property {
        private final List<String> path;
        private final Object fallback;
        private final double min, max;
        Property(List<String> path,Object fallback,double min,double max) { this.path=path;this.fallback=fallback;this.min=min;this.max=max; }
        private Object value() { return data.get(path); }
        public double getDouble() { return getDouble(fallback instanceof Number n ? n.doubleValue() : 0); }
        public double getDouble(double defaultValue) {
            Object found=value(); double number=found instanceof Number n ? n.doubleValue() : defaultValue;
            if (!Double.isFinite(number)) number=defaultValue;
            number=Math.max(min,Math.min(max,number)); if(Double.isFinite(number))data.set(path,number); return number;
        }
        public double[] getDoubleList() {
            Object found=value();
            if (!(found instanceof List<?> list)) return fallback instanceof double[] array ? array.clone() : new double[0];
            return list.stream().mapToDouble(entry -> entry instanceof Number n ? n.doubleValue() : Double.NaN).toArray();
        }
        public String[] getStringList() {
            Object found=value();return found instanceof List<?> list ? list.stream().filter(String.class::isInstance).map(String.class::cast).toArray(String[]::new) : new String[0];
        }
        public String getString() { return String.valueOf(value()); }
        public void set(Object value) { data.set(path,listValue(value)); }
        public void setComment(String comment) { if(comment==null||comment.isEmpty())data.removeComment(path);else data.setComment(path,comment); }
    }
}
