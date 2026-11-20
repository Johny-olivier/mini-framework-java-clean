package testframework.controllers;

import framework.annotation.Controller;
import framework.annotation.Mapping;
import framework.annotation.Param;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import testframework.services.ServiceSalutation;

@Controller
public class SpringController {

    @Mapping(url = "/spring")
    public String salutation(ApplicationContext contexte, @Param("nom") String nom) {
        return contexte.getBean(ServiceSalutation.class).saluer(nom);
    }

    @Mapping(url = "/spring/contexte")
    public String contexte(WebApplicationContext contexte) {
        return "Contexte Spring : " + contexte.getBeanDefinitionCount() + " bean(s).";
    }
}
