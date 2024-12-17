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
	static ChatPanel chatPanel;
	static ChatRoom chatRoom;

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

	public static void setChatPanel(ChatPanel chatPanel) {ClientSocket.chatPanel = chatPanel;}

	public static void setChatRoom(ChatRoom chatRoom) {ClientSocket.chatRoom = chatRoom;}

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

	static public void sendTextMessage(String msg) {
		if(msg == null) return;
		try {
			os.writeUTF("CHAT_MESSAGE");
			os.writeUTF("TEXT");
			os.writeUTF(msg);
			os.flush();
		} catch (IOException e) { }
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

							String chatTitle = String.join(", ", participants);

							// UI 스레드에서 ChatRoom창 오픈
							SwingUtilities.invokeLater(() -> {
								ChatRoom chatRoom = new ChatRoom(chatId, participants);
								chatRoom.setSize(307, 613);
								chatRoom.setLocationRelativeTo(null);
								chatRoom.setVisible(true);

								if (ClientSocket.chatPanel != null) {
									ClientSocket.chatPanel.addChatEntry(chatTitle, "");
								}
							});

							break;
						}
						case "CHAT_MESSAGE": {
							// 채팅방 메시지 처리 로직
							String chatId = is.readUTF();
							String cmsgType = is.readUTF();
							// 여기서 TEXT, IMAGE, VIDEO에 따라 처리
							// 필요 시 UI 업데이트
							String sender = is.readUTF();
							String txt = is.readUTF();
							SwingUtilities.invokeLater(() -> {
								if (ClientSocket.chatPanel != null) {
									// 여기서는 sender를 friendName으로, txt를 lastMessage로 표시
									ClientSocket.chatPanel.updateLastMessage(sender, txt);
								}
							});
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
						case "USER_LEAVE_CHAT": {
							String chatId = is.readUTF();
							String leaverName = is.readUTF();

							SwingUtilities.invokeLater(() -> {
								if (ClientSocket.chatRoom != null && ClientSocket.chatRoom.getChatId().equals(chatId)) {
									// ChatRoom UI에서 사용자 목록 업데이트 또는 메시지 표시
									ClientSocket.chatRoom.addSystemMessage(leaverName + "님이 채팅방을 나갔습니다.");
								}
							});
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
	
	public static void sendImageMessage(String imagePath) {
	    try {
	        // 서버로 이미지 메시지 전송 요청
	        getDataOutputStream().writeUTF("IMAGE_MESSAGE"); // 메시지 타입 전송
	        getDataOutputStream().writeUTF(imagePath);       // 이미지 파일 경로 전송
	        getDataOutputStream().flush();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}


}

