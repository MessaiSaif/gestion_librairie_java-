# Gestion de Librairie (Java Spring Boot)

Ce projet est un **Système de Gestion de Librairie** développé en **Java** avec le framework **Spring Boot**. 

Voici une explication détaillée de la structure du projet, de son architecture et des différentes fonctions qu'il propose.

---

## 🛠️ Stack Technologique
- **Framework :** Spring Boot 3.3.4 (Java 21)
- **Base de données :** MySQL intégrée via Spring Data JPA
- **Moteur de template :** Thymeleaf (pour le rendu des pages HTML)
- **Sécurité :** Authentification personnalisée utilisant `jBCrypt` pour le hachage des mots de passe
- **Utilitaires :** Lombok (pour réduire le code répétitif comme les getters et setters)

---

## 📂 Architecture du Projet
Le projet suit l'architecture standard **MVC (Modèle-Vue-Contrôleur)** :

### 1. Modèles (`com.bibliotheque.model`)
Ils représentent les entités de la base de données (tables) et mappent les données :
- **`Utilisateur`** : Représente les utilisateurs, qui peuvent avoir différents rôles comme `admin` ou `membre`.
- **`Livre`** : Représente un livre. Il possède des propriétés comme le titre et le nombre d'exemplaires disponibles (`nombreExemplaires`).
- **`Categorie`** : Représente la catégorie/le genre d'un livre.
- **`Emprunt`** : Représente un emprunt de livre. Il connecte un utilisateur à un livre et trace les dates ainsi que le statut (`en_cours`, `en_retard`, `retourne`).
- **`Reservation`** : Représente une réservation. Utilisée lorsqu'un livre est actuellement indisponible.
- **`Sanction`** : Représente une pénalité financière (amende) appliquée à un utilisateur pour un retour tardif.
- **`Parametre`** : Utilisé pour les configurations dynamiques du système stockées en base de données, comme le montant de l'amende journalière (`amende_journaliere`).

### 2. Repositories (`com.bibliotheque.repository`)
Ce sont des interfaces Spring Data JPA qui génèrent automatiquement les requêtes SQL pour interagir avec la base de données (ex: trouver tous les livres, trouver les emprunts par ID utilisateur, etc.).

### 3. Contrôleurs (`com.bibliotheque.controller`)
Les contrôleurs gèrent les requêtes HTTP et déterminent quelles vues (pages HTML) afficher :
- **`AuthController`** : Gère le processus de connexion et d'inscription.
- **`MemberController`** : Dédié aux membres réguliers de la bibliothèque.
  - **Tableau de bord (Dashboard) :** Les membres peuvent y voir leurs notifications et leurs amendes non payées.
  - **Catalogue & Panier :** Les membres peuvent parcourir les livres, filtrer par catégorie, ajouter des livres à un "panier" basé sur la session, et confirmer leurs emprunts.
  - **Réservations :** Si un livre est indisponible, un membre peut le réserver pour être placé dans une file d'attente.
  - **Emprunts :** Les membres peuvent consulter leurs emprunts actifs.
- **`AdminController`** : Dédié aux administrateurs de la bibliothèque. Gère les opérations CRUD (Créer, Lire, Mettre à jour, Supprimer) pour les livres, catégories, utilisateurs, les retours de livres et les paramètres du système.

### 4. Services (`com.bibliotheque.service`)
Les services contiennent la logique métier de l'application :
- **`AuthService`** : Gère la validation des utilisateurs et le cryptage/vérification des mots de passe.
- **`LibrarySchedulerService`** : Un processus d'arrière-plan (worker) très important. Il utilise l'annotation `@Scheduled` de Spring pour s'exécuter automatiquement toutes les heures et effectuer deux fonctions principales :
  1. **`checkExpiredReservations()`** : Vérifie si un utilisateur notifié n'a pas récupéré son livre réservé dans les 48 heures. Si c'est le cas, la réservation expire et la personne suivante dans la file d'attente est automatiquement notifiée.
  2. **`checkLateLoans()`** : Analyse tous les emprunts actifs. Si la date de retour prévue d'un emprunt est dépassée, son statut passe à `en_retard` et une amende (`Sanction`) est générée ou mise à jour quotidiennement en fonction du paramètre de taux d'amende du système.

---

## 🚀 Résumé du Flux Utilisateur

1. Les **Administrateurs** remplissent le système avec des catégories et des livres, et définissent le montant de l'amende journalière.
2. Les **Membres** se connectent, parcourent le catalogue et ajoutent les livres disponibles à leur panier.
3. Lors de la validation, le système vérifie qu'ils n'ont pas d'amendes impayées. Si tout est en ordre, l'emprunt (`Emprunt`) est créé pour une durée standard (ex: 14 jours).
4. Si un livre n'a plus d'exemplaires disponibles, le membre peut le **Réserver**.
5. En arrière-plan, le **Planificateur (Scheduler)** vérifie continuellement l'heure. Si un membre oublie de rendre un livre, il reçoit une amende dynamique. Une fois le livre rapporté, l'administrateur le marque comme retourné et le système notifie le prochain membre en attente dans la file de réservation.
