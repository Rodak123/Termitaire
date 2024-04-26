package com.rodak.termitaire.game.components;

import com.rodak.termitaire.Termitaire;
import com.rodak.termitaire.game.settings.GameSettings;
import com.rodak.termitaire.ui.ColoredString;

import java.io.Serializable;
import java.util.Stack;

public class Card implements Serializable {

    public static ColoredString.Color RED = ColoredString.Color.RED;
    public static ColoredString.Color BLACK = ColoredString.Color.BLUE;

    public enum Suit {
        SPADES("cards/spades"),
        HEARTS("cards/hearts"),
        CLUBS("cards/clubs"),
        DIAMONDS("cards/diamonds"),
        ;

        private final String shortNameKey;

        Suit(String shortNameKey) {
            this.shortNameKey = shortNameKey;
        }

        public String getShortName() {
            return GameSettings.getInstance().getSetting(shortNameKey).getStringVal();
        }
    }

    public enum Rank {
        ACE("A"),
        TWO("2"),
        THREE("3"),
        FOUR("4"),
        FIVE("5"),
        SIX("6"),
        SEVEN("7"),
        EIGHT("8"),
        NINE("9"),
        TEN("10"),
        JACK("J"),
        QUEEN("Q"),
        KING("K"),
        ;

        private final String shortName;

        Rank(String shortName) {
            this.shortName = shortName;
        }

        public String getShortName() {
            return shortName;
        }
    }

    public static final int plotWidth = 9;
    public static final int plotHeight = 5;

    public static String[] getCardLines(Card card) {
        if (card == null) {
            return getEmptyCardLines();
        }

        String rank = card.rank.getShortName();
        String suit = card.suit.getShortName();
        if (card.hidden) {
            rank = " ";
            suit = " ";
        }

        boolean isRankShort = rank.length() < 2;

        String[] cardLines = new String[plotHeight];
        cardLines[0] = "+-------+";
        cardLines[1] = "| " + suit + "  " + (isRankShort ? " " + rank : rank) + " |";
        cardLines[2] = "|       |";
        cardLines[3] = "| " + (isRankShort ? rank + " " : rank) + "  " + suit + " |";
        cardLines[4] = "+-------+";
        return cardLines;
    }

    public static ColoredString.Color[][] getCardColors(Card card) {
        ColoredString.Color[][] colors = new ColoredString.Color[plotHeight][plotWidth];
        if (card == null || card.hidden) {
            return colors;
        }

        ColoredString.Color color = card.suit.ordinal() % 2 == 0 ? BLACK : RED;

        for (int j = 0; j < 2; j++) {
            for (int i = 0; i < plotWidth - 2; i++) {
                colors[1 + j * 2][1 + i] = color;
            }
        }
        return colors;
    }

    public static String[] getEmptyCardLines() {
        return new String[]{
                " --   -- ",
                "|       |",
                "         ",
                "|       |",
                " --   -- "
        };
    }

    public static Stack<Card> allCards() {
        Stack<Card> cards = new Stack<>();
        for (Rank rank : Rank.values()) {
            for (Suit suit : Suit.values()) {
                cards.add(new Card(rank, suit));
            }
        }
        return cards;
    }

    public enum CardStackRuleset {
        TABLEAU,
        FOUNDATION
    }

    public static boolean canCardStack(Card base, Card card, CardStackRuleset ruleset) {
        switch (ruleset) {
            case TABLEAU -> {
                if (base == null) {
                    return card.rank == Rank.KING;
                }
                return base.rank.ordinal() - card.rank.ordinal() == 1 &&
                        base.suit.ordinal() % 2 != card.suit.ordinal() % 2;
            }
            case FOUNDATION -> {
                if (base == null) {
                    return card.rank == Rank.ACE;
                }
                return base.rank.ordinal() - card.rank.ordinal() == -1 &&
                        base.suit == card.suit;
            }
        }
        return true;
    }

    public final Rank rank;
    public final Suit suit;

    private boolean hidden;

    public Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
        hidden = true;
    }

    public void show() {
        hidden = false;
        Termitaire.game.getStatistics().didACardFlip();
    }

    public void hide() {
        hidden = true;
    }

    public boolean isHidden() {
        return hidden;
    }

    @Override
    public String toString() {
        return "[" + rank + " of " + suit + "]";
    }

    public String toOrdinalString() {
        return String.valueOf(rank.ordinal()) + suit.ordinal();
    }
}
