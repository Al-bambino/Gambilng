package client;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.*;

/**
 * Ideja orkestratora je da pravi klijente i da ih pokrece
 * Svaki klijent ce da operise na svojoj niti sa svojim soketom.
 * Mozete prilagoditi primer da koristite sve vreme jedan soket ili
 * mozda da svaka poruka bude novi soket.
 */
public class Orchestrator implements Runnable{

    private ScheduledExecutorService executorService;

    public Orchestrator() {
        this.executorService = Executors.newScheduledThreadPool(30);
    }

    public void run() {
        try {
            for (int i = 0; i < 25; i++) {
                Random r = new Random();
                this.executorService.schedule(new Client(), (long)(r.nextInt(5000)), TimeUnit.MILLISECONDS);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        this.executorService.shutdown();

    }
}
