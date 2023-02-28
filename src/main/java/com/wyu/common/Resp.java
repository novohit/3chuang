package com.wyu.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wyu
 * @date 2023-02-20 23:30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class Resp {

    /**
     * 状态码 0 表示成功
     */

    private Integer code;
    /**
     * 数据
     */
    private Object data;
    /**
     * 描述
     */
    private String msg;


    /**
     * 成功，不传入数据
     * @return
     */
    public static Resp success() {
        return new Resp(0, null, null);
    }

    /**
     *  成功，传入数据
     * @param data
     * @return
     */
    public static Resp success(Object data) {
        return new Resp(0, data, null);
    }

    /**
     * 失败，传入描述信息
     * @param msg
     * @return
     */
    public static Resp error(String msg) {
        return new Resp(1, null, msg);
    }



    /**
     * 自定义状态码和错误信息
     * @param code
     * @param msg
     * @return
     */
    public static Resp buildCodeAndMsg(int code, String msg) {
        return new Resp(code, null, msg);
    }

}
