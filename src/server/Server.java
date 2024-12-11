package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;
import java.util.stream.Collectors;

public class Server {
    static Map<Socket, UserInfo> userMap = new HashMap<>();
    // 채팅방: 채팅방ID -> 참여자 리스트
    static Map<String, List<UserInfo>> chatRooms = new HashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocket chatServer = new ServerSocket(6000);
        Socket socket = null;
        try {
            while(true) {
                socket = chatServer.accept();
                ServerThread serverThread = new ServerThread(socket);
                serverThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    // 특정 채팅방에 메시지 방송
    public static void broadcastChatMessage(String chatId, String msgType, String sender, byte[] data, String text) {
        List<UserInfo> participants = chatRooms.get(chatId);
        if (participants == null) return;

        for (UserInfo u : participants) {
            try {
                u.os.writeUTF("CHAT_MESSAGE");
                u.os.writeUTF(chatId);
                u.os.writeUTF(msgType);
                if (msgType.equals("TEXT")) {
                    u.os.writeUTF(sender);
                    u.os.writeUTF(text);
                } else if (msgType.equals("IMAGE") || msgType.equals("VIDEO")) {
                    u.os.writeUTF(sender);
                    u.os.writeInt(data.length);
                    u.os.write(data);
                }
                u.os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 채팅방 생성 후 참여자에게 알림
    public static void sendChatCreated(String chatId, List<UserInfo> participants) {
        for (UserInfo u : participants) {
            try {
                u.os.writeUTF("CHAT_CREATED");
                u.os.writeUTF(chatId); 
                u.os.writeInt(participants.size());
                for (UserInfo p : participants) {
                    u.os.writeUTF(p.name);
                }
                u.os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 모든 사용자에게 USER_JOIN 알림
    public static void broadcastUserJoin(String name) {
        for (UserInfo ui : userMap.values()) {
            try {
                ui.os.writeUTF("USER_JOIN");
                ui.os.writeUTF(name);
                ui.os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 모든 사용자에게 USER_LEAVE 알림
    public static void broadcastUserLeave(String name) {
        for (UserInfo ui : userMap.values()) {
            try {
                ui.os.writeUTF("USER_LEAVE");
                ui.os.writeUTF(name);
                ui.os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class ServerThread extends Thread {
    private Socket socket;
    DataInputStream is;
    DataOutputStream os;
    private UserInfo userInfo;

    public ServerThread(Socket socket) throws IOException {
        this.socket = socket;
        this.is = new DataInputStream(socket.getInputStream());
        this.os = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        try {
            // 초기 접속시: 클라이언트로부터 이름 받는다 (프로필 이미지는 추후 UPDATE_PROFILE로 설정한다고 가정)
            String clientName = is.readUTF();
            userInfo = new UserInfo(clientName, null, socket, os, is);

            Server.userMap.put(socket, userInfo);
            System.out.println(clientName + "님이 입장했습니다.");

            // 새로운 유저 입장 알림
            Server.broadcastUserJoin(clientName);
            
            // 메시지를 계속 수신
            while (true) {
                String messageType = is.readUTF();

                switch (messageType) {
                    case "TEXT": {
                        // 기존 broadcast: 모두에게
                        String message = is.readUTF();
                        broadcastTextMessage(userInfo.name + ": " + message);
                        break;
                    }
                    case "IMAGE": {
                        int length = is.readInt();
                        byte[] imageData = new byte[length];
                        is.readFully(imageData);
                        broadcastImage(imageData, userInfo.name);
                        break;
                    }
                    case "VIDEO": {
                        int length = is.readInt();
                        byte[] videoData = new byte[length];
                        is.readFully(videoData);
                        broadcastVideo(videoData, userInfo.name);
                        break;
                    }
                    case "FETCH_USER_LIST": {
                        sendUserListToClient(userInfo);
                        break;
                    }
                    // 프로필 업데이트 명령
                    case "UPDATE_PROFILE": {
                        // 클라이언트: UPDATE_PROFILE -> 새이름 -> int 이미지길이 -> 이미지데이터
                        String newName = is.readUTF();
                        int imgLen = is.readInt();
                        byte[] imgData = null;
                        if (imgLen > 0) {
                            imgData = new byte[imgLen];
                            is.readFully(imgData);
                        }
                        userInfo.name = newName;
                        userInfo.profileImageData = imgData;
                        break;
                    }

                    // 채팅방 생성 요청
                    case "CREATE_CHAT": {
                        int count = is.readInt();
                        List<String> invitees = new ArrayList<>();
                        for (int i = 0; i < count; i++) {
                            invitees.add(is.readUTF());
                        }
                        invitees.add(userInfo.name);

                        List<UserInfo> participants = Server.userMap.values().stream()
                                .filter(u -> invitees.contains(u.name))
                                .collect(Collectors.toList());

                        String chatId = UUID.randomUUID().toString();
                        Server.chatRooms.put(chatId, participants);

                        Server.sendChatCreated(chatId, participants);
                        break;
                    }

                    // 채팅방 메시지
                    case "CHAT_MESSAGE": {
                        // 클라이언트: CHAT_MESSAGE -> chatId -> msgType(TEXT/IMAGE/VIDEO) -> 데이터
                        String chatId = is.readUTF();
                        String cmsgType = is.readUTF();
                        if (cmsgType.equals("TEXT")) {
                            String txt = is.readUTF();
                            Server.broadcastChatMessage(chatId, "TEXT", userInfo.name, null, txt);
                        } else if (cmsgType.equals("IMAGE")) {
                            int len = is.readInt();
                            byte[] img = new byte[len];
                            is.readFully(img);
                            Server.broadcastChatMessage(chatId, "IMAGE", userInfo.name, img, null);
                        } else if (cmsgType.equals("VIDEO")) {
                            int len = is.readInt();
                            byte[] vid = new byte[len];
                            is.readFully(vid);
                            Server.broadcastChatMessage(chatId, "VIDEO", userInfo.name, vid, null);
                        }
                        break;
                    }

                    default:
                        System.out.println("알 수 없는 메시지 타입: " + messageType);
                        break;
                }
            }

        } catch (IOException e) {
            if (userInfo != null)
                System.out.println(userInfo.name + "님이 연결 종료.");
        } finally {
            try {
                if (userInfo != null) {
                    Server.userMap.remove(socket);
                    if (!socket.isClosed()) {
                        socket.close();
                    }

                    // 유저 퇴장 알림
                    Server.broadcastUserLeave(userInfo.name);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void sendUserListToClient(UserInfo requester) {
        try {
            List<UserInfo> allUsers = new ArrayList<>(Server.userMap.values());
            requester.os.writeUTF("USER_LIST");
            requester.os.writeInt(allUsers.size());
            for (UserInfo user : allUsers) {
                requester.os.writeUTF(user.name);
                if (user.profileImageData != null) {
                    requester.os.writeInt(user.profileImageData.length);
                    requester.os.write(user.profileImageData);
                } else {
                    requester.os.writeInt(0);
                }
            }
            requester.os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcastTextMessage(String message) {
        for (UserInfo ui : Server.userMap.values()) {
            try {
                ui.os.writeUTF("TEXT");
                ui.os.writeUTF(message);
                ui.os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void broadcastImage(byte[] imageData, String sender) {
        for (UserInfo ui : Server.userMap.values()) {
            try {
                ui.os.writeUTF("IMAGE");
                ui.os.writeInt(imageData.length);
                ui.os.write(imageData);
                ui.os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println(sender + "님이 이미지를 전송했습니다.");
    }

    private void broadcastVideo(byte[] videoData, String sender) {
        for (UserInfo ui : Server.userMap.values()) {
            try {
                ui.os.writeUTF("VIDEO");
                ui.os.writeInt(videoData.length);
                ui.os.write(videoData);
                ui.os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println(sender + "님이 영상을 전송했습니다.");
    }
}

//사용자 정보 관리 클래스
class UserInfo {
	String name;
	byte[] profileImageData;
	Socket socket;
	DataOutputStream os;
	DataInputStream is;
	
	public UserInfo(String name, byte[] profileImageData, Socket socket, DataOutputStream os, DataInputStream is) {
		this.name = name;
		this.profileImageData = profileImageData;
		this.socket = socket;
		this.os = os;
		this.is = is;
	}
}
