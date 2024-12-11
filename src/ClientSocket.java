import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import server.Server;

public class ClientSocket {
    static String name;
    static Socket socket;
    static DataOutputStream os;
    static DataInputStream is;
    static List<String> currentUsers = new ArrayList<>();
    static MainPanel mainPanel;

    public ClientSocket(String name, Socket socket, DataOutputStream os, DataInputStream is) {
        this.name = name;
        this.socket = socket;
        this.os = os;
        this.is = is;

        // 클라이언트 수신 스레드 시작
        ClientThread ct = new ClientThread();
        ct.start();
    }

    public static void setMainPanel(MainPanel mainPanel) {ClientSocket.mainPanel = mainPanel;}

    public static DataOutputStream getDataOutputStream() {return os;}

    public static DataInputStream getDataInputStream() {return is;}

    // 서버에게 현재 유저 목록 요청
    public static void requestUserList() {
        try {
            os.writeUTF("FETCH_USER_LIST");
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getUsersInChat() {
        List<String> userList = new ArrayList<>();
        try {
            os.writeUTF("FETCH_USER_LIST");
            os.flush();

            while (true) {
                String messageType = is.readUTF();
                if ("USER_LIST".equals(messageType)) {
                    int userCount = is.readInt();
                    for (int i = 0; i < userCount; i++) {
                        String userName = is.readUTF();
                        int profileImageLength = is.readInt();
                        if (profileImageLength > 0) {
                            byte[] profileImageData = new byte[profileImageLength];
                            is.readFully(profileImageData);
                        }
                        userList.add(userName);
                    }
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return userList;
    }

    static class ClientThread extends Thread {
        @Override
        public void run() {
            try {
                while (true) {
                    String messageType = is.readUTF();
                    switch (messageType) {
                        case "USER_JOIN": {
                            String userName = is.readUTF();
                            if (!currentUsers.contains(userName)) {
                                currentUsers.add(userName);
                            }
                            updateFriendListOnUI();
                            break;
                        }
                        case "USER_LEAVE": {
                            String userName = is.readUTF();
                            currentUsers.remove(userName);
                            updateFriendListOnUI();
                            break;
                        }
                        case "USER_LIST": {
                            int userCount = is.readInt();
                            currentUsers.clear();
                            for (int i = 0; i < userCount; i++) {
                                String userName = is.readUTF();
                                int profileImageLength = is.readInt();
                                if (profileImageLength > 0) {
                                    byte[] profileImageData = new byte[profileImageLength];
                                    is.readFully(profileImageData);
                                    // 필요 시 프로필 이미지 데이터 처리 가능
                                }
                                currentUsers.add(userName);
                            }
                            updateFriendListOnUI();
                            break;
                        }
                        case "CHAT_CREATED": {
                            // 서버에서 채팅방 생성 완료 알림
                            String chatId = is.readUTF();
                            int participantCount = is.readInt();
                            List<String> participants = new ArrayList<>();
                            for (int i = 0; i < participantCount; i++) {
                                participants.add(is.readUTF());
                            }

                            // UI 스레드에서 ChatRoom창 오픈
                            SwingUtilities.invokeLater(() -> {
                                ChatRoom chatRoom = new ChatRoom(chatId, participants);
                                chatRoom.setSize(307, 613);
                                chatRoom.setLocationRelativeTo(null);
                                chatRoom.setVisible(true);
                            });
                            break;
                        }
                        case "CHAT_MESSAGE": {
                            // 채팅방 메시지 처리 로직
                            String chatId = is.readUTF();
                            String cmsgType = is.readUTF();
                            // 여기서 TEXT, IMAGE, VIDEO에 따라 처리
                            // 필요 시 UI 업데이트
                            break;
                        }
                        // 그 외 TEXT, IMAGE, VIDEO 등 일반 브로드캐스트 메시지는 필요에 따라 처리
                        case "TEXT": {
                            String message = is.readUTF();
                            // 메인 UI에 채팅 표시 등
                            break;
                        }
                        case "IMAGE":
                        case "VIDEO": {
                            int length = is.readInt();
                            byte[] data = new byte[length];
                            is.readFully(data);
                            // 메인 UI에 이미지/비디오 표시 등
                            break;
                        }
                        default:
                            // 알 수 없는 메시지 타입
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // UI 업데이트는 EDT에서 수행
        private void updateFriendListOnUI() {
            SwingUtilities.invokeLater(() -> {
                if (mainPanel != null) {
                    mainPanel.updateFriends(currentUsers);
                }
            });
        }
    }

}

