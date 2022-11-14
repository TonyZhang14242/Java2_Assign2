import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Service implements Runnable {
    private InputStream inputStream;
    private OutputStream outputStream;
    private DataInputStream dataInputStream;
    private DataOutputStream dataoutputStream;

    private InputStream inputStream2;
    private OutputStream outputStream2;
    private DataInputStream dataInputStream2;
    private DataOutputStream dataoutputStream2;

    private Socket client;

    private Socket client2;

    private boolean TURN = false;

    public int[][] chessboard = new int[3][3];

    public Service(Socket client, Socket client2) {
        this.client = client;
        this.client2 = client2;
    }

    @Override
    public void run() {
        try {
            inputStream = client.getInputStream();
            outputStream = client.getOutputStream();
            inputStream2 = client2.getInputStream();
            outputStream2 = client2.getOutputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
        dataInputStream = new DataInputStream(inputStream);
        dataoutputStream = new DataOutputStream(outputStream);
        dataInputStream2 = new DataInputStream(inputStream2);
        dataoutputStream2 = new DataOutputStream(outputStream2);
        while (true) {
            try {
                String s1 = dataInputStream.readUTF();
                System.out.println(s1 + client.getPort());
                int x = Integer.parseInt(s1.split(" ")[0]);
                int y = Integer.parseInt(s1.split(" ")[1]);
                chessboard[x][y] = 1;
                System.out.println(Arrays.deepToString(chessboard));
                dataoutputStream2.writeUTF(s1);
                dataoutputStream2.flush();
                sendinfo();

                String s2 = dataInputStream2.readUTF();
                System.out.println(s2 + client2.getPort());
                x = Integer.parseInt(s2.split(" ")[0]);
                y = Integer.parseInt(s2.split(" ")[1]);
                chessboard[x][y] = 2;
                System.out.println(Arrays.deepToString(chessboard));
                dataoutputStream.writeUTF(s2);
                dataoutputStream.flush();
                sendinfo();

            } catch (IOException e) {
                System.out.println("The connection between " + client.getPort() + " " + client2.getPort() + " has terminated");
                break;
            }

        }
        try {
            client.close();
            client2.close();
            dataoutputStream.close();
            dataInputStream.close();
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    public int judgeWinner() {
        int winner = 0;
        for (int i = 0; i < 3; i++) {
            if (chessboard[i][0] == 1 && chessboard[i][1] == 1 && chessboard[i][2] == 1)
                winner = 1;
            else if (chessboard[i][0] == 2 && chessboard[i][1] == 2 && chessboard[i][2] == 2)
                winner = 2;
        }
        for (int j = 0; j < 3; j++) {
            if (chessboard[0][j] == 1 && chessboard[1][j] == 1 && chessboard[2][j] == 1)
                winner = 1;
            else if (chessboard[0][j] == 2 && chessboard[1][j] == 2 && chessboard[2][j] == 2)
                winner = 2;
        }
        if (chessboard[0][0] == 1 && chessboard[1][1] == 1 && chessboard[2][2] == 1)
            winner = 1;
        else if (chessboard[0][0] == 2 && chessboard[1][1] == 2 && chessboard[2][2] == 2)
            winner = 2;
        return winner;
    }

    public void sendinfo() throws IOException {
        int winner = judgeWinner();
        try {
            if (winner != 0) {
                if (winner == 1) {
                    dataoutputStream.writeUTF("YOU WIN!!");
                    dataoutputStream.flush();
                    dataoutputStream2.writeUTF("YOU LOSE!!");
                    dataoutputStream2.flush();
                } else {
                    dataoutputStream2.writeUTF("YOU WIN!!");
                    dataoutputStream2.flush();
                    dataoutputStream.writeUTF("YOU LOSE!!");
                    dataoutputStream.flush();
                }

            }
            if (winner == 0) {
                boolean isfull = true;
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        if (chessboard[i][j] == 0) {
                            isfull = false;
                        }
                    }
                }
                if (isfull) {
                    dataoutputStream.writeUTF("TIE GAME!!");
                    dataoutputStream.flush();
                    dataoutputStream2.writeUTF("TIE GAME!!");
                    dataoutputStream2.flush();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}

public class Server_test {
    private static ServerSocket s;

    private static List<Socket> waiting;


    public static void main(String[] args) throws IOException {
        s = new ServerSocket(8282);
        waiting = new ArrayList<>();
        while (true) {
            Socket client = s.accept();
            if (waiting.isEmpty()) {
                new DataOutputStream(client.getOutputStream()).writeUTF("Please wait for another player...");
                waiting.add(client);
            } else {
                try {
                    Socket client2 = waiting.get(0);
                    waiting.clear();
                    new DataOutputStream(client2.getOutputStream()).writeUTF("You are about to play with player " + client.getPort() + ", player " + client.getPort() + " plays first.");
                    new DataOutputStream(client.getOutputStream()).writeUTF("You are about to play with player " + client2.getPort() + ", you play first.");
                    new Thread(new Service(client, client2)).start();
                } catch (Exception e) {
                    new DataOutputStream(client.getOutputStream()).writeUTF("Please wait for another player...");
                    waiting.clear();
                    waiting.add(client);
                }

            }

            //System.out.println(client.getPort());

        }
    }


}
