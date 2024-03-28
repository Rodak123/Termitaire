package com.rodak.termitaire;

import java.util.*;

public class Game {

    private final List<Stack<Card>> foundations;
    private final List<Stack<Card>> tableau;
    private final Stack<Card> waste;
    private final Stack<Card> stock;

    private final Stack<Card> selectedCardsPile;
    private Stack<Card> selectedCardsPileSource = null;

    private boolean playing;
    private boolean started;

    public Game() {
        playing = false;
        started = false;

        foundations = new ArrayList<>();
        tableau = new ArrayList<>();
        waste = new Stack<>();
        stock = new Stack<>();
        selectedCardsPile = new Stack<>();

        for (int i = 0; i < Card.Suit.values().length; i++) {
            foundations.add(new Stack<>());
        }
        for (int i = 0; i < 7; i++) {
            tableau.add(new Stack<>());
        }
    }

    public void newGame() {
        playing = true;
        started = true;

        waste.clear();
        stock.clear();
        selectedCardsPile.clear();
        for (Stack<Card> cards : tableau) {
            cards.clear();
        }
        for (Stack<Card> cards : foundations) {
            cards.clear();
        }

        Stack<Card> cards = Card.allCards();
        Collections.shuffle(cards);

        for (int i = 0; i < tableau.size(); i++) {
            for (int j = 0; j < 1 + i; j++) {
                tableau.get(i).add(cards.pop());
            }
            tableau.get(i).peek().show();
        }

        stock.addAll(cards);
    }

    public List<Stack<Card>> getFoundations() {
        return foundations;
    }

    public List<Stack<Card>> getTableau() {
        return tableau;
    }

    public Stack<Card> getWaste() {
        return waste;
    }

    public Stack<Card> getStock() {
        return stock;
    }

    public Stack<Card> getSelectedCardsPile() {
        return selectedCardsPile;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void addSelectedCardsToStack(Stack<Card> stack) {
        stack.addAll(selectedCardsPile);
        selectedCardsPile.clear();

        if (!selectedCardsPileSource.empty()) selectedCardsPileSource.peek().show();
        selectedCardsPileSource = null;
    }

    public List<Action> getActions() {
        List<Action> actions = new ArrayList<>();

        if (playing) {
            addFoundationsActions(actions);
            addTableauActions(actions);

            if (selectedCardsPile.empty()) {
                addWasteActions(actions);
                addStockActions(actions);
            } else {
                actions.add(new Action() {
                    @Override
                    public void execute(String key, int index) {
                        selectedCardsPileSource.addAll(selectedCardsPile);
                        selectedCardsPile.clear();
                        selectedCardsPileSource = null;
                    }

                    @Override
                    public String[] getCommands() {
                        ArrayList<String> commands = new ArrayList<>();
                        commands.add("unselect");
                        commands.addAll(List.of(GameBinds.Unselect));
                        return commands.toArray(new String[0]);
                    }

                    @Override
                    public String getInfo() {
                        return "Unselect all cards";
                    }
                });
            }

            actions.add(new Action() {
                @Override
                public void execute(String key, int index) {
                    playing = false;
                }

                @Override
                public String[] getCommands() {
                    return new String[]{"pause"};
                }

                @Override
                public String getInfo() {
                    return "Pause the game";
                }
            });
        } else if (started) {
            actions.add(new Action() {
                @Override
                public void execute(String key, int index) {
                    playing = true;
                }

                @Override
                public String[] getCommands() {
                    return new String[]{"play"};
                }

                @Override
                public String getInfo() {
                    return "Play the paused game";
                }
            });
        }

        actions.add(new Action() {
            @Override
            public void execute(String key, int index) {
                newGame();
            }

            @Override
            public String[] getCommands() {
                return new String[]{"new"};
            }

            @Override
            public String getInfo() {
                return "Create new game";
            }
        });

        return actions;
    }

    private void addStockActions(List<Action> actions) {
        if (!stock.empty()) {
            actions.add(new Action() {
                @Override
                public void execute(String key, int index) {
                    waste.add(stock.pop());
                    waste.peek().show();
                }

                @Override
                public String[] getCommands() {
                    ArrayList<String> commands = new ArrayList<>();
                    commands.add("draw");
                    commands.addAll(List.of(GameBinds.Stock));
                    return commands.toArray(new String[0]);
                }

                @Override
                public String getInfo() {
                    return "Draw a card";
                }
            });
        } else {
            actions.add(new Action() {
                @Override
                public void execute(String key, int index) {
                    stock.addAll(waste);
                    waste.clear();
                    for (Card card : stock) {
                        card.hide();
                    }
                    Collections.shuffle(stock);
                }

                @Override
                public String[] getCommands() {
                    ArrayList<String> commands = new ArrayList<>();
                    commands.add("redeal");
                    commands.addAll(List.of(GameBinds.Stock));
                    return commands.toArray(new String[0]);
                }

                @Override
                public String getInfo() {
                    return "Redeal the card from the waste to the stock";
                }
            });
        }
    }

    private void addFoundationsActions(List<Action> actions) {
        if (selectedCardsPile.size() == 1) {
            actions.add(new Action() {
                @Override
                public void execute(String key, int index) {
                    Stack<Card> foundation = foundations.get(index);
                    Card base = foundation.empty() ? null : foundation.peek();
                    Card card = selectedCardsPile.get(0);
                    if (Card.canCardStack(base, card, Card.CardStackRuleset.FOUNDATION)) {
                        addSelectedCardsToStack(foundation);
                    } else {
                        if (base == null) {
                            System.out.println(card + " is not a foundation starter");
                        } else {
                            System.out.println("Can't stack " + card + " on " + base);
                        }
                    }
                }

                @Override
                public String[] getCommands() {
                    return GameBinds.Foundations;
                }

                @Override
                public String getInfo() {
                    return "Put the selected card on a foundation";
                }
            });
        } else {
            actions.add(new Action() {
                @Override
                public void execute(String key, int index) {
                    Stack<Card> foundation = foundations.get(index);

                    if (foundation.empty()) {
                        System.out.println("Foundation is empty.");
                        return;
                    }

                    selectedCardsPile.add(foundation.pop());
                    selectedCardsPileSource = foundation;
                }

                @Override
                public String[] getCommands() {
                    return GameBinds.Foundations;
                }

                @Override
                public String getInfo() {
                    return "Select a card from a foundation";
                }
            });
        }
    }

    private void addTableauActions(List<Action> actions) {
        if (selectedCardsPile.empty()) {
            actions.add(new Action() {
                @Override
                public void execute(String key, int index) {
                    Stack<Card> column = tableau.get(index);
                    int initialSize = column.size();
                    if (column.empty()) {
                        System.out.println("Column is empty.");
                        return;
                    }

                    if (column.size() == 1) {
                        selectedCardsPile.add(column.pop());
                    } else {
                        String[] cardInput = ActionInput.promptInput("Which card to select? ")
                                .toLowerCase().strip().split(" ");

                        if (cardInput.length == 1 && cardInput[0].length() == 0) {
                            Card card = column.pop();
                            selectedCardsPile.add(card);
                        } else if (cardInput.length == 1) {
                            try {
                                int cardIndex = Integer.parseInt(cardInput[0]);
                                if (cardIndex <= 0) {
                                    System.out.println("No cards where taken.");
                                }

                                while (cardIndex < column.size()) {
                                    if (column.get(cardIndex).isHidden()) {
                                        cardIndex++;
                                        continue;
                                    }
                                    selectedCardsPile.add(column.remove(cardIndex));
                                }
                            } catch (NumberFormatException e) {
                                System.out.println(cardInput[0] + " is not a valid index.");
                            }
                        } else if (cardInput.length == 2) {
                            for (int i = column.size() - 1; i >= 0; i--) {
                                Card card = column.get(i);
                                if (card.isHidden()) break;
                                if (
                                        card.rank.getShortName().toLowerCase().equals(cardInput[1]) &&
                                                card.suit.getShortName().toLowerCase().equals(cardInput[0])
                                ) {
                                    while (i < column.size()) {
                                        selectedCardsPile.add(column.remove(i));
                                    }
                                    break;
                                }
                            }
                        } else {
                            System.out.println("No card found.");
                        }
                    }

                    if (column.size() != initialSize) {
                        selectedCardsPileSource = column;
                    }
                }

                @Override
                public String[] getCommands() {
                    return GameBinds.Tableau;
                }

                @Override
                public boolean clearAfter() {
                    return true;
                }

                @Override
                public String getInfo() {
                    return "Select card/s from tableau";
                }
            });
        } else {
            actions.add(new Action() {
                @Override
                public void execute(String key, int index) {
                    Stack<Card> column = tableau.get(index);
                    Card base = column.empty() ? null : column.peek();
                    Card card = selectedCardsPile.get(0);
                    if (Card.canCardStack(base, card, Card.CardStackRuleset.TABLEAU)) {
                        addSelectedCardsToStack(column);
                    } else {
                        if (base == null) {
                            System.out.println(card + " is not a tableau column starter");
                        } else {
                            System.out.println("Can't stack " + card + " on " + base);
                        }
                    }
                }

                @Override
                public String[] getCommands() {
                    return GameBinds.Tableau;
                }

                @Override
                public String getInfo() {
                    return "Add the selected " + (selectedCardsPile.size() == 1 ? "card" : "cards") + " to tableau";
                }
            });
        }
    }


    private void addWasteActions(List<Action> actions) {
        if (!waste.empty()) {
            actions.add(new Action() {
                @Override
                public void execute(String key, int index) {
                    selectedCardsPile.add(waste.pop());
                    selectedCardsPileSource = waste;
                }

                @Override
                public String[] getCommands() {
                    return GameBinds.Waste;
                }

                @Override
                public String getInfo() {
                    return "Select a card from the waste";
                }
            });
        }
    }
}
