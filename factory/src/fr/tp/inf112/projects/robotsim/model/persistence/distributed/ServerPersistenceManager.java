package fr.tp.inf112.projects.robotsim.model.persistence.distributed;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

import fr.tp.inf112.projects.robotsim.app.SimulatorApplication;

public class ServerPersistenceManager implements Runnable{

    private final ServerSocket serverSocket;
    
    private static final Logger LOGGER = Logger.getLogger(ServerPersistenceManager.class.getName());
    
    public ServerPersistenceManager() throws IOException {
        this.serverSocket = new ServerSocket(9999);
    }

    @Override
    public void run() {
    	LOGGER.info("Démarrage du serveur de persistence.");
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                LOGGER.info("Nouvelle requête reçue, lancement d'un handler.");
                ServerPersistenceProcess process = new ServerPersistenceProcess(clientSocket);
                Thread processorThread = new Thread(process);
                processorThread.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        final ServerPersistenceManager serverManager = new ServerPersistenceManager();
        final Thread serverThread = new Thread(serverManager);
        serverThread.start();
    }
}
