import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;

public class userPage {
    public static void main(int userId, String userName) {
        JFrame frame = new JFrame("DEU Library");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 500);
        frame.setLayout(null);

        String titleSuffix = "님 도서 이용 현황";

        JLabel titleLabel = new JLabel(userName + titleSuffix, SwingConstants.CENTER);
        titleLabel.setFont(new Font("돋움", Font.BOLD, 16));
        titleLabel.setBounds(50, 30, 300, 30);
        frame.add(titleLabel);

        JLabel back = new JLabel("<이전");
        back.setBounds(13, 10, 60, 30);
        back.setCursor(new Cursor(Cursor.HAND_CURSOR));
        frame.add(back);

        // 뒤로가기 버튼 클릭시 mainPage로 이동
        back.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                frame.dispose();
                mainPage.main(userId, userName);
            }
        });

        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BorderLayout());
        userPanel.setBounds(13, 80, 360, 300); // 위치와 크기 설정
        frame.add(userPanel);

        // 테이블 생성
        String[] overdueColumns = {"대출번호", "도서명", "저자", "대출일자", "연체기한"};
        DefaultTableModel overdueModel = new DefaultTableModel(overdueColumns, 0);
        JTable overdueTable = new JTable(overdueModel);
        JScrollPane overdueScrollPane = new JScrollPane(overdueTable); // 스크롤 추가

        userPanel.add(overdueScrollPane, BorderLayout.CENTER);

        JButton returnButton = new JButton("반납");
        returnButton.setBounds(13, 400, 360, 30);
        frame.add(returnButton);

        Connect dbConnection = new Connect();
        dbConnection.DB_Connect();

        // 데이터 로드 및 테이블 초기화
        refreshTableData(userId, overdueModel, dbConnection);

        // 반납 버튼 동작 정의
        returnButton.addActionListener(e -> {
            int selectedRow = overdueTable.getSelectedRow();
            if (selectedRow != -1) {
                // 대출번호가 올바르게 가져오는지 확인
                Object loanIdObj = overdueTable.getValueAt(selectedRow, 0);
                if (loanIdObj instanceof Integer) {
                    int loanId = (int) loanIdObj;
                    System.out.println("선택된 대출번호: " + loanId);
                    boolean isReturned = dbConnection.returnBook(loanId, userId);
                    if (isReturned) {
                        overdueModel.removeRow(selectedRow);
                        JOptionPane.showMessageDialog(frame, "선택된 도서가 성공적으로 반납되었습니다.");
                    } else {
                        JOptionPane.showMessageDialog(frame, "도서 반납 처리에 실패했습니다.");
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "대출번호 형식이 올바르지 않습니다.");
                }
            }
        });

        frame.setVisible(true);
    }

    // 테이블 새로고침 메서드
    private static void refreshTableData(int userId, DefaultTableModel overdueModel, Connect dbConnection) {
        overdueModel.setRowCount(0); // 기존 테이블 데이터 초기화

        try {
            ResultSet rs;

            if (String.valueOf(userId).length() == 5) {
                rs = dbConnection.getStaffLoanData(userId); // 교직원 대출 데이터 가져오기
            } else {
                rs = dbConnection.getStudentLoanData(userId); // 학생 대출 데이터 가져오기
            }

            // 새 데이터를 테이블에 추가
            while (rs != null && rs.next()) {
                overdueModel.addRow(new Object[]{
                        rs.getInt("대출번호"),
                        rs.getString("도서명"),
                        rs.getString("저자"),
                        rs.getDate("대출일자"),
                        rs.getInt("연체기한")
                });
            }
        } catch (SQLException e) {
            System.out.println("Error refreshing table data: " + e.getMessage());
        }
    }
}