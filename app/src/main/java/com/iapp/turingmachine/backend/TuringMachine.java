package com.iapp.turingmachine.backend;

import java.util.*;

public class TuringMachine {

    private final List<Character> endlessTapeNotNeg = new ArrayList<>(100);
    private final List<Character> endlessTapeNeg =  new ArrayList<>(100);
    private final String alphabet;
    private final Map<String, List<String>> stateRegister;
    private int head;
    private String nameStateRegister;
    private boolean finished = false;


    public Map<Integer, Character> getCurrentState() {
        Map<Integer, Character> state = new HashMap<>();

        for (int i = 0; i < endlessTapeNotNeg.size(); i++) {
            char val = endlessTapeNotNeg.get(i);
            if (val != '_') {
                state.put(i, endlessTapeNotNeg.get(i));
            }
        }

        for (int i = 0; i < endlessTapeNeg.size(); i++) {
            char val = endlessTapeNeg.get(i);
            if (val != '_') {
                state.put(i * - 1, val);
            }
        }

        return state;
    }

    public void execute() {
        if (finished) {
            throw new IllegalStateException("The Turing machine has already completed its work");
        }

        // at this point the validity of all values has been checked in the constructor
        // The Turing machine can operate assuming that everything is filled in correctly

        while (!finished) {
            int index = alphabet.indexOf(get(head));
            executeCommands(stateRegister.get(nameStateRegister).get(index));
        }
    }

    private void executeCommands(String lineCommands) {
        // void allowed
        if (lineCommands.length() == 0) return;

        String[] commands = lineCommands.split(" ");

        // transition to a new state
        nameStateRegister = commands[0];
        // replace the symbol in the endless tape with a new one at the index pointed to by the head
        set(head, commands[1].charAt(0));

        if (commands.length == 3) {
            switch (commands[2]) {
                case "L":
                    head--;
                    break;
                case "R":
                    head++;
                    break;
                case "N":
                    finished = true;
                    break;
            }
        }

    }

    public TuringMachine(int head, String alphabet, Map<Integer,
            Character> startValue, Map<String, List<String>> stateRegister,
                          String startNameStateRegister) {

        for (int i = 0; i < 100; i++) {
            endlessTapeNeg.add('_');
            endlessTapeNotNeg.add('_');
        }
        this.head = head;
        this.alphabet = alphabet + "_";
        nameStateRegister = startNameStateRegister;

        for (Map.Entry<Integer, Character> entry : startValue.entrySet()) {
            if (!alphabet.contains(entry.getValue().toString())) {
                throw new IllegalArgumentException("The initial values of the bexon tape contain elements that are not in the alphabet!");
            }
            set(entry.getKey(), entry.getValue());
        }

        checkValidity(stateRegister);
        // close external access to the link by copying
        this.stateRegister = new HashMap<>(stateRegister);

        if (!stateRegister.containsKey(nameStateRegister)) {
            throw new IllegalArgumentException("Non-existent initial state");
        }
    }

    private void set(int index, char value) {
        if (index >= 0) {
            if (index >= endlessTapeNotNeg.size()) {
                expand(endlessTapeNotNeg,  index - endlessTapeNotNeg.size() + 1);
            }

            endlessTapeNotNeg.set(index, value);
            return;
        }

        index = Math.abs(index);
        if (index >= endlessTapeNeg.size()) {
            expand(endlessTapeNeg,  index - endlessTapeNeg.size() + 1);
        }

        endlessTapeNeg.set(index, value);
    }

    private char get(int index) {
        if (index >= 0) {
            if (index >= endlessTapeNotNeg.size()) {
                expand(endlessTapeNotNeg,  index - endlessTapeNotNeg.size() + 1);
            }

            return endlessTapeNotNeg.get(index);
        }

        index = Math.abs(index);
        if (index >= endlessTapeNeg.size()) {
            expand(endlessTapeNeg,  index - endlessTapeNeg.size() + 1);
        }


        return endlessTapeNeg.get(index);
    }

    private void expand(List<Character> list, int dif) {
        for (int i = 0; i < dif; i++) {
            list.add('_');
        }
    }

    private void checkValidity(Map<String, List<String>> stateRegister) {
        Set<String> names = stateRegister.keySet();

        for (String q : names) {

            List<String> alpha = stateRegister.get(q);
            if (alpha.size() != alphabet.length()) {
                throw new IllegalArgumentException("In column " + q + " The meaning is not defined for every letter of the alphabet!");
            }

            for (String line : alpha) {
                // разрешена пустота
                if (line.length() == 0) continue;

                String[] commands = line.split(" ");

                if (commands.length < 2) {
                    throw new IllegalArgumentException("In field " + line + " total " + commands.length + " commands");
                }

                if (!names.contains(commands[0])) {
                    throw new IllegalArgumentException("In field " + line + " contains an error: " + " transition to an unknown state " + commands[0]);
                }

                if (!alphabet.contains(commands[1])) {
                    throw new IllegalArgumentException("In field " + line + " contains an error: " + " replacing with a character that is not in the alphabet - " + commands[1]);
                }

                if (commands[1].length() != 1) {
                    throw new IllegalArgumentException("You cannot replace a character with strings (more than one character) in a field" + line);
                }

                if (commands.length == 3) {
                    if (!commands[2].equals("L") && !commands[2].equals("R") && !commands[2].equals("N")) {
                        throw new IllegalArgumentException("In field " + line + " defunct third team - " + commands[2]);
                    }
                }

                if (commands.length > 3) {
                    throw new IllegalArgumentException("In field " + line + " contains extra commands");
                }

            }

        }
    }

    public static void main(String[] args) {

        String alphabet = "0123";
        Map<Integer, Character> startValue = new HashMap<>();
        startValue.put(1, '0');
        startValue.put(2, '1');
        startValue.put(3, '2');
        startValue.put(4, '1');
        startValue.put(5, '1');
        startValue.put(6, '2');

        Map<String, List<String>> stateRegister = new HashMap<>();

        List<String> q0 = new ArrayList<>();
        q0.add("");       // 0
        q0.add("");       // 1
        q0.add("");       // 2
        q0.add("q1 3 R"); // 3
        q0.add("q1 _ R"); // _

        List<String> q1 = new ArrayList<>();
        q1.add("q1 1 R"); // 0
        q1.add("q1 2 R"); // 1
        q1.add("q1 3 R"); // 2
        q1.add("q1 3 R"); // 3
        q1.add("q1 _ N"); // _

        stateRegister.put("q0", q0);
        stateRegister.put("q1", q1);

        TuringMachine turingMachine = new TuringMachine(0, alphabet, startValue, stateRegister, "q0");
        // Before execution
        System.out.println(turingMachine);

        turingMachine.execute();
        // After execution
        System.out.println(turingMachine);
    }

    @Override
    public String toString() {
        return endlessTapeNeg + endlessTapeNotNeg.toString();
    }
}
