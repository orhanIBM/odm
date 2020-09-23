package com.ibm.odmBatch;

import miniloan.Borrower;
import miniloan.Loan;
import ilog.rules.res.model.IlrPath;
import ilog.rules.res.session.*;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;
import java.util.logging.Logger;



public class DecisionRunner implements DBAccess {
    IlrStatelessSession session;
    IlrSessionRequest sessionRequest;
    Statement stmt;

    private static Logger logger = Logger.getLogger(DecisionRunner.class.getName());

    private static final String RULESET_PATH = "/mydeployment/Miniloan_ServiceRuleset";

    public DecisionRunner(Connection con) throws Exception {
        IlrPath rulesetPath = IlrPath.parsePath(RULESET_PATH);
        IlrSessionFactory factory = new IlrJ2SESessionFactory();


        IlrManagementSession managementSession = factory.createManagementSession();
        managementSession.loadUptodateRuleset(rulesetPath);

        session = factory.createStatelessSession();
        sessionRequest = factory.createRequest();
        sessionRequest.setRulesetPath(rulesetPath);
        sessionRequest.setTraceEnabled(false);

        stmt = con.createStatement();
    }

//    public void runAsynchronous(Map<String, Object> inputParameters, DecisionObserver observer) throws Exception {
//        sessionRequest.setInputParameters(inputParameters);
//        session.executeAsynchronous(sessionRequest, observer, -1);
//    }

    public void runSynchronous(Map<String, Object> inputParameters) throws Exception {
        sessionRequest.setInputParameters(inputParameters);
        IlrSessionResponse response = session.execute(sessionRequest);

        String name = ((Borrower)inputParameters.get("borrower")).getName();
        String approved = Boolean.toString(((Loan)response.getOutputParameters().get("loan")).isApproved());

        stmt.executeUpdate(
                "INSERT INTO RESPONSE(BorrowerName, LoanApproved)"
                        + " VALUES('" + name + "', '" + approved + "')");

    }
}
