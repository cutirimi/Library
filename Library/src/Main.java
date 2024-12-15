public class Main {
    public static void main(String[] args) {
        Connect connect = new Connect(); // Connect 객체 생성
        connect.DB_Connect();           // DB 연결 메서드 호출
        login.main(null);

        connect.DB_Disconnect();        // DB 연결해제
    }
}