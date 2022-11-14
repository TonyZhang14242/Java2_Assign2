package application.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    private static final int PLAY_1 = 1;
    private static final int PLAY_2 = 2;
    private static final int EMPTY = 0;
    private static final int BOUND = 90;
    private static final int OFFSET = 15;

    public static Socket s;

    private static InputStream inputStream;
    private static OutputStream outputStream;
    private static DataInputStream dataInputStream;
    private static DataOutputStream dataoutputStream;

    @FXML
    private Pane base_square;

    @FXML
    private Rectangle game_panel;

    private static boolean TURN = false;

    private static final int[][] chessBoard = new int[3][3];
    private static final boolean[][] flag = new boolean[3][3];

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            s = new Socket("127.0.0.1", 8282);
            inputStream = s.getInputStream();
            outputStream = s.getOutputStream();
            dataInputStream = new DataInputStream(inputStream);
            dataoutputStream = new DataOutputStream(outputStream);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        game_panel.setOnMouseClicked(event -> {
            int x = (int) (event.getX() / BOUND);
            int y = (int) (event.getY() / BOUND);
            if (refreshBoard(x, y)) {
                TURN = !TURN;
            }
            game_panel.setVisible(false);
        });

        game_panel.setVisible(false);


        action();

    }


    public synchronized void action() {
        Runnable r = () -> {

            while (true) {

                try {
                    String rev = dataInputStream.readUTF();
                    System.out.println(rev + " from server");
                    int x, y;
                    if (rev.contains("you play first")) {
                        Platform.runLater(() -> {
                            game_panel.setVisible(true);
                        });
                    }
                    if (rev.matches("[0-9 ]+")) {
                        x = Integer.parseInt(rev.split(" ")[0]);
                        y = Integer.parseInt(rev.split(" ")[1]);
                        System.out.println(x + " " + y);
                        chessBoard[x][y] = TURN ? PLAY_1 : PLAY_2;
                        TURN = !TURN;
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                drawChess();
                                game_panel.setVisible(true);
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
                    }
                    if (rev.contains("WIN") || rev.contains("LOSE") || rev.contains("TIE")) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                game_panel.setVisible(false);
                            }
                        });

                    }

                } catch (Exception e) {
                    //e.printStackTrace();
                    //Thread.currentThread().interrupt();
                    boolean servercrash = false;
                    try {
                        s.sendUrgentData(0);
                    } catch (Exception ex) {
                        servercrash = true;
                        System.out.println("Server crashed!");

                    }

                    if (!servercrash) {
                        try {
                            System.out.println("Opponent crashed or exited from game!");
                            Thread.sleep(5000);
                            System.exit(0);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                    try {
                        Thread.sleep(5000);
                        System.exit(0);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }

            }
        };
        new Thread(r).start();
    }

    private boolean refreshBoard(int x, int y) {
        if (chessBoard[x][y] == EMPTY) {
            chessBoard[x][y] = TURN ? PLAY_1 : PLAY_2;

            String s1 = x + " " + y + " " + s.getPort();
            try {
                dataoutputStream.writeUTF(s1);
                dataoutputStream.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }

            drawChess();
            return true;
        }
        return false;
    }

    private void drawChess() {
        for (int i = 0; i < chessBoard.length; i++) {
            for (int j = 0; j < chessBoard[0].length; j++) {
                if (flag[i][j]) {
                    // This square has been drawing, ignore.
                    continue;
                }
                switch (chessBoard[i][j]) {
                    case PLAY_1:
                        drawCircle(i, j);
                        break;
                    case PLAY_2:
                        drawLine(i, j);
                        break;
                    case EMPTY:
                        // do nothing
                        break;
                    default:
                        System.err.println("Invalid value!");
                }
            }
        }
    }

    private void drawCircle(int i, int j) {
        Circle circle = new Circle();
        base_square.getChildren().add(circle);
        circle.setCenterX(i * BOUND + BOUND / 2.0 + OFFSET);
        circle.setCenterY(j * BOUND + BOUND / 2.0 + OFFSET);
        circle.setRadius(BOUND / 2.0 - OFFSET / 2.0);
        circle.setStroke(Color.RED);
        circle.setFill(Color.TRANSPARENT);
        flag[i][j] = true;
    }

    private void drawLine(int i, int j) {
        Line line_a = new Line();
        Line line_b = new Line();
        base_square.getChildren().add(line_a);
        base_square.getChildren().add(line_b);
        line_a.setStartX(i * BOUND + OFFSET * 1.5);
        line_a.setStartY(j * BOUND + OFFSET * 1.5);
        line_a.setEndX((i + 1) * BOUND + OFFSET * 0.5);
        line_a.setEndY((j + 1) * BOUND + OFFSET * 0.5);
        line_a.setStroke(Color.BLUE);

        line_b.setStartX((i + 1) * BOUND + OFFSET * 0.5);
        line_b.setStartY(j * BOUND + OFFSET * 1.5);
        line_b.setEndX(i * BOUND + OFFSET * 1.5);
        line_b.setEndY((j + 1) * BOUND + OFFSET * 0.5);
        line_b.setStroke(Color.BLUE);
        flag[i][j] = true;
    }
}
