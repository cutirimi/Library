import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class employeeLogin {
    public static void main(String userName) {
        JFrame frame = new JFrame("DEU Library");
        frame.setSize(300, 350);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);

        JLabel titleLabel = new JLabel("근로자 로그인");
        titleLabel.setBounds(45, 70, 200, 30);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 25)); // 폰트 크기 변경
        frame.add(titleLabel);

        JLabel idLabel = new JLabel("근로ID");
        idLabel.setBounds(50, 145, 50, 30);
        idLabel.setFont(new Font("맑은 고딕", Font.BOLD, 10)); // 폰트 크기 변경
        frame.add(idLabel);

        JPasswordField idField = new JPasswordField();
        idField.setBounds(95, 145, 140, 30);
        frame.add(idField);

        JButton loginButton = new JButton("로그인");
        loginButton.setBounds(65, 220, 170, 40);
        loginButton.setFont(new Font("맑은 고딕", Font.BOLD, 10)); // 폰트 크기 변경
        frame.add(loginButton);

        // 로그인 버튼 클릭시 관리자 페이지로 이동
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String enteredId = idField.getText().trim(); // 근로 ID 입력값 가져오기
                if (enteredId.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "근로 ID를 입력하세요.", "알림", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Connect dbConnect = new Connect();
                dbConnect.DB_Connect(); // DB 연결

                try {
                    // 근로 ID로 예금주명 가져오기
                    String query = "SELECT 예금주명 FROM 근로학생 WHERE 근로ID = ?";
                    PreparedStatement pstmt = dbConnect.con.prepareStatement(query);
                    pstmt.setString(1, enteredId);

                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        // 근로 ID가 존재할 경우 예금주명 저장
                        String accountHolderName = rs.getString("예금주명");

                        // 이후 로직에서 예금주명 사용
                        JOptionPane.showMessageDialog(frame, "로그인 성공! 근로자명: " + accountHolderName, "로그인 성공", JOptionPane.INFORMATION_MESSAGE);

                        frame.dispose();
                        employeePage.main(enteredId, accountHolderName); // 예금주명과 함께 employeePage로 이동
                    } else {
                        // 근로 ID가 존재하지 않을 경우
                        JOptionPane.showMessageDialog(frame, "근로 ID가 유효하지 않습니다.", "로그인 실패", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "로그인 처리 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                } finally {
                    dbConnect.DB_Disconnect(); // DB 연결 해제
                }
            }
        });

        // 프레임 표시
        frame.setVisible(true);
    }
}
