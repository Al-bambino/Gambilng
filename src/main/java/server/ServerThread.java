package server;

import com.google.gson.Gson;
import model.*;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BrokenBarrierException;

/**
 * Ova nit ce se baviti obavljanjem same komunikacije
 * izmedju klijentskog i serverskog soketa.
 */
public class ServerThread implements Runnable
{
    private Socket socket;
    private BufferedWriter out;
    private BufferedReader in;

    private Gson gson;              // Jedna od mnogobrojnih biblioteka za rad sa JSON
    private Croupier croupier;
    private int score;
    private int chairNumber;
    private boolean isAlive;
    private String clientId;

    private StickType chosenStickType;
    private int chosenStickIndex;


    public ServerThread(Socket socket, Croupier croupier) throws IOException
    {
        this.isAlive = true;
        this.socket = socket;
        this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.croupier = croupier;
        this.gson = new Gson();
        this.score = 0;
    }

    public void run()
    {
        boolean chaired = this.chairRequested();

        if(!chaired)
        {
            try {
                this.out.close();
                this.in.close();
                this.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        System.out.println("Seat " + this.chairNumber + " has someone");

        this.playGame();
        this.won();

    }

    private void playGame()
    {
        while (this.croupier.gameIsActive())
        {
            try {

                this.croupier.getStartBarrier().await();

                if(this.isDrawing())
                    this.requestDraw();
                else
                    this.requestBet();

                this.croupier.getBetBarrier().await();

                this.updateScore();

                this.croupier.getEndBarrier().await();

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            } catch (IOException e) {
                // Todo
                e.printStackTrace();
            } finally {
                if(!this.isAlive) {
                    this.shutDown();
                    return;
                }
            }
        }
    }

    // Korisnik zahteva stolicu, probaj da mu je das. U suprotnom objasni mu da nije uspeo.
    private boolean chairRequested()
    {
        try {
            String requestStr = in.readLine();
            Message request = gson.fromJson(requestStr, Message.class); // Ovde navodimo u sta tekst treba da se konvertuje

            this.chairNumber = this.croupier.giveSeat(new User(request.getId()));
            System.out.println("Stigao zahtev za stolicu od korisnika " + request.getId());
            this.clientId = request.getId();

            Response response = new Response();

            if(this.chairNumber == -1)
            {
                response.setStatus(Status.DENIED);
                this.send(gson.toJson(response));
                return false;
            }

            response.setStatus(Status.OK);
            this.send(gson.toJson(response));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void requestBet() throws IOException
    {
        Response response = this.sendMessage(Intent.BET, "");

        this.chosenStickType = StickType.valueOf(response.getData());
        System.out.println("Client< "+ this.clientId  + " has placed bet: " + this.chosenStickType);
    }

    private void requestDraw() throws IOException
    {
        int maxSticks = this.croupier.getCurrSticksCount();

        Response response = this.sendMessage(Intent.DRAW, Integer.toString(maxSticks));

        this.chosenStickIndex = Integer.valueOf(response.getData());
        this.croupier.setChosenStickIndex(this.chosenStickIndex);
        System.out.println("Client< "+ this.clientId + " has drawn: " + this.chosenStickIndex);
    }

   private void shutDown()
   {
       try {
           System.out.println("I'm shutingdown client <" + this.clientId + ">");
           this.sendMessage(Intent.GAME_OVER, String.valueOf(this.score));
           this.croupier.setFreeSeat(this.chairNumber);
           this.croupier.shuffle();

       } catch (IOException e) {
           e.printStackTrace();
       }
   }

    private void updateScore() throws IOException
    {
        if(this.isDrawing())
        {
            if(this.croupier.stickOnIndex(this.chosenStickIndex) == StickType.SHORT)
            {
                this.isAlive = false;
                System.out.println("Cleint <" + this.clientId + "> has drawn SHORT stick.");
            } else System.out.println("Cleint <" + this.clientId + "> has drawn NORMAL stick.");
            return;
        }
        // player is betting
        if(this.croupier.stickOnIndex(croupier.getChosenStickIndex()) == this.chosenStickType) {
            System.out.println("Cleint <" + this.clientId + "> has got the bet.");
            this.score++;
            this.croupier.setMaxScoreIfGreater(this.score);
        } else System.out.println("Cleint <" + this.clientId + "> has lost the bet.");

    }

    /*
     *  Checks whether user has won and notifies him.
     */
    private void won()
    {
        if(this.croupier.getMaxScore() <= this.score) {
            System.out.println("Client <" + this.clientId + "> WON!!!");
            try {
                this.sendMessage(Intent.WON, String.valueOf(this.score));
                this.out.close();
                this.in.close();
                this.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Client <" + this.clientId + "> lost");
            try {
                this.sendMessage(Intent.GAME_OVER, String.valueOf(this.score));
                this.out.close();
                this.in.close();
                this.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Response sendMessage(Intent intent, String options) throws IOException
    {
        Message message = new Message();
        message.setId(this.clientId);
        message.setIntent(intent);
        message.setOptions(options);
        String convertedMessage = gson.toJson(message);
        this.send(convertedMessage);

        String responseStr = in.readLine();
        return gson.fromJson(responseStr, Response.class); // Ovde navodimo u sta tekst treba da se konvertuje
    }

    private void send(String message) throws IOException
    {
        this.out.write(message);
        this.out.newLine();
        this.out.flush();
    }

    private boolean isDrawing()
    {
        return this.croupier.currentPlayerIndex() == this.chairNumber;
    }
}
