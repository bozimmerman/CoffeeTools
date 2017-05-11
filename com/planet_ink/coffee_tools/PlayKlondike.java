package com.planet_ink.coffee_tools;

import java.io.*;

public class PlayKlondike 
{
    final static java.util.Random rand = new java.util.Random();
    final static int BASE_CLUBS=1; // 1 = ace, 2-10, J, Q, K
    final static int BASE_SPADES=14;
    final static int BASE_DIAMONDS=27;
    final static int BASE_HEARTS=40;
    final static int[] SUIT_BASES={BASE_CLUBS,BASE_SPADES,BASE_DIAMONDS,BASE_HEARTS};
    final static char[] SUIT_CHARS={'C','S','d','h'};
    
    int[] raw_deck = new int[53]; // last int is length
    boolean[][] finalPlays = new boolean[4][13];
    int[][] field = new int[7][53]; // negative numbers are down-cards
    
    public static void main(String[] args) {
        double winners = 0;
        int games = 1000000;
        PlayKlondike deal;
        rand.setSeed(System.currentTimeMillis());
        for(int x = 0; x<games; x++) 
        {
            deal = new PlayKlondike();
            if(playThisDeal(deal))
                winners++;
        }
        double winPct = winners/new Integer(games).doubleValue() * 10000.0;
        System.out.println("Winners: " + winners + "("+(Math.round(winPct)/100.0)+"%)");
    }

    public static boolean playThisDeal(PlayKlondike deal) {
        boolean success = true;
        boolean successSinceLastFlip = false;
        // algorithm:
        while(true) {
            //deal.displayDeck(System.out);
            success = false;
            // 1. check if won.  if won, stop and cheer.
            if(deal.didWon()) {
                //System.out.println("Winner!");
                //deal.displayDeck(System.out);
                //System.out.flush();
                return true;
            }
            success = success || deal.playFlipFieldTops();
            success = success || deal.playMoveFieldToTop();
            success = success || deal.playFieldKingsAround();
            success = success || deal.playOtherFieldCardsAround();
            
            if(!success) {
                boolean deckSuccess = false;
                int topDeckCardIndex = deal.nextUpDeckCardIndex();
                if(topDeckCardIndex >= 0) {
                    deckSuccess = deckSuccess || deal.playMoveCardToTop(topDeckCardIndex);
                    deckSuccess = deckSuccess || deal.playKingAround(topDeckCardIndex);
                    deckSuccess = deckSuccess || deal.playCardAround(topDeckCardIndex);
                    if(deckSuccess)
                        successSinceLastFlip = true;
                }
                if(!deckSuccess) {
                    int didFlip = deal.flipThree(false);
                    if(didFlip < 0) {// did NOT flip 
                        if(!successSinceLastFlip) {
                            //System.out.println("Flipped through the whole deck. You lose.");
                            //deal.displayDeck(System.out);
                            //System.out.flush();
                            return false;
                        }
                        didFlip = deal.flipThree(true);
                        successSinceLastFlip = false;
                    }
                }
                success = success || deckSuccess;
            }
            //System.out.println("success=" + success );
            //System.out.flush();
        }
    }
    
    public boolean didWon() {
        for(int i = 0; i<finalPlays.length; i++) {
            for(int x = 0; x< finalPlays[i].length; x++) {
                if(!finalPlays[i][x])
                    return false;
            }
        }
        return true;
    }
    
    public PlayKlondike(int[] deckToUse) {
        for(int i = 0; i<deckToUse.length; i++)
            raw_deck[i] = deckToUse[i];
    }
    
    public PlayKlondike() {
        dealRandom();
    }

    private int getSuitBase(int card) {
        if(card < 0) card = card * -1;
        if(card >= BASE_HEARTS) return BASE_HEARTS;
        if(card >= BASE_DIAMONDS) return BASE_DIAMONDS;
        if(card >= BASE_SPADES) return BASE_SPADES;
        return BASE_CLUBS;
    }
    
    public boolean fieldPlayableSuits(int card1, int card2) {
        if(card1 >= BASE_DIAMONDS) 
            return (card2 < BASE_DIAMONDS);
        return (card2 >= BASE_DIAMONDS);
    }
    
    public boolean playMoveFieldToTop() {
        boolean didOne = false;
        for(int col = 0; col<field.length; col++) {
            int colCardDex = getTopCardIndex(field[col],0,0);
            if((colCardDex >= 0) && (field[col][colCardDex] > 0)) {
                int suitBase = getSuitBase(field[col][colCardDex]);
                int cardValueMinus1 = field[col][colCardDex] - suitBase;
                int suitValue = (suitBase - 1) / 13;
                if((cardValueMinus1 == 0) || (finalPlays[suitValue][cardValueMinus1 - 1])) {
                    field[col][colCardDex] = 0;
                    finalPlays[suitValue][cardValueMinus1] = true;
                    didOne = true;
                }
            }
        }
        return didOne;
    }
    
    public void displayCard(PrintStream ps, int cardValue) {
        if(cardValue == 0) return;
        ps.print(" ");
        int absValue = cardValue < 0 ? -cardValue : cardValue;
        int suitBase = this.getSuitBase(absValue);
        int cardValueSmall = absValue - suitBase + 1;
        int suitValue = (suitBase - 1) / 13;
        char suitChar = SUIT_CHARS[suitValue];
        //ps.print(0x27);
        ps.print(cardValue < 0 ? "[" : "");
        ps.print(suitChar + "" + cardValueSmall);
        ps.print(cardValue < 0 ? "]" : "");
        //ps.print(0x27);
        //ps.print("34m");
    }
    
    public void displayDeck(PrintStream ps) {
        ps.print("raw_deck: ");
        for(int i = 0; i< raw_deck[raw_deck.length-1]; i++) {
            displayCard(ps,raw_deck[i]);
        }
        ps.println("\n");
        for(int col = 0; col < finalPlays.length; col++) {
            ps.print("top # " + col+ ": ");
            int lowest = Integer.MAX_VALUE;
            int highest = Integer.MIN_VALUE;
            for(int i = 0; i< finalPlays[col].length; i++)
                if(finalPlays[col][i])
                {
                    if(i < lowest) lowest = i;
                    if(i > highest) highest = i;
                }
            if(lowest != Integer.MAX_VALUE) {
                displayCard(ps,SUIT_BASES[col] + lowest);
                ps.print(" -");
                displayCard(ps,SUIT_BASES[col] + highest);
            }
            ps.println("");
        }
        ps.println("");
        for(int col = 0; col < field.length; col++) {
            ps.print("col # " + col + ": ");
            for(int i = 0; i< field[col][field[col].length-1]; i++) {
                displayCard(ps,field[col][i]);
            }
            ps.println("");
        }
        ps.println("");
        ps.flush();
    }
    
    public boolean playMoveCardToTop(int cardValueIndex) {
        boolean didOne = false;
        int cardValue = raw_deck[cardValueIndex];
        int suitBase = getSuitBase(cardValue);
        int cardValueMinus1 = cardValue - suitBase;
        int suitValue = (suitBase - 1) / 13;
        if((cardValueMinus1 == 0) || (finalPlays[suitValue][cardValueMinus1 - 1])) {
            finalPlays[suitValue][cardValueMinus1] = true;
            raw_deck[cardValueIndex] = 0;
            didOne = true;
        }
        return didOne;
    }
    
    public boolean playFlipFieldTops() {
        boolean didOne = false;
        for(int col = 0; col<field.length; col++) {
            int colCardDex = getTopCardIndex(field[col],0,0);
            if((colCardDex >= 0) && (field[col][colCardDex] < 0)) {
                field[col][colCardDex] = -field[col][colCardDex];
                didOne = true;
            }
        }
        return didOne;
    }

    public boolean playFieldKingsAround() {
        boolean didOne = false;
        for(int col = 0; col<field.length; col++) {
            int colCardDex = getBottomCardIndex(field[col],0,1); // must be face up, must be bottom
            // must be >0, not >=0, because we dont want to just shuffle the same king around!
            if((colCardDex > 0) && ((field[col][colCardDex] % 13) == 0)) { // test for kingship == 13, 26, 39, 52
                for(int emptyCol = 0; emptyCol<field.length; emptyCol++) {
                    if(emptyCol != col) {
                        int emptyCardDex = getTopCardIndex(field[emptyCol],0,0);
                        if(emptyCardDex < 0) { // got an empty col to move to!!!
                            didOne = true;
                            pack(field[emptyCol]);
                            if(field[emptyCol][field[emptyCol].length-1] != 0) {
                                System.out.println("EMPTYCOLERROR!!!");
                                System.exit(-1);
                            }
                            for(int x = colCardDex; x < field[col].length-1; x++) {
                                field[emptyCol][field[emptyCol][field[emptyCol].length-1]] = field[col][x];
                                field[emptyCol][field[emptyCol].length-1]++;
                                field[col][x] = 0;
                            }
                            break;
                        }
                    }
                }
            }
        }
        return didOne;
    }
    
    public boolean playKingAround(int cardValueIndex) {
        boolean didOne = false;
        int cardValue = raw_deck[cardValueIndex];
        if((cardValue % 13) == 0) { // test for kingship == 13, 26, 39, 52
            for(int emptyCol = 0; emptyCol<field.length; emptyCol++) {
                int emptyCardDex = getTopCardIndex(field[emptyCol],0,0);
                if(emptyCardDex < 0) { // got an empty col to move to!!!
                    didOne = true;
                    pack(field[emptyCol]);
                    if(field[emptyCol][field[emptyCol].length-1] != 0) {
                        System.out.println("EMPTYCOLERROR!!!");
                        System.exit(-1);
                    }
                    field[emptyCol][field[emptyCol][field[emptyCol].length-1]] = cardValue;
                    field[emptyCol][field[emptyCol].length-1]++;
                    raw_deck[cardValueIndex] = 0;
                    break;
                }
            }
        }
        return didOne;
    }
    
    public boolean playOtherFieldCardsAround() {
        boolean didOne = false;
        for(int col = 0; col<field.length; col++) {
            int colCardDex = getBottomCardIndex(field[col],0,1); // must be face up, must be bottom
            if((colCardDex >= 0) 
            && ((field[col][colCardDex] % 13) != 0)
            && ((field[col][colCardDex] % 13) != 1)) { // NO KINGS OR ACES!
                int cardValue = field[col][colCardDex];
                int suitBase = getSuitBase(cardValue);
                int cardValueMinus1 = cardValue - suitBase;
                for(int playableCol = 0; playableCol<field.length; playableCol++) {
                    if(playableCol != col) {
                        int playableCardDex = getTopCardIndex(field[playableCol],0,1);
                        if(playableCardDex >= 0) {
                            int playableCardValue = field[playableCol][playableCardDex];
                            if (fieldPlayableSuits(cardValue,playableCardValue)
                            && ((playableCardValue - getSuitBase(playableCardValue)) == (cardValueMinus1 + 1))) {
                                didOne = true;
                                pack(field[playableCol]);
                                for(int x = colCardDex; x < field[col].length-1; x++) {
                                    if(field[col][x] > 0) {
                                        field[playableCol][field[playableCol][field[playableCol].length-1]] = field[col][x];
                                        field[playableCol][field[playableCol].length-1]++;
                                        field[col][x] = 0;
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
        return didOne;
    }
    
    public boolean playCardAround(int cardValueIndex) {
        boolean didOne = false;
        int cardValue = raw_deck[cardValueIndex];
        if (((cardValue % 13) != 0)
        && ((cardValue % 13) != 1)) { // NO KINGS OR ACES!
            int suitBase = getSuitBase(cardValue);
            int cardValueMinus1 = cardValue - suitBase;
            for(int playableCol = 0; playableCol<field.length; playableCol++) {
                int playableCardDex = getTopCardIndex(field[playableCol],0,1);
                if(playableCardDex >= 0) {
                    int playableCardValue = field[playableCol][playableCardDex];
                    if (fieldPlayableSuits(cardValue,playableCardValue)
                    && (( playableCardValue - getSuitBase(playableCardValue)) == (cardValueMinus1 + 1))) {
                        didOne = true;
                        pack(field[playableCol]);
                        field[playableCol][field[playableCol][field[playableCol].length-1]] = cardValue;
                        field[playableCol][field[playableCol].length-1]++;
                        raw_deck[cardValueIndex] = 0;
                        break;
                    }
                }
            }
        }
        return didOne;
    }
    
    private int nextUpDeckCardIndex() {
        int UpTop = 0;
        while ((UpTop < raw_deck.length - 1) && (raw_deck[UpTop] <= 0)) {
            UpTop++;
        }
        if(UpTop >= raw_deck.length-1) return -1;
        return UpTop;
    }
    
    private int flipThree(boolean evenIfDone) {
        int UpTop = 0;
        // look for last Down card
        boolean foundDownCard = false;
        while ((UpTop < raw_deck.length - 1) && (raw_deck[UpTop] <= 0)) {
            foundDownCard = foundDownCard || raw_deck[UpTop] < 0;
            UpTop++;
        }
        if(!foundDownCard) { // if NO down cards (or no cards at all)
            if(!evenIfDone)
                return -1;
            for(int i = 0; i<raw_deck.length - 1; i++) {
                raw_deck[i] = -raw_deck[i]; // turn them all face down
            }
            UpTop = 0; // and look again
            while ((UpTop < raw_deck.length - 1) && (raw_deck[UpTop] <= 0)) {
                foundDownCard = foundDownCard || raw_deck[UpTop] < 0;
                UpTop++;
            }
        }
        if(!foundDownCard) return -1; // if still no down cards exit
        int numFlippedUp = 3;
        while((numFlippedUp > 0) && ((--UpTop) >=0) && (raw_deck[UpTop] <=0)) {
            raw_deck[UpTop] = -raw_deck[UpTop];
            if(raw_deck[UpTop] > 0) {
                numFlippedUp--;
            }
        }
        return UpTop;
    }

    
    private boolean anyFalse(boolean[] tfs) {
        for(int i = 0; i<tfs.length; i++)
            if(!tfs[i]) return true;
        return false;
    }
    
    private int dealRandomFromBools(boolean[] deck) {
        if(!anyFalse(deck)) return -1;
        int dealNum = rand.nextInt(52);
        while(deck[dealNum]) 
            dealNum = rand.nextInt(52);
        deck[dealNum] = true;
        return dealNum + 1;
    }
    
    public void dealRandom() {
        boolean[] cardsDealt = new boolean[53];
        int[] newDeck = new int[52];
        for(int i = 0; i<newDeck.length; i++) {
            int card = dealRandomFromBools(cardsDealt);
            newDeck[i] = card;
        }
        dealFromDeck(newDeck);
    }
    
    public void dealFromDeck(int[] cards) {
        int numDealt = 0;
        for(int playDex = 0; playDex < 7; playDex++) {
            for(int dealDex = playDex; dealDex < 7; dealDex++) {
                int cardValue = cards[numDealt];
                addCard(field[dealDex],-cardValue);
                numDealt++;
            }
        }
        while(numDealt < 52){
            int cardValue = cards[numDealt];
            addCard(raw_deck,-cardValue);
            numDealt++;
        }
    }
    
    private void pack(int[] cards) {
        int endofline = cards[cards.length-1];
        int delBack = 0;
        for(int i = 0; i < endofline; i++) {
            if(cards[i] == 0) {
                delBack++;
                cards[cards.length-1]--;
            }
            else
            if(delBack > 0) {
                cards[i-delBack] = cards[i];
            }
        }
    }

    private int getTopCardIndex(int[] cards, int skip, int faceUpDown) {
        for(int i = cards[cards.length-1] - 1; i>=0; i--) {
            if(cards[i] != 0) {
                if((faceUpDown==0)
                ||((faceUpDown>0)&&(cards[i]>0))
                ||((faceUpDown<0)&&(cards[i]<0)))
                {
                    if(--skip < 0)
                        return i;
                }
            }
        }
        return -1;
    }
    
    private int getBottomCardIndex(int[] cards, int skip, int faceUpDown) {
        for(int i = 0; i< cards[cards.length-1]; i++) {
            if(cards[i] != 0) {
                if((faceUpDown==0)
                ||((faceUpDown>0)&&(cards[i]>0))
                ||((faceUpDown<0)&&(cards[i]<0)))
                {
                    if(--skip < 0)
                        return i;
                }
            }
        }
        return -1;
    }
    
    private void addCard(int[] cards, int card) {
        if(cards[cards.length-1] >= cards.length-1)
            pack(cards);
        cards[cards[cards.length-1]] = card;
        cards[cards.length-1]++;
    }
    
}
