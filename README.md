# LibrarySystem - DataBase Team Project
### 개발 기간: 2024.10.21 ~ 2024.12.09
### 개발 언어: JAVA, Oracle
### 개발 환경: IntelliJ, SQL Developer
### 개발조건: 저장 프로시저, 트리거, Statement, PreparedStatement, CallableStatement 구현
<br><br>
## 기능소개
### 로그인
#### : 첫화면으로 학생과 교직원을 분류하여 로그인(학생-학번, 이름 / 교직원-교번, 이름)
### 도서 검색
#### : 메인화면으로 도서 검색을 통해 도서 대출
### My Page
#### : 사용자의 개인 페이지로 특정 사용자의 현재 도서 대출 현황 조회
### 반납
#### : 대출한 도서를 반납하면 GUI상에서 도서 데이터는 삭제(단, DB에는 대출 데이터 기록이 존재). DB 대출 테이블의 반납일자에는 반납버튼을 누른 현재날짜(SYSDATE)로 UPDATE
### 근로자 Page
#### : 근무정보 등록 / 월급조회 / 연체자조회 기능 포함
