package fr.tp.inf112.projects.robotsim.model.persistence.distributed;

import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.persistence.local.FactoryPersistenceManager;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerPersistenceProcess extends FactoryPersistenceManager implements Runnable {

    private final Socket clientSocket;
    
    private static final Logger LOGGER = Logger.getLogger(ServerPersistenceManager.class.getName());
    
    public ServerPersistenceProcess(Socket clientSocket) {
        super(null);
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() throws RuntimeException{
    	LOGGER.info("Handler créé.");
        try {
            InputStream inputStream = clientSocket.getInputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            OutputStream outputStream = clientSocket.getOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

            Object object = objectInputStream.readObject();
            if (object instanceof String canvasId) {
            	LOGGER.info("Requête read reconnue, envoie commencé.");
                Canvas canvas = read(canvasId);
                objectOutputStream.writeObject(canvas);
            } else if (object instanceof Canvas canvas) {
            	LOGGER.info("Requête persist reconnue, enregistrement commencé.");
                persist(canvas);
            } else {
            	LOGGER.log(Level.WARNING, "Requête inconnue");
            }
            clientSocket.close();
        }
        catch (IOException | ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }
}
