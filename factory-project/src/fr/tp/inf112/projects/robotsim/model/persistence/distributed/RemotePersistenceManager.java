package fr.tp.inf112.projects.robotsim.model.persistence.distributed;

import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasChooser;
import fr.tp.inf112.projects.canvas.model.impl.AbstractCanvasPersistenceManager;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class RemotePersistenceManager extends AbstractCanvasPersistenceManager {

    public RemotePersistenceManager(CanvasChooser canvasChooser) {
        super(canvasChooser);
    }

    @Override
    public Canvas read(final String canvasId) throws IOException {
        try {
            Socket serverSocket = new Socket(InetAddress.getLocalHost(), 9999);

            OutputStream outputStream = serverSocket.getOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(canvasId);

            InputStream inputStream = serverSocket.getInputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            Canvas canvas = (Canvas) objectInputStream.readObject();

            serverSocket.close();

            return canvas;
        }
        catch (IOException | ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void persist(Canvas canvasModel) throws IOException {
        try {
            Socket serverSocket = new Socket(InetAddress.getLocalHost(), 9999);

            OutputStream outputStream = serverSocket.getOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(canvasModel);

            serverSocket.close();
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean delete(Canvas canvasModel) throws IOException {
        return true;
    }

}
