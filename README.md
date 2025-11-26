# DistribFactory

## Présentation

Ce projet vise à modifier et adapter le code fourni du simulateur d'usine pour permettre un simulation distribuée. 

## Usage
Le test du projet est facilité. Pour éviter des problèmes de librairies et d'imports, des runnables JAR ont été créés. Pas besoin de les chercher. Pour lancer les différents composants du projet, les scripts suivants (**À exécuter en racine du projet**) sont disponibles:
| Script                      | Fonction                                                                                    |
|-----------------------------|---------------------------------------------------------------------------------------------|
| `scripts/run_broker.sh`     | Lance le broker                                                                             |
| `scripts/run_zoo.sh`        | Lance Zookeeper                                                                             |
| `kafka/stop.sh`             | Arrête proprement Zookeeper et le Broker puis efface les logs                               |
| `scripts/run_persistence.sh`| Lance le serveur de persistence                                                             |
| `scripts/run_app.sh`        | Lance l'application                                                                         |
| `scripts/run_service.sh`    | Lance le microservice SpringBoot                                                                                 |


## Problèmes rencontrés

Les seuls problèmes encore obscurs à ce jour sont en lien avec Kafka. Après vérification rapide sur Internet, Kafka est supposé être fonctionnel sur Mac. J'ai donc effectué les tests sur mon ordianateur personnel.
- Le premier problème survient parfois en lançant une simulation. il m'est arrivé plusieurs fois d'avoir une simulation accélérée puis subitement un retour à la position de départ et la simulation à une vitesse normale.
- Le second problème est un délai de 0-5s entre le démarrage de la simulation et un changement à l'écran.
- Le dernier problème est une grande difficulté pour lancer le Broker, du fait d'un *Node Already Exists*, demandant parfois 5-6 redémarrages du ZooKeeper pour réussir à lancer le Broker. Le script `stop.sh` dans le dossier Kafka vise à éteindre proprement les serveurs, mais n'a pas solutionné la situation dans mon cas. 

**Hypothèse**: Kafka possède un buffer interne avec les messages et ce buffer est mal géré sur mon ordinateur.
