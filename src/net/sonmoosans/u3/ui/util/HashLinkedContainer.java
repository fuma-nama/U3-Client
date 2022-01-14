package net.sonmoosans.u3.ui.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.socket.client.Socket;
import net.sonmoosans.u3.ui.AddableComponent;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.sonmoosans.u3.api.core.APICaller.mapper;
import static net.sonmoosans.u3.ui.util.CommonUtil.repaintContainer;

public class HashLinkedContainer<K extends Key<M>, V extends AddableComponent, M> {
    private final HashMap<M, V> componentMap = new HashMap<>();
    private HashMap<M, K> pool;
    private final Container base;
    private final Function<K, V> keyToValueMapper;

    public HashLinkedContainer(Container base, Function<K, V> keyToValueMapper) {
        this.base = base;
        this.keyToValueMapper = keyToValueMapper;
    }

    public Component add(K key) throws NullPointerException {
        if (keyToValueMapper == null) throw new NullPointerException("mapper is null");

        V component = keyToValueMapper.apply(key);
        return add(key, component);
    }

    public Component add(K key, V component) {
        if (component == null) return null;

        M mapKey = key.getKey();

        componentMap.put(mapKey, component);

        if (pool != null)
            pool.put(mapKey, key);

        return base.add(component.getComponent());
    }

    @Nullable
    public V get(K key) {
        return get(key.getKey());
    }

    @Nullable
    public V get(M key) {
        return componentMap.getOrDefault(key, null);
    }

    public boolean containsKey(K key) {
        return containsKey(key.getKey());
    }

    public boolean containsKey(M key) {
        return componentMap.containsKey(key);
    }

    public void getIfPresent(K key, Consumer<V> consumer) {
        getIfPresent(key.getKey(), consumer);
    }

    public void getIfPresent(M key, Consumer<V> consumer) {
        componentMap.computeIfPresent(key, (k, v)-> {
            consumer.accept(v);
            return v;
        });
    }

    public V remove(K key) {
        return remove(key.getKey());
    }

    public V remove(M key) {
        V component = componentMap.remove(key);

        if (component != null) {
            if (pool != null)
                pool.remove(key);

            base.remove(component.getComponent());

            repaintContainer(base);
        }

        return component;
    }

    public void clear() {
        componentMap.clear();
        base.removeAll();
    }

    public HashMap<M, V> getMap() {
        return componentMap;
    }

    public void linkMemory(HashMap<M, K> pool) {
        this.pool = pool;
    }

    public void linkTo(Socket socket, String addEvent, String removeEvent, Class<?> type) throws NullPointerException {
        linkTo(socket, addEvent, removeEvent, keyToValueMapper, (Class<K>) type, (Class<M>) type);
    }

    public void linkTo(Socket socket, String addEvent, String removeEvent, Class<K> addType, Class<M> removeType) throws NullPointerException {
        linkTo(socket, addEvent, removeEvent, keyToValueMapper, addType, removeType);
    }

    public void linkTo(Socket socket, String addEvent, String removeEvent, Function<K, V> keyToValueMapper, Class<K> addType, Class<M> removeType) throws NullPointerException {
        if (keyToValueMapper == null) throw new NullPointerException("mapper is null");

        socket.on(addEvent, args -> {
            try {
                K key = mapper.readValue(args[0].toString(), addType);
                add(key, keyToValueMapper.apply(key));
                onSocketAdd(key);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        socket.on(removeEvent, args -> {
            try {
                M key = mapper.readValue(args[0].toString(), removeType);
                remove(key);
                repaintContainer(base);
                onSocketRemove(key);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    protected void onSocketAdd(K key) {

    }

    protected void onSocketRemove(M key) {

    }
}
