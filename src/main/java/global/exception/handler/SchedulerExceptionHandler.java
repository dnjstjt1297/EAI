package main.java.global.exception.handler;


import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchedulerExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(SchedulerExceptionHandler.class);

    public void handle(JobExecutionContext context, Exception e) {
        String jobName = context.getJobDetail().getKey().getName();
        String groupName = context.getJobDetail().getKey().getGroup();

        while (e instanceof InvocationTargetException ite) {
            e = (Exception) ite.getTargetException();
        }

        if (e instanceof RuntimeException && e.getCause() instanceof SQLException) {
            log.error("[ERROR] Scheduler system failure | Job: {}.{} | Cause: DB 연결 불가",
                    groupName, jobName);
        } else {
            log.warn("[WARM] Scheduler system failure | Job: {} | message: {}",
                    jobName, e.getMessage());
        }
    }
}
