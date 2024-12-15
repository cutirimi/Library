import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;

public class employeePage {
    private static String enteredId;
    private static LocalDate currentDate;
    private static DefaultTableModel workInfoModel = new DefaultTableModel(
            new String[]{"근무번호", "근무일자", "근무시간", "일일급여"}, 0
    );

    // DB 연결 객체를 전역 변수로 선언
    private static Connect dbConnect = new Connect();

    public static void main(String enteredIdParam, String userName) {
        enteredId = enteredIdParam; // 근로ID 저장
        currentDate = LocalDate.now(); // 현재 날짜

        // DB 연결 확인 및 초기화
        if (dbConnect == null || dbConnect.con == null) {
            dbConnect = new Connect();
            dbConnect.DB_Connect();
        }

        JFrame frame = new JFrame("DBP Library");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 500);
        frame.setLayout(new BorderLayout());

        // 제목 패널 생성
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(null); // null 레이아웃 사용
        titlePanel.setPreferredSize(new Dimension(frame.getWidth(), 50));

        // 제목 레이블 설정
        JLabel titleLabel = new JLabel(userName + "님 근로 Page", SwingConstants.CENTER);
        titleLabel.setFont(new Font("돋움", Font.BOLD, 18));
        titleLabel.setBounds(100, 10, 200, 30);
        titlePanel.add(titleLabel);

        // 로그아웃 레이블 설정
        JLabel logoutLabel = new JLabel("<html><u>로그아웃</u></html>");
        logoutLabel.setForeground(Color.BLACK);
        logoutLabel.setFont(new Font("돋움", Font.PLAIN, 12));
        logoutLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutLabel.setBounds(frame.getWidth() - 110, 10, 80, 30);
        logoutLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        titlePanel.add(logoutLabel);

        // 로그아웃 클릭 시 첫 로그인 화면으로 이동
        logoutLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                frame.dispose();
                login.main(null);
            }
        });

        frame.add(titlePanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();

        // 근로 정보 탭
        JPanel workInfoPanel = new JPanel();
        workInfoPanel.setLayout(null);

        JLabel workDate = new JLabel("근무일자: ");
        workDate.setBounds(13, 20, 60, 30);
        JLabel toDay = new JLabel();
        toDay.setBounds(70, 20, 80, 30);
        workInfoPanel.add(workDate);
        workInfoPanel.add(toDay);

        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        toDay.setText(currentDate.format(formatter));

        JLabel workHours = new JLabel("일일근무시간");
        workHours.setBounds(150, 20, 80, 30);
        workInfoPanel.add(workHours);

        JComboBox<Integer> workTimes = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});
        workTimes.setBounds(230, 20, 50, 30);
        workInfoPanel.add(workTimes);

        JButton addInfoButton = new JButton("등록");
        addInfoButton.setBounds(300, 20, 60, 30);
        workInfoPanel.add(addInfoButton);
        loadWorkInfo(dbConnect.con, enteredId); // 새로고침 호출

        // 근로 정보 테이블 생성
        JTable workInfoTable = new JTable(workInfoModel);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        try {
            workInfoTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
            workInfoTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
            workInfoTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
            workInfoTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "테이블 열 정렬 설정 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }

        JScrollPane workInfoScrollPane = new JScrollPane(workInfoTable);
        workInfoScrollPane.setBounds(15, 70, 350, 300);
        workInfoPanel.add(workInfoScrollPane);

        tabbedPane.addTab("근로정보", workInfoPanel);

        frame.add(tabbedPane, BorderLayout.CENTER);
        frame.setVisible(true);

        // 초기 데이터 로드 (DB에서 사용자 데이터 가져오기)
        try {
            Connect dbConnect = new Connect();
            dbConnect.DB_Connect();
            loadWorkInfo(dbConnect.con, enteredId); // 사용자 근로정보 로드
            dbConnect.DB_Disconnect();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "근로정보를 불러오는 중 오류가 발생했습니다: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }


        // 월급 조회 탭
        JPanel salaryPanel = new JPanel();
        salaryPanel.setLayout(null);

        // ComboBox 초기화
        JComboBox<String> yearCombo = new JComboBox<>();
        yearCombo.setBounds(50, 100, 100, 30);
        JComboBox<String> monthCombo = new JComboBox<>();
        monthCombo.setBounds(200, 100, 100, 30);

        JLabel yearLabel = new JLabel("년");
        yearLabel.setBounds(160, 100, 20, 30);
        JLabel monthLabel = new JLabel("월");
        monthLabel.setBounds(310, 100, 20, 30);

        salaryPanel.add(yearCombo);
        salaryPanel.add(yearLabel);
        salaryPanel.add(monthCombo);
        salaryPanel.add(monthLabel);

        // 월급 조회 버튼
        JButton queryButton = new JButton("월급 조회");
        queryButton.setBounds(135, 150, 100, 30);
        salaryPanel.add(queryButton);

        // 결과 출력 레이블
        JLabel dateLabel = new JLabel("", SwingConstants.CENTER);
        dateLabel.setFont(new Font("돋움", Font.BOLD, 18));
        dateLabel.setBounds(50, 200, 270, 30);
        salaryPanel.add(dateLabel);

        JLabel salaryLabel = new JLabel("", SwingConstants.CENTER);
        salaryLabel.setFont(new Font("돋움", Font.BOLD, 20));
        salaryLabel.setBounds(50, 240, 280, 30);
        salaryPanel.add(salaryLabel);

        // ComboBox 데이터 업데이트 호출
        updateComboBoxOptions(yearCombo, monthCombo);

        // 월급 조회 버튼 클릭 이벤트
        queryButton.addActionListener(e -> {
            String selectedYear = (String) yearCombo.getSelectedItem();
            String selectedMonth = (String) monthCombo.getSelectedItem();
            int totalSalary = 0;

            if (selectedYear == null || selectedMonth == null) {
                JOptionPane.showMessageDialog(null, "조회할 연도와 월을 선택하세요.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // DB 연결
                Connect dbConnect = new Connect();
                dbConnect.DB_Connect();

                // 저장 프로시저 호출
                totalSalary = dbConnect.getMonthlySalary(
                        enteredId,
                        Integer.parseInt(selectedYear),
                        Integer.parseInt(selectedMonth)
                );

                // DB 연결 해제
                dbConnect.DB_Disconnect();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        null,
                        "월급 조회 중 오류가 발생했습니다: " + ex.getMessage(),
                        "오류",
                        JOptionPane.ERROR_MESSAGE
                );
            }

            // 결과 출력
            if (totalSalary > 0) {
                dateLabel.setText(selectedYear + "년 " + selectedMonth + "월");
                salaryLabel.setText("총 월급: " + String.format("%,d원", totalSalary));
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        "선택한 월에 대한 근무 기록이 없습니다.",
                        "조회 결과 없음",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        });

        // 탭에 월급 조회 추가
        tabbedPane.addTab("월급조회", salaryPanel);

        //근로정보 등록 및 DB 업데이트
        addInfoButton.addActionListener(e -> {
            int selectedWorkHours = (int) workTimes.getSelectedItem(); // 선택된 근무시간 가져오기

            try {
                Connect dbConnect = new Connect();
                dbConnect.DB_Connect();

                // 근로ID의 시급 가져오기
                int hourlyWage = getHourlyWage(dbConnect.con, enteredId);

                // 일일급여 계산
                int dailySalary = hourlyWage * selectedWorkHours;

                // 근로ID에 대한 다음 근무번호 계산
                int nextWorkNumber = getNextWorkNumber(dbConnect.con, enteredId);

                // SQL INSERT 준비
                String query = "INSERT INTO 근로정보 (근무번호, 근로ID, 근무일자, 근무시간, 일일급여) " +
                        "VALUES (?, ?, TO_DATE(?, 'YYYY-MM-DD'), ?, ?)";
                PreparedStatement pstmt = dbConnect.con.prepareStatement(query);
                pstmt.setInt(1, nextWorkNumber);
                pstmt.setString(2, enteredId);
                pstmt.setString(3, currentDate.toString());
                pstmt.setInt(4, selectedWorkHours);
                pstmt.setInt(5, dailySalary);

                // 디버깅 로그 출력
                System.out.println("쿼리 실행: " + query);
                System.out.println("삽입 데이터: " + nextWorkNumber + ", " + enteredId + ", " +
                        currentDate.toString() + ", " + selectedWorkHours + ", " + dailySalary);

                // 쿼리 실행
                pstmt.executeUpdate();

                // Auto-Commit이 비활성화된 경우 명시적으로 커밋
                dbConnect.con.commit();

                // UI 테이블에 데이터 추가
                workInfoModel.addRow(new Object[]{
                        nextWorkNumber,
                        currentDate.toString(),
                        selectedWorkHours + "시간",
                        String.format("%,d원", dailySalary) // 숫자 포맷팅
                });

                JOptionPane.showMessageDialog(null, "근로 정보가 성공적으로 등록되었습니다.", "등록 성공", JOptionPane.INFORMATION_MESSAGE);

                pstmt.close();
                dbConnect.DB_Disconnect();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "등록 중 오류가 발생했습니다: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            }
        });


        // 연체자 조회 탭
        JPanel overduePanel = new JPanel();
        overduePanel.setLayout(new BorderLayout());

        DefaultTableModel overdueModel = new DefaultTableModel(
                new String[]{"학번/교번", "연체자명", "대출일자", "연체기한"}, 0
        );

        JTable overdueTable = new JTable(overdueModel);
        JScrollPane overdueScrollPane = new JScrollPane(overdueTable);
        overduePanel.add(overdueScrollPane, BorderLayout.CENTER);

        if (centerRenderer == null) {
            centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        }

        overdueTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // 학번/교번
        overdueTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer); // 연체자명
        overdueTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); // 대출일자
        overdueTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer); // 연체기한


        // 조회 버튼 생성 및 추가
        JButton refreshButton = new JButton("조회");
        refreshButton.setBounds(150, 10, 100, 30);
        overduePanel.add(refreshButton, BorderLayout.SOUTH);

        tabbedPane.addTab("연체자조회", overduePanel);

        // 연체자 조회 버튼 클릭 이벤트
        refreshButton.addActionListener(e -> {
            ResultSet rs = null;
            try {
                dbConnect.DB_Connect();

                // 기존 테이블 초기화
                overdueModel.setRowCount(0);

                // 저장 프로시저 호출하여 데이터 가져오기
                rs = dbConnect.getOverdueUsers();
                if (rs == null) {
                    JOptionPane.showMessageDialog(null, "저장 프로시저 실행 결과가 없습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                boolean hasData = false;
                while (rs.next()) {
                    hasData = true;
                    overdueModel.addRow(new Object[]{
                            rs.getString("학번_교번"),
                            rs.getString("사용자명"),
                            rs.getDate("대출일자"),
                            rs.getInt("연체기한") + "일"
                    });
                }

                if (!hasData) {
                    JOptionPane.showMessageDialog(null, "현재 연체자 정보가 없습니다.", "조회 결과 없음", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "연체자 조회 중 오류가 발생했습니다: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            } finally {
                try {
                    if (rs != null) rs.close();
                    dbConnect.DB_Disconnect();
                } catch (SQLException closeEx) {
                    closeEx.printStackTrace();
                }
            }
        });
    }


    // 근로학생 테이블에서 시급 가져오기
    private static int getHourlyWage(Connection con, String workerId) {
        int hourlyWage = 0;
        try {
            String query = "SELECT 시급 FROM 근로학생 WHERE 근로ID = ?";
            PreparedStatement pstmt = con.prepareStatement(query);
            pstmt.setString(1, workerId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                hourlyWage = rs.getInt("시급");
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hourlyWage;
    }

    // 근로ID별 다음 근무번호 계산
    private static int getNextWorkNumber(Connection con, String workerId) {
        int nextNumber = 1;
        try {
            String query = "SELECT NVL(MAX(근무번호), 0) + 1 AS NEXT_WORK_NO FROM 근로정보 WHERE 근로ID = ?";
            PreparedStatement pstmt = con.prepareStatement(query);
            pstmt.setString(1, workerId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                nextNumber = rs.getInt("NEXT_WORK_NO");
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nextNumber;
    }

    // 근로정보 조회
    private static void loadWorkInfo(Connection con, String workerId) {
    try {
        if (con == null || con.isClosed()) {
            dbConnect.DB_Connect(); // 연결 재설정
            con = dbConnect.con;   // 최신 연결 객체 갱신
        }

        // Statement로 쿼리 작성
        String query = "SELECT 근무번호, TO_CHAR(근무일자, 'YYYY-MM-DD') AS 근무일자, 근무시간, 일일급여 " +
                "FROM 근로정보 WHERE 근로ID = '" + workerId + "'";

        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        // 기존 데이터 초기화
        workInfoModel.setRowCount(0);

        // 결과 데이터 추가
        while (rs.next()) {
            int workNumber = rs.getInt("근무번호");
            String workDate = rs.getString("근무일자");
            int workHours = rs.getInt("근무시간");
            int dailySalary = rs.getInt("일일급여");

            // 일일급여 포맷팅
            NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.KOREA);
            String formattedSalary = numberFormat.format(dailySalary);

            workInfoModel.addRow(new Object[]{
                    workNumber, workDate, workHours + "시간", formattedSalary + "원"
            });
        }
        rs.close();
        stmt.close();
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "근로정보를 불러오는 중 오류가 발생했습니다: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
    } finally {
        dbConnect.DB_Disconnect(); // 연결 닫기
    }
}


    // ComboBox 업데이트 메서드
    private static void updateComboBoxOptions(JComboBox<String> yearCombo, JComboBox<String> monthCombo) {
        try {
            dbConnect.DB_Connect();

            PreparedStatement pstmt = dbConnect.con.prepareStatement(
                    "SELECT DISTINCT EXTRACT(YEAR FROM 근무일자) AS 년도, EXTRACT(MONTH FROM 근무일자) AS 월 " +
                            "FROM 근로정보 WHERE 근로ID = ?");
            pstmt.setString(1, enteredId);
            ResultSet rs = pstmt.executeQuery();

            // 기존 ComboBox 초기화
            yearCombo.removeAllItems();
            monthCombo.removeAllItems();

            while (rs.next()) {
                int year = rs.getInt("년도");
                int month = rs.getInt("월");

                // 중복 체크 후 추가
                if (((DefaultComboBoxModel<String>) yearCombo.getModel()).getIndexOf(String.valueOf(year)) == -1) {
                    yearCombo.addItem(String.valueOf(year));
                }
                if (((DefaultComboBoxModel<String>) monthCombo.getModel()).getIndexOf(String.valueOf(month)) == -1) {
                    monthCombo.addItem(String.valueOf(month));
                }
            }

            rs.close();
            pstmt.close();
            dbConnect.DB_Disconnect();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "ComboBox 옵션 업데이트 중 오류가 발생했습니다: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
}

