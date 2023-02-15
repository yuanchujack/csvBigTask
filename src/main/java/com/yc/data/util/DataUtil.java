package com.yc.data.util;

import com.yc.data.entity.PartitionPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import java.util.HashSet;
import java.util.Set;
@Component
public class DataUtil {
    private static final Logger logger = LoggerFactory.getLogger(DataUtil.class);


    public static void main(String[] arg) throws Exception {
      /*  File file = new File("e://market_quote.csv");
        FileInputStream fi = new FileInputStream(file);
        bigExcelGenerate(fi);*/

       /* File file = new File("E://market_quote/market_quote.csv");
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
        long length = file.length();
        //获取处理器核心数
        int availProcessors = Runtime.getRuntime().availableProcessors() * 2 + 1;
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
        //文件分片
        Set partition = DataUtil.partition(index, blockLength, length, randomAccessFile);

        for(Object pair : partition) {

            //todo Runable
            PartitionPair partitionPair = (PartitionPair) pair;
            long start = partitionPair.getStart();
            long end = partitionPair.getEnd();
            randomAccessFile.seek(start);

            while(start <= end) {
                byte b1 = '0';
                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                List<CmbMarketQuote> list = new ArrayList<>(1000);
                while (b1 != '\r' && b1 != '\n') {
                    bs.write(b1);
                    b1 = randomAccessFile.readByte();
                    ++start;
                }
                if(b1 != '\n') {
                    String[] line = bs.toString().split(",");
                    if(line != null && line.length == 7){
                        CmbMarketQuote cmbMarketQuote = new CmbMarketQuote();
                        cmbMarketQuote.setCurveName(line[0]);
                        cmbMarketQuote.setInstrumentType(line[1]);
                        cmbMarketQuote.setInstrumentName(line[2]);
                        cmbMarketQuote.setTenor(line[3]);
                        cmbMarketQuote.setQuote(line[4]);
                        cmbMarketQuote.setMaturityDate(line[5]);
                        cmbMarketQuote.setRepDate(line[6]);
                        list.add(cmbMarketQuote);
                    }
                }
                if(list.size() > 1000){
                    //insert
                }
            }
            //insert
        }*/

    }
    /**
     * 根据当前文件 创建生产2G文件
     *
     * @param inputStream
     * @return
     */
    public static void bigExcelGenerate(InputStream inputStream) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            Stream<String> lines = bufferedReader.lines();
            List<String> contents = lines.collect(Collectors.toList());

            //根据当前文件 创建生产2G文件
            File dir = new File("E://market_quote.csv");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String splitFilePath = "E://market_quote/market_quote.csv";
            File splitFileName = new File(splitFilePath);
            splitFileName.createNewFile();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(splitFileName)));
            for (int i = 0; i < 28000000; i++) {
                if (i == 0) {
                        bufferedWriter.write(contents.get(i));
                        bufferedWriter.newLine();
                } else {
                    bufferedWriter.write(contents.get((int)(Math.random()*100+1)));
                    bufferedWriter.newLine();
                }
            }

            //关流

            try {
                bufferedWriter.flush();
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            logger.info("csv文件生成失败  ：" + e);
            e.printStackTrace();
        }
        stopWatch.stop();
        logger.info("csv文件生成共花费：  " + stopWatch.getTotalTimeMillis() + " ms");
    }



    /**
     *
     * @program: io.util.DataUtil
     * @description: 分片数据
     */
    public static Set partition(long start, long length, long totalSize, RandomAccessFile randomAccessFile) throws IOException {
        if(start > totalSize - 1) {
            return null;
        }

        //每次获取length长度，并判断这个位置是否是换行符
        Set partitionPairs = new HashSet();
        PartitionPair partitionPair = new PartitionPair();
        partitionPair.setStart(start);
        //判断这个length是否是换行符
        long index = start + length;

        //递归终止条件
        if(index > totalSize - 1) {
            //最后一个递归终止
            partitionPair.setEnd(totalSize - 1);
            partitionPairs.add(partitionPair);
            return partitionPairs;

        } else {
            //设置位置并读取一个字节
            randomAccessFile.seek(index);
            byte oneByte = randomAccessFile.readByte();
            //判断是否是换行符号,如果不是换行符，那么读取到换行符为止
            while(oneByte != '\n' && oneByte != '\r') {
                //不能越界
                if(++index > totalSize - 1) {
                    index = totalSize-1;
                    break;
                }

                randomAccessFile.seek(index);
                oneByte = randomAccessFile.readByte();
            }

            partitionPair.setEnd(index);
            //递归下一个位置
            partitionPairs.add(partitionPair);

            partitionPairs.addAll(partition(index + 1, length, totalSize, randomAccessFile));
        }

        return partitionPairs;
    }



    /**
     *  复制单个文件,这里考虑使用文件锁，保证线程安全
     *  @param  oldPath  String  原文件路径  如：c:/fqf.txt
     *  @param  newPath  String  复制后路径  如：f:/fqf.txt
     *  @return  boolean
     */
    public static void copyFile(String  oldPath,  String  newPath) throws Exception {
//        int  byteread  =  0;
        File oldfile  =  new  File(oldPath);
        if  (oldfile.exists())  {  //文件存在时
            //对文件枷锁，然后进行复制操作，
            InputStream inStream  =  new  FileInputStream(oldPath);  //读入原文件
            FileOutputStream fs  =  new  FileOutputStream(newPath);
            FileChannel fileChannel = fs.getChannel();
            //开始加锁
            FileLock fileLock = null;
            try {
                while (true) {
                    fileLock = fileChannel.lock(); //直接上锁
                    if(fileLock != null) {
                        break;
                    } else {
                        //文件无法被锁定，1s后尝试
                        logger.warn(oldPath + " 文件无法被锁定，1s后尝试");
                        Thread.sleep(1000);
                    }
                }

                //开始拷贝数据
                byte[] buf = new byte[2048];
                int len = 0;
                while((len = inStream.read(buf)) != -1) {
                    fs.write(buf, 0, len);
                }

                //刷新
                fileLock.release();
                fs.flush();
                fs.close();
                inStream.close();

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                if(fileLock.isValid()) {

                    fileLock.release();
                }
            }
        }

    }

    /**
     *
     * @program: com.ztesoft.interfaces.predeal.util.DataUtil
     * @description: 删除文件
     * @auther: xiaof
     * @date: 2019/3/5 18:08
     */
    public static void deletFile(String filePath) {
        File file = new File(filePath);
        file.delete();
    }

}