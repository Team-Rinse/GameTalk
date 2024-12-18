import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.*;

public class ClientSocket {
	static String name;
	private static Socket socket;
	private static DataOutputStream os;
	private static DataInputStream is;
	static List<String> currentUsers = new ArrayList<>();
	private static MainPanel mainPanel;
	static ChatPanel chatPanel;
	private static OptionPanel optionPanel;
	private static ChatRoom chatRoom;
	private static RacingGameFrame racingGameFrame;
	private static DrawingGameFrame drawingGameFrame;
	
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

	public static void setOptionPanel(OptionPanel optionPanel) {ClientSocket.optionPanel = optionPanel;}

	public static void setChatRoom(ChatRoom chatRoom) {ClientSocket.chatRoom = chatRoom;}

	public static void setRacingGameFrame(RacingGameFrame racingGameFrame) {ClientSocket.racingGameFrame = racingGameFrame;}

	public static void setDrawingGameFrame(DrawingGameFrame drawingGameFrame) {ClientSocket.drawingGameFrame = drawingGameFrame;}

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

	static public void sendTextMessage(String chatId, String msg) {
		if (msg == null) return;
		try {
			os.writeUTF("CHAT_MESSAGE");
			os.writeUTF(chatId);
			os.writeUTF("TEXT");
			os.writeUTF(msg);
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static void sendImageMessage(String chatId, byte[] imageData) {
		try {
			os.writeUTF("CHAT_MESSAGE");
			os.writeUTF(chatId);
			os.writeUTF("IMAGE"); // 이미지 타입 지정
			os.writeInt(imageData.length);
			os.write(imageData);
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void createRacingGameFrame(String chatId) {
		try {
			DataOutputStream os = ClientSocket.getDataOutputStream();
			os.writeUTF("CREATE_RACING_GAME");
			os.writeUTF(chatId); // 현재 채팅방 ID 전송
			os.flush();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	static void createDrawingGameFrame(String chatId) {
		try {
			DataOutputStream os = ClientSocket.getDataOutputStream();
			os.writeUTF("CREATE_DRAWING_GAME");
			os.writeUTF(chatId); // 현재 채팅방 ID 전송
			os.flush();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	static void sendRaceReadySignal(String chatId) {
		try {
			os.writeUTF("RACE_READY");
			os.writeUTF(chatId);
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void sendCatchmindReadySignal(String chatId) {
		try {
			os.writeUTF("CATCHMIND_READY");
			os.writeUTF(chatId);
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void sendAnswerAttempt(String chatId, String answer) {
		try {
			os.writeUTF("CATCHMIND_ANSWER_ATTEMPT"); // 서버에 메시지 타입 전송
			os.writeUTF(chatId);                    // 현재 채팅방 ID
			os.writeUTF(answer);                    // 유저가 입력한 정답
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void startRacingGame(String chatId) {
		try {
			os.writeUTF("START_RACE");
			os.writeUTF(chatId);
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void sendMoveCommand(String chatId) {
		try {
			os.writeUTF("MOVE");
			os.writeUTF(chatId);
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void sendResetCanvasCommand(String chatId) {
		try {
			ClientSocket.getDataOutputStream().writeUTF("RESET_CANVAS");
			ClientSocket.getDataOutputStream().writeUTF(chatId); // 현재 채팅방 ID 전송
			ClientSocket.getDataOutputStream().flush();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
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
							String chatId = is.readUTF();
							int participantCount = is.readInt();
							List<String> participants = new ArrayList<>();
							for (int i = 0; i < participantCount; i++) {
								participants.add(is.readUTF());
							}

							String chatTitle = String.join(", ", participants);

							SwingUtilities.invokeLater(() -> {
								ChatRoom chatRoom = new ChatRoom(chatId, participants);
								chatRoom.setSize(307, 613);
								chatRoom.setLocationRelativeTo(null);
								chatRoom.setVisible(true);

								// 생성한 채팅방을 ClientSocket에 등록
								ClientSocket.setChatRoom(chatRoom);

								if (ClientSocket.chatPanel != null) {
									ClientSocket.chatPanel.addChatEntry(chatTitle, "");
								}
							});

							break;
						}
						case "CHAT_MESSAGE": {
							String chatId = is.readUTF(); // 채팅방 ID
							String msgType = is.readUTF(); // 메시지 타입(TEXT, IMAGE 등)
							String sender = is.readUTF(); // 보낸 사람

							if ("TEXT".equals(msgType)) {
								String msgContent = is.readUTF(); // 메시지 내용
								SwingUtilities.invokeLater(() -> {
									if (ClientSocket.chatRoom != null && ClientSocket.chatRoom.getChatId().equals(chatId)) {
										ClientSocket.chatRoom.addOtherMessage(sender, msgContent); // 텍스트 메시지 표시
									}
								});
							} else if ("IMAGE".equals(msgType)) {
								int imageLength = is.readInt();
								byte[] imageData = new byte[imageLength];
								is.readFully(imageData); // 이미지 데이터 읽기

								SwingUtilities.invokeLater(() -> {
									if (ClientSocket.chatRoom != null && ClientSocket.chatRoom.getChatId().equals(chatId)) {
										ClientSocket.chatRoom.addImageMessage(sender, imageData); // 이미지 메시지 표시
									}
								});
							}
							// 필요한 경우 VIDEO 등 다른 msgType 처리
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
						case "CREATE_RACING_GAME": {
							String chatId = is.readUTF();
							SwingUtilities.invokeLater(() -> {
								RacingGameFrame frame = new RacingGameFrame(chatId);
								ClientSocket.setRacingGameFrame(frame);
							});
							break;
						}
						case "CREATE_DRAWING_GAME": {
							String chatId = is.readUTF();
							SwingUtilities.invokeLater(() -> {
								DrawingGameFrame frame = new DrawingGameFrame(chatId);
								ClientSocket.setDrawingGameFrame(frame);
							});
							break;
						}
						case "RACE_READY_STATUS": {
							String chatId = is.readUTF();
							int count = is.readInt();
							HashMap<String, Boolean> readinessMap = new HashMap<>();
							for (int i = 0; i < count; i++) {
								String playerName = is.readUTF();
								boolean ready = is.readBoolean();
								readinessMap.put(playerName, ready);
							}
							SwingUtilities.invokeLater(() -> {
								if (racingGameFrame != null) {
									racingGameFrame.updateReadyStatus(readinessMap);
								}
							});
							break;
						}

						case "RACE_COUNTDOWN": {
							String chatId = is.readUTF();
							int c = is.readInt();
							SwingUtilities.invokeLater(() -> {
								if (racingGameFrame != null) {
									racingGameFrame.showCountDown(c);
								}
							});
							break;
						}
						case "RACE_START": {
							String chatId = is.readUTF();
							SwingUtilities.invokeLater(() -> {
								racingGameFrame.startGameUI(chatId);
							});
							break;
						}
						case "RACE_UPDATE": {
							String chatId = is.readUTF();
							String playerName = is.readUTF();
							int position = is.readInt();

							SwingUtilities.invokeLater(() -> {
								racingGameFrame.updatePlayerPosition(playerName, position);
							});
							break;
						}
						case "RACE_END": {
							String chatId = is.readUTF();
							String winnerName = is.readUTF();

							SwingUtilities.invokeLater(() -> {
								racingGameFrame.showRaceResult(winnerName);
							});
							break;
						}
						case "CATCHMIND_READY_STATUS": {
							String chatId = is.readUTF();
							int count = is.readInt();
							HashMap<String, Boolean> readinessMap = new HashMap<>();
							for (int i = 0; i < count; i++) {
								String playerName = is.readUTF();
								boolean ready = is.readBoolean();
								readinessMap.put(playerName, ready);
							}
							SwingUtilities.invokeLater(() -> {
								if (drawingGameFrame != null) {
									drawingGameFrame.updateReadyStatus(readinessMap);
								}
							});
							break;
						}
						case "CATCHMIND_START": {
							String chatId = is.readUTF();
							String mode = is.readUTF(); // "DRAWER" or "VIEW_ONLY"
							SwingUtilities.invokeLater(() -> {
								// DrawingGameFrame 인스턴스를 가져와 setMode 호출
								if (ClientSocket.drawingGameFrame != null) {
									ClientSocket.drawingGameFrame.startGameUI(chatId);
									ClientSocket.drawingGameFrame.setMode(mode);
								}
							});
							break;
						}

						case "CATCHMIND_ANSWER": {
							String chatId = is.readUTF();
							String answer = is.readUTF();
							drawingGameFrame.showAnswer(answer);
							System.out.println("정답: " + answer);
							break;
						}

						case "DRAW_EVENT": {
							String chatId = is.readUTF();
							int x1 = is.readInt();
							int y1 = is.readInt();
							int x2 = is.readInt();
							int y2 = is.readInt();
							int rgb = is.readInt();
							SwingUtilities.invokeLater(() -> {
								if (ClientSocket.drawingGameFrame != null) {
									ClientSocket.drawingGameFrame.drawLineFromServer(x1, y1, x2, y2, rgb);
								}
							});
							break;
						}
						case "RESET_CANVAS": {
							String chatId = is.readUTF();
							SwingUtilities.invokeLater(() -> {
								if (ClientSocket.drawingGameFrame != null && ClientSocket.drawingGameFrame.getChatId().equals(chatId)) {
									ClientSocket.drawingGameFrame.canvas.clearCanvas();
								}
							});
							break;
						}
						case "CATCHMIND_CORRECT_ANSWER": {
							String chatId = is.readUTF();
							String winnerName = is.readUTF();
							SwingUtilities.invokeLater(() -> {
								JOptionPane.showMessageDialog(null, winnerName + "님이 정답을 맞췄습니다!");
							});
							break;
						}
						case "POINTS_UPDATED": {
							int newPoints = is.readInt();
							SwingUtilities.invokeLater(() -> {
								if (ClientSocket.optionPanel != null) {
									ClientSocket.optionPanel.updatePoint(newPoints);
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

}

