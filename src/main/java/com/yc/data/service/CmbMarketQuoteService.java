package com.yc.data.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yc.data.entity.CmbMarketQuote;

import java.io.IOException;

/**
 * 服务接口
 *
 * @author cy
 * @since 2023-02-10 21:22:05
 * @description 由 Mybatisplus Code Generator 创建
 */
public interface CmbMarketQuoteService   extends IService<CmbMarketQuote>{


    /**
     *  sftp下载文件
     */
    Boolean downloadExcel(String date);


    /**
     *  读取文件导入
     */
    void insertByFile(String date) throws Exception;
}
