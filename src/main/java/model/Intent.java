package model;

/**
 * Ovaj enum predstavlja nameru. U nasem domacem imamo samo tri namere jedne musterije.
 */
public enum Intent
{
    REQUEST_CHAIR,
    BET,            // asks to guess wether picked stick is normal or short
    DRAW,           // asks to pick stick index
    GAME_OVER,
    WON,
    UPDATE_SCORE
}
