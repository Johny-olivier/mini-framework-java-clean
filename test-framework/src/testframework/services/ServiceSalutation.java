package testframework.services;

import org.springframework.stereotype.Service;

@Service
public class ServiceSalutation {

    public String saluer(String nom) {
        if (nom == null || nom.isBlank()) {
            return "Bonjour inconnu";
        }
        return "Bonjour " + nom;
    }
}
