package com.yc.data.entity;

import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * (cmb_market_quote)实体类
 *
 * @author cy
 * @since 2023-02-10 21:22:05
 * @description 由 Mybatisplus Code Generator 创建
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("cmb_market_quote")
public class CmbMarketQuote extends Model<CmbMarketQuote> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId
	private Long id;
    /**
     * curveName
     */
    private String curveName;
    /**
     * instrumentType
     */
    private String instrumentType;
    /**
     * instrumentName
     */
    private String instrumentName;
    /**
     * tenor
     */
    private String tenor;
    /**
     * quote
     */
    private String quote;
    /**
     * maturityDate
     */
    private String maturityDate;
    /**
     * repDate
     */
    private String repDate;
    /**
     * checkDate
     */
    private String checkDate;
    /**
     * gmtCreate
     */
    private Date gmtCreate;
    /**
     * gmtModified
     */
    private Date gmtModified;

}