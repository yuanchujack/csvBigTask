package com.yc.data.task;


import com.yc.data.service.CmbMarketQuoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.SimpleDateFormat;
import java.util.Date;


@Configuration
@EnableScheduling
public class DataExtractTask {
    private static final Logger logger = LoggerFactory.getLogger(DataExtractTask.class);
    @Autowired
    private CmbMarketQuoteService cmbMarketQuoteService;



    @Scheduled(cron = "0 0 16 * * ?")
    public void downloadExcel() {
        //操作时间key  杜绝操作失误如重复调用引起的重复下载 单机部署 通过concurrentHashMap 实现 ,集群部署 通过redis分布式锁实现
        //接口幂等 判断quoteDate字段是否已经存在
        //1.拆分csv文件 2.多线程

        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        if(cmbMarketQuoteService.downloadExcel(date)){
            try {
                cmbMarketQuoteService.insertByFile(date);
            } catch (Exception e) {
                logger.error("insertByFile error:{}",e.getMessage());
                e.printStackTrace();
            }
        }

        //下载完成 如果和其他业务有往来,通过消息中间件消息推送提醒(解耦)
    }

    @Scheduled(cron = "20 0 12 * * ?  ")
    public void check() throws Exception {
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        //cmbMarketQuoteService.downloadExcel(date);

        cmbMarketQuoteService.insertByFile(date);
/*
        try {
            cmbMarketQuoteService.insertByFile(date);
        } catch (Exception e) {
            logger.error("insertByFile error:{}",e.getMessage());
            e.printStackTrace();
        }*/

    }
}
