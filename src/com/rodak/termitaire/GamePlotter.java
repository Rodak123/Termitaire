package com.rodak.termitaire;

import java.util.Stack;

public class GamePlotter {
    public final static int[] cardSpacing = new int[]{1, 2};
    private final static int cardIndexSpace = 4 + cardSpacing[0];

    private final static ColoredString.Color keysColor = ColoredString.Color.GREEN;
    private final static ColoredString.Color handColor = ColoredString.Color.PURPLE;

    public static void plotGameToCanvas(Game game, ConsoleCanvas canvas) {
        int x = cardIndexSpace;
        int y = 1;

        int centerLabelXOff = (int) Math.floor(Card.plotWidth / 2.0) - 1;

        for (int i = 0; i < game.getFoundations().size(); i++) {
            // Foundation
            Stack<Card> foundation = game.getFoundations().get(i);
            plotCardStack(x, y, foundation, CardStackMode.PILE, canvas);

            // Foundation Key
            plotKey(x + centerLabelXOff, 0, GameBinds.Foundations[i], canvas);

            x += Card.plotWidth + GamePlotter.cardSpacing[0];
        }

        // Waste
        x += Card.plotWidth + GamePlotter.cardSpacing[0];
        plotCardStack(x, y, game.getWaste(), CardStackMode.LAST_THREE, canvas);

        // Waste key
        plotKey(x + centerLabelXOff, 0, GameBinds.Waste[0], canvas);

        // Stock
        x += Card.plotWidth + GamePlotter.cardSpacing[0];
        plotCardStack(x, y, game.getStock(), CardStackMode.PILE, canvas);

        // Stock key
        plotKey(x + centerLabelXOff, 0, GameBinds.Stock[0], canvas);

        // Selected cards
        x += (Card.plotWidth + GamePlotter.cardSpacing[0]) * 2;
        canvas.plot(x, 0, "Hand:");
        canvas.setColors(x, 0, handColor, 5);

        plotCardStack(x, y, game.getSelectedCardsPile(), CardStackMode.STAGGERED, canvas);

        x = cardIndexSpace;
        y += Card.plotHeight + GamePlotter.cardSpacing[1];

        int mostCardsInPile = 0;
        for (int i = 0; i < game.getTableau().size(); i++) {
            // Column
            Stack<Card> column = game.getTableau().get(i);
            mostCardsInPile = Math.max(column.size(), mostCardsInPile);
            plotCardStack(x, y, column, CardStackMode.STAGGERED, canvas);

            // Column Key
            plotKey(x + centerLabelXOff, y - 1, GameBinds.Tableau[i], canvas);

            x += Card.plotWidth + GamePlotter.cardSpacing[0];
        }

        // Indexes
        plotCardIndexes(0, y, mostCardsInPile, canvas);
        plotCardIndexes(x, y, mostCardsInPile, canvas);

    }

    private static void plotKey(int x, int y, String key, ConsoleCanvas canvas) {
        canvas.plot(x, y, "[" + key.charAt(0) + "]");
        canvas.setColor(x + 1, y, keysColor);
    }

    private static void plotCardIndexes(int x, int y, int upto, ConsoleCanvas canvas) {
        for (int i = 0; i < Math.min(upto, 99); i++) {
            String index = String.valueOf(i);
            canvas.plot(x, y, "[" + (index.length() == 1 ? " " : "") + index + "]");
            canvas.setColors(x + 1, y, keysColor, 2);
            y += GamePlotter.cardSpacing[1];
        }
    }

    private enum CardStackMode {
        PILE,
        STAGGERED,
        LAST_THREE
    }

    private static void plotCardStack(int x, int y, Stack<Card> stack, CardStackMode mode, ConsoleCanvas canvas) {
        if (stack.size() == 0) {
            plotCard(x, y, null, canvas);
            return;
        }

        switch (mode) {
            case PILE -> plotCard(x, y, stack.peek(), canvas);
            case STAGGERED -> {
                for (Card card : stack) {
                    plotCard(x, y, card, canvas);
                    y += GamePlotter.cardSpacing[1];
                }
            }
            case LAST_THREE -> {
                int cardOff = (int) Math.ceil(Card.plotWidth / 2.0);
                int cards = Math.min(stack.size(), 3);

                x -= (cards - 1) * cardOff;
                for (int i = stack.size() - cards; i < stack.size(); i++) {
                    plotCard(x, y, stack.get(i), canvas);
                    x += cardOff;
                }
            }
        }
    }

    public static void plotCard(int x, int y, Card card, ConsoleCanvas canvas) {
        String[] cardLines = Card.getCardLines(card);
        ColoredString.Color[][] colors = Card.getCardColors(card);
        canvas.plot(x, y, cardLines);
        canvas.setColors(x, y, colors);
    }

    public static ConsoleCanvas createFittingCanvas(Game game) {
        return new ConsoleCanvas(cardIndexSpace + (Card.plotWidth + GamePlotter.cardSpacing[0]) * (game.getTableau().size() + 2), Card.plotHeight + GamePlotter.cardSpacing[1] * (Card.Rank.values().length + 2) + 2);
    }

}
