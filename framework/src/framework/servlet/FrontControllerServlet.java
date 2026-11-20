package framework.servlet;

import framework.annotation.ResponseBody;
import framework.binding.MauvaisParametreException;
import framework.binding.ResolveurArguments;
import framework.listener.InitialiseurApplication;
import framework.utils.AssistantReponse;
import framework.utils.Chemins;
import framework.utils.ConfigurationFramework;
import framework.utils.RegistreRoutes;
import framework.utils.RouteInfo;
import framework.web.ModelAndView;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

public class FrontControllerServlet extends HttpServlet {

    private RegistreRoutes registre;
    private final ResolveurArguments resolveur = new ResolveurArguments();
    private String prefixeVues = "";
    private String suffixeVues = "";

    @Override
    public void init() throws ServletException {
        ServletContext contexte = getServletContext();
        prefixeVues = ConfigurationFramework.lire(contexte, ConfigurationFramework.PREFIXE_VUES, "");
        suffixeVues = ConfigurationFramework.lire(contexte, ConfigurationFramework.SUFFIXE_VUES, "");

        registre = (RegistreRoutes) contexte.getAttribute(InitialiseurApplication.ATTRIBUT_ROUTES);
        if (registre == null) {
            // Repli : le listener n'est pas déclaré dans web.xml.
            contexte.log("Framework : listener absent, initialisation des routes dans le servlet.");
            registre = InitialiseurApplication.initialiser(contexte);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        traiter(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        traiter(request, response);
    }

    private void traiter(HttpServletRequest requete, HttpServletResponse reponse)
            throws IOException, ServletException {
        String chemin = Chemins.depuisRequete(requete);
        RouteInfo route = registre.trouver(requete.getMethod(), chemin);

        if (route == null) {
            Set<String> methodes = registre.methodesAutorisees(chemin);
            if (methodes.isEmpty()) {
                AssistantReponse.ecrireNonTrouve(reponse, registre, chemin);
            } else {
                AssistantReponse.ecrireMethodeNonAutorisee(reponse, chemin, methodes);
            }
            return;
        }

        invoquer(route, requete, reponse);
    }

    private void invoquer(RouteInfo route, HttpServletRequest requete, HttpServletResponse reponse)
            throws IOException, ServletException {
        Method methode = route.getMethod();
        try {
            Object instance = route.getController().getDeclaredConstructor().newInstance();
            Object resultat = methode.invoke(instance, resolveur.resoudre(methode, requete));
            ecrireResultat(methode, resultat, requete, reponse);
        } catch (MauvaisParametreException e) {
            AssistantReponse.ecrireRequeteInvalide(reponse, e.getMessage());
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            getServletContext().log("Erreur dans " + nommer(route), cause);
            AssistantReponse.ecrireErreurInterne(reponse);
        } catch (ReflectiveOperationException e) {
            getServletContext().log("Impossible d'invoquer " + nommer(route), e);
            AssistantReponse.ecrireErreurInterne(reponse);
        }
    }

    private void ecrireResultat(Method methode, Object resultat, HttpServletRequest requete,
            HttpServletResponse reponse) throws IOException, ServletException {
        if (methode.isAnnotationPresent(ResponseBody.class)) {
            AssistantReponse.ecrireJson(reponse, resultat);
            return;
        }

        if (resultat instanceof ModelAndView) {
            ModelAndView vue = (ModelAndView) resultat;
            for (Map.Entry<String, Object> attribut : vue.getAttributes().entrySet()) {
                requete.setAttribute(attribut.getKey(), attribut.getValue());
            }
            requete.getRequestDispatcher(prefixeVues + vue.getUrl() + suffixeVues).forward(requete, reponse);
            return;
        }

        if (resultat != null) {
            AssistantReponse.ecrireTexte(reponse, resultat.toString());
        }
    }

    private String nommer(RouteInfo route) {
        return route.getController().getName() + "#" + route.getMethod().getName();
    }
}
