package wordHelper;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

/**
 * A single letter cell in the guess grid.
 *
 * The letter itself is set programmatically (it mirrors what the user
 * types into that row's text field). Clicking the box cycles its
 * color: white -> gray -> yellow -> green -> white -> ...
 */
public class LetterBox extends JButton {

    private static final Color BORDER_COLOR = new Color(120, 124, 126);
    private static final Color[] STATE_COLORS = {
            Color.WHITE,                  // 0 EMPTY / unmarked
            new Color(120, 124, 126),     // 1 GRAY
            new Color(201, 180, 88),      // 2 YELLOW
            new Color(106, 170, 100)      // 3 GREEN
    };

    private int state = WordFilterEngine.EMPTY;
    private char letter = '\0';

    public LetterBox() {
        setPreferredSize(new Dimension(48, 48));
        setFont(new Font("SansSerif", Font.BOLD, 20));
        setFocusPainted(false);
        setOpaque(true);
        setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 2));
        applyColor();

        addActionListener(e -> cycleState());
    }

    private void cycleState() {
        if (letter == '\0') {
            return; // nothing typed in this box yet, nothing to mark
        }
        state = (state + 1) % 4;
        applyColor();
    }

    private void applyColor() {
        setBackground(STATE_COLORS[state]);
        setForeground(state == WordFilterEngine.EMPTY ? Color.BLACK : Color.WHITE);
    }

    /** Sets the letter shown in this box, resetting its color to white/unmarked. */
    public void setLetter(char c) {
        this.letter = (c == '\0') ? '\0' : Character.toUpperCase(c);
        setText(this.letter == '\0' ? "" : String.valueOf(this.letter));
        state = WordFilterEngine.EMPTY;
        applyColor();
    }

    public char getLetter() {
        return letter;
    }

    /** Returns EMPTY if this box has no letter yet, regardless of click history. */
    public int getState() {
        return (letter == '\0') ? WordFilterEngine.EMPTY : state;
    }

    public void reset() {
        letter = '\0';
        state = WordFilterEngine.EMPTY;
        setText("");
        applyColor();
    }
}