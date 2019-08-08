package com.topfox.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.topfox.annotation.Ignore;
//import com.topfox.misc.JsonUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.Collection;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Setter
@Getter
@Accessors(chain = true)
public class Response<T> {
    @JsonIgnore @Ignore transient Logger logger= LoggerFactory.getLogger(getClass());

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 异常编码
     */
    private String code;

    /**
     * 详细信息
     */
    private String msg;

    /**
     * 前端(请求方)每次请求传入的 唯一 执行Id, 如果没有, 后端会自动生成一个唯一的
     */
    private String executeId;

    /**
     * 1.查询结果的所有页的行数合计,
     * 2.update insert delete SQL执行结果影响的记录数
     */
    private Integer totalCount;


    /**
     * 查询结果(当前页)的行数合计
     */
    private Integer pageCount;

    /**
     * 数据, List<UserDTO> 或者是一个DTO
     */
    @JsonSerialize(using = ListJsonSerialize.class)
    private T data;


    /**
     * data 泛型 T 的实际类型
     */
    @JsonIgnore
    Type dataType;
    public Type getDataType() {
        if (dataType == null && data != null) {
            dataType = data.getClass();
        }
        return dataType;
    }
    public void setDataType(Type dataType) {
        if (this.dataType == null) {
            this.dataType = dataType;
        }
    }
    /**
     * 异常类名
     */
    private String exception;

    /**
     * 方法
     */
    private String method;

    private JSONObject attributes = new JSONObject();

    public Response() {
        super();//spring cloud 序列化必须


    }

    public Response(String code, String msg) {
        this.code = code;
        this.msg = msg;
        this.success = ResponseCode.SUCCESS.getCode().equals(code);
    }

    public Response(ResponseCode responseCode) {
        this(responseCode.getCode(), responseCode.getMsg());
    }

    public Response(ResponseCode responseCode, String msg) {
        this(responseCode.getCode(), msg);
    }

    public Response(Throwable e, ResponseCode responseCode) {
        this.code = responseCode.getCode();
        this.msg = responseCode.getMsg();
        setError(e);
    }
    public Response(Throwable e, ResponseCode responseCode, String msg) {
        this.code = responseCode.getCode();
        this.msg = msg;
        setError(e);
    }
    public Response(Throwable e) {
        this.code = ResponseCode.SYSTEM_ERROR.getCode();
        this.msg = e.getMessage();
        setError(e);
    }

    public Response(T data) {
        this(ResponseCode.SUCCESS);
        this.data = data;
        if (data != null) {
            dataType = data.getClass();
            if (data instanceof Collection) {
                this.totalCount = ((Collection) data).size();
            }else {
                this.totalCount = 1;
            }
        } else {
            this.totalCount = 0;
        }
    }

    /**
     *
     * @param data
     * @param totalCount 查询时 为 所有页的行数,  插入 更新 删除时, 为SQL执行成功所影响记录的行数
     */
    public Response(T data, Integer totalCount) {
        this(ResponseCode.SUCCESS);
        this.data = data;
        this.totalCount = totalCount;

        if (data != null) {
            dataType = data.getClass();
            if (data instanceof Collection) {
                this.pageCount = ((Collection) data).size();//当前页的行数
            } else {
                this.totalCount = 1;
            }
        } else {
            this.totalCount = 0;
        }
    }

    protected void setError(Throwable e){
        success = false;
        msg = msg==null?"":msg;

        if (e instanceof CommonException){
            CommonException commonException = (CommonException)e;
            this.code=commonException.getCode();
            int pos =msg.lastIndexOf("]\n\r");
            if (pos > 0 ) {
                msg = msg.substring(pos + 3);
            }
        }
        this.exception=e.getCause()!=null?e.getCause().getClass().getName():e.getClass().getName();

        method = getMethodString(e);

//        if (false && logger.isDebugEnabled()){
//            //开发环境下(debug),打印失败的信息
//            e.printStackTrace();
//        }else{
            //生产环境下,  将日志用logback输出,  便于结合 ELK分布式日志系统 接收日志处理
            StringWriter sw = new StringWriter();
            if (msg.equals(e.getMessage()) == false) {
                //解决 异常全局拦击器 自定msg时, 不能输出msg的问题
                // 如 new Response<>(ex,ResponseCode.DB_SQL_ERROR, "自定文字")
                sw.write(msg);sw.write("\r");
            }
            e.printStackTrace(new PrintWriter(sw, false));
            //logger.error(sw.toString());
//        }
        e.printStackTrace();
    }

    public static String getMethodString(Throwable e){
        StackTraceElement[] stackTraceElement=e.getStackTrace();
        StringBuilder sb=new StringBuilder();

        //异常抛出层次优化
        StackTraceElement findStackTraceElement=null;
        for (StackTraceElement current:stackTraceElement){
            if (current.getClassName().startsWith("org.")
                    || current.getClassName().startsWith("java")
                    || current.getClassName().startsWith("com.topfox.common.CommonException")
                    || current.getClassName().startsWith("com.topfox.sql.Condition") && "endWhere".equals(current.getMethodName())
                    || current.getClassName().startsWith("com.sun")
                    || current.getClassName().startsWith("feign.")
            ) {
                continue;
            }
            findStackTraceElement=current;
            break;
        }
        if (findStackTraceElement != null) {
            sb.append(findStackTraceElement.getClassName())
                    .append(".")
                    .append(findStackTraceElement.getMethodName())
                    .append(":")
                    .append(findStackTraceElement.getLineNumber());
            return sb.toString();
        }
        return "";
    }

//    public String toString(){
//        String temp= JsonUtil.toString(this);
//        return "执行结果 "+ (temp.length()>500?temp.substring(0,500):temp);
//    }

    public String toJSONString(){
        return JSON.toJSONString(this);
    }
}
