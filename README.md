# Guidini - Guide Touristique Tunisie

Application mobile Android développée dans le cadre de l'examen TP (Année universitaire 2026-2027, ISET Sidi Bouzid).

## Description du Projet
**Guidini** est un guide touristique intelligent de la Tunisie qui permet de découvrir les sites historiques, plages, déserts et monuments tunisiens (Sidi Bou Said, Carthage, El Djem, Djerba, Sbeïtla, etc.) avec photos, régions, catégories, carte interactive et météo en direct.

## Fonctionnalités Principales (Cahier des Charges)
- **F1 : Liste des lieux** : Affichage sous forme de liste (`RecyclerView`) avec nom, image, région et catégorie.
- **F2 : Recherche et filtres** : Recherche rapide par nom et filtrage par catégorie (Historique, Plage, Désert, Médina, etc.).
- **F3 : Fiche détaillée** : Vue détaillée de chaque site avec photo, description, région et position.
- **F4 : Carte interactive** : Affichage des marqueurs des sites touristiques (via osmdroid / OpenStreetMap).
- **F5 : Météo en direct** : Consultation de la météo pour chaque destination.
- **F6 : Favoris** : Gestion des favoris avec stockage local (`Room`).
- **F7 : Événements** : Liste des festivals et événements culturels en Tunisie.
- **F8 : Itinéraire et partage** : Ouverture d'itinéraires et partage de lieux.
- **F9 : Navigation** : Navigation fluide et intuitive.

## Technologies Utilisées
- **Langage** : Java
- **UI / Design** : XML, Material Design, RecyclerView, CardView
- **Chargement d'images** : Glide (`com.github.bumptech.glide:glide`)
- **Cartographie** : osmdroid (`org.osmdroid:osmdroid-android`)
- **Base de données / Stockage** : Room (`androidx.room`)
- **Réseau / API** : Retrofit & Gson (`com.squareup.retrofit2:retrofit`)

## Génération de l'APK
Pour générer l'APK installable depuis Android Studio :
1. Dans le menu supérieur, allez sur **Build** > **Build Bundle(s) / APK(s)** > **Build APK(s)**.
2. Une fois la compilation terminée, cliquez sur le lien **locate** pour récupérer le fichier `app-debug.apk`.

## Dépôt GitHub
Le projet est structuré et prêt à être publié sur GitHub avec un historique de commits progressif.
