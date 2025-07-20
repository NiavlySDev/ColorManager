# ColorManager

Une librairie Java légère et efficace pour créer des gradients de couleurs dans les plugins Minecraft utilisant Kyori Adventure.

## 📋 Description

ColorManager est une librairie spécialement conçue pour les développeurs de plugins Minecraft qui souhaitent créer des effets de gradient sur du texte. Elle utilise l'API Kyori Adventure pour générer des transitions fluides entre les couleurs, permettant de créer des textes visuellement attractifs avec des dégradés personnalisés.

## ✨ Fonctionnalités

- **Génération de gradients** : Création de dégradés fluides entre deux couleurs
- **Application sur texte** : Applique automatiquement un gradient sur chaque caractère d'un texte
- **Palette de couleurs prédéfinies** : 24 couleurs couramment utilisées disponibles via l'énumération `Colors`
- **Support Kyori Adventure** : Compatibilité native avec l'API Adventure pour Minecraft moderne
- **Interpolation linéaire** : Calcul mathématique précis des couleurs intermédiaires
- **API simple** : Une seule méthode publique pour une utilisation facile

## 🎨 Couleurs disponibles

La librairie inclut une énumération `Colors` avec 24 couleurs prédéfinies :

| Couleur | Code HEX | Couleur | Code HEX |
|---------|----------|---------|----------|
| RED | #FF0000 | ORANGE | #FFA500 |
| GREEN | #00FF00 | PINK | #FFC0CB |
| BLUE | #0000FF | PURPLE | #800080 |
| YELLOW | #FFFF00 | BROWN | #A52A2A |
| CYAN | #00FFFF | MAROON | #800000 |
| WHITE | #FFFFFF | LIGHT_BLUE | #ADD8E6 |
| BLACK | #000000 | LIGHT_GREEN | #90EE90 |
| GRAY | #808080 | LIGHT_YELLOW | #FFFFE0 |
| DARK_GRAY | #404040 | LIGHT_PINK | #FFB6C1 |
| LIGHT_GRAY | #C0C0C0 | SILVER | #C0C0C0 |
| LIME | #00FF00 | AQUA | #00FFFF |
| MAGENTA | #FF00FF | LIGHT_PURPLE | #D3D3D3 |

## 🚀 Installation

### Prérequis
- Java 8 ou supérieur
- Kyori Adventure API
- Spigot/Paper/Velocity ou tout serveur supportant Adventure

### Ajout au projet
1. Copiez les fichiers `ColorManager.java` et `Colors.java` dans votre projet
2. Assurez-vous que Kyori Adventure est disponible dans votre classpath

### Dépendance Adventure (Maven)
```xml
<dependency>
    <groupId>net.kyori</groupId>
    <artifactId>adventure-api</artifactId>
    <version>4.14.0</version>
</dependency>
```

## 📖 Utilisation

### Exemple basique
```java
import fr.niavlys.dev.cm.main.ColorManager;
import fr.niavlys.dev.cm.main.Colors;

// Créer un gradient du rouge au bleu
String texteGradient = ColorManager.getText("Bonjour Minecraft !", Colors.RED, Colors.BLUE);

// Envoyer le texte à un joueur (exemple avec Paper)
player.sendMessage(Component.text(texteGradient));
```

### Exemples avec différentes couleurs
```java
// Gradient du jaune au orange
String titre = ColorManager.getText("SERVEUR MINECRAFT", Colors.YELLOW, Colors.ORANGE);

// Gradient du cyan au violet
String message = ColorManager.getText("Bienvenue sur notre serveur !", Colors.CYAN, Colors.PURPLE);

// Gradient subtil avec des tons clairs
String info = ColorManager.getText("Informations importantes", Colors.LIGHT_BLUE, Colors.LIGHT_PINK);
```

### Utilisation dans un plugin Spigot/Paper
```java
@Override
public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (command.getName().equalsIgnoreCase("gradient")) {
        if (args.length >= 1) {
            String texte = String.join(" ", args);
            String gradientText = ColorManager.getText(texte, Colors.RED, Colors.BLUE);
            sender.sendMessage(Component.text(gradientText));
            return true;
        }
    }
    return false;
}
```

## 🔧 Fonctionnement technique

### Algorithme de gradient
La librairie utilise une interpolation linéaire pour calculer les couleurs intermédiaires :

1. **Génération du gradient** : Calcule `n` couleurs entre la couleur de début et de fin (où `n` = longueur du texte)
2. **Interpolation RGB** : Pour chaque étape, interpole les valeurs Rouge, Vert et Bleu séparément
3. **Application au texte** : Applique chaque couleur calculée au caractère correspondant

### Structure du code
```
fr.niavlys.dev.cm.main/
├── ColorManager.java    # Classe principale avec la logique de gradient
└── Colors.java          # Énumération des couleurs prédéfinies
```

### Méthodes principales

#### `ColorManager.getText(String text, Colors start, Colors end)`
- **Paramètres** :
  - `text` : Le texte à coloriser
  - `start` : Couleur de début (énumération Colors)
  - `end` : Couleur de fin (énumération Colors)
- **Retour** : String avec les codes couleur Adventure appliqués

## 🎯 Exemples d'utilisation avancés

### Messages de serveur
```java
// Message de bienvenue avec gradient
String welcome = ColorManager.getText("═══ BIENVENUE SUR NOTRE SERVEUR ═══", Colors.GOLD, Colors.YELLOW);

// Message d'erreur avec gradient rouge
String error = ColorManager.getText("Une erreur est survenue !", Colors.RED, Colors.MAROON);

// Information avec gradient bleu
String info = ColorManager.getText("Nouveau joueur connecté !", Colors.LIGHT_BLUE, Colors.BLUE);
```

### Interface utilisateur
```java
// Titre de GUI avec gradient
String guiTitle = ColorManager.getText("⚔ BOUTIQUE PREMIUM ⚔", Colors.PURPLE, Colors.MAGENTA);

// Boutons avec différents gradients
String buttonBuy = ColorManager.getText("ACHETER", Colors.GREEN, Colors.LIME);
String buttonCancel = ColorManager.getText("ANNULER", Colors.RED, Colors.PINK);
```

## ⚠️ Limitations actuelles

- Les paramètres `bold` et `italic` dans les méthodes privées ne sont pas encore implémentés
- Seules les couleurs de l'énumération `Colors` sont supportées (pas de couleurs HEX personnalisées)
- Pas de support pour les gradients multi-couleurs (seulement 2 couleurs)

## 🛠️ Améliorations possibles

- Support des couleurs HEX personnalisées
- Ajout du support pour les styles (gras, italique, souligné)
- Gradients avec plus de 2 couleurs
- Gradients radiaux ou non-linéaires
- Cache des gradients pour de meilleures performances

## 📄 Licence

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

## 🔗 Liens utiles

- [Repository GitHub](https://github.com/NiavlySDev/ColorManager)
- [Documentation Kyori Adventure](https://docs.adventure.kyori.net/)
- [Spigot API Documentation](https://hub.spigotmc.org/javadocs/spigot/)

## 👥 Auteur

- **NiavlySDev** - *Développeur principal* - [@NiavlySDev](https://github.com/NiavlySDev)

## 🙏 Remerciements

- L'équipe Kyori pour l'API Adventure
- La communauté Minecraft pour l'inspiration
- Les développeurs de plugins utilisant cette librairie
