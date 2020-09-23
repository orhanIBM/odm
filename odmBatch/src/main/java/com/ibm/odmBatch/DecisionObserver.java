package com.ibm.odmBatch;

import java.sql.Connection;
import java.util.Map;
import java.util.logging.Logger;

import ilog.rules.res.session.IlrSessionResponse;
import ilog.rules.res.session.async.IlrAsyncExecutionEndedEvent;
import ilog.rules.res.session.async.IlrAsyncExecutionEvent;
import ilog.rules.res.session.async.IlrAsyncExecutionFailedEvent;
import ilog.rules.res.session.async.IlrAsyncExecutionObserver;

public class DecisionObserver implements IlrAsyncExecutionObserver, DBAccess {

    private static Logger logger = Logger.getLogger(DecisionObserver.class.getName());

    public DecisionObserver(Connection con) {
    }

    @Override
    public void update(IlrAsyncExecutionEvent event) {
        if (event instanceof IlrAsyncExecutionEndedEvent) {
            IlrAsyncExecutionEndedEvent endedEvent = (IlrAsyncExecutionEndedEvent) event;
            IlrSessionResponse response = endedEvent.getResponse();
            writeResponse(response.getOutputParameters());
        } else if (event instanceof IlrAsyncExecutionFailedEvent) {
            IlrAsyncExecutionFailedEvent failedEvent = (IlrAsyncExecutionFailedEvent) event;
            logger.severe("Error: " + failedEvent.getException().getMessage());
        }
    }

    private void writeResponse(Map<String, Object> output) {
        try {
            logger.info("Processing decision response");
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
    }

}

