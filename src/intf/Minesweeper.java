/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package intf;

/**
 *
 * @author acer
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class Minesweeper extends JFrame {
    private JButton[][] buttons;
    private int[][] board;
    private boolean[][] revealed;
    private boolean[][] flagged;
    private int rows, cols, mines;
    private boolean gameOver = false;
    private boolean firstClick = true;

    public Minesweeper(int variation) {
        // Determine grid size and mine count based on the variation
        if (variation == 1) {
            rows = 10;
            cols = 10;
            mines = 10;
        } else if (variation == 2) {
            rows = 15;
            cols = 15;
            mines = 20;
        }
        initialize();
    }

    private void initialize() {
        setTitle("MineSweeper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Menu bar for game options
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");
        JMenuItem withdrawItem = new JMenuItem("Withdraw");
        withdrawItem.addActionListener(e -> handleWithdraw());
        gameMenu.add(withdrawItem);
        menuBar.add(gameMenu);
        setJMenuBar(menuBar);

        JPanel gridPanel = new JPanel(new GridLayout(rows, cols));
        buttons = new JButton[rows][cols];
        board = new int[rows][cols];
        revealed = new boolean[rows][cols];
        flagged = new boolean[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                buttons[r][c] = new JButton();
                buttons[r][c].setPreferredSize(new Dimension(50, 50));
                buttons[r][c].setFont(new Font("Arial", Font.BOLD, 24));
                buttons[r][c].setMargin(new Insets(0, 0, 0, 0));
                buttons[r][c].setBorder(BorderFactory.createRaisedBevelBorder());
                buttons[r][c].setBackground(new Color(192, 192, 192));
                buttons[r][c].setFocusPainted(false);
                buttons[r][c].addMouseListener(new CellClickHandler(r, c));
                gridPanel.add(buttons[r][c]);
            }
        }

        add(gridPanel, BorderLayout.CENTER);

        // Timer Panel
        JPanel timerPanel = new JPanel();
        JLabel timerLabel = new JLabel("Time: 0");
        timerPanel.add(timerLabel);
        add(timerPanel, BorderLayout.NORTH);

        Timer timer = new Timer(1000, e -> {
            if (!gameOver) {
                String text = timerLabel.getText();
                int currentTime = Integer.parseInt(text.split(": ")[1]) + 1;
                timerLabel.setText("Time: " + currentTime);
            }
        });
        timer.start();

        pack();
        setLocationRelativeTo(null);
    }

    private void placeMines(int firstRow, int firstCol) {
        Random rand = new Random();
        int placedMines = 0;

        while (placedMines < mines) {
            int r = rand.nextInt(rows);
            int c = rand.nextInt(cols);

            if (board[r][c] != -1 && !(r == firstRow && c == firstCol)) {
                board[r][c] = -1; // -1 represents a mine
                placedMines++;
            }
        }
        calculateNumbers();
    }

    private void handleWithdraw() {
        int choice = JOptionPane.showOptionDialog(
            this,
            "Do you want to quit the game or start a new game?",
            "Withdraw",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            new Object[]{"Quit", "New Game"},
            "Quit"
        );

        if (choice == JOptionPane.YES_OPTION) {
            System.exit(0); // Quit the application
        } else if (choice == JOptionPane.NO_OPTION) {
            restartGame(); // Start a new game
        }
    }

    private void restartGame() {
        dispose(); // Close the current window
        SwingUtilities.invokeLater(() -> {
            Minesweeper newGame = new Minesweeper(rows == 10 ? 1 : 2); // Restart with the same variation
            newGame.setVisible(true);
        });
    }

    private void calculateNumbers() {
        int[] dr = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dc = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (board[r][c] == -1) continue;

                int mineCount = 0;
                for (int i = 0; i < dr.length; i++) {
                    int nr = r + dr[i];
                    int nc = c + dc[i];

                    if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && board[nr][nc] == -1) {
                        mineCount++;
                    }
                }
                board[r][c] = mineCount;
            }
        }
    }

    private void revealCell(int r, int c) {
        if (r < 0 || r >= rows || c < 0 || c >= cols || revealed[r][c] || flagged[r][c]) return;

        revealed[r][c] = true;
        buttons[r][c].setBorder(BorderFactory.createLoweredBevelBorder());
        buttons[r][c].setBackground(new Color(220, 220, 220));

        if (board[r][c] == -1) { // Mine clicked
            buttons[r][c].setText("");
            buttons[r][c].setBackground(Color.RED);
            gameOver = true;
            revealAllMines();
            JOptionPane.showMessageDialog(this, "Game Over! You hit a mine!");
            return;
        }

        if (board[r][c] > 0) { // Number cell
            buttons[r][c].setText(String.valueOf(board[r][c]));
            buttons[r][c].setForeground(getNumberColor(board[r][c]));
        } else { // Blank tile
            buttons[r][c].setText("");
            int[] dr = {-1, -1, -1, 0, 0, 1, 1, 1};
            int[] dc = {-1, 0, 1, -1, 1, -1, 0, 1};

            for (int i = 0; i < dr.length; i++) {
                revealCell(r + dr[i], c + dc[i]);
            }
        }

        if (checkWin()) {
            gameOver = true;

            // Custom victory message with a trophy icon
            ImageIcon originalIcon = new ImageIcon("happyface.png"); // Replace with your image file path
            Image scaledImage = originalIcon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
            ImageIcon trophyIcon = new ImageIcon(scaledImage);
            JOptionPane.showMessageDialog(
                this,
                "Congratulations! You won!",
                "Victory",
                JOptionPane.INFORMATION_MESSAGE,
                trophyIcon
            );
        }
    }

    private void revealAllMines() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (board[r][c] == -1) {
                    buttons[r][c].setText("");
                    buttons[r][c].setBackground(Color.RED);
                }
            }
        }
    }

    private boolean checkWin() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (board[r][c] != -1 && !revealed[r][c]) {
                    return false;
                }
            }
        }
        return true;
    }

    private Color getNumberColor(int number) {
        switch (number) {
            case 1: return Color.BLUE;
            case 2: return new Color(0, 128, 0); // Dark Green
            case 3: return Color.RED;
            case 4: return new Color(0, 0, 128); // Dark Blue
            case 5: return new Color(128, 0, 0); // Dark Red
            case 6: return new Color(0, 128, 128); // Teal
            case 7: return Color.BLACK;
            case 8: return Color.GRAY;
            default: return Color.BLACK;
        }
    }

    private class CellClickHandler extends MouseAdapter {
        int r, c;

        public CellClickHandler(int r, int c) {
            this.r = r;
            this.c = c;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (gameOver) return;

            if (SwingUtilities.isRightMouseButton(e)) {
                if (!revealed[r][c]) {
                    flagged[r][c] = !flagged[r][c];
                    buttons[r][c].setText(flagged[r][c] ? "F" : "");
                }
            } else if (SwingUtilities.isLeftMouseButton(e)) {
                if (flagged[r][c]) return;
                if (firstClick) {
                    firstClick = false;
                    placeMines(r, c);
                }
                revealCell(r, c);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Minesweeper game = new Minesweeper(1); // 1 for small, 2 for large
            game.setVisible(true);
        });
    }
}




