import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;

public class RenjuGame extends JFrame {
    private final int SIZE = 15;
    private final JButton[][] buttons = new JButton[SIZE][SIZE];
    private String playerX = "Крестики";
    private String playerO = "Нолики";
    private String currentPlayerName = playerX;
    private final JLabel statusLabel = new JLabel("Ход: " + currentPlayerName);
    private boolean currentPlayerIsX = true;

    private Clip loopedClip;

    public RenjuGame() {
        showWelcomeDialog();
        setupGame();
    }

    private void showWelcomeDialog() {
        JOptionPane.showMessageDialog(this, "Добро пожаловать в игру Рэндзю!\n" +
                "Игроки по очереди ставят крестики и нолики на поле 15x15.\n" +
                "Выигрывает тот, кто первым соберет линию из 5 своих символов.\n" +
                "Удачи!", "Приветствие", JOptionPane.INFORMATION_MESSAGE);

        playerX = JOptionPane.showInputDialog(this, "Введите имя игрока за крестики:");
        playerO = JOptionPane.showInputDialog(this, "Введите имя игрока за нолики:");
        if (playerX == null || playerX.trim().isEmpty()) playerX = "Крестики";
        if (playerO == null || playerO.trim().isEmpty()) playerO = "Нолики";
        currentPlayerName = playerX;
        statusLabel.setText("Ход: " + currentPlayerName);
    }

    private void setupGame() {
        playLoopedSound("/backSound.wav");
        setTitle("Игра Рэндзю");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(SIZE, SIZE));
        add(statusLabel, BorderLayout.NORTH);
        add(boardPanel, BorderLayout.CENTER);

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                JButton button = new JButton();
                button.addActionListener(new ButtonListener(i, j));
                button.setOpaque(true);
                button.setBorderPainted(false);
                buttons[i][j] = button;
                boardPanel.add(button);
            }
        }

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Опции");
        JMenuItem newGame = new JMenuItem("Новая игра");
        newGame.addActionListener(e -> resetBoard());
        JMenuItem surrender = new JMenuItem("Сдаться");
        surrender.addActionListener(e -> surrender());
        menu.add(newGame);
        menu.add(surrender);
        menuBar.add(menu);
        setJMenuBar(menuBar);

        setVisible(true);
        setSize(800,800);
        setLocationRelativeTo(null);
    }


    private void resetBoard() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                buttons[i][j].setText("");
                buttons[i][j].setEnabled(true);
                buttons[i][j].setBackground(null);
            }
        }
        currentPlayerIsX = true;
        currentPlayerName = playerX;
        statusLabel.setText("Ход: " + currentPlayerName);
    }

    private void surrender() {
        String winner = currentPlayerIsX ? playerO : playerX;
        JOptionPane.showMessageDialog(this, winner + " победил!", "Сдаться", JOptionPane.INFORMATION_MESSAGE);
        resetBoard();
    }

    private class ButtonListener implements ActionListener {
        private final int x;
        private final int y;

        public ButtonListener(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (((JButton) e.getSource()).getText().isEmpty()) {
                if (currentPlayerIsX) {
                    String color = "#ADD8E6";
                    buttons[x][y].setBackground(Color.decode(color));
                    buttons[x][y].setText("X");
                    buttons[x][y].setForeground(Color.BLUE);
                } else {
                    String color = "#ffcccb";
                    buttons[x][y].setBackground(Color.decode(color));
                    buttons[x][y].setText("O");
                    buttons[x][y].setForeground(Color.RED);
                }
                buttons[x][y].setEnabled(false);

                playSound("/click.wav");

                if (checkForWin()) {
                    stopLoopedSound();
                    playSound("/win.wav");
                    JOptionPane.showMessageDialog(RenjuGame.this, currentPlayerName + " победил!", "Победа", JOptionPane.INFORMATION_MESSAGE);
                    for (int i = 0; i < SIZE; i++) {
                        for (int j = 0; j < SIZE; j++) {
                            buttons[i][j].setEnabled(false); // Отключаем кнопки после победы
                        }
                    }
                    
                } else {
                    switchPlayer();
                }
            }
        }

    }

    private void switchPlayer() {
        currentPlayerIsX = !currentPlayerIsX;
        currentPlayerName = currentPlayerIsX ? playerX : playerO;
        statusLabel.setText("Ход: " + currentPlayerName);
    }


    private boolean checkForWin() {
        String symbol = currentPlayerIsX ? "X" : "O";
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (buttons[row][col].getText().equals(symbol)) {
                    if (checkLine(symbol, row, col, 1, 0) ||
                            checkLine(symbol, row, col, 0, 1) ||
                            checkLine(symbol, row, col, 1, 1) ||
                            checkLine(symbol, row, col, 1, -1)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void playSound(String soundFileName) {
        try {
            URL soundURL = getClass().getResource(soundFileName);
            if (soundURL == null) {
                System.err.println("Не удалось загрузить файл звука " + soundFileName);
                return;
            }
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void playLoopedSound(String soundFileName) {
        try {
            URL soundURL = getClass().getResource(soundFileName);
            if (soundURL == null) {
                System.err.println("Не удалось загрузить файл звука " + soundFileName);
                return;
            }
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundURL);
            loopedClip = AudioSystem.getClip(); // Используем поле класса для хранения Clip
            loopedClip.open(audioInputStream);

            loopedClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    // Метод для остановки зацикленного воспроизведения
    public void stopLoopedSound() {
        if (loopedClip != null) {
            loopedClip.stop(); // Останавливаем воспроизведение
            loopedClip.close(); // Освобождаем ресурсы, связанные с Clip
            loopedClip = null; // Удаляем ссылку на Clip, чтобы избежать утечек памяти
        }
    }


    private boolean checkLine(String symbol, int row, int col, int dRow, int dCol) {
        int count = 1;
        int r = row + dRow;
        int c = col + dCol;
        while (r >= 0 && r < SIZE && c >= 0 && c < SIZE && buttons[r][c].getText().equals(symbol)) {
            count++;
            r += dRow;
            c += dCol;
        }
        r = row - dRow;
        c = col - dCol;
        while (r >= 0 && r < SIZE && c >= 0 && c < SIZE && buttons[r][c].getText().equals(symbol)) {
            count++;
            r -= dRow;
            c -= dCol;
        }
        if (count >= 5) {
            highlightWinningCombination(row, col, dRow, dCol);
            return true;
        }
        return false;
    }

    private void highlightWinningCombination(int row, int col, int dRow, int dCol) {
        buttons[row][col].setBackground(Color.GREEN);
        int r = row + dRow;
        int c = col + dCol;
        for (int i = 1; i < 5; i++) { // Assuming a win requires exactly 5 in a row
            if (r >= 0 && r < SIZE && c >= 0 && c < SIZE) {
                buttons[r][c].setBackground(Color.GREEN);
                r += dRow;
                c += dCol;
            }
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(RenjuGame::new);
    }
}
