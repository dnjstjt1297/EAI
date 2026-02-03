package main.java.order.scheduler;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import lombok.AllArgsConstructor;
import main.java.global.logging.LogContext;
import main.java.global.logging.annotation.LogExecution;
import main.java.order.service.OrderService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;


@AllArgsConstructor
public class OrderJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(OrderJob.class);

    private final OrderService orderService;

    @Override
    @LogExecution
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        MDC.put("request_id", "BATCH-" + UUID.randomUUID().toString().substring(0, 5));
        try {
            orderService.shipmentUpload();
        } catch (Exception e) {
            exceptionHandle(jobExecutionContext, e);
        }
    }

    private void exceptionHandle(JobExecutionContext jobExecutionContext, Exception e) {
        Exception exception = e;
        while (exception instanceof InvocationTargetException ite) {
            exception = (Exception) ite.getCause();
        }
        logger.error("{}[ERROR] jobExecutionContext: {}, Exception: ", LogContext.getIndent(),
                jobExecutionContext, exception);
    }
}
