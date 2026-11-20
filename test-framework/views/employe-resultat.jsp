<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<body>
<h1>Employé enregistré</h1>

<dl>
    <dt>Nom</dt>
    <dd>${employe.nom}</dd>
    <dt>Poste</dt>
    <dd>${employe.poste}</dd>
    <dt>Salaire</dt>
    <dd>${employe.salaire}</dd>
    <dt>Actif</dt>
    <dd>${employe.actif ? 'oui' : 'non'}</dd>
    <dt>Rue</dt>
    <dd>${employe.adresse.rue}</dd>
    <dt>Ville</dt>
    <dd>${employe.adresse.ville}</dd>
</dl>

<p><a href="${pageContext.request.contextPath}/employe">Nouveau formulaire</a></p>
</body>
</html>
