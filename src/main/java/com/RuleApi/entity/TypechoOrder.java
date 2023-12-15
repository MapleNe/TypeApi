package com.RuleApi.entity;
import java.io.Serializable;
import java.math.BigInteger;

import lombok.Data;
public class TypechoOrder implements Serializable {
    private static final long serialVersionUID = 1L;

    /***
     * 订单ID
     */
    private Integer id;

    /***
     * 订单号
     */
    private BigInteger orders;

    /***
     * 价格
     */
    private Integer price;

    /***
     * 是否支付
     */
    private Integer paid;

    /***
     * 用户id
     */
    private Integer user_id;

    /***
     * 商品id
     */
    private Integer product;

    /***
     * 商品名称
     */
    private String product_name;

    /***
     * 规格
     */
    private String specs;

    /***
     * 运单号
     */
    private Long tracking_number;

    /***
     * 创建时间
     */
    private Integer created;
}
