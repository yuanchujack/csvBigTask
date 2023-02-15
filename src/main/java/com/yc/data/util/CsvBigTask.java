package com.yc.data.util;

import com.yc.data.entity.CmbMarketQuote;
import com.yc.data.service.CmbMarketQuoteService;

import java.io.ByteArrayOutputStream;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.concurrent.CyclicBarrier;


public class CsvBigTask  implements Runnable{



    //拦截器
    private CyclicBarrier cyclicBarrier;
    private long start; //起始位置
    private long end; //结束位置
    private String date; //运行日期
    private Map mapping; //
    private RandomAccessFile randomAccessFile;
    private CmbMarketQuoteService cmbMarketQuoteService;

    public CsvBigTask(CyclicBarrier cyclicBarrier, Map map, long start, long end,String date, RandomAccessFile randomAccessFile, CmbMarketQuoteService cmbMarketQuoteService) {
        this.cyclicBarrier = cyclicBarrier;
        this.start = start;
        this.end = end;
        this.mapping = map;
        this.date = date;
        this.randomAccessFile = randomAccessFile;
        this.cmbMarketQuoteService = cmbMarketQuoteService;
    }

    @Override
    public void run() {
        try {
            randomAccessFile.seek(start);

            List<CmbMarketQuote> list = new ArrayList<>(1000);
            while(start <= end) {
                byte b1 = '0';
                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                while (b1 != '\r' && b1 != '\n') {
                    bs.write(b1);
                    b1 = randomAccessFile.readByte();
                    ++start;
                }
                if(b1 != '\n') {
                    String[] line = bs.toString().split(",");
                    if(line != null && line.length == 7){

                        CmbMarketQuote cmbMarketQuote = BeanConvert.lineToBean(line,mapping,date);
                        list.add(cmbMarketQuote);
                    }
                }
                if(list.size() > 1000){
                    cmbMarketQuoteService.saveBatch(list);

                    list = new ArrayList<>();
                }
            }
            cmbMarketQuoteService.saveBatch(list);
            cyclicBarrier.await();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
