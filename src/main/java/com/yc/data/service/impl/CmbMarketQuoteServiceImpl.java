package com.yc.data.service.impl;

import com.yc.data.dao.CmbMarketQuoteDao;
import com.yc.data.entity.PartitionPair;
import com.yc.data.util.BeanConvert;
import com.yc.data.util.CsvBigTask;
import com.yc.data.util.DataUtil;
import com.yc.data.util.SftpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.yc.data.service.CmbMarketQuoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.yc.data.entity.CmbMarketQuote;
import com.yc.data.mapper.CmbMarketQuoteMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CyclicBarrier;

/**
 * 服务接口实现
 *
 * @author cy
 * @since 2023-02-10 21:22:05
 * @description 由 Mybatisplus Code Generator 创建
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CmbMarketQuoteServiceImpl    extends ServiceImpl<CmbMarketQuoteMapper, CmbMarketQuote> implements CmbMarketQuoteService  {
    private final CmbMarketQuoteDao cmbMarketQuoteDao;
    private static final Logger logger = LoggerFactory.getLogger(CmbMarketQuoteService.class);
    @Autowired
    private SftpUtil sftpUtil;

    @Value("${ftpConfig.marketQuoteDst}")
    public  String  dst;
    @Override
    public Boolean downloadExcel(String date) {
        try {
            sftpUtil.downloadFile(date);
            return true;
        } catch (Exception e) {
            logger.error("stp下载失败,原因:{}",e.getMessage());
            e.getStackTrace();
            return false;
        }
    }

    @Override
    public void insertByFile(String date) throws Exception {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        dst = MessageFormat.format(dst, date);
        File file = new File(dst);
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");

        long length = file.length();
        //获取处理器核心数
        int availProcessors = 50;
        logger.info("可使用线程=======" + availProcessors);
        Long blockLength = length / availProcessors;
        byte b = '0';
        int index = 0;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while(b != '\r' && b != '\n') {
            b = randomAccessFile.readByte();
            byteArrayOutputStream.write(b);
            ++index;
        }
        //获取首行
        String[] firstLine = byteArrayOutputStream.toString().split(",");
        Map<String,Integer> map = new HashMap();
        if(firstLine != null ){
            for(int i = 0;i < firstLine.length ; i++){
                map.put(firstLine[i],i);
            }
        }
        Map<Integer,String> mapping = new HashMap();
        for(Map.Entry<String,String> e :  BeanConvert.excelMapping.entrySet()){ // 遍历
            for(Map.Entry<String,Integer> e1 : map.entrySet()){
                if(e.getKey().equals(e1.getKey())){
                    mapping.put(e1.getValue(),e.getValue());
                }
            }

        }

        //文件分片
        Set partition = DataUtil.partition(index, blockLength, length, randomAccessFile);

        CyclicBarrier cyclicBarrier = new CyclicBarrier(partition.size(), new Runnable() {
            @Override
            public void run() {
                //最后的数据提交上去
                stopWatch.stop();
                logger.info("===========数据入库结束=======" + stopWatch.getTotalTimeMillis());
            }
        });

        for(Object pair : partition) {

            PartitionPair partitionPair = (PartitionPair) pair;
            long start = partitionPair.getStart();
            long end = partitionPair.getEnd();
            RandomAccessFile r = new RandomAccessFile(file, "r");
            CsvBigTask csvBigTask = new CsvBigTask(cyclicBarrier,mapping, start, end, date,r, this);
            Thread thread = new Thread(csvBigTask);
            thread.start();

        }
    }
}