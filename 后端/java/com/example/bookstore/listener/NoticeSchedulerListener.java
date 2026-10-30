package com.example.bookstore.listener;

import com.example.bookstore.service.NoticeService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 通知扫描定时任务：启动 60 秒后首次执行，此后每小时扫描一次
 * 到期提醒、逾期提醒与预约过期，生成站内通知。
 */
@WebListener
public class NoticeSchedulerListener implements ServletContextListener {

    private ScheduledExecutorService pool;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        pool = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "notice-scheduler");
                t.setDaemon(true);
                return t;
            }
        });
        pool.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    new NoticeService().scanAndNotify();
                } catch (Exception ignored) {
                    // 扫描失败不影响主流程，等下一轮
                }
            }
        }, 60, 3600, TimeUnit.SECONDS);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (pool != null) {
            pool.shutdownNow();
        }
    }
}
