# DistribFactory

## Présentation

Ce projet vise à modifier et adapter le code fourni du simulateur d'usine pour permettre un simulation distribuée. 

## Usage
Le test du projet a été grandement simplifié. Pour éviter des problèmes de librairies et d'import, des runnables JAR ont été créés. Pas besoin de les chercher.
Pour lancer les différents composants du projet, les scripts suivants (**en racine du repo**):
- ```scripts/run_broker```: Lance le broker
- ```scripts/run_zoo```: Lance Zookeeper
- ```scripts/run_persistence```: Lance le serveur de persistence
- ```scripts/run_app```: Lance l'application
- ```scripts/run_service```: Lance le microservice SpringBoot (actuellement en panne, Eclipse n'a psa envie de compiler une classe dans le .jar pourtant importée dans le microservice ...)

## Problèmes rencontrés

Les seuls problèmes encore obscurs à ce jour sont en lien avec Kafka. Après vérification rapide sur Internet, Kafka est supposé être fonctionnel sur Mac.
J'ai donc effectué les tests sur mon ordianateur personnel. Cependant, j'ai eu affaire à quelques problèmes étranges. Premièrement, en lançant la simulation, il m'est arrivé plusieurs fois d'avoir une simulation accélérée puis subitement un retour à la position de départ et la simulation à une vitesse normale.
Le deuxième problème rencontré consiste à un délai de 5s entre le démarrage de la simulation et un changement à l'écran. Enfin, le dernier problème est une grande difficulté pour lancer le broker, du fait d'un "Node Already Exists", demandant parfois 5-6 redémarrage du ZooKeeper pour réussir à lancer le Broker. Le script en racine du dossier Kafka vise à éteindre proprement les serveurs, mais n'a pas arrangé la situation dans mon cas. 
