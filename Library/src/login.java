import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class login {
    public static void main(String[] args) {
        JFrame frame = new JFrame("DEU Library");
        frame.setSize(400, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);

        JLabel titleLabel = new JLabel("도서관 로그인");
        titleLabel.setBounds(100, 70, 200, 30);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 30));
        frame.add(titleLabel);

        JRadioButton studentButton = new JRadioButton("학생");
        studentButton.setBounds(110, 130, 60, 30);
        studentButton.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        studentButton.setSelected(true);

        JRadioButton staffButton = new JRadioButton("교직원");
        staffButton.setBounds(200, 130, 80, 30);
        staffButton.setFont(new Font("맑은 고딕", Font.BOLD, 15));

        ButtonGroup roleGroup = new ButtonGroup();
        roleGroup.add(studentButton);
        roleGroup.add(staffButton);

        frame.add(studentButton);
        frame.add(staffButton);

        JLabel idLabel = new JLabel("학번");
        idLabel.setBounds(70, 200, 80, 30);
        idLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        frame.add(idLabel);

        JPasswordField idField = new JPasswordField();
        idField.setBounds(120, 200, 180, 30);
        frame.add(idField);

        JLabel nameLabel = new JLabel("이름");
        nameLabel.setBounds(70, 250, 80, 30);
        nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        frame.add(nameLabel);

        JTextField nameField = new JTextField();
        nameField.setBounds(120, 250, 180, 30);
        frame.add(nameField);

        studentButton.addActionListener(e -> {
            idLabel.setText("학번");
            idField.setText(null);
            nameField.setText(null);
        });

        staffButton.addActionListener(e -> {
            idLabel.setText("교번");
            idField.setText(null);
            nameField.setText(null);
        });

        JButton loginButton = new JButton("로그인");
        loginButton.setBounds(100, 320, 200, 40);
        frame.add(loginButton);

        Connect dbConnection = new Connect();
        dbConnection.DB_Connect();

        loginButton.addActionListener(e -> {
            try {
                String idText = new String(idField.getPassword());
                Integer userId = Integer.parseInt(idText);
                String userName = nameField.getText();

                boolean loginSuccess = false;
                String userType = "";
                if (studentButton.isSelected()) {
                    loginSuccess = dbConnection.validateStudent(userId, userName);
                    userType = "학생";
                } else if (staffButton.isSelected()) {
                    loginSuccess = dbConnection.validateStaff(userId, userName);
                    userType = "교직원";
                }

                if (loginSuccess) {
                    JOptionPane.showMessageDialog(frame, "로그인 성공!");
                    frame.dispose();
                    mainPage.main(userId, userName);
                } else {
                    JOptionPane.showMessageDialog(frame, "학번(교번)과 이름을 확인하세요.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "학번(교번)은 숫자로 입력해야 합니다.", "입력 오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.setVisible(true);
    }
}
