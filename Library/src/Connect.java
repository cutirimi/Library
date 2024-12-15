import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

import oracle.jdbc.OracleTypes;

public class Connect {
    Connection con = null;
    String url;
    String id;
    String password;

    public Connect() {
        try {
            // 설정 파일 읽기
            Properties properties = new Properties();
            properties.load(new FileInputStream("config.properties"));

            // 설정값 가져오기
            url = properties.getProperty("db.url");
            id = properties.getProperty("db.username");
            password = properties.getProperty("db.password");

        } catch (IOException e) {
            System.err.println("설정 파일을 읽는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    public void DB_Connect() {
        try {
            con = DriverManager.getConnection(url, id, password);
            con.setAutoCommit(false); // 자동 커밋 비활성화
            System.out.println("DB 연결 성공");
        } catch (SQLException e) {
            System.out.println("Connection Fail");
            e.printStackTrace();
        }
    }

    public void DB_Disconnect() {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
                System.out.println("DB 연결 종료 성공");
            }
        } catch (SQLException e) {
            System.out.println("Disconnect Fail: " + e.getMessage());
        }
    }

    // 학생 데이터 확인(login)
    public boolean validateStudent(int userId, String userName) {
        if (con == null) {
            System.out.println("Database connection is null. Check your connection.");
            throw new IllegalStateException("Database connection is not established.");
        }

        String query = "SELECT COUNT(*) FROM 학생 WHERE 학번 = ? AND 이름 = ?";
        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, userName);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true; // 사용자 존재
            }
        } catch (SQLException e) {
            System.out.println("Validation Error: " + e.getMessage());
        }
        return false; // 사용자 없음
    }

    // 교직원 데이터 확인(login)
    public boolean validateStaff(int userId, String userName) {
        String query = "SELECT COUNT(*) FROM 교직원 WHERE 교번 = ? AND 이름 = ?";
        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, userName);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true; // 교직원 존재
            }
        } catch (SQLException e) {
            System.out.println("Validation Error: " + e.getMessage());
        }
        return false; // 교직원 없음
    }

    // 학생 대출 데이터 가져오기
    public ResultSet getStudentLoanData(int userId) {
        String query = "SELECT 대출1.대출번호, 도서.도서명, 도서.저자, 대출1.대출일자, 대출1.연체기한 " +
                "FROM 대출1 " +
                "JOIN 도서 ON 대출1.도서번호 = 도서.도서번호 " +
                "WHERE 대출1.학번 = ? AND 도서.도서상태 != '대출가능'";
        try {
            PreparedStatement pstmt = con.prepareStatement(query);
            pstmt.setInt(1, userId);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            System.out.println("Data Fetch Error (Student): " + e.getMessage());
        }
        return null;
    }

    // 교직원 대출 데이터 가져오기
    public ResultSet getStaffLoanData(int userId) {
        String query = "SELECT 대출2.대출번호, 도서.도서명, 도서.저자, 대출2.대출일자, 대출2.연체기한 " +
                "FROM 대출2 " +
                "JOIN 도서 ON 대출2.도서번호 = 도서.도서번호 " +
                "WHERE 대출2.교번 = ? AND 도서.도서상태 != '대출가능'";
        try {
            PreparedStatement pstmt = con.prepareStatement(query);
            pstmt.setInt(1, userId);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            System.out.println("Data Fetch Error (Staff): " + e.getMessage());
        }
        return null;
    }

    // 도서 반납시 도서 상태 처리
    public boolean returnBook(int loanId, int userId) {
        String updateBookStatusQuery;
        String updateReturnDateQuery;

        if (String.valueOf(userId).length() == 5) { // 교직원
            updateBookStatusQuery = "UPDATE 도서 SET 도서상태 = '대출가능' " +
                    "WHERE 도서상태 = '대출중' AND 도서번호 = (" +
                    "    SELECT 도서번호 FROM 대출2 WHERE 대출번호 = ? AND 교번 = ? AND ROWNUM = 1" +
                    ")";
            updateReturnDateQuery = "UPDATE 대출2 SET 반납일자 = SYSDATE " +
                    "WHERE 대출번호 = ? AND 교번 = ?";
        } else { // 학생
            updateBookStatusQuery = "UPDATE 도서 SET 도서상태 = '대출가능' " +
                    "WHERE 도서상태 = '대출중' AND 도서번호 = (" +
                    "    SELECT 도서번호 FROM 대출1 WHERE 대출번호 = ? AND 학번 = ? AND ROWNUM = 1" +
                    ")";
            updateReturnDateQuery = "UPDATE 대출1 SET 반납일자 = SYSDATE " +
                    "WHERE 대출번호 = ? AND 학번 = ?";
        }

        try {
            con.setAutoCommit(false); // 트랜잭션 시작

            // 1. 도서 상태를 '대출가능'으로 변경
            try (PreparedStatement pstmt1 = con.prepareStatement(updateBookStatusQuery)) {
                pstmt1.setInt(1, loanId);     // 대출번호
                pstmt1.setInt(2, userId);     // 교번/학번
                int bookRowsAffected = pstmt1.executeUpdate();
                if (bookRowsAffected == 0) {
                    System.out.println("도서 상태 업데이트 실패: 대출번호 또는 교번/학번이 일치하지 않습니다.");
                    con.rollback();
                    return false;
                }
            }

            // 2. 대출 테이블에 반납일자 기록
            try (PreparedStatement pstmt2 = con.prepareStatement(updateReturnDateQuery)) {
                pstmt2.setInt(1, loanId);     // 대출번호
                pstmt2.setInt(2, userId);     // 교번/학번
                int returnRowsAffected = pstmt2.executeUpdate();
                if (returnRowsAffected == 0) {
                    System.out.println("반납일자 업데이트 실패: 대출번호 또는 교번/학번이 일치하지 않습니다.");
                    con.rollback();
                    return false;
                }
            }

            con.commit(); // 트랜잭션 커밋
            System.out.println("반납 처리 완료.");
            return true;
        } catch (SQLException e) {
            try {
                con.rollback(); // 트랜잭션 롤백
            } catch (SQLException rollbackEx) {
                System.out.println("Rollback Error: " + rollbackEx.getMessage());
            }
            System.out.println("Return Book Error: " + e.getMessage());
        }
        return false;
    }

    // 저장 프로시저 GET_MONTHLY_SALARY 호출 메서드
    public int getMonthlySalary(String workerId, int year, int month) throws SQLException {
        int totalSalary = 0;
        String procedureCall = "{CALL GET_MONTHLY_SALARY(?, ?, ?, ?)}";

        try (CallableStatement cstmt = con.prepareCall(procedureCall)) {
            // 파라미터 설정
            cstmt.setString(1, workerId);  // 근로자 ID
            cstmt.setInt(2, year);         // 연도
            cstmt.setInt(3, month);        // 월
            cstmt.registerOutParameter(4, Types.INTEGER); // 출력 파라미터 등록

            // 저장 프로시저 실행
            cstmt.execute();

            // 출력 파라미터에서 결과 가져오기
            totalSalary = cstmt.getInt(4);
        } catch (SQLException e) {
            System.err.println("저장 프로시저 호출 중 오류 발생: " + e.getMessage());
            throw e; // 호출한 쪽에서 처리할 수 있도록 예외 재발생
        }

        return totalSalary; // 조회된 월급 반환
    }

    // 연체자 정보 조회를 위한 저장 프로시저 호출 메서드
    public ResultSet getOverdueUsers() {
        String procedureCall = "{CALL check_overdue_info(?)}";
        ResultSet rs = null;

        try {
            CallableStatement cstmt = con.prepareCall(procedureCall);
            cstmt.registerOutParameter(1, OracleTypes.CURSOR); // REF CURSOR 설정
            cstmt.execute(); // 저장 프로시저 실행
            rs = (ResultSet) cstmt.getObject(1); // REF CURSOR를 ResultSet으로 반환
        } catch (SQLException e) {
            System.out.println("연체자 정보 조회 중 오류 발생: " + e.getMessage());
        }
        return rs;
    }

}
