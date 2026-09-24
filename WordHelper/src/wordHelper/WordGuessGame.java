package wordHelper;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JFrame front end for the word-guessing helper.
 *
 * The user picks Wordle (5 letters, 6 rows) or Jeff Goldblum (12
 * letters, 3 rows). Each row has a text field to type a guess; the
 * typed letters appear in colored boxes below the field. Clicking a
 * box cycles its color white -> gray -> yellow -> green -> white,
 * matching Wordle's rules (gray = letter not in the word, yellow =
 * right letter/wrong spot, green = right letter/right spot). The
 * "Update Results" button re-filters the word list using every row's
 * current letters and colors.
 */
public class WordGuessGame extends JFrame {

    private static final long serialVersionUID = 1L;

	private static final Color BACKGROUND = new Color(245, 245, 245);

    private JPanel cards;
    private CardLayout cardLayout;

    // Game-panel state (rebuilt each time a mode is started)
    private LetterBox[][] boxes;
    private JTextField[] rowFields;
    private List<String> fullWordList;
    private int wordLength;
    private int rowCount;
    private JLabel resultCountLabel;
    private DefaultListModel<String> resultListModel;
    private JPanel currentGamePanel;

    public WordGuessGame() {
        super("Word Guess Helper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);
        cards.add(buildMenuPanel(), "menu");
        add(cards);
    }

    // ---------------------------------------------------------------
    // Menu screen
    // ---------------------------------------------------------------

    private JPanel buildMenuPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BACKGROUND);
        panel.setLayout(new GridBagLayout());

        JPanel inner = new JPanel();
        inner.setBackground(BACKGROUND);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Word Guess Helper");
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Choose a game to narrow down the answer");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 16));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton wordleBtn = new JButton("Wordle (5 letters, 6 tries)");
        JButton jeffBtn = new JButton("Jeff Goldblum (12 letters, 3 tries)");
        for (JButton b : new JButton[]{wordleBtn, jeffBtn}) {
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.setFont(new Font("SansSerif", Font.PLAIN, 18));
            b.setMaximumSize(new Dimension(320, 45));
        }

        wordleBtn.addActionListener(e -> startGame(5, 6, "wordle.csv"));
        jeffBtn.addActionListener(e -> startGame(12, 3, "jeff.csv"));

        inner.add(title);
        inner.add(Box.createVerticalStrut(8));
        inner.add(subtitle);
        inner.add(Box.createVerticalStrut(30));
        inner.add(wordleBtn);
        inner.add(Box.createVerticalStrut(15));
        inner.add(jeffBtn);

        panel.add(inner);
        return panel;
    }

    // ---------------------------------------------------------------
    // Game screen
    // ---------------------------------------------------------------

    private void startGame(int wordLength, int rows, String csvFile) {
        try {
            fullWordList = WordFilterEngine.loadWords(csvFile);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Couldn't load " + csvFile + " from the current folder:\n" + ex.getMessage(),
                    "File not found", JOptionPane.ERROR_MESSAGE);
            return;
        }

        this.wordLength = wordLength;
        this.rowCount = rows;
        this.boxes = new LetterBox[rows][wordLength];
        this.rowFields = new JTextField[rows];

        JPanel gamePanel = new JPanel(new BorderLayout(10, 10));
        gamePanel.setBackground(BACKGROUND);
        gamePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        gamePanel.add(buildTopBar(csvFile), BorderLayout.NORTH);
        gamePanel.add(buildRowsPanel(rows), BorderLayout.CENTER);
        gamePanel.add(buildResultsPanel(), BorderLayout.SOUTH);

        if (currentGamePanel != null) {
            cards.remove(currentGamePanel);
        }
        currentGamePanel = gamePanel;
        cards.add(gamePanel, "game");
        cardLayout.show(cards, "game");
        updateResults();
    }

    private JPanel buildTopBar(String csvFile) {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(BACKGROUND);

        JLabel instructions = new JLabel(
                "<html>Type a guess in each row, then click a box to cycle its color: "
                        + "white &rarr; gray (not in word) &rarr; yellow (wrong spot) &rarr; green (correct spot). "
                        + "Click \"Update Results\" any time.</html>");
        instructions.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JButton backBtn = new JButton("Back to Menu");
        backBtn.addActionListener(e -> cardLayout.show(cards, "menu"));

        top.add(instructions, BorderLayout.CENTER);
        top.add(backBtn, BorderLayout.EAST);
        return top;
    }

    private JPanel buildRowsPanel(int rows) {
        JPanel rowsPanel = new JPanel();
        rowsPanel.setBackground(BACKGROUND);
        rowsPanel.setLayout(new BoxLayout(rowsPanel, BoxLayout.Y_AXIS));

        for (int r = 0; r < rows; r++) {
            rowsPanel.add(buildRow(r));
            rowsPanel.add(Box.createVerticalStrut(8));
        }

        JScrollPane scroll = new JScrollPane(rowsPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BACKGROUND);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildRow(int rowIndex) {
        JPanel row = new JPanel();
        row.setBackground(BACKGROUND);
        row.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 4));

        JLabel rowLabel = new JLabel("Guess " + (rowIndex + 1) + ":");
        rowLabel.setPreferredSize(new Dimension(70, 25));

        JTextField field = new JTextField(wordLength + 2);
        limitToLettersUppercase(field, wordLength);
        rowFields[rowIndex] = field;

        JPanel boxesPanel = new JPanel(new GridLayout(1, wordLength, 4, 0));
        boxesPanel.setBackground(BACKGROUND);
        for (int c = 0; c < wordLength; c++) {
            LetterBox box = new LetterBox();
            boxes[rowIndex][c] = box;
            boxesPanel.add(box);
        }

        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { syncRow(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { syncRow(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { syncRow(); }

            private void syncRow() {
                String text = field.getText().toUpperCase();
                for (int c = 0; c < wordLength; c++) {
                    char ch = c < text.length() ? text.charAt(c) : '\0';
                    boxes[rowIndex][c].setLetter(ch);
                }
            }
        });

        row.add(rowLabel);
        row.add(field);
        row.add(boxesPanel);
        return row;
    }

    /** Restricts a text field to letters only, up to maxLen characters. */
    private void limitToLettersUppercase(JTextField field, int maxLen) {
        PlainDocument doc = (PlainDocument) field.getDocument();
        doc.setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
                    throws BadLocationException {
                String filtered = filterText(text);
                if (filtered.isEmpty()) return;
                int currentLen = fb.getDocument().getLength();
                int room = maxLen - currentLen;
                if (room <= 0) return;
                if (filtered.length() > room) filtered = filtered.substring(0, room);
                super.insertString(fb, offset, filtered, attr);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attr)
                    throws BadLocationException {
                String filtered = filterText(text);
                int currentLen = fb.getDocument().getLength() - length;
                int room = maxLen - currentLen;
                if (room < 0) room = 0;
                if (filtered.length() > room) filtered = filtered.substring(0, room);
                super.replace(fb, offset, length, filtered, attr);
            }

            private String filterText(String text) {
                StringBuilder sb = new StringBuilder();
                for (char c : text.toCharArray()) {
                    if (Character.isLetter(c)) sb.append(Character.toUpperCase(c));
                }
                return sb.toString();
            }
        });
    }

    private JPanel buildResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panel.setPreferredSize(new Dimension(800, 220));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonRow.setBackground(BACKGROUND);
        JButton updateBtn = new JButton("Update Results");
        updateBtn.addActionListener((ActionEvent e) -> updateResults());
        JButton resetBtn = new JButton("Reset All Boxes");
        resetBtn.addActionListener(e -> resetAll());
        resultCountLabel = new JLabel("Possible words: --");
        resultCountLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        buttonRow.add(updateBtn);
        buttonRow.add(resetBtn);
        buttonRow.add(Box.createHorizontalStrut(20));
        buttonRow.add(resultCountLabel);

        resultListModel = new DefaultListModel<>();
        JList<String> resultList = new JList<>(resultListModel);
        resultList.setFont(new Font("Monospaced", Font.PLAIN, 14));
        resultList.setVisibleRowCount(8);
        JScrollPane listScroll = new JScrollPane(resultList);

        panel.add(buttonRow, BorderLayout.NORTH);
        panel.add(listScroll, BorderLayout.CENTER);
        return panel;
    }

    private void resetAll() {
        for (int r = 0; r < rowCount; r++) {
            rowFields[r].setText("");
            for (int c = 0; c < wordLength; c++) {
                boxes[r][c].reset();
            }
        }
        updateResults();
    }

    private void updateResults() {
        List<WordFilterEngine.Guess> guesses = new ArrayList<>();
        for (int r = 0; r < rowCount; r++) {
            char[] letters = new char[wordLength];
            int[] states = new int[wordLength];
            boolean anyLetter = false;
            for (int c = 0; c < wordLength; c++) {
                letters[c] = boxes[r][c].getLetter();
                states[c] = boxes[r][c].getState();
                if (letters[c] != '\0') anyLetter = true;
            }
            if (anyLetter) {
                guesses.add(new WordFilterEngine.Guess(letters, states));
            }
        }

        List<String> matches = WordFilterEngine.filter(fullWordList, guesses);
        Collections.sort(matches);

        resultListModel.clear();
        int shown = Math.min(matches.size(), 500);
        for (int i = 0; i < shown; i++) {
            resultListModel.addElement(matches.get(i));
        }
        String suffix = matches.size() > shown ? "  (showing first " + shown + ")" : "";
        resultCountLabel.setText("Possible words: " + matches.size() + suffix);
    }

    // ---------------------------------------------------------------

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WordGuessGame game = new WordGuessGame();
            game.setVisible(true);
        });
    }
}