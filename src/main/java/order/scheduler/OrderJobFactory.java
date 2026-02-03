package main.java.order.scheduler;

import lombok.AllArgsConstructor;
import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

@AllArgsConstructor
public class OrderJobFactory implements JobFactory {

    private final OrderJob orderJob;

    @Override
    public Job newJob(TriggerFiredBundle triggerFiredBundle, Scheduler scheduler) {
        return orderJob;
    }
}
