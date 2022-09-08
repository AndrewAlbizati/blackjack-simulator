package com.github.AndrewAlbizati;

import java.util.ArrayList;
import java.util.Collections;

public class Main {
    private static int simulations = 100000;
    private static int gamesPlayed = 0;

    private static int startingAmount = 10000;
    private static int amount = startingAmount;
    private static int betPerRound = 2;

    private static int wins = 0;
    private static  int losses = 0;
    private static int pushes = 0;

    private static int busts = 0;
    private static int dealerBusts = 0;

    private static int valueWins = 0;
    private static int valueLosses = 0;

    private static int playerBlackjacks = 0;
    private static int dealerBlackjacks = 0;

    private static int hits = 0;
    private static int stands = 0;
    private static int doubles = 0;
    private static int splits = 0;

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        ArrayList<Card> deck = createDeck();

        ArrayList<Card> dealerHand = new ArrayList<>();
        ArrayList<Card> playerHand = new ArrayList<>();

        for (int i = 0; i < simulations; i++) {
            dealerHand.clear();
            playerHand.clear();

            dealCard(deck, playerHand);
            dealCard(deck, dealerHand);
            dealCard(deck, playerHand);
            dealCard(deck, dealerHand);

            simulateGame(deck, dealerHand, playerHand);
        }

        // Print out all game stats
        long endTime = System.currentTimeMillis();

        System.out.println("Total games simulated: " + gamesPlayed);
        System.out.println("Time elapsed: " + (endTime - startTime) + " millis");

        System.out.println();

        System.out.println("Wins: " + wins + " (" + round((wins / (double) gamesPlayed) * 100, 100) + "%)");
        System.out.println("Losses: " + losses + " (" + round((losses / (double) gamesPlayed) * 100, 100) + "%)");
        System.out.println("Pushes: " + pushes + " (" + round((pushes / (double) gamesPlayed) * 100, 100) + "%)");

        System.out.println();

        System.out.println("Busts: " + busts + " (" + round((busts / (double) gamesPlayed) * 100, 100) + "%)");
        System.out.println("Dealer busts: " + dealerBusts + " (" + round((dealerBusts / (double) gamesPlayed) * 100, 100) + "%)");

        System.out.println();

        System.out.println("Value wins: " + valueWins + " (" + round((valueWins / (double) gamesPlayed) * 100, 100) + "%)");
        System.out.println("Value losses: " + valueLosses + " (" + round((valueLosses / (double) gamesPlayed) * 100, 100) + "%)");

        System.out.println();

        System.out.println("Player Blackjacks: " + playerBlackjacks + " (" + round((playerBlackjacks / (double) gamesPlayed) * 100, 100) + "%)");
        System.out.println("Dealer Blackjacks: " + dealerBlackjacks + " (" + round((dealerBlackjacks / (double) gamesPlayed) * 100, 100) + "%)");

        System.out.println();

        System.out.println("Hits: " + hits);
        System.out.println("Stands: " + stands);
        System.out.println("Doubles: " + doubles);
        System.out.println("Splits: " + splits);

        System.out.println();

        System.out.println("Final amount: " + amount + " (" + (amount - startingAmount) + ")");
    }

    /**
     * Simulates a game of Blackjack and tracks all of the statistics.
     *
     * @param deck The deck to be drawn from.
     * @param dealerHand The ArrayList of cards representing the dealers hand.
     * @param playerHand The ArrayList of cards representing the players hand.
     */
    public static void simulateGame(ArrayList<Card> deck, ArrayList<Card> dealerHand, ArrayList<Card> playerHand) {
        int bet = betPerRound;
        gamesPlayed++;

        // Dealer is dealt a Blackjack
        // Only here for stat tracking
        if (getValue(dealerHand) == 21) {
            dealerBlackjacks++;
        }

        // End game if dealer is dealt a Blackjack AND their upcard is an ace
        if (dealerHand.get(0).getValue() == 1 && getValue(dealerHand) == 21) {
            losses++;
            amount -= bet;
            return;
        }

        // End game if player is dealt a Blackjack
        if (getValue(playerHand) == 21) {
            wins++;
            amount += Math.floor(bet * 3/(double)2);
            playerBlackjacks++;
            return;
        }

        Action action;
        boolean active = true;
        do {
            // Go through all possible player hands
            action = determineAction(dealerHand.get(1), playerHand);

            if (action == Action.SPLIT) {
                splits++;
                gamesPlayed--;
                ArrayList<Card> hand1 = new ArrayList<>();
                hand1.add(playerHand.get(0));
                dealCard(deck, hand1);

                ArrayList<Card> hand2 = new ArrayList<>();
                hand2.add(playerHand.get(1));
                dealCard(deck, hand2);

                simulateGame(deck, dealerHand, hand1);
                simulateGame(deck, dealerHand, hand2);
                return;
            }

            if ((playerHand.size() > 2 && action == Action.DOUBLE)) {
                action = Action.HIT;
            }

            // Track player actions
            switch (action) {
                case HIT -> hits++;
                case STAND -> stands++;
                case DOUBLE -> doubles++;
                case NONE -> {
                    StringBuilder playerHandStr = new StringBuilder();
                    for (Card c : playerHand) {
                        playerHandStr.append(c.getValue());
                        playerHandStr.append(" ");
                    }
                    throw new IllegalStateException("Dealer upcard: " + dealerHand.get(1).getValue() + ", player hand (" + playerHand.size() + "): " + playerHandStr);
                }
            }

            // Deal one card for hit and double
            if (action == Action.DOUBLE || action == Action.HIT) {
                dealCard(deck, playerHand);
            }

            if (action == Action.DOUBLE) {
                bet *= 2;
            }

            // End game is stand, double, or bust
            if (action == Action.STAND || action == Action.DOUBLE || getValue(playerHand) > 21) {
                active = false;
            }
        } while (active);

        // Player busts
        if (getValue(playerHand) > 21) {
            losses++;
            busts++;
            amount -= bet;
            return;
        }

        // Dealer hits until 17
        while (getValue(dealerHand) < 17) {
            dealCard(deck, dealerHand);
        }

        // Add a win if dealer busts
        if (getValue(dealerHand) > 21) {
            wins++;
            dealerBusts++;
            amount += bet;
            return;
        }

        // A value loss is when neither player busts, and one player's hand has a higher value than the other
        if (getValue(dealerHand) > getValue(playerHand)) {
            // Player value loss
            losses++;
            valueLosses++;
            amount -= bet;
        } else if (getValue(dealerHand) < getValue(playerHand)) {
            // Player value win
            wins++;
            valueWins++;
            amount += bet;
        } else {
            // Both hands have the same value (push)
            pushes++;
        }
    }

    /**
     * Calculates and returns the value of a hand.
     * Determines the value of Aces in a hand.
     *
     * @param hand The hand that will be evaluated.
     * @return The value of the hand.
     */
    public static byte getValue(ArrayList<Card> hand) {
        byte score = 0;

        ArrayList<Card> d2 = new ArrayList<>(hand);

        sortDeck(d2);
        Collections.reverse(d2);

        for (Card c : d2) {
            switch (c.getValue()) {
                case 1 -> {
                    // Next card is an Ace
                    if (score + 11 <= 21) {
                        if (d2.size() > d2.indexOf(c) + 1) {
                            if (d2.get(d2.indexOf(c) + 1).getValue() == (byte) 1) {
                                score += 1;
                                break;
                            }
                        }

                        score += 11;
                        break;
                    }
                    score += 1;
                }
                case 11, 12, 13 -> score += 10;
                default -> score += c.getValue();
            }
        }

        return score;
    }

    /**
     * Sorts the deck based on value (least -> most)
     *
     * @param deck The deck/hand to be sorted.
     */
    public static void sortDeck(ArrayList<Card> deck) {
        boolean sorted = false;
        while(!sorted) {
            sorted = true;
            for (int i = 0; i < deck.size() - 1; i++) {
                Card c = deck.get(i);
                Card nextC = deck.get(i + 1);
                if (c.getValue() - nextC.getValue() > 0) {
                    deck.set(i, nextC);
                    deck.set(i + 1, c);
                    sorted = false;
                }
            }
        }
    }

    /**
     * Deals one card to a hand.
     * Re-initializes the deck if empty.
     *
     * @param deck The deck to be dealt from.
     * @param hand The hand to be dealt to.
     */
    public static void dealCard(ArrayList<Card> deck, ArrayList<Card> hand) {
        if (deck.isEmpty()) {
            deck = createDeck();
        }
        hand.add(deck.remove(0));
    }

    /**
     * Creates a new Blackjack deck with 6 shuffled decks.
     *
     * @return A new ArrayList of cards representing a deck.
     */
    public static ArrayList<Card> createDeck() {
        ArrayList<Card> arr = new ArrayList<>();

        // 6 decks
        for (byte i = 0; i < 6; i++) {
            // 4 suits
            for (byte j = 0; j < 4; j++) {
                // 13 cards
                for (byte k = 1; k <= 13; k++) {
                    arr.add(new Card(k));
                }
            }
        }
        Collections.shuffle(arr);
        return arr;
    }

    /**
     * Rounds a double to a precision.
     * (e.g. 100 is 2 decimals, 1000 is 3 decimals...)
     *
     * @param val The double value to be rounded.
     * @param precision The amount of precision it will be rounded to.
     * @return A new rounded double.
     */
    public static double round(double val, int precision) {
        val = val * precision;
        val = Math.round(val);
        val = val / precision;
        return val;
    }

    /**
     * Checks if a deck is soft (contains an ace valued at 11).
     *
     * @return Whether or not the deck is soft.
     */
    public static boolean isSoft(ArrayList<Card> deck) {
        ArrayList<Card> d2 = new ArrayList<>(deck);
        sortDeck(d2);

        int aceCount = 0;
        for (Card c : deck) {
            if (c.getValue() == 1) {
                aceCount++;
            }
        }
        // Hands without aces can't be soft
        if (aceCount == 0) {
            return false;
        }

        int scoreWithoutAce = 0;
        for (int i = 1; i < d2.size(); i++) {
            Card c = d2.get(i);
            switch (c.getValue()) {
                case 1:
                    scoreWithoutAce += 1;

                case 11:
                case 12:
                case 13:
                    scoreWithoutAce += 10;
                    break;

                default:
                    scoreWithoutAce += c.getValue();
            }
        }

        if (scoreWithoutAce > 9 && aceCount > 1) {
            return false;
        }

        return scoreWithoutAce < 11;
    }

    /**
     * Determines what action should be taken at a given gamestate.
     *
     * @param dealerUpCard The card that the dealer is showing to the player.
     * @param playerHand The ArrayList of cards representing the player's hand.
     * @return The Action that should be taken for that gamestate.
     */
    public static Action determineAction(Card dealerUpCard, ArrayList<Card> playerHand) {
        if (playerHand.size() == 2 && playerHand.get(0).getValue() == playerHand.get(1).getValue()) {
            return switch (playerHand.get(0).getValue()) {
                case 1, 8 -> Action.SPLIT;

                case 2, 3 -> switch (dealerUpCard.getValue()) {
                    case 2, 3, 8, 9, 10, 1 -> Action.HIT;
                    case 4, 5, 6, 7 -> Action.SPLIT;

                    default -> Action.NONE;
                };

                case 4 -> Action.HIT;

                case 5 -> switch (dealerUpCard.getValue()) {
                    case 2, 3, 4, 5, 6 -> Action.DOUBLE;
                    case 7, 8, 9, 10, 1 -> Action.HIT;

                    default -> Action.NONE;
                };

                case 6 -> switch (dealerUpCard.getValue()) {
                    case 2, 3, 4, 5, 6 -> Action.SPLIT;
                    case 7, 8, 9, 10, 1 -> Action.HIT;

                    default -> Action.NONE;
                };

                case 7 -> switch (dealerUpCard.getValue()) {
                    case 2, 3, 4, 5, 6, 7 -> Action.SPLIT;
                    case 8, 9, 10, 1 -> Action.HIT;

                    default -> Action.NONE;
                };

                case 9 -> switch (dealerUpCard.getValue()) {
                    case 2, 3, 4, 5, 6, 8, 9 -> Action.SPLIT;
                    case 7, 10, 1 -> Action.STAND;

                    default -> Action.NONE;
                };

                case 10 -> Action.STAND;

                default -> Action.NONE;
            };
        } else if (isSoft(playerHand)) {
            return switch (getValue(playerHand) - 11) {
                case 2, 3, 4, 5, 6 -> switch (dealerUpCard.getValue()) {
                    case 2, 3, 7, 8, 9, 10, 1 -> Action.HIT;
                    case 4, 5, 6 -> Action.DOUBLE;
                    default -> Action.NONE;
                };

                case 7 -> switch (dealerUpCard.getValue()) {
                    case 2, 7, 8 -> Action.STAND;
                    case 3, 4, 5, 6 -> Action.DOUBLE;
                    case 9, 10, 1 -> Action.HIT;
                    default -> Action.NONE;
                };

                case 8, 9, 10 -> Action.STAND;

                default -> Action.NONE;
            };
        } else {
            return switch (getValue(playerHand)) {
                case 3, 4, 5, 6, 7, 8 -> Action.HIT;

                case 9 -> switch (dealerUpCard.getValue()) {
                    case 2, 7, 8, 9, 10, 1 -> Action.HIT;
                    case 3, 4, 5, 6 -> Action.DOUBLE;
                    default -> Action.NONE;
                };

                case 10 -> switch (dealerUpCard.getValue()) {
                    case 2, 3, 4, 5, 6, 7, 8, 9 -> Action.DOUBLE;
                    case 10, 1 -> Action.HIT;
                    default -> Action.NONE;
                };

                case 11 -> Action.DOUBLE;

                case 12 -> switch (dealerUpCard.getValue()) {
                    case 2, 3, 7, 8, 9, 10, 1 -> Action.HIT;
                    case 4, 5, 6 -> Action.STAND;
                    default -> Action.NONE;
                };

                case 13, 14, 15, 16 -> switch (dealerUpCard.getValue()) {
                    case 2, 3, 4, 5, 6 -> Action.STAND;
                    case 7, 8, 9, 10, 1 -> Action.HIT;
                    default -> Action.NONE;
                };

                case 17, 18, 19, 20, 21 -> Action.STAND;

                default -> Action.NONE;
            };
        }
    }
}
