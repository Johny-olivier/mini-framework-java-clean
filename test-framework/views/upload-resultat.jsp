<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<body>
<h1>Fichier reçu</h1>

<dl>
    <dt>Nom</dt>
    <dd>${employe.nom}</dd>
    <dt>Poste</dt>
    <dd>${employe.poste}</dd>
    <dt>Fichier</dt>
    <dd>${fichier.nom}</dd>
    <dt>Type</dt>
    <dd>${fichier.typeContenu}</dd>
    <dt>Taille</dt>
    <dd>${fichier.taille}</dd>
    <dt>Enregistré dans</dt>
    <dd>${chemin}</dd>
</dl>

<p><a href="${pageContext.request.contextPath}/upload">Nouvel envoi</a></p>
</body>
</html>
