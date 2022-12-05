import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client_test {
    private static Socket s;
    private static InputStream inputStream;
    private static OutputStream outputStream;
    private static DataInputStream dataInputStream;
    private static DataOutputStream dataoutputStream;


    public static void main(String[] args) throws IOException {
        s = new Socket("127.0.0.1",8282);
        inputStream = s.getInputStream();
        outputStream = s.getOutputStream();
        dataInputStream = new DataInputStream(inputStream);
        dataoutputStream = new DataOutputStream(outputStream);
        while (true){
            Scanner in = new Scanner(System.in);
            String s1 = in.nextLine();
            dataoutputStream.writeUTF(s1);
            dataoutputStream.flush();
        }

    }
}
