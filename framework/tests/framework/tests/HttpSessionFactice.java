package framework.tests;

import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public final class HttpSessionFactice {

    private HttpSessionFactice() {
    }

    public static HttpSession creer(Map<String, Object> attributs) {
        Map<String, Object> stockage = attributs == null ? new HashMap<>() : new HashMap<>(attributs);
        return (HttpSession) java.lang.reflect.Proxy.newProxyInstance(
                HttpSession.class.getClassLoader(),
                new Class<?>[] {HttpSession.class},
                (proxy, methode, arguments) -> {
                    switch (methode.getName()) {
                        case "getAttribute":
                            return stockage.get((String) arguments[0]);
                        case "setAttribute":
                            stockage.put((String) arguments[0], arguments[1]);
                            return null;
                        case "removeAttribute":
                            stockage.remove((String) arguments[0]);
                            return null;
                        case "getAttributeNames":
                            return Collections.enumeration(stockage.keySet());
                        case "getId":
                            return "SESSION-TEST";
                        case "toString":
                            return "HttpSessionFactice" + stockage;
                        case "hashCode":
                            return System.identityHashCode(proxy);
                        case "equals":
                            return proxy == arguments[0];
                        default:
                            throw new UnsupportedOperationException(methode.getName());
                    }
                });
    }
}
