import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class SudokuSolverGUI {
    private static final int SIZE = 9;
    private static final int SUBGRID_SIZE = 3;
    private static JTextField[][] cells = new JTextField[SIZE][SIZE];
    private static Border boldBorder = BorderFactory.createLineBorder(Color.BLACK, 3);

    public static void main(String[] args) {
        JFrame frame = new JFrame("Sudoku Solver");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.setLayout(new BorderLayout());

        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(SIZE, SIZE));
        gridPanel.setBorder(boldBorder);

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                cells[row][col] = new JTextField();
                cells[row][col].setHorizontalAlignment(JTextField.CENTER);
                cells[row][col].setFont(new Font("Arial", Font.PLAIN, 20));
                cells[row][col].setBorder(new LineBorder(Color.GRAY, 1)); // Thin gray border for cells

                // Add bold borders for 3x3 subgrids
                if (row % SUBGRID_SIZE == 0 && col % SUBGRID_SIZE == 0) {
                    cells[row][col].setBorder(BorderFactory.createMatteBorder(3, 3, 1, 1, Color.BLACK));
                } else if (row % SUBGRID_SIZE == 0) {
                    cells[row][col].setBorder(BorderFactory.createMatteBorder(3, 1, 1, 1, Color.BLACK));
                } else if (col % SUBGRID_SIZE == 0) {
                    cells[row][col].setBorder(BorderFactory.createMatteBorder(1, 3, 1, 1, Color.BLACK));
                } else {
                    cells[row][col].setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY));
                }

                cells[row][col].addKeyListener(new SudokuKeyListener(row, col));
                gridPanel.add(cells[row][col]);
            }
        }

        JButton solveButton = new JButton("Solve");
        solveButton.setFont(new Font("Arial", Font.BOLD, 20));
        solveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[][] board = new int[SIZE][SIZE];
                for (int row = 0; row < SIZE; row++) {
                    for (int col = 0; col < SIZE; col++) {
                        String text = cells[row][col].getText();
                        if (!text.isEmpty()) {
                            int number = Integer.parseInt(text);
                            if (number < 1 || number > 9) {
                                JOptionPane.showMessageDialog(frame, "Please enter numbers in the range of 1 to 9.");
                                return;
                            }
                            board[row][col] = number;
                        } else {
                            board[row][col] = 0;
                        }
                    }
                }

                if (solveSudoku(board)) {
                    for (int row = 0; row < SIZE; row++) {
                        for (int col = 0; col < SIZE; col++) {
                            if (cells[row][col].getText().isEmpty()) {
                                cells[row][col].setText(Integer.toString(board[row][col]));
                                cells[row][col].setForeground(Color.RED);
                            }
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Sudoku puzzle is unsolvable.");
                }
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(gridPanel, BorderLayout.CENTER);
        panel.add(solveButton, BorderLayout.SOUTH);

        frame.add(panel);
        frame.setVisible(true);
    }

    // Solve Sudoku using backtracking
    public static boolean solveSudoku(int[][] board) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == 0) {
                    for (int number = 1; number <= SIZE; number++) {
                        if (isSafe(board, row, col, number)) {
                            board[row][col] = number;

                            if (solveSudoku(board)) {
                                return true;
                            }

                            // Backtrack
                            board[row][col] = 0;
                        }
                    }
                    return false; // trigger backtracking
                }
            }
        }
        return true; // puzzle solved
    }

    // Check if it's safe to place a number in a cell
    public static boolean isSafe(int[][] board, int row, int col, int number) {
        return !isInRow(board, row, number) &&
                !isInCol(board, col, number) &&
                !isInBox(board, row, col, number);
    }

    // Check if the number is in the row
    public static boolean isInRow(int[][] board, int row, int number) {
        for (int col = 0; col < SIZE; col++) {
            if (board[row][col] == number) {
                return true;
            }
        }
        return false;
    }

    // Check if the number is in the column
    public static boolean isInCol(int[][] board, int col, int number) {
        for (int row = 0; row < SIZE; row++) {
            if (board[row][col] == number) {
                return true;
            }
        }
        return false;
    }

    // Check if the number is in the 3x3 box
    public static boolean isInBox(int[][] board, int row, int col, int number) {
        int boxRowStart = row - row % SUBGRID_SIZE;
        int boxColStart = col - col % SUBGRID_SIZE;

        for (int r = boxRowStart; r < boxRowStart + SUBGRID_SIZE; r++) {
            for (int c = boxColStart; c < boxColStart + SUBGRID_SIZE; c++) {
                if (board[r][c] == number) {
                    return true;
                }
            }
        }
        return false;
    }

    // KeyListener to handle Enter key for navigation
    static class SudokuKeyListener implements KeyListener {
        private int row, col;

        public SudokuKeyListener(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public void keyTyped(KeyEvent e) {
            // Handle Enter key press to move to next cell
            if (e.getKeyChar() == '\n' || e.getKeyChar() == '\r') {
                int nextRow = row;
                int nextCol = col + 1;
                if (nextCol >= SIZE) {
                    nextRow++;
                    nextCol = 0;
                }
                if (nextRow < SIZE) {
                    cells[nextRow][nextCol].requestFocus();
                }
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
        }

        @Override
        public void keyReleased(KeyEvent e) {
            // Validate input and prompt user for out-of-range numbers
            JTextField textField = (JTextField) e.getSource();
            String text = textField.getText();
            if (!text.isEmpty()) {
                try {
                    int number = Integer.parseInt(text);
                    if (number < 1 || number > 9) {
                        JOptionPane.showMessageDialog(textField.getRootPane(), "Please enter numbers in the range of 1 to 9.");
                        textField.setText("");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(textField.getRootPane(), "Please enter valid numbers (1-9) only.");
                    textField.setText("");
                }
            }
        }
    }
}
