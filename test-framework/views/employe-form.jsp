<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<body>
<h1>Nouvel employé</h1>

<form method="post" action="${pageContext.request.contextPath}/employe">
    <p>
        <label for="nom">Nom</label>
        <input id="nom" type="text" name="nom">
    </p>
    <p>
        <label for="poste">Poste</label>
        <input id="poste" type="text" name="poste">
    </p>
    <p>
        <label for="salaire">Salaire</label>
        <input id="salaire" type="number" step="0.01" name="salaire">
    </p>
    <p>
        <label for="actif">Actif</label>
        <input id="actif" type="checkbox" name="actif" value="true">
    </p>
    <p>
        <label for="rue">Rue</label>
        <input id="rue" type="text" name="adresse.rue">
    </p>
    <p>
        <label for="ville">Ville</label>
        <input id="ville" type="text" name="adresse.ville">
    </p>
    <p>
        <button type="submit">Enregistrer</button>
    </p>
</form>
</body>
</html>
