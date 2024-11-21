package server;

import java.io.*;
import java.net.*;
import java.util.Vector;

public class Server {
	static Vector<ServerThread> socketVector = new Vector<>();

	public static void main(String[] args) throws IOException {
		ServerSocket chatServer = new ServerSocket(6000);
		Socket socket = new Socket();
		
		try {
			while(true) {
				socket = chatServer.accept();
				
				DataInputStream is = new DataInputStream(socket.getInputStream());
				DataOutputStream os = new DataOutputStream(socket.getOutputStream());
				
				ServerThread serverThread = new ServerThread(socket);
				
				socketVector.add(serverThread);
				serverThread.start();
			}
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
            	
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
	}

}


// 이미지,이모티콘  주고 받기  -> 바이트로 변환하는 과정 필요 !! 
class ServerThread extends Thread {
    private Socket socket;
    private DataInputStream is;
    private DataOutputStream os;
    private String clientName;

    public ServerThread(Socket socket) throws IOException {
        this.socket = socket;
        this.is = new DataInputStream(socket.getInputStream());
        this.os = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        try {
            // 클라이언트로부터 이름을 수신
            clientName = is.readUTF();
            System.out.println(clientName + "님이 입장했습니다.");
            broadcastMessage(clientName + "님이 입장했습니다.");

            // 메시지를 계속 수신 및 전송
            while (true) {
                String message = is.readUTF();
                broadcastMessage(clientName + ": " + message);
            }
        } catch (IOException e) {
            System.out.println(clientName + "님이 연결을 종료했습니다.");
        } finally {
            try {
                // 연결이 끊긴 클라이언트를 Vector에서 제거
                Server.socketVector.remove(this);
                socket.close();

                // 다른 클라이언트에게 퇴장 알림
                broadcastMessage(clientName + "님이 퇴장했습니다.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void broadcastMessage(String message) {
        for (ServerThread client : Server.socketVector) {
            try {
                client.os.writeUTF(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

