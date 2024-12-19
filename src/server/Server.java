package server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class Server {
    static Map<Socket, UserInfo> userMap = new HashMap<>();
    // 채팅방: 채팅방ID -> 참여자 리스트
    static Map<String, List<UserInfo>> chatRooms = new HashMap<>();
    // 준비 상태: chatId -> (userName -> Boolean)
    static HashMap<String, HashMap<String, Boolean>> raceReady = new HashMap<>();
    static Map<String, Map<String, Integer>> racePositions = new HashMap<>();
    static Map<String, Boolean> raceInProgress = new HashMap<>();
    static HashMap<String, HashMap<String, Boolean>> catchmindReady = new HashMap<>();
    static Map<String, String> chatDrawerMap = new HashMap<>();
    static List<String> catchmindWords = new ArrayList<>();
    static Map<String, String> chatAnswerMap = new HashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocket chatServer = new ServerSocket(6000);
        Socket socket = null;
        try {
            while (true) {
                socket = chatServer.accept();
                ServerThread serverThread = new ServerThread(socket);
                serverThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    public static void broadcastProfileUpdate(UserInfo user) {
        for (UserInfo ui : userMap.values()) {
            try {
                ui.os.writeUTF("PROFILE_UPDATED");
                ui.os.writeUTF(user.name);
                if (user.profileImageData != null) {
                    ui.os.writeInt(user.profileImageData.length);
                    ui.os.write(user.profileImageData);
                } else {
                    ui.os.writeInt(0);
                }
                ui.os.writeUTF(user.statusMessage == null ? "" : user.statusMessage);
                ui.os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 특정 채팅방에 메시지 방송
    public static void broadcastChatMessage(String chatId, String msgType, String sender, byte[] data, String text) {
        List<UserInfo> participants = chatRooms.get(chatId);
        if (participants == null) return;

        for (UserInfo u : participants) {
            try {
                u.os.writeUTF("CHAT_MESSAGE");  // 메시지 타입
                u.os.writeUTF(chatId);         // 채팅방 ID
                u.os.writeUTF(msgType);        // 메시지 유형(TEXT, IMAGE 등)
                if ("TEXT".equals(msgType)) {
                    u.os.writeUTF(sender);    // 메시지 보낸 사람
                    u.os.writeUTF(text);      // 메시지 내용
                } else if ("IMAGE".equals(msgType) && data != null) {
                    u.os.writeUTF(sender);    // 메시지 보낸 사람
                    u.os.writeInt(data.length); // 이미지 데이터 길이
                    u.os.write(data);          // 이미지 데이터
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

    public static void startRacingGameForAll(String chatId) {
        List<UserInfo> participants = chatRooms.get(chatId);
        if (participants == null || participants.isEmpty()) return;

        for (UserInfo u : participants) {
            try {
                u.os.writeUTF("CREATE_RACING_GAME");
                u.os.writeUTF(chatId);
                u.os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void startDrawingGameForAll(String chatId) {
        List<UserInfo> participants = chatRooms.get(chatId);
        if (participants == null || participants.isEmpty()) return;

        for (UserInfo u : participants) {
            try {
                u.os.writeUTF("CREATE_DRAWING_GAME");
                u.os.writeUTF(chatId);
                u.os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void startRacingGame(String chatId) {
        List<UserInfo> participants = chatRooms.get(chatId);
        if (participants == null) return;

        Map<String, Integer> playerPositions = new HashMap<>();
        for (UserInfo u : participants) {
            playerPositions.put(u.name, 0);
        }
        racePositions.put(chatId, playerPositions);
        raceInProgress.put(chatId, true);

        broadcastRaceStart(chatId, participants);
    }

    public static void handleMoveCommand(String chatId, String playerName) {
        if (!raceInProgress.getOrDefault(chatId, false)) return;
        Map<String, Integer> playerPositions = racePositions.get(chatId);
        if (playerPositions == null) return;

        int newPosition = playerPositions.get(playerName) + 3;
        playerPositions.put(playerName, newPosition);
        broadcastPlayerPosition(chatId, playerName, newPosition);

        if (newPosition >= 100) {
            raceInProgress.put(chatId, false);
            broadcastRaceResult(chatId, playerName);
            awardPointsRacing(chatId, playerName); // 먼저 도착한 플레이어에게 포인트 부여
        }
    }

    private static void broadcastRaceStart(String chatId, List<UserInfo> participants) {
        for (UserInfo u : participants) {
            try {
                u.os.writeUTF("RACE_START");
                u.os.writeUTF(chatId);
                u.os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void broadcastPlayerPosition(String chatId, String playerName, int position) {
        List<UserInfo> participants = chatRooms.get(chatId);
        for (UserInfo u : participants) {
            try {
                u.os.writeUTF("RACE_UPDATE");
                u.os.writeUTF(chatId);
                u.os.writeUTF(playerName);
                u.os.writeInt(position);
                u.os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void broadcastRaceResult(String chatId, String winnerName) {
        List<UserInfo> participants = chatRooms.get(chatId);
        for (UserInfo u : participants) {
            try {
                u.os.writeUTF("RACE_END");
                u.os.writeUTF(chatId);
                u.os.writeUTF(winnerName);
                u.os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void handleRaceReady(String chatId, String userName) {
        if (!raceReady.containsKey(chatId)) {
            HashMap<String, Boolean> map = new HashMap<>();
            // 채팅방 참가자 모두를 false로 초기화
            List<UserInfo> participants = chatRooms.get(chatId);
            if (participants != null) {
                for (UserInfo u : participants) {
                    map.put(u.name, false);
                }
            }
            raceReady.put(chatId, map);
        }

        Map<String, Boolean> readinessMap = raceReady.get(chatId);
        if (readinessMap != null && readinessMap.containsKey(userName)) {
            readinessMap.put(userName, true);
            // 모든 참가자가 준비 완료인지 체크
            if (readinessMap.values().stream().allMatch(Boolean::booleanValue)) {
                broadcastReadyStatus(chatId, readinessMap);
                // 모두 준비 완료 -> 카운트다운 시작
                startRaceCountDown(chatId);
            } else {
                broadcastReadyStatus(chatId, readinessMap);
            }
        }
    }

    // 준비 상태 브로드캐스트: 각 사용자에게 누가 준비인지/아닌지 전달
    public static void broadcastReadyStatus(String chatId, Map<String, Boolean> readinessMap) {
        List<UserInfo> participants = chatRooms.get(chatId);
        if (participants == null) return;

        for (UserInfo u : participants) {
            try {
                u.os.writeUTF("RACE_READY_STATUS");
                u.os.writeUTF(chatId);
                u.os.writeInt(readinessMap.size());
                for (Map.Entry<String, Boolean> entry : readinessMap.entrySet()) {
                    u.os.writeUTF(entry.getKey());          // 유저 이름
                    u.os.writeBoolean(entry.getValue());    // 준비 여부
                }
                u.os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static void handleCatchmindReady(String chatId, String userName) {
        if (!Server.catchmindReady.containsKey(chatId)) {
            HashMap<String, Boolean> map = new HashMap<>();
            // 채팅방 참가자 모두 false로 초기화
            List<UserInfo> participants = Server.chatRooms.get(chatId);
            if (participants != null) {
                for (UserInfo u : participants) {
                    map.put(u.name, false);
                }
            }
            Server.catchmindReady.put(chatId, map);
        }

        Map<String, Boolean> readinessMap = Server.catchmindReady.get(chatId);
        if (readinessMap != null && readinessMap.containsKey(userName)) {
            readinessMap.put(userName, true);
            if (readinessMap.values().stream().allMatch(Boolean::booleanValue)) {
                broadcastCatchmindReadyStatus(chatId, readinessMap);
                startCatchmindGame(chatId);
            } else {
                broadcastCatchmindReadyStatus(chatId, readinessMap);
            }
        }
    }

    private static void broadcastCatchmindReadyStatus(String chatId, Map<String, Boolean> readinessMap) {
        List<UserInfo> participants = Server.chatRooms.get(chatId);
        if (participants == null) return;

        for (UserInfo u : participants) {
            try {
                u.os.writeUTF("CATCHMIND_READY_STATUS");
                u.os.writeUTF(chatId);
                u.os.writeInt(readinessMap.size());
                for (Map.Entry<String, Boolean> entry : readinessMap.entrySet()) {
                    u.os.writeUTF(entry.getKey());        // 유저 이름
                    u.os.writeBoolean(entry.getValue());  // 준비 여부
                }
                u.os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 캐치마인드 단어 목록 로드 메서드
    private static void loadCatchmindWords() {
        InputStream is = Server.class.getClassLoader().getResourceAsStream("text/catchmind_words.txt");
        if (is == null) {
            System.err.println("Catchmind words file not found in resources: text/catchmind_words.txt");
            return;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            catchmindWords.clear(); // 기존 단어 목록 초기화
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    catchmindWords.add(line);
                }
            }
            System.out.println("Loaded " + catchmindWords.size() + " catchmind words.");
        } catch (IOException e) {
            System.err.println("Error reading catchmind words file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void startCatchmindGame(String chatId) {
        loadCatchmindWords();

        List<UserInfo> participants = Server.chatRooms.get(chatId);
        if (participants != null && !participants.isEmpty()) {
            // 랜덤 출제자 선정
            UserInfo drawer = participants.get((int) (Math.random() * participants.size()));
            // 출제자 이름을 채팅방별로 저장
            chatDrawerMap.put(chatId, drawer.name);

            Random rand = new Random();
            String answerWord = catchmindWords.get(rand.nextInt(catchmindWords.size()));

            chatAnswerMap.put(chatId, answerWord);

            try {
                // 출제자에게 정답 알림
                drawer.os.writeUTF("CATCHMIND_ANSWER");
                drawer.os.writeUTF(chatId);
                drawer.os.writeUTF(answerWord);
                drawer.os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 나머지 참가자에게 VIEW_ONLY 모드 알림
            for (UserInfo u : participants) {
                if (!u.name.equals(drawer.name)) {
                    try {
                        u.os.writeUTF("CATCHMIND_START");
                        u.os.writeUTF(chatId);
                        u.os.writeUTF("VIEW_ONLY");
                        u.os.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        u.os.writeUTF("CATCHMIND_START");
                        u.os.writeUTF(chatId);
                        u.os.writeUTF("DRAWER");
                        u.os.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // 모든 준비 완료 후 3초 카운트다운 -> RACE_START
    public static void startRaceCountDown(String chatId) {
        // 카운트다운 브로드캐스트
        for (int i = 3; i > 0; i--) {
            broadcastCountDown(chatId, i);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // 카운트다운 끝나면 레이스 시작
        startRacingGame(chatId);
    }

    private static void broadcastCountDown(String chatId, int count) {
        List<UserInfo> participants = chatRooms.get(chatId);
        if (participants == null) return;

        for (UserInfo u : participants) {
            try {
                u.os.writeUTF("RACE_COUNTDOWN");
                u.os.writeUTF(chatId);
                u.os.writeInt(count);
                u.os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void broadcastResetCanvas(String chatId) {
        List<UserInfo> participants = chatRooms.get(chatId);
        if (participants == null) return;

        for (UserInfo u : participants) {
            try {
                u.os.writeUTF("RESET_CANVAS");
                u.os.writeUTF(chatId);
                u.os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 레이싱 게임에 특화된 포인트 부여 메서드
    public static void awardPointsRacing(String chatId, String winnerName) {
        List<UserInfo> participants = chatRooms.get(chatId);
        if (participants == null) return;

        UserInfo winner = null;
        for (UserInfo u : participants) {
            if (u.name.equals(winnerName)) {
                winner = u;
                break;
            }
        }

        if (winner != null) {
            winner.addPoints(100); // 예: 100 포인트
            notifyPointsUpdated(winner);
        }
    }

    public static void awardPoints(String chatId, String winnerName, boolean timeUp) {
        List<UserInfo> participants = chatRooms.get(chatId);
        if (participants == null) return;

        String drawerName = getDrawerName(chatId);
        UserInfo drawer = null;
        UserInfo winner = null;

        for (UserInfo u : participants) {
            if (u.name.equals(drawerName)) {
                drawer = u;
            }
            if (u.name.equals(winnerName)) {
                winner = u;
            }
        }

        if (timeUp) {
            // 시간 초과: 출제자에게 포인트 부여
            if (drawer != null) {
                drawer.addPoints(100); // 예: 100 포인트
                notifyPointsUpdated(drawer);
            }
        } else {
            // 정답 맞춘 사용자에게 포인트 부여
            if (winner != null) {
                winner.addPoints(100); // 예: 100 포인트
                notifyPointsUpdated(winner);
            }
        }

        // 정답을 맞춘 경우 게임 종료 처리
        if (!timeUp) {
            endCatchmindGame(chatId);
        }
    }

    // 출제자 이름 가져오기
    static String getDrawerName(String chatId) {
        return chatDrawerMap.get(chatId);
    }

    // 포인트 업데이트를 클라이언트에 알림
    private static void notifyPointsUpdated(UserInfo user) {
        try {
            user.os.writeUTF("POINTS_UPDATED");
            user.os.writeInt(user.points);
            user.os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 게임 종료 처리
    static void endCatchmindGame(String chatId) {
        // 게임 종료 메시지를 모든 참가자에게 전송
        List<UserInfo> participants = chatRooms.get(chatId);
        if (participants == null) return;

        for (UserInfo u : participants) {
            try {
                u.os.writeUTF("CATCHMIND_END");
                u.os.writeUTF(chatId);
                u.os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 게임 상태 초기화
        catchmindReady.remove(chatId);
        chatAnswerMap.remove(chatId); // 정답 정보 삭제

        // 출제자 정보 정리
        chatDrawerMap.remove(chatId);
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
            String clientName = is.readUTF();
            userInfo = new UserInfo(clientName, null, "", socket, os, is);

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
                        String newName = is.readUTF();
                        int imgLen = is.readInt();
                        byte[] imgData = null;
                        if (imgLen > 0) {
                            imgData = new byte[imgLen];
                            is.readFully(imgData);
                        }
                        String newStatusMessage = is.readUTF(); // 상태메시지 수신

                        userInfo.name = newName;
                        userInfo.profileImageData = imgData;
                        userInfo.statusMessage = newStatusMessage;

                        // 모든 사용자에게 프로필 업데이트 알림
                        Server.broadcastProfileUpdate(userInfo);
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
                        String chatId = is.readUTF(); // 채팅방 ID
                        String msgType = is.readUTF(); // 메시지 타입(TEXT/IMAGE 등)

                        if ("TEXT".equals(msgType)) {
                            String txt = is.readUTF(); // 메시지 내용
                            Server.broadcastChatMessage(chatId, msgType, userInfo.name, null, txt);
                        } else if ("IMAGE".equals(msgType)) {
                            int len = is.readInt();
                            byte[] img = new byte[len];
                            is.readFully(img);
                            Server.broadcastChatMessage(chatId, msgType, userInfo.name, img, null);
                        }
                        break;
                    }
                    case "LEAVE_CHAT": {
                        String chatId = is.readUTF();
                        List<UserInfo> participants = Server.chatRooms.get(chatId);

                        if (participants != null) {
                            // 현재 사용자 제거
                            participants.removeIf(u -> u.name.equals(userInfo.name));

                            // 채팅방 참가자들에게 퇴장 알림
                            for (UserInfo participant : participants) {
                                try {
                                    participant.os.writeUTF("USER_LEAVE_CHAT");
                                    participant.os.writeUTF(chatId);
                                    participant.os.writeUTF(userInfo.name); // 퇴장한 사용자 이름
                                    participant.os.flush();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            // 채팅방에서 모든 참여자가 나갔으면 방 삭제
                            if (participants.isEmpty()) {
                                Server.chatRooms.remove(chatId);
                            }
                        }
                        break;
                    }

                    case "CREATE_RACING_GAME": {
                        String chatId = is.readUTF();
                        Server.startRacingGameForAll(chatId);
                        break;
                    }

                    case "CREATE_DRAWING_GAME": {
                        String chatId = is.readUTF();
                        Server.startDrawingGameForAll(chatId);
                        break;
                    }

                    case "RACE_READY": {
                        String chatId = is.readUTF();
                        Server.handleRaceReady(chatId, userInfo.name);
                        break;
                    }

                    case "MOVE": {
                        String chatId = is.readUTF();
                        Server.handleMoveCommand(chatId, userInfo.name);
                        break;
                    }

                    case "CATCHMIND_READY": {
                        String chatId = is.readUTF();
                        Server.handleCatchmindReady(chatId, userInfo.name);
                        break;
                    }

                    case "DRAW_EVENT": {
                        String chatId = is.readUTF();
                        int x1 = is.readInt();
                        int y1 = is.readInt();
                        int x2 = is.readInt();
                        int y2 = is.readInt();
                        int rgb = is.readInt(); // 펜 색상

                        broadcastDrawEvent(chatId, x1, y1, x2, y2, rgb);
                        break;
                    }
                    case "RESET_CANVAS": {
                        String chatId = is.readUTF();
                        Server.broadcastResetCanvas(chatId);
                        break;
                    }
                    case "CATCHMIND_ANSWER_ATTEMPT": {
                        String chatId = is.readUTF();
                        String attemptedAnswer = is.readUTF();

                        String correctAnswer = Server.chatAnswerMap.get(chatId);
                        if (attemptedAnswer.equalsIgnoreCase(correctAnswer)) {
                            Server.awardPoints(chatId, userInfo.name, false);
                            broadcastCorrectAnswer(chatId, userInfo.name);
                        } else {
                            System.out.println(userInfo.name + "님이 틀린 답: " + attemptedAnswer);
                        }
                        break;
                    }
                    case "CATCHMIND_TIME_UP": {
                        String chatId = is.readUTF();
                        System.out.println("CATCHMIND_TIME_UP received for chatId: " + chatId);
                        String drawerName = Server.getDrawerName(chatId);
                        if (drawerName != null) {
                            Server.awardPoints(chatId, drawerName, true);
                            Server.endCatchmindGame(chatId);
                        } else {
                            System.err.println("Drawer not found for chatId: " + chatId);
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

    public static void broadcastDrawEvent(String chatId, int x1, int y1, int x2, int y2, int rgb) {
        List<UserInfo> participants = Server.chatRooms.get(chatId);
        if (participants == null) return;
        for (UserInfo u : participants) {
            try {
                u.os.writeUTF("DRAW_EVENT");
                u.os.writeUTF(chatId);
                u.os.writeInt(x1);
                u.os.writeInt(y1);
                u.os.writeInt(x2);
                u.os.writeInt(y2);
                u.os.writeInt(rgb);
                u.os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void broadcastCorrectAnswer(String chatId, String winnerName) {
        List<UserInfo> participants = Server.chatRooms.get(chatId);
        for (UserInfo user : participants) {
            try {
                user.os.writeUTF("CATCHMIND_CORRECT_ANSWER");
                user.os.writeUTF(chatId);
                user.os.writeUTF(winnerName); // 정답을 맞춘 사람
                user.os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

//사용자 정보 관리 클래스
class UserInfo {
	String name;
	byte[] profileImageData;
    String statusMessage;
	Socket socket;
	DataOutputStream os;
	DataInputStream is;
    int points;

	public UserInfo(String name, byte[] profileImageData, String statusMessage, Socket socket, DataOutputStream os, DataInputStream is) {
		this.name = name;
		this.profileImageData = profileImageData;
        this.statusMessage = statusMessage;
		this.socket = socket;
		this.os = os;
		this.is = is;
        this.points = 0;
	}

    public void addPoints(int amount) {
        this.points += amount;
    }
}