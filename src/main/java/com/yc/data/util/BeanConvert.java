package com.yc.data.util;

import com.yc.data.entity.CmbMarketQuote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BeanConvert {
    private static final Logger logger = LoggerFactory.getLogger(BeanConvert.class);

    public static Map<String, String> excelMapping = new HashMap<String, String>()
    {{
        put("CURVENAME", "curveName");
        put("INSTRUMENTTYPE", "instrumentType");
        put("INSTRUMENTNAME", "instrumentName");
        put("TENOR", "tenor");
        put("QUOTE", "quote");
        put("MATURITYDATE", "maturityDate");
        put("M_H_REP_DATE", "repDate");
    }};

    public static CmbMarketQuote lineToBean(String[] line,Map<Integer,String> mapping,String date){
        CmbMarketQuote cmbMarketQuote = new CmbMarketQuote();
        Class<? extends CmbMarketQuote> cmbMarketQuoteClass = cmbMarketQuote.getClass();
        mapping.forEach((k, v) -> {
            try {
                Field field = cmbMarketQuoteClass.getDeclaredField(v);
                field.setAccessible(true);
                field.set(cmbMarketQuote, line[Integer.valueOf(k)]);
                cmbMarketQuote.setCheckDate(date);
                Date now = new Date();
                cmbMarketQuote.setGmtCreate(now);
                cmbMarketQuote.setGmtModified(now);
            } catch (Exception e) {
                logger .error("转换bean错误,原数据为:{},error:{}", line ,e.getMessage());
            }
        });
        return cmbMarketQuote;
    }
}
