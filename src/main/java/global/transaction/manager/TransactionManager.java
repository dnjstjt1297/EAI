package main.java.global.transaction.manager;

public interface TransactionManager {

    void doBegin();

    void doCommit();

    void doRollback();

    void doCleanUpAfterCompletion();
}
