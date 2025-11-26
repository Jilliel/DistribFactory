# DistribFactory

## Présentation

Ce projet vise à modifier et adapter le code fourni du simulateur d'usine pour permettre un simulation distribuée. 

## Usage
Trois dossiers:
- Kafka: Contient le dossier Kafka donné dans le sujet. Le démarrage de ZooKeeper et du Broker sont détaillés dans le sujet. Pour éteindre les serveurs proprement et vider les logs:
```
./stop.sh
``` 
- Factory-project: Contient le projet modifié de l'usine. 
 - Pour lancer le serveur de persistence, voir ```/fr.tp.inf112.projects.robotsim/src/fr/tp/inf112/projects/robotsim/model/persistence/distributed/ServerPersistenceManager.java```	
 - Pour lancer l'application`, voir ```/fr.tp.inf112.projects.robotsim/src/fr/tp/inf112/projects/robotsim/app/SimulatorApplication.java``` 

## Problèmes rencontrés

Le seul problème encore obscur à ce jour est en lien avec Kafka. Après vérification rapide sur Internet, Kafka est supposé être fonctionnel sur Mac.
J'ai donc effectué les tests sur mon ordianateur personnel. Cependant, j'ai eu affaire à quelques problèmes étranges. Premièrement, en lançant la simulation, il m'est arrivé plusieurs fois d'avoir une simulation accélérée puis subitement un retour à la position de départ et la simulation à une vitesse normale.
Le deuxième problème rencontré consiste à un délai de 5s entre le démarrage de la simulation et un changement à l'écran. Enfin, le dernier problème est une grande difficulté pour lancer le broker, du fait d'un "Node Already Exists", demandant parfois 5-6 redémarrage du ZooKeeper pour réussir à lancer le Broker. Le script en racine du dossier Kafka vise à éteindre proprement les serveurs, mais n'a pas arrangé la situation dans mon cas. 
