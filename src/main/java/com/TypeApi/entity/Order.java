package com.TypeApi.entity;
import java.io.Serializable;
import java.math.BigInteger;
import lombok.Data;
@Data
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    /***
     * 订单ID
     */
    private Integer id;

    /***
     * 订单号
     */
    private String orders;

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
     * 老板id
     */
    private Integer boss_id;

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
    private String tracking_number;

    /***
     * 是否发货
     */
    private Integer isTracking;

    /***
     * 地址
     */
    private String address;

    /***
     * freight
     */
    private  Integer freight;

    /***
     * 创建时间
     */
    private Integer created;
}
