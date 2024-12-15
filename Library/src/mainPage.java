import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class mainPage {
    private static int getNextLoanId(Connection con, int userId) throws SQLException {
        // 사용자 ID의 길이를 계산 (int를 String으로 변환한 후 길이 계산)
        boolean isStudent = String.valueOf(userId).length() == 6; // 학번은 6자리, 교번은 5자리

        // 쿼리 작성
        String query = "SELECT NVL(MAX(대출번호), 0) AS MAX_LOAN_ID " +
                "FROM " + (isStudent ? "대출1" : "대출2") + " " +
                "WHERE " + (isStudent ? "학번" : "교번") + " = ?";

        // 초기값 설정
        int nextLoanId = 1;

        // 자원 해제 보장 (try-with-resources 사용)
        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setInt(1, userId); // 사용자 ID를 int로 설정
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    nextLoanId = rs.getInt("MAX_LOAN_ID") + 1; // 최대값 + 1
                }
            }
        } catch (SQLException e) {
            System.err.println("대출번호 생성 중 오류 발생: " + e.getMessage());
            throw e; // 에러 재발생
        }

        return nextLoanId; // 결과 반환
    }
    public static void main(int userId, String userName) {
        JFrame frame = new JFrame("DEU Library");
        frame.setSize(400, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);

        JButton myPageButton = new JButton("My Page");
        myPageButton.setBounds(10, 10, 100, 30);
        frame.add(myPageButton);

        // My Page 클릭시 개인 도서 이용 현황 페이지로 이동
        myPageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose(); // 현재 창 닫기
                userPage.main(userId, userName); // userPage 열기
            }
        });

        JLabel logoutLabel = new JLabel("<html><u>로그아웃</u></html>");
        logoutLabel.setBounds(270, 10, 50, 30);
        logoutLabel.setForeground(Color.BLACK); // 파란색 텍스트
        logoutLabel.setCursor(new Cursor(Cursor.HAND_CURSOR)); // 손가락 커서
        frame.add(logoutLabel);

        // 로그아웃 클릭시 로그인 페이지로 이동
        logoutLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                frame.dispose();
                login.main(null);
            }
        });

        JLabel employeeLabel = new JLabel("<html><u>근무자</u></html>");
        employeeLabel.setBounds(330, 10, 50, 30);
        employeeLabel.setForeground(Color.BLUE); // 파란색 텍스트
        employeeLabel.setCursor(new Cursor(Cursor.HAND_CURSOR)); // 손가락 커서
        frame.add(employeeLabel);

        // 근무자 클릭시 근무자 로그인 페이지로 이동
        employeeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                frame.dispose();
                employeeLogin.main(userName);
            }
        });

        JLabel searchLabel = new JLabel("도서 검색");
        searchLabel.setBounds(160, 50, 200, 30);
        searchLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        frame.add(searchLabel);

        JLabel bookNameLabel = new JLabel("도서명:");
        bookNameLabel.setBounds(15, 100, 50, 30);
        frame.add(bookNameLabel);

        JTextField bookNameField = new JTextField();
        bookNameField.setBounds(60, 100, 100, 30);
        frame.add(bookNameField);

        JLabel authorLabel = new JLabel("저자:");
        authorLabel.setBounds(165, 100, 50, 30);
        frame.add(authorLabel);

        JTextField authorField = new JTextField();
        authorField.setBounds(200, 100, 100, 30);
        frame.add(authorField);

        JButton searchButton = new JButton("검색");
        searchButton.setBounds(313, 100, 60, 30);
        frame.add(searchButton);

        // 테이블 생성
        String[] columnNames = {"도서번호", "분야", "도서명", "저자", "출판사", "도서상태"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(tableModel);

        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBounds(13, 150, 360, 200);
        frame.add(tableScrollPane);

        JButton loanButton = new JButton("대출");
        loanButton.setBounds(150, 370, 100, 40);
        frame.add(loanButton);

        frame.setVisible(true);

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String bookName = bookNameField.getText().trim();
                String author = authorField.getText().trim();

                // 테이블 초기화
                tableModel.setRowCount(0);

                // Connect 클래스 사용
                Connect dbConnect = new Connect();
                dbConnect.DB_Connect(); // DB 연결

                try {
                    // 동적 쿼리 생성
                    String query = "SELECT * FROM 도서 WHERE 1=1";
                    if (!bookName.isEmpty()) {
                        query += " AND 도서명 LIKE '%" + bookName + "%'";
                    }
                    if (!author.isEmpty()) {
                        query += " AND 저자 LIKE '%" + author + "%'";
                    }

                    // Statement 사용
                    Statement stmt = dbConnect.con.createStatement();
                    ResultSet rs = stmt.executeQuery(query);

                    if (!rs.isBeforeFirst()) { // 결과가 없을 경우
                        JOptionPane.showMessageDialog(frame, "검색 결과가 없습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }

                    while (rs.next()) {
                        Object[] row = {
                                rs.getInt("도서번호"),
                                rs.getString("분야"),
                                rs.getString("도서명"),
                                rs.getString("저자"),
                                rs.getString("출판사"),
                                rs.getString("도서상태")
                        };
                        tableModel.addRow(row);
                    }

                    // UI 업데이트
                    SwingUtilities.invokeLater(() -> tableModel.fireTableDataChanged());
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "검색 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                } finally {
                    dbConnect.DB_Disconnect(); // DB 연결 종료
                }
            }
        });

        //대출 버튼
        loanButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(frame, "대출할 도서를 선택하세요.", "알림", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // 테이블에서 선택된 도서번호 가져오기
                int bookId = (int) table.getValueAt(selectedRow, 0);

                // Connect 클래스 사용
                Connect dbConnect = new Connect();
                dbConnect.DB_Connect();

                try {
                    String loanTable;
                    String idColumn;
                    if (String.valueOf(userId).length() == 5) {
                        loanTable = "대출2";
                        idColumn = "교번";
                    } else if (String.valueOf(userId).length() == 6) {
                        loanTable = "대출1";
                        idColumn = "학번";
                    } else {
                        JOptionPane.showMessageDialog(frame, "잘못된 ID 형식입니다.", "오류", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // 트랜잭션 시작
                    dbConnect.con.setAutoCommit(false); //자동 커밋 비활성화
                    // 최대 대출번호 가져오기
                    int loanId = getNextLoanId(dbConnect.con, userId);

                    // 대출 데이터 삽입
                    String insertQuery = "INSERT INTO " + loanTable + " (대출번호, " + idColumn + ", 도서번호, 대출일자, 반납일자, 연체기한) " +
                            "VALUES (?, ?, ?, SYSDATE, NULL, NULL)";

                    PreparedStatement pstmt = dbConnect.con.prepareStatement(insertQuery);
                    pstmt.setInt(1, loanId);
                    pstmt.setInt(2, userId);
                    pstmt.setInt(3, bookId);

                    pstmt.executeUpdate();

                    //트랜잭션 커밋
                    dbConnect.con.commit();
                    JOptionPane.showMessageDialog(frame, "도서 대출이 성공적으로 완료되었습니다.", "대출 완료", JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException ex) {
                    // 트리거에서 발생한 예외 처리
                    String errorMessage = ex.getMessage();

                    // ORA-20001 또는 ORA-20002 등의 사용자 정의 예외 코드 처리
                    if (errorMessage.contains("ORA-20002")) {
                        JOptionPane.showMessageDialog(frame, "최대 대출 권수를 초과하여 대출이 불가능합니다.", "대출 불가", JOptionPane.WARNING_MESSAGE);
                    } else if (errorMessage.contains("ORA-20001")) {
                        JOptionPane.showMessageDialog(frame, "연체 도서가 있어 대출이 불가능합니다.", "대출 불가", JOptionPane.WARNING_MESSAGE);
                    } else if (errorMessage.contains("ORA-20003")) {
                        JOptionPane.showMessageDialog(frame, "이미 대출중인 도서입니다.", "대출 불가", JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(frame, "대출 처리 중 알 수 없는 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                    }
                    ex.printStackTrace();
                } finally {
                    dbConnect.DB_Disconnect();
                }
            }
        });

    }
}
