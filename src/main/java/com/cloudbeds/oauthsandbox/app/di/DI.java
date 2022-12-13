package com.cloudbeds.oauthsandbox.app.di;

import com.cloudbeds.oauthsandbox.app.App;
import com.cloudbeds.oauthsandbox.app.AppContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXMLLoader;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;

import java.util.HashMap;
import java.util.Map;

import java.util.Objects;
import java.util.prefs.Preferences;

public class DI {
    static Logger log = LoggerFactory.getLogger(DI.class.getName());

    public static final AppContext context = new AppContext();
    public static Map<Class,Object> singletons = new HashMap<>();

    public static <T> T create(Class<T> type) {
        try {
            T out;
            if (Config.class.isAssignableFrom(type)) {
                out = (T)ConfigFactory.create((Class<Config>)type);
            } else {
                out = type.getConstructor().newInstance();
            }
            applyContext(out);
            return out;
        } catch (Exception ex) {
            throw new RuntimeException("No no-arg constructor found for class " + type,  ex);
        }
    }

    public static <T> T get(Class<T> type) {
        if (type.getAnnotation(Singleton.class) != null) {
            for (Field field : context.getClass().getDeclaredFields()) {
                if (type.isAssignableFrom(field.getType())) {
                    try {
                        field.setAccessible(true);
                        Object out = field.get(context);
                        if (out == null) {
                            field.set(context, create(type));
                            out = field.get(context);
                        }
                        return (T)out;
                    } catch (Exception ex) {
                        log.warn("Failed to get field "+field.getName()+" from context");
                    }
                }
            }
            synchronized (DI.class) {
                if (singletons.containsKey(type)) {
                    return (T)singletons.get(type);
                } else {
                    singletons.put(type, create(type));
                    return (T)singletons.get(type);
                }
            }
        } else {
            return create(type);
        }
    }

    public static void applyContext(Object dest) {
        Map localSingletons;
        synchronized (DI.class) {
            localSingletons = new HashMap(singletons);
        }
        outer: for (Field destField : dest.getClass().getDeclaredFields()) {
            if (destField.getAnnotation(Preference.class) != null) {
                Preferences prefs = Preferences.userNodeForPackage(dest.getClass());
                String val = prefs.get(dest.getClass().getSimpleName()+"."+destField.getName(), null);
                if (val != null) {
                    destField.setAccessible(true);
                    if (destField.getType() == String.class) {
                        try {
                            log.debug("Loading field from preferences: {}, value {}", destField, val);
                            destField.set(dest, val);
                        } catch (Exception ex) {
                            log.warn(
                                    "Failed to extract field {} from preferences for class {}",
                                    destField,
                                    dest.getClass()
                            );
                        }
                    } else {
                        try {
                            log.debug("Loading field from preferences: {}, value {}", destField, val);
                            destField.set(dest, new ObjectMapper().readValue(val, destField.getType()));
                        } catch (Exception ex) {
                            log.warn(
                                    "Failed to extract field {} from preferences for class {}",
                                    destField,
                                    dest.getClass()
                            );
                        }
                    }
                }
            }

            if (destField.getAnnotation(Inject.class) == null) {
                continue;
            }

            inner: for (Field contextField : context.getClass().getDeclaredFields()) {
                if (destField.getType().isAssignableFrom(contextField.getType())) {
                    contextField.setAccessible(true);
                    destField.setAccessible(true);
                    try {
                        destField.set(dest, contextField.get(context));
                        continue outer;
                    } catch (Exception ex) {
                        log.error("Failed to assign field "+destField.getName()+" in class " + dest.getClass(), ex);
                    }
                }
            }
            if (destField.getType().getAnnotation(Singleton.class) != null) {
                try {
                    destField.setAccessible(true);
                    destField.set(dest, get(destField.getType()));
                } catch (Exception ex) {
                    log.error("Failed to assign field " + destField.getName() + " in class " + dest.getClass(), ex);
                }
            }
        }

    }

    public static FXMLLoader loadFXML(String resourcePath) {
        return loadFXML(App.class, resourcePath);
    }
    public static FXMLLoader loadFXML(Class source, String resourcePath) {
        if (source == null) source = App.class;
        FXMLLoader fxmlLoader = new FXMLLoader(source.getResource(resourcePath));
        fxmlLoader.setControllerFactory(type -> get(type));

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        return fxmlLoader;
    }

    public static void savePreferences(Object dest) {
        Preferences prefs = Preferences.userNodeForPackage(dest.getClass());
        boolean changed = false;
        for (Field destField : dest.getClass().getDeclaredFields()) {
            if (destField.getAnnotation(Preference.class) != null) {

                String val = prefs.get(dest.getClass().getSimpleName() + "." + destField.getName(), null);
                destField.setAccessible(true);
                try {
                    Object newVal = destField.get(dest);
                    String key = dest.getClass().getSimpleName()+"."+destField.getName();
                    if (!Objects.equals(val, newVal)) {
                        changed = true;
                        if (newVal == null) {
                            prefs.remove(key);
                        } else if (newVal instanceof String) {
                            prefs.put(key, (String) newVal);
                        } else {
                            prefs.put(key, new ObjectMapper().writeValueAsString(newVal));
                        }
                    }
                } catch (Exception ex) {
                    log.warn("Failed to extract value for field {} in class {}", destField, dest);
                }
                /*
                if (val != null) {

                    if (destField.getType() == String.class) {
                        try {
                            destField.set(dest, val);
                        } catch (Exception ex) {
                            log.warn(
                                    "Failed to extract field {} from preferences for class {}",
                                    destField,
                                    dest.getClass()
                            );
                        }
                    } else {
                        try {
                            destField.set(dest, new ObjectMapper().readValue(val, destField.getType()));
                        } catch (Exception ex) {
                            log.warn(
                                    "Failed to extract field {} from preferences for class {}",
                                    destField,
                                    dest.getClass()
                            );
                        }
                    }
                }*/
            }
        }
        if (changed) {
            try {
                prefs.flush();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
