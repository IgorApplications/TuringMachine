package com.iapp.turingmachine.view;

import static com.iapp.turingmachine.view.TuringConstants.*;

import android.app.Activity;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import com.iapp.turingmachine.R;
import com.iapp.turingmachine.backend.TuringMachine;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private final List<LinearLayout> vertical = new ArrayList<>();
    private final List<TextView> tape = new ArrayList<>();
    private final List<TextView> tapeText = new ArrayList<>();

    private TextView alphabetText;
    private LinearLayout registers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashScreen.installSplashScreen(this);
        setContentView(R.layout.activity_main);

        alphabetText = findViewById(R.id.alphabet_text);
        registers = findViewById(R.id.registers);
        vertical.add(findViewById(R.id.alphabet));
        vertical.add(findViewById(R.id.q0));

        tape.add(findViewById(R.id.tape0));
        tape.add(findViewById(R.id.tape1));
        tape.add(findViewById(R.id.tape2));
        tape.add(findViewById(R.id.tape3));
        tape.add(findViewById(R.id.tape4));
        tape.add(findViewById(R.id.tape5));
        tape.add(findViewById(R.id.tape6));
        tape.add(findViewById(R.id.tape7));
        tape.add(findViewById(R.id.tape8));
        tape.add(findViewById(R.id.tape9));
        tape.add(findViewById(R.id.tape10));

        tapeText.add(findViewById(R.id.tapeText0));
        tapeText.add(findViewById(R.id.tapeText1));
        tapeText.add(findViewById(R.id.tapeText2));
        tapeText.add(findViewById(R.id.tapeText3));
        tapeText.add(findViewById(R.id.tapeText4));
        tapeText.add(findViewById(R.id.tapeText5));
        tapeText.add(findViewById(R.id.tapeText6));
        tapeText.add(findViewById(R.id.tapeText7));
        tapeText.add(findViewById(R.id.tapeText8));
        tapeText.add(findViewById(R.id.tapeText9));
        tapeText.add(findViewById(R.id.tapeText10));

        // for restart activity
        backendStateRegister.put("q0", new HashMap<>());
        
        //updateTape(backendTape, startTape);
        //updateRegister(backendStateRegister);
        //alphabetText.setText(alphabet);
    }

    public void launch(View view) {
        Map<String, List<String>> stateRegister = new HashMap<>();

        for (Map.Entry<String, Map<String, String>> entry : backendStateRegister.entrySet()) {
            List<String> q = new ArrayList<>();
            Map<String, String> alpha = entry.getValue();

            for (char c : (alphabet.substring(1) + "_").toCharArray()) {
                String key = String.valueOf(c);

                if (alpha.containsKey(key)) {
                    q.add(alpha.get(key));
                } else {
                    q.add("");
                }

            }
            stateRegister.put(entry.getKey(), q);
        }

        try {
            TuringMachine turingMachine = new TuringMachine(0,
                    // without underline
                    alphabet.substring(1),
                    backendTape,
                    stateRegister,
                    "q0");
            turingMachine.execute();
            updateTape(turingMachine.getCurrentState(), startTape);

        } catch (Throwable t) {
            showError(getString(R.string.error_inner_machine), t.toString());
        }
    }

    public void updateTape(Map<Integer, Character> state, int newStartTape) {
        backendTape = state;

        if (startTape != newStartTape) {
            startTape = newStartTape;

            int start = newStartTape;
            for (TextView view : tapeText) {
                view.setText(String.valueOf(start));
                start++;
            }
        }

        int start = newStartTape;
        for (TextView view : tape) {
            view.setText(getBackendTape(start));
            start++;
        }
    }

    public void updateRegister(Map<String, Map<String, String>> state) {
        backendStateRegister = state;

        for (int i = 1; i < vertical.size(); i++) {
            LinearLayout vert = vertical.get(i);
            String qName = ((TextView) vert.getChildAt(0)).getText().toString();

            for (int j = 1; j < vert.getChildCount(); j++) {
                String alphaName = String.valueOf(alphabet.charAt(j - 1));
                String res = getBackendStateRegister(qName, alphaName);
                ((TextView) vert.getChildAt(j)).setText(res);
            }
        }
    }

    // ----------------------------------------------------------

    public void changeRegister(View view) {

        EditText editText = new EditText(this);
        editText.setText(((TextView) view).getText());

        AlertDialog confirmDialog =
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.change_register))
                        .setView(editText)
                        .setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        })
                        .setPositiveButton(getString(R.string.confirm), (dialog, which) -> {

                            int vert = -1;
                            int horiz = -1;
                            for (int i = 0; i < vertical.size(); i++) {
                                // link equal
                                if (vertical.get(i) == view.getParent()) {
                                    vert = i;
                                    break;
                                }
                            }

                            if (vert == -1) {
                                throw new IllegalStateException("changeRegister: vert == -1 (q index)");
                            }

                            LinearLayout layout = vertical.get(vert);
                            for (int i = 0; i < layout.getChildCount(); i++) {
                                // link equal
                                if (layout.getChildAt(i) == view) {
                                    horiz = i;
                                    break;
                                }
                            }

                            if (horiz == -1) {
                                throw new IllegalStateException("changeRegister: horiz == -1 (alphabet index)");
                            }

                            String q = "q" + (vert - 1);
                            String alphaChar = String.valueOf(alphabet.charAt(horiz - 1));

                            Pair<Boolean, String> pair = isValidRegister(editText.getText().toString());
                            if (pair.first) {
                                setBackendStateRegister(q, alphaChar, editText.getText().toString());
                                ((TextView) view).setText(editText.getText().toString());
                            } else {
                                showError(getString(R.string.command_error), pair.second);
                            }

                        })
                        .create();
        confirmDialog.show();
        confirmDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.main_blue));
        confirmDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.main_blue));
    }

    public void makeLeftMove(View view) {
        startTape--;
        for (TextView textView : tapeText) {
            textView.setText(String.valueOf(Integer.parseInt(textView.getText().toString()) - 1));
        }
        for (int i = 0; i < tape.size(); i++) {
            tape.get(i).setText(getBackendTape(i + startTape));
        }
    }

    public void makeRightMove(View view) {
        startTape++;
        for (TextView textView : tapeText) {
            textView.setText(String.valueOf(Integer.parseInt(textView.getText().toString()) + 1));
        }
        for (int i = 0; i < tape.size(); i++) {
            tape.get(i).setText(getBackendTape(i + startTape));
        }
    }

    public void changeInfTape(View view) {

        char[] arr = alphabet.toCharArray();
        String[] strArr = new String[arr.length];
        for (int i = 0; i < arr.length; i++) {
            strArr[i] = String.valueOf(arr[i]);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.select_char));
        builder.setItems(strArr, (dialog, which) -> {

            // endless belt shift (+ startTape)
            int i = findIndexOfTape(view) + startTape;
            setBackendTape(i, strArr[which].charAt(0));
            ((TextView) view).setText(strArr[which]);
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void changeAlphabet(View view) {

        EditText editText = new EditText(this);
        editText.setText(alphabet.substring(1));

        AlertDialog confirmDialog =
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.change_alphabet))
                        .setView(editText)
                        .setMessage(getString(R.string.warn_change_alphabet))
                        .setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        })
                        .setPositiveButton(getString(R.string.confirm), (dialog, which) -> {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                            String editStr = editText.getText().toString();
                            Pair<Boolean, String> res = isValid(editStr);

                            if (res.first) {
                                String newAlphabet = "_" + editText.getText().toString();
                                changeAlphabet(alphabet, newAlphabet);

                                alphabet = newAlphabet;
                                alphabetText.setText(alphabet);
                                Toast.makeText(this, getString(R.string.done), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, res.second, Toast.LENGTH_SHORT).show();
                            }

                        })
                        .create();
        confirmDialog.show();
        confirmDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.main_blue));
        confirmDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.main_blue));
    }

    public void addRegister(View view) {

        View.inflate(this, R.layout.register_titile, registers);

        LinearLayout q = (LinearLayout) registers.getChildAt(vertical.size());
        vertical.add(q);

        TextView title = (TextView) q.getChildAt(0);
        String qText = "q" + (lastQ + 1);
        title.setText(qText);
        lastQ++;
        // Announcement of the existence of the register
        backendStateRegister.put(qText, new HashMap<>());

        for (int i = 0; i < alphabet.length(); i++) {
            View.inflate(this, R.layout.alphabet_cell, q);
        }
    }

    private void changeAlphabet(String last, String newAlpha) {

        if (newAlpha.startsWith(last)) {
            safetyChangeAlphabet(newAlpha.substring(last.length()));
            return;
        }

        // The most important place!
        // Be careful!
        backendTape.clear();
        backendStateRegister.clear();
        for (int i = 0; i < tape.size(); i++) {
            tape.get(i).setText(getBackendTape(i + startTape));
        }


        for (LinearLayout el : vertical) {
            if (el.getChildCount() > 2) {
                el.removeViews(2, el.getChildCount() - 2);
            }
        }

        safetyChangeAlphabet(newAlpha.substring(1));
    }

    private void safetyChangeAlphabet(String add) {

        for (int i = 0; i < registers.getChildCount(); i++) {
            LinearLayout q = (LinearLayout) registers.getChildAt(i);


            for (char c : add.toCharArray()) {
                View.inflate(this, R.layout.alphabet_cell, q);
                if (i == 0) {
                    TextView text = (TextView) q.getChildAt(q.getChildCount() - 1);
                    text.setText(String.valueOf(c));
                }
            }
        }
    }

    private Pair<Boolean, String> isValid(String text) {
        Map<Character, Integer> count = new HashMap<>();
        for (char c : text.toCharArray()) {
            if (count.containsKey(c)) {
                return new Pair<>(false, getString(R.string.repeating_chars));
            } else {
                count.put(c, 1);
            }
        }

        return new Pair<>(true, "");
    }

    private int findIndexOfTape(View view) {
        for (int i = 0; i < tape.size(); i++) {
            // equals by link!
            if (tape.get(i) == view) {
                return i;
            }
        }
        throw new IllegalStateException("findIndexOfTape == -1");
    }

    private void setBackendTape(int i, char val) {
        if (val == '_') {
            return;
        }
        backendTape.put(i, val);
    }

    private String getBackendTape(int i) {
        if (!backendTape.containsKey(i)) {
            return "_";
        }
        return String.valueOf(backendTape.get(i));
    }

    private String getBackendStateRegister(String q, String alphaChar) {
        Map<String, String> inner = backendStateRegister.get(q);
        if (inner == null) return "";
        if (!inner.containsKey(alphaChar)) return "";
        return inner.get(alphaChar);
    }

    private void setBackendStateRegister(String q, String alphaChar, String val) {
        Map<String, String> inner = backendStateRegister.get(q);

        if (inner != null) {
            inner.put(alphaChar, val);
        } else {
            inner = new HashMap<>();
            inner.put(alphaChar, val);
            backendStateRegister.put(q, inner);
        }
    }

    private void showError(String title, String log) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(log)
                .setPositiveButton(getString(R.string.ok), (dialog1, which) -> {})
                .create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.main_blue));
    }

    private Pair<Boolean, String> isValidRegister(String register) {
        if (register.isEmpty()) return new Pair<>(true, "");

        String explain = getString(R.string.explain_command);
        String[] tokens = register.split(" ");
        if (tokens.length != 3) {
            return new Pair<>(false, explain);
        }

        if (!backendStateRegister.containsKey(tokens[0])) {
            return new Pair<>(false, explain);
        }

        if (!alphabet.contains(tokens[1]) || tokens[1].length() != 1) {
            return new Pair<>(false, explain);
        }

        if (tokens[2].length() != 1 || !"LRN".contains(tokens[2])) {
            return new Pair<>(false, explain);
        }

        return new Pair<>(true, "");
    }
}