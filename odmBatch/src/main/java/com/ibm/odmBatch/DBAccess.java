package com.ibm.odmBatch;

public interface DBAccess {
        final String URL = System.getenv("DBCONNECTIONURL");
        final String UID = System.getenv("DBUSERID");
        final String PWD = System.getenv("DBPASSWORD");
}