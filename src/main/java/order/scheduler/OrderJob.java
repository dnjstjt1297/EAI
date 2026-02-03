package main.java.order.scheduler;

import java.util.UUID;
import lombok.AllArgsConstructor;
import main.java.global.exception.handler.SchedulerExceptionHandler;
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
    private final SchedulerExceptionHandler schedulerExceptionHandler;

    @Override
    @LogExecution
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        MDC.put("request_id", "BATCH-" + UUID.randomUUID().toString().substring(0, 5));
        try {
            orderService.shipmentUpload();
        } catch (Exception e) {
            schedulerExceptionHandler.handle(jobExecutionContext, e);
        }
    }
}
