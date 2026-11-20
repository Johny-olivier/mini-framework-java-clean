<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<body>
<h1>Envoyer un fichier</h1>

<form method="post" action="${pageContext.request.contextPath}/upload" enctype="multipart/form-data">
    <p>
        <label for="nom">Nom</label>
        <input id="nom" type="text" name="nom">
    </p>
    <p>
        <label for="poste">Poste</label>
        <input id="poste" type="text" name="poste">
    </p>
    <p>
        <label for="fichier">Fichier</label>
        <input id="fichier" type="file" name="fichier">
    </p>
    <p>
        <button type="submit">Envoyer</button>
    </p>
</form>
</body>
</html>
