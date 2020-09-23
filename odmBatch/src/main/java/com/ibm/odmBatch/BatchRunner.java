package com.ibm.odmBatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import miniloan.Borrower;
import miniloan.Loan;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;


@SpringBootApplication(scanBasePackages = "com.ibm")
public class BatchRunner implements DBAccess {
    private void initResponseTable(Connection con) throws SQLException {
        Statement stmt = con.createStatement();
        stmt.executeUpdate("DROP TABLE RESPONSE");
        stmt.executeUpdate("CREATE TABLE RESPONSE(BorrowerName VARCHAR(20), LoanApproved VARCHAR(10) )");
        stmt.close();
        con.commit();
    }


    public void run() throws Exception {
        try {

            //Connection con = DriverManager.getConnection(URL, UID, PWD);

            //SSL settings for DB2 on OCP
            //https://www.ibm.com/support/knowledgecenter/SSEPGG_11.5.0/com.ibm.db2.luw.apdv.java.doc/src/tpc/imjcc_cjvjdbas.html
            //https://www.ibm.com/support/knowledgecenter/SSEPEK_11.0.0/java/src/tpc/imjcc_t0054065.html
            Class.forName("com.ibm.db2.jcc.DB2Driver");
            java.util.Properties properties = new java.util.Properties();
            properties.put("user", UID);
            properties.put("password", PWD);
            properties.put("sslConnection", "true");
            java.sql.Connection con =
                    java.sql.DriverManager.getConnection(URL, properties);

            initResponseTable(con);

            Statement stmt = con.createStatement();

            DecisionRunner decisionRunner = new DecisionRunner(con);
            //DecisionObserver decisionObserver = new DecisionObserver(con);

            long t0 = System.currentTimeMillis();

            String query = "SELECT * FROM REQUEST";
            ResultSet rs = stmt.executeQuery(query);
            //while (rs.next()) {
            //
            // Just process the first N rows...
            //
            final int N = 10;
            for (int i = 0; i < N; i++) {
                rs.next();
                //
                // Borrower attributes
                //
                String borrowerName = rs.getString("BorrowerName");
                int borrowerScore = rs.getInt("CreditScore");
                int borrowerIncome = rs.getInt("YearlyIncome");
                Borrower borrower = new Borrower(borrowerName, borrowerScore, borrowerIncome);
                //
                // Loan attributes
                //
                int loanAmount = rs.getInt("LoanAmount");
                int loanDuration = rs.getInt("LoanDuration");
                double loanInterestRate = rs.getDouble("LoanInterestRate");
                Loan loan = new Loan(loanAmount, loanDuration, loanInterestRate);

                Map<String, Object> params = new HashMap<String, Object>();
                params.put("borrower", borrower);
                params.put("loan", loan);

                //decisionRunner.runAsynchronous(params, decisionObserver);
                decisionRunner.runSynchronous(params);
            }

            rs.close();
            stmt.close();

            con.commit();

            long t1 = System.currentTimeMillis();
            long t = t1 - t0;
            System.out.println("Processed " + N + " rows in " + t + "ms");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static void testConnection() {
        try {
            //Connection con = DriverManager.getConnection(URL, UID, PWD);

            //SSL settings for DB2 on OCP
            //https://www.ibm.com/support/knowledgecenter/SSEPGG_11.5.0/com.ibm.db2.luw.apdv.java.doc/src/tpc/imjcc_cjvjdbas.html
            //https://www.ibm.com/support/knowledgecenter/SSEPEK_11.0.0/java/src/tpc/imjcc_t0054065.html
            Class.forName("com.ibm.db2.jcc.DB2Driver");
            java.util.Properties properties = new java.util.Properties();
            properties.put("user", UID);
            properties.put("password", PWD);
            properties.put("sslConnection", "true");
            java.sql.Connection con =
                    java.sql.DriverManager.getConnection(URL, properties);


            Statement stmt = con.createStatement();
            String query = "SELECT COUNT(*) FROM REQUEST"; // SYSCAT.TABLES";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                System.out.println(rs.getInt(1) + " requests in REQUEST table");
            }

            rs.close();
            stmt.close();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(BatchRunner.class, args);
//        System.out.print("Docker!");
        testConnection();
        BatchRunner runner = new BatchRunner();
        runner.run();
    }
}
