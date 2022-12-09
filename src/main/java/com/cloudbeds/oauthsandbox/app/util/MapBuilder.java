package com.cloudbeds.oauthsandbox.app.util;

import java.util.HashMap;
import java.util.Map;

public class MapBuilder<K,V> {
    private Map<K,V> map = new HashMap<>();
    public MapBuilder put(K key, V value) {
        map.put(key, value);
        return this;
    }

    public Map<K,V> build() {
        Map<K,V> out = map;
        map = null;
        return out;
    }
}
