package client;

import com.google.gson.Gson;
import model.*;
import server.Server;

import java.io.*;
import java.net.Socket;
import java.util.Random;
import java.util.UUID;

/**
 * Ovaj klijent je dizajniran da otvori jednu konekciju i da radi sve vreme na njoj.
 * I to je za domaci prihvatljiv pristup.
 *
 * Ovde je kao primer uradjen zahtev da se sedne za sto. Ako ne sedne za sto ova nit ce zavrsiti sa radom, sto je ok po specifikaciji.
 */
public class Client implements Runnable
{
    private Socket socket;
    private BufferedWriter out;
    private BufferedReader in;
    private Gson gson;              // Jedna od mnogobrojnih biblioteka za rad sa JSON

    private String id;              // Definise jedinstveni identifikator za korisnika
    private Boolean seated;         // Definise da li je klijent sedi za stolom
    private Boolean alive;         // Definise da li je klijent ziv

    public Client() throws IOException
    {
        this.seated = false;
        this.alive = true;
        this.gson = new Gson();
        this.id = UUID.randomUUID().toString();

        this.startConnection();
    }

    public void run()
    {
        requestChair();

        if(!this.seated)
        {
            try {
                this.closeConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        while (true)
        {
            try {
                String requestStr = in.readLine();
                Message request = gson.fromJson(requestStr, Message.class);
                Intent serverIntent = request.getIntent();

                switch (serverIntent) {
                    case DRAW:
                        this.draw(request);
                        break;
                    case BET:
                        this.makeBet();
                        break;
                    case GAME_OVER:
                        this.lost();
                        break;
                    case WON:
                        this.won();
                        break;
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(!this.alive) {
                    break;
                }
            }

        }
        System.out.println("Client <" + this.id + "> has been turned off");
    }

    private void startConnection() throws IOException
    {
        this.socket = new Socket(Server.HOST, Server.PORT);
        this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    private void closeConnection() throws IOException
    {
        this.in.close();
        this.out.close();
        this.socket.close();
    }


    private void requestChair()
    {
        try {
            Message message = new Message();
            message.setId(this.id);
            message.setIntent(Intent.REQUEST_CHAIR);
            String convertedMessage = gson.toJson(message);

            out.write(convertedMessage);
            out.newLine();
            out.flush();

            String responseStr = in.readLine();
            Response response = gson.fromJson(responseStr, Response.class); // Ovde navodimo u sta tekst treba da se konvertuje

            if(response.getStatus().equals(Status.OK)){
                this.seated = true;
                // Uradite nesto sa klijentom koji je seo ...
                System.out.println(this.toString() + " has taken the chair");
            } else {
                this.seated = false;
                System.out.println(this.toString() + " wasn't been able to take the chair");
            }

            /*
             Ovaj sleep sluzi samo da se zadrzi malo konekcija.
             Ovo moze da vam sluzi da isproveravate da li server moze da opsluzi sve odjednom.
              */

            Thread.sleep(1000);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void makeBet() throws IOException
    {
        StickType choice = null;
        Random r = new Random();
        int index = r.nextInt(2);
        if(index == 1) choice = StickType.NORMAL;
        else choice = StickType.SHORT;
        Response response = new Response();
        response.setData(choice.toString());
        this.send(gson.toJson(response));
    }

    private void draw(Message request) throws IOException
    {
        int sticksCount = Integer.valueOf(request.getOptions());
        System.out.println("Imam opcija " + sticksCount);

        Random r = new Random();
        int index = r.nextInt(sticksCount);

        Response response = new Response();
        response.setData(String.valueOf(index));
        this.send(gson.toJson(response));
    }

    private void send(String message) throws IOException
    {
        this.out.write(message);
        this.out.newLine();
        this.out.flush();
    }


    private void lost()
    {
        this.alive = false;
        System.out.println("I lost " + this.id);
        try {
            this.closeConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void won()
    {
        System.out.println("I won " + this.id);
        this.alive = false;
        try {
            this.closeConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "Client " + this.id;
    }
}

