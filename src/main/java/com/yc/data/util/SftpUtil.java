package com.yc.data.util;

import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

@Component
public class SftpUtil {
    private static final Logger logger = LoggerFactory.getLogger(SftpUtil.class);



    // 服务器连接ip
    @Value("${ftpConfig.host}")
    private String host;
    // 服务器连接端口
    @Value("${ftpConfig.port}")
    public  String  port;
    @Value("${ftpConfig.username}")
    public  String  username;
    @Value("${ftpConfig.password}")
    public  String  password ;
    @Value("${ftpConfig.marketQuoteSrc}")
    public  String  src;
    @Value("${ftpConfig.marketQuoteDst}")
    public  String  dst;


    Session session = null;
    Channel channel = null;




    public  void downloadFile(String date) throws Exception {
        //src linux服务器文件地址，dst 本地存放地址
        ChannelSftp chSftp = getChannel( 60000);
        dst = MessageFormat.format(dst, date);
        chSftp.get(src, dst,new MyProgressMonitor(), ChannelSftp.RESUME);
        chSftp.quit();
        closeChannel();

    }


    public  void uploadFile() throws Exception {
        ChannelSftp chSftp = getChannel( 60000);

        /**
         * 代码段1/代码段2/代码段3分别演示了如何使用JSch的不同的put方法来进行文件上传。这三段代码实现的功能是一样的，
         * 都是将本地的文件src上传到了服务器的dst文件
         */

        /**代码段1
         OutputStream out = chSftp.put(dst,new MyProgressMonitor2(), ChannelSftp.OVERWRITE); // 使用OVERWRITE模式
         byte[] buff = new byte[1024 * 256]; // 设定每次传输的数据块大小为256KB
         int read;
         if (out != null) {
         InputStream is = new FileInputStream(src);
         do {
         read = is.read(buff, 0, buff.length);
         if (read > 0) {
         out.write(buff, 0, read);
         }
         out.flush();
         } while (read >= 0);
         }
         **/

        // 使用这个方法时，dst可以是目录，当dst是目录时，上传后的目标文件名将与src文件名相同
        // ChannelSftp.RESUME：断点续传
        chSftp.put(src, dst, new MyProgressMonitor(), ChannelSftp.RESUME); // 代码段2

        // 将本地文件名为src的文件输入流上传到目标服务器，目标文件名为dst。
        // chSftp.put(new FileInputStream(src), dst,new MyProgressMonitor2(), ChannelSftp.OVERWRITE); // 代码段3

        chSftp.quit();
        closeChannel();
    }

    /**
     * 根据ip，用户名及密码得到一个SFTP
     * channel对象，即ChannelSftp的实例对象，在应用程序中就可以使用该对象来调用SFTP的各种操作方法
     *
     * @param timeout
     * @return
     * @throws JSchException
     */
    public ChannelSftp getChannel( int timeout)
            throws JSchException {
        String ftpHost = host;

        String ftpUserName =  username;
        String ftpPassword = password;

        int ftpPort = 22;
        if (port != null && !port.equals("")) {
            ftpPort = Integer.valueOf(port);
        }

        JSch jsch = new JSch(); // 创建JSch对象
        session = jsch.getSession(ftpUserName, ftpHost, ftpPort); // 根据用户名，主机ip，端口获取一个Session对象
        if (ftpPassword != null) {
            session.setPassword(ftpPassword); // 设置密码
        }
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config); // 为Session对象设置properties
        session.setTimeout(timeout); // 设置timeout时间
        session.connect(5000); // 通过Session建立链接
        channel = session.openChannel("sftp"); // 打开SFTP通道
        channel.connect(); // 建立SFTP通道的连接
        return (ChannelSftp) channel;
    }

    public void closeChannel() throws Exception {
        if (channel != null) {
            channel.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
    }


    /**
     * 进度监控器-JSch每次传输一个数据块，就会调用count方法来实现主动进度通知
     *
     */
    public static class MyProgressMonitor implements SftpProgressMonitor {
        private long count = 0;     //当前接收的总字节数
        private long max = 0;       //最终文件大小
        private long percent = -1;  //进度

        /**
         * 当每次传输了一个数据块后，调用count方法，count方法的参数为这一次传输的数据块大小
         */
        @Override
        public boolean count(long count) {
            this.count += count;
            if (percent >= this.count * 100 / max) {
                return true;
            }
            percent = this.count * 100 / max;

            logger.info("Completed " + this.count + "(" + percent
                    + "%) out of " + max + ".");
            return true;
        }

        /**
         * 当传输结束时，调用end方法
         */
        @Override
        public void end() {
            System.out.println("Transferring done.");
        }

        /**
         * 当文件开始传输时，调用init方法
         */
        @Override
        public void init(int op, String src, String dest, long max) {
            System.out.println("Transferring begin.");
            this.max = max;
            this.count = 0;
            this.percent = -1;
        }
    }

    /**
     * 官方提供的进度监控器
     *
     */
    public static class DemoProgressMonitor implements SftpProgressMonitor {
        ProgressMonitor monitor;
        long count = 0;
        long max = 0;

        /**
         * 当文件开始传输时，调用init方法。
         */
        public void init(int op, String src, String dest, long max) {
            this.max = max;
            monitor = new ProgressMonitor(null,
                    ((op == SftpProgressMonitor.PUT) ? "put" : "get") + ": "
                            + src, "", 0, (int) max);
            count = 0;
            percent = -1;
            monitor.setProgress((int) this.count);
            monitor.setMillisToDecideToPopup(1000);
        }

        private long percent = -1;

        /**
         * 当每次传输了一个数据块后，调用count方法，count方法的参数为这一次传输的数据块大小。
         */
        public boolean count(long count) {
            this.count += count;

            if (percent >= this.count * 100 / max) {
                return true;
            }
            percent = this.count * 100 / max;

            monitor.setNote("Completed " + this.count + "(" + percent
                    + "%) out of " + max + ".");
            monitor.setProgress((int) this.count);

            return !(monitor.isCanceled());
        }

        /**
         * 当传输结束时，调用end方法。
         */
        public void end() {
            monitor.close();
        }
    }
}