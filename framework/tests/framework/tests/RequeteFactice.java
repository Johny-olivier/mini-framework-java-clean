package framework.tests;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Map;

/**
 * Fausse requête HTTP : seuls les accès aux paramètres sont implémentés.
 */
public final class RequeteFactice {

    private RequeteFactice() {
    }

    public static HttpServletRequest creer(Map<String, String> parametres) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[] {HttpServletRequest.class},
                (proxy, methode, arguments) -> {
                    switch (methode.getName()) {
                        case "getParameter":
                            return parametres.get((String) arguments[0]);
                        case "getParameterNames":
                            return Collections.enumeration(parametres.keySet());
                        case "toString":
                            return "RequeteFactice" + parametres;
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
