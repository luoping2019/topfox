package com.topfox.util;

import com.topfox.common.CommonException;
import com.topfox.common.ResponseCode;
import com.topfox.data.DataHelper;
import com.topfox.data.SqlUnderscore;
import com.topfox.misc.Misc;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class SysConfigDefault implements SysConfig {
    @Autowired
    protected Environment environment;

    @Override
    public String getAppName(){
        return environment.getProperty("spring.application.name");
    }

    /**
     * 默认100
     * 分页时,默认的每页条数
     */
    private Integer pageSize =null;
    @Override
    public int getPageSize(){
        if (pageSize == null) {
            String strTemp = environment.getProperty("top.page-size");
            pageSize = Misc.isNull(strTemp)?100:DataHelper.parseInt(strTemp);
        }
        return pageSize;
    }

    /**
     * 默认20000
     * 不分页时(pageSize<=0),查询时最多返回的条数
     */
    private Integer maxPageSize;
    @Override
    public int getMaxPageSize() {
        if (maxPageSize == null) {
            String strTemp = environment.getProperty("top.max-page-size");
            maxPageSize = Misc.isNull(strTemp)?20000:DataHelper.parseInt(strTemp);
        }
        return maxPageSize;
    }

    /**
     # 默认3
     # 重要参数:更新时DTO序列化策略 和 更新SQL生成策略
     # 1 时, service的DTO=提交的数据.               更新SQL 提交数据不等null 的字段 生成 set field=value
     # 2 时, service的DTO=修改前的原始数据+提交的数据. 更新SQL (当前值 != 原始数据) 的字段 生成 set field=value
     # 3 时, service的DTO=修改前的原始数据+提交的数据. 更新SQL (当前值 != 原始数据 + 提交数据的所有字段)生成 set field=value
     #   值为3, 则始终保证了前台(调用方)提交的字段, 不管有没有修改, 都能生成更新SQL, 这是与2最本质的区别
     */
    private Integer updateMode;
    @Override
    public int getUpdateMode() {
        if (updateMode == null) {
            String strTemp = environment.getProperty("top.service.update-mode");
            updateMode = Misc.isNull(strTemp)?3:DataHelper.parseInt(strTemp);
        }
        if (updateMode <1 || updateMode>3) {
            throw CommonException.newInstance(ResponseCode.PARAM_IS_INEXISTENCE).text("top.service.update-mode值设置错误");
        }
        return updateMode;
    }

    private Boolean selectByBeforeUpdate;
    @Override
    public boolean isSelectByBeforeUpdate() {
        if (selectByBeforeUpdate == null) {
            String strTemp = environment.getProperty("top.service.select-by-before-update");
            selectByBeforeUpdate = Misc.isNull(strTemp)?false:DataHelper.parseBoolean(strTemp);
        }

        return selectByBeforeUpdate;
    }


    /**
     * 默认 true
     * # redis序列化支持两种, true:jackson2JsonRedisSerializer false:JdkSerializationRedisSerializer
     * # 注意, 推荐生产环境下更改为 false, 类库将采用JdkSerializationRedisSerializer 序列化对象,
     * # 这时必须禁用devtools(pom.xml 注释掉devtools), 否则报错.
     */
    private Boolean redisSerializerJson;
    @Override
    public boolean isRedisSerializerJson() {
        if (redisSerializerJson == null) {
            String strTemp = environment.getProperty("top.redis.serializer-json");
            redisSerializerJson = Misc.isNull(strTemp)?true:DataHelper.parseBoolean(strTemp);
        }
        return redisSerializerJson;
    }

    /**
     * 默认 true
     * 更新记录返回0时 是否抛出异常
     * true  抛出异常
     * false 不抛异常
     */
    private Boolean updateNotResultError;
    @Override
    public boolean isUpdateNotResultError() {
        if (updateNotResultError == null) {
            String strTemp = environment.getProperty("top.service.update-not-result-error");
            updateNotResultError = Misc.isNull(strTemp)?true:DataHelper.parseBoolean(strTemp);
        }
        return updateNotResultError;
    }

//    /**
//     * 二级缓存开关
//     * 默认 false
//     * service层是否开启redis缓存,  false, 增删改查 将不会启用redis
//     */
//    @Deprecated
//    private Boolean openRedis;
//    @Override
//    @Deprecated
//    public boolean isOpenRedis() {
//        if (openRedis == null) {
//            String strTemp = environment.getProperty("top.service.open-redis");
//            openRedis = Misc.isNull(strTemp)?false:DataHelper.parseBoolean(strTemp);
//        }
//        return openRedis;
//    }

    /**
     * 一级缓存开关 当前线程的缓存
     * 默认 false
     * service层是否开启redis缓存, 增删改查 将不会启用redis
     */
    private Boolean threadCache;
    @Override
    public boolean isThreadCache() {
        if (threadCache == null) {
            String strTemp = environment.getProperty("top.service.thread-cache");
            threadCache = Misc.isNull(strTemp)?false:DataHelper.parseBoolean(strTemp);
        }
        return threadCache;
    }

    /**
     * 二级缓存开关 redis缓存
     * 默认 false
     * service层是否开启redis缓存, 增删改查 将不会启用redis
     */
    private Boolean redisCache;
    @Override
    public boolean isRedisCache() {
        if (redisCache == null) {
            String strTemp = environment.getProperty("top.service.redis-cache");
            strTemp = Misc.isNull(strTemp)?environment.getProperty("top.service.open-redis"):strTemp;

            redisCache = Misc.isNull(strTemp)?false:DataHelper.parseBoolean(strTemp);
        }
        return redisCache;
    }


    /**
     * 默认 false 不打印操作redis的日志
     */
    private Boolean redisLog;
    @Override
    public boolean isRedisLog() {
        if (redisLog == null) {
            String strTemp = environment.getProperty("top.service.redis-log");
            redisLog = Misc.isNull(strTemp)?false:DataHelper.parseBoolean(strTemp);
        }
        return redisLog;
    }

    /**
     * DTO自动生成SQL 是否启用 驼峰命名转下划线,且大写
     * 例子: 参数设置为true时则将 DTO中的 orderCustomerName 转为 ORDER_CUSTOMER_NAME
     * 默认 OFF, 值为以下3种
     *  1. OFF 关闭
     *  2. ON-UPPER 打开并大写
     *  3. ON-LOWER 打开并小写
     */
    private SqlUnderscore sqlCamelToUnderscore;
    @Override
    public SqlUnderscore getSqlCamelToUnderscore() {
        if (sqlCamelToUnderscore == null) {
            String temp=environment.getProperty("top.service.sql-camel-to-underscore");
            temp = Misc.isNull(temp)?"OFF":temp.toUpperCase();
            sqlCamelToUnderscore = SqlUnderscore.getByValue(temp);//新的配置
        }
        if (sqlCamelToUnderscore!=SqlUnderscore.OFF && sqlCamelToUnderscore!=SqlUnderscore.ON_LOWER && sqlCamelToUnderscore!=SqlUnderscore.ON_UPPER){
            //sqlCamelToUnderscore = null;
            throw CommonException.newInstance(
                    ResponseCode.PARAM_IS_INVALID)
                    .text("top.service.sql-camel-to-underscore 参数的值配置错误, 只能是以下3种:",
                            "1.OFF 关闭  2.ON-UPPER 打开,转大写  3.ON-LOWER 打开,转小写");
        }
        return sqlCamelToUnderscore;
    }

    /**
     * 默认 false 表示是驼峰命名方式, 与QTO DTO 的field对应的
     * 设置为 true时, 提交的数据后台自动转换, 保证序列化Wie DTO  List<DTO>不出错
     * 提交的数据的字段名是否是带下划线的小写
     */
    private Boolean commitDataKeysIsUnderscore;
    @Override
    public boolean isCommitDataKeysIsUnderscore(){
        if (commitDataKeysIsUnderscore == null) {
            String strTemp = environment.getProperty("spring.jackson.property-naming-strategy");
            commitDataKeysIsUnderscore = Misc.isNull(strTemp)?false
                    : strTemp.equals("CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES");
        }
        return commitDataKeysIsUnderscore;
    }

    //控制台 打印 符号控制
    private String logStart;
    @Override
    public String getLogStart(){
        if (logStart == null) {
            String strTemp = environment.getProperty("top.log.start");
            logStart = Misc.isNotNull(strTemp)?strTemp:"▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼";
        }
        return logStart;
    }

    private String logEnd;
    @Override
    public String getLogEnd(){
        if (logEnd == null) {
            String strTemp = environment.getProperty("top.log.end");
            logEnd = Misc.isNotNull(strTemp)?strTemp:"▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲";
        }
        return logEnd;
    }

    private String logPrefix;
    @Override
    public String getLogPrefix() {
        if (logPrefix == null) {
            String strTemp = environment.getProperty("top.log.prefix");
            logPrefix = Misc.isNotNull(strTemp)?strTemp:"#";
        }
        return logPrefix;
    }

    /**
     * 获取配置文件key的值
     * @param key
     * @return
     */
    @Override
    public String getString(String key){
        String value = environment.getProperty(key);
        return Misc.isNotNull(value)?value:"";
    }
}
