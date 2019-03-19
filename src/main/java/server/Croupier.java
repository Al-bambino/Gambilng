package server;

import model.StickType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Ova klasa sluzi za deljene resurse.
 * Ideja mi je bila da ih drzim sve na jednom mestu
 * da bih lakse mogao sa njima da upravljam.
 */
public class Croupier
{
    private static final int MAX_ROUNDS = 10;
    private int tableSeats = 6;
    private User[] users;
    private CyclicBarrier startBarrier;
    private CyclicBarrier betBarrier;
    private CyclicBarrier endBarrier;

    private AtomicInteger currSticksCount = new AtomicInteger(0);
    private AtomicInteger currPlayer = new AtomicInteger(-1);
    private AtomicInteger currRound = new AtomicInteger(-1);
    private AtomicInteger shortStickIndex = new AtomicInteger(-1); // all other sticks are StickType.NORMAL
    private AtomicInteger currChosenStickIndex = new AtomicInteger(-1); // which stick is chosen
    private int maxScore = -1;
    private List<StickType> sticks = new ArrayList<>(this.tableSeats);


    public Croupier()
    {
        this.users = new User[this.tableSeats];
        this.currRound.set(0);
        this.currSticksCount.set(this.tableSeats); // they are indexed from 0

        this.startBarrier = new CyclicBarrier(this.tableSeats, () -> {
            System.out.println("We have " + tableSeats + " players. Table is preparing...");
        });

        this.betBarrier = new CyclicBarrier(this.tableSeats, () -> {
            System.out.println("All bets are placed, let's see what has been drawn...");
        });

        this.endBarrier = new CyclicBarrier(this.tableSeats, () -> {
            System.out.println("All scores are updated, I'm preparing for next round");
            this.drawStick(this.currChosenStickIndex.get());
            this.currRound.getAndIncrement();
            this.currPlayer.getAndIncrement();
        });

        this.shuffle();
    }

    public void shuffle()
    {
        this.currPlayer.set(0);
        this.currSticksCount.set(this.tableSeats);
        Random r = new Random();
        this.shortStickIndex.set(r.nextInt(this.currSticksCount.get())); // save short stick index for later
        for (int i = 0; i < this.tableSeats; i++)
        {
            StickType stick = StickType.NORMAL;
            if(i == this.shortStickIndex.get()) stick = StickType.SHORT;
            this.sticks.add(stick);
        }
        System.out.println("Short stick index is " + this.shortStickIndex.get());
    }

    /**
     * Removes stick at given index and returns true if drawn stick was short.
     * @param index
     * @return false if removed stick was normal
     */
    public StickType drawStick(int index)
    {
        if(index >= this.currSticksCount.get()) {
            System.out.println("KORISNIK prosledio " + index + " a ima "+ this.currSticksCount.get() );
            return null;
        }
        StickType deleted = this.sticks.remove(index);
        if(index < this.shortStickIndex.get()) {
            System.out.println("Short stick is now on " + this.shortStickIndex.decrementAndGet());
        }

        int left = this.currSticksCount.decrementAndGet();
        System.out.println("We have " + left + " sticks");
        for(int i = 0; i < left; i++){
            System.out.println("sticks[" + i + "] = " + this.sticks.get(i).toString());
        }
        return deleted;
    }


    public synchronized int giveSeat(User user)
    {
        for (int i = 0; i < tableSeats; i++) {
            if(users[i] == null){
                users[i] = user;
                return i;
            }
        }
        return -1;
    }

    public synchronized void setFreeSeat(int index)
    {
        this.users[index] = null;
    }

    public boolean gameIsActive()
    {
        return this.currRound.get() > -1 && this.currRound.get() < Croupier.MAX_ROUNDS;
    }

    public synchronized StickType stickOnIndex(int index)
    {
        return this.sticks.get(index);
    }

    public synchronized int getMaxScore() {
        return maxScore;
    }

    public synchronized void setMaxScoreIfGreater(int maxScore) {
        if(this.maxScore < maxScore) this.maxScore = maxScore;
    }

    public int getChosenStickIndex() {
        return this.currChosenStickIndex.get();
    }

    public void setChosenStickIndex(int chosenStickIndex) {
        this.currChosenStickIndex.set(chosenStickIndex);
    }

    public int currentPlayerIndex()
    {
        return this.currPlayer.get();
    }

    public int getCurrSticksCount()
    {
        return this.currSticksCount.get();
    }

    public int getShortStickIndex() {
        return this.shortStickIndex.get();
    }

    public void newGame()
    {

    }

    public void resetTable()
    {

    }

    public CyclicBarrier getEndBarrier() {
        return endBarrier;
    }

    public CyclicBarrier getBetBarrier() {
        return betBarrier;
    }

    public CyclicBarrier getStartBarrier() {
        return startBarrier;
    }

}
