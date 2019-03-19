package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server ce prihvatati konekcije i prosledjivati ih
 * instancama klase ServerThread.
 */
public class Server implements Runnable
{
    public static final int PORT = 9393;
    public static final String HOST = "localhost";


    private ServerSocket serverSocket;
    private Croupier croupier;

    public Server() throws IOException {
        this.serverSocket = new ServerSocket(Server.PORT);
        this.croupier = new Croupier();
    }


    public void run()
    {
        while (true)
        {
            try {
                Socket socket = serverSocket.accept();
                ServerThread serverThread = new ServerThread(socket, this.croupier);
                new Thread(serverThread).start();


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
