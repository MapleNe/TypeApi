package com.TypeApi.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.TypeApi.entity.*;
import com.TypeApi.common.*;
import com.TypeApi.service.*;
import net.dreamlu.mica.xss.core.XssCleanIgnore;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 控制层
 * TypechoShopController
 *
 * @author buxia97
 * @date 2022/01/27
 */
@Controller
@RequestMapping(value = "/shop")
public class ShopController {

    @Autowired
    ShopService service;

    @Autowired
    private ShoptypeService shoptypeService;

    @Autowired
    private UsersService usersService;

    @Autowired
    private UserlogService userlogService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MailService MailService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private PaylogService paylogService;

    @Autowired
    private ApiconfigService apiconfigService;

    @Autowired
    private InboxService inboxService;

    @Autowired
    private SpaceService spaceService;

    @Autowired
    private OrderService orderService;


    @Autowired
    private PushService pushService;

    @Value("${web.prefix}")
    private String dataprefix;

    RedisHelp redisHelp = new RedisHelp();
    ResultAll Result = new ResultAll();
    UserStatus UStatus = new UserStatus();
    EditFile editFile = new EditFile();
    baseFull baseFull = new baseFull();

    /***
     * 商品列表
     */
    @RequestMapping(value = "/shopList")
    @ResponseBody
    public String shopList(@RequestParam(value = "searchParams", required = false) String searchParams,
                           @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                           @RequestParam(value = "searchKey", required = false, defaultValue = "") String searchKey,
                           @RequestParam(value = "order", required = false, defaultValue = "created") String order,
                           @RequestParam(value = "limit", required = false, defaultValue = "15") Integer limit,
                           @RequestParam(value = "token", required = false) String token) {
        Shop query = new Shop();
        Integer uStatus = UStatus.getStatus(token, this.dataprefix, redisTemplate);
        Map map = new HashMap();
        Integer uid = 0;
        String group = "";
        if (uStatus != 0) {
            map = redisHelp.getMapValue(this.dataprefix + "_" + "userInfo" + token, redisTemplate);
            uid = Integer.parseInt(map.get("uid").toString());
            group = map.get("group").toString();
        }
        String sqlParams = "null";
        if (limit > 50) {
            limit = 50;
        }
        Integer total = 0;
        List jsonList = new ArrayList();
        if (StringUtils.isNotBlank(searchParams)) {
            JSONObject object = JSON.parseObject(searchParams);
            query = object.toJavaObject(Shop.class);
            Map paramsJson = JSONObject.parseObject(JSONObject.toJSONString(query), Map.class);
            sqlParams = paramsJson.toString();

        }
        total = service.total(query, searchKey);
        List cacheList = redisHelp.getList(this.dataprefix + "_" + "shopList_" + page + "_" + limit + "_" + searchKey + "_" + sqlParams + "_" + order + "_" + uid, redisTemplate);

        try {
            if (cacheList.size() > 0) {
                jsonList = cacheList;
            } else {
                PageList<Shop> pageList = service.selectPage(query, page, limit, searchKey, order);
                List<Shop> list = pageList.getList();
                if (list.size() < 1) {
                    JSONObject noData = new JSONObject();
                    noData.put("code", 1);
                    noData.put("msg", "");
                    noData.put("data", new ArrayList());
                    noData.put("count", 0);
                    noData.put("total", total);
                    return noData.toString();
                }
                for (int i = 0; i < list.size(); i++) {
                    Map json = JSONObject.parseObject(JSONObject.toJSONString(list.get(i)), Map.class);
                    Shop shop = list.get(i);
                    Integer userid = shop.getUid();
                    if (json.get("imgurl") != null && !json.get("imgurl").toString().isEmpty() && !json.get("imgurl").toString().equals("")) {
                        json.put("imgurl", JSONArray.parse(json.get("imgurl").toString()));
                    }
                    //获取用户信息
                    Map userJson = UserStatus.getUserInfo(userid, apiconfigService, usersService);
                    json.put("userJson", userJson);
                    if (!group.equals("administrator") && !group.equals("editor")) {
                        if (!shop.getUid().equals(uid)) {
                            json.remove("value");
                        }
                    }

                    jsonList.add(json);
                }
                redisHelp.delete(this.dataprefix + "_" + "shopList_" + page + "_" + limit + "_" + searchKey + "_" + sqlParams + "_" + order + "_" + uid, redisTemplate);
                redisHelp.setList(this.dataprefix + "_" + "shopList_" + page + "_" + limit + "_" + searchKey + "_" + sqlParams + "_" + order + "_" + uid, jsonList, 10, redisTemplate);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (cacheList.size() > 0) {
                jsonList = cacheList;
            }
        }


        JSONObject response = new JSONObject();
        response.put("code", 1);
        response.put("msg", "");
        response.put("data", null != jsonList ? jsonList : new JSONArray());
        response.put("count", jsonList.size());
        response.put("total", total);
        return response.toString();
    }

    /**
     * 查询商品详情
     */
    @RequestMapping(value = "/shopInfo")
    @ResponseBody
    public String shopInfo(@RequestParam(value = "key", required = false) String key,
                           @RequestParam(value = "token", required = false) String token) {
        Map shopInfoJson = new HashMap<String, String>();
        try {
            Integer uStatus = UStatus.getStatus(token, this.dataprefix, redisTemplate);
            Map cacheInfo = new HashMap();
            if (uStatus == 0) {
                cacheInfo = redisHelp.getMapValue(this.dataprefix + "_" + "shopInfo" + key, redisTemplate);
            }
            if (cacheInfo.size() > 0) {
                shopInfoJson = cacheInfo;
            } else {
                Shop info = service.selectByKey(key);
                Map shopinfo = JSONObject.parseObject(JSONObject.toJSONString(info), Map.class);
                // 格式化商品图片为Array
                if (shopinfo.get("imgurl") != null && !shopinfo.get("imgurl").toString().isEmpty() && !shopinfo.get("imgurl").toString().equals("")) {
                    shopinfo.put("imgurl", JSONArray.parse(shopinfo.get("imgurl").toString()));
                }
                // 获取发布者信息
                Map bossInfo = JSONObject.parseObject(JSONObject.toJSONString(usersService.selectByKey(shopinfo.get("uid"))));
                bossInfo.remove("password");
                bossInfo.remove("assets");
                shopinfo.put("bossInfo", bossInfo);
                // 格式化规格数据格式
                JSONArray specs = JSONArray.parseArray(shopinfo.get("specs").toString());
                shopinfo.put("specs", specs);
                if (uStatus == 0) {
                    shopinfo.remove("value");
                    redisHelp.delete(this.dataprefix + "_" + "spaceInfo_" + key, redisTemplate);
                    redisHelp.setKey(this.dataprefix + "_" + "spaceInfo_" + key, shopinfo, 10, redisTemplate);
                } else {
                    Map map = redisHelp.getMapValue(this.dataprefix + "_" + "userInfo" + token, redisTemplate);
                    String group = map.get("group").toString();
                    Integer uid = Integer.parseInt(map.get("uid").toString());
                    //如果登陆，判断是否购买过
                    Userlog log = new Userlog();
                    log.setType("buy");
                    log.setUid(uid);
                    log.setCid(Integer.parseInt(key));
                    Integer isBuy = userlogService.total(log);
                    //判断自己是不是发布者
                    Integer aid = info.getUid();
                    if (!group.equals("administrator") && !group.equals("editor")) {
                        if (!uid.equals(aid) && isBuy < 1) {
                            shopinfo.remove("value");
                        }
                    }


                }
                shopInfoJson = shopinfo;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject JsonMap = JSON.parseObject(JSON.toJSONString(shopInfoJson), JSONObject.class);
        return JsonMap.toJSONString();


    }

    /***
     * 添加商品
     */
    @XssCleanIgnore
    @RequestMapping(value = "/addShop")
    @ResponseBody
    public String addShop(@RequestParam(value = "params", required = false) String params,
                          @RequestParam(value = "token", required = false) String token,
                          @RequestParam(value = "text", required = false) String text,
                          @RequestParam(value = "isSpace", required = false, defaultValue = "0") Integer isSpace,
                          @RequestParam(value = "isMd", required = false, defaultValue = "1") Integer isMd) {
        Integer uStatus = UStatus.getStatus(token, this.dataprefix, redisTemplate);
        if (uStatus == 0) {
            return Result.getResultJson(0, "用户未登录或Token验证失败", null);
        }
        Map map = redisHelp.getMapValue(this.dataprefix + "_" + "userInfo" + token, redisTemplate);
        Integer uid = Integer.parseInt(map.get("uid").toString());
        //登录情况下，刷数据攻击拦截
        Apiconfig apiconfig = UStatus.getConfig(this.dataprefix, apiconfigService, redisTemplate);
        if (apiconfig.getBanRobots().equals(1)) {
            String isSilence = redisHelp.getRedis(this.dataprefix + "_" + uid + "_silence", redisTemplate);
            if (isSilence != null) {
                return Result.getResultJson(0, "你已被禁言，请耐心等待", null);
            }
            String isRepeated = redisHelp.getRedis(this.dataprefix + "_" + uid + "_isRepeated", redisTemplate);
            if (isRepeated == null) {
                redisHelp.setRedis(this.dataprefix + "_" + uid + "_isRepeated", "1", 3, redisTemplate);
            } else {
                Integer frequency = Integer.parseInt(isRepeated) + 1;
                if (frequency == 3) {
                    securityService.safetyMessage("用户ID：" + uid + "，在商品发布接口疑似存在攻击行为，请及时确认处理。", "system");
                    redisHelp.setRedis(this.dataprefix + "_" + uid + "_silence", "1", apiconfig.getSilenceTime(), redisTemplate);
                    return Result.getResultJson(0, "你的请求存在恶意行为，10分钟内禁止操作！", null);
                } else {
                    redisHelp.setRedis(this.dataprefix + "_" + uid + "_isRepeated", frequency.toString(), 3, redisTemplate);
                }
                return Result.getResultJson(0, "你的操作太频繁了", null);
            }
        }

        //攻击拦截结束
        Map jsonToMap = null;
        Shop insert = null;

        if (StringUtils.isNotBlank(params)) {

            jsonToMap = JSONObject.parseObject(JSON.parseObject(params).toString());
            //支持两种模式提交商品内容
            if (text == null) {
                text = jsonToMap.get("text").toString();
            }
            Integer price = 0;
            if (jsonToMap.get("price") != null) {
                price = Integer.parseInt(jsonToMap.get("price").toString());
                if (price < 0) {
                    return Result.getResultJson(0, "请输入正确的参数", null);
                }
            }
            jsonToMap.put("status", "0");

            if (text.length() < 1) {
                return Result.getResultJson(0, "内容不能为空", null);
            } else {
                if (text.length() > 10000) {
                    return Result.getResultJson(0, "超出最大内容长度", null);
                }
            }
            //是否开启代码拦截
            if (apiconfig.getDisableCode().equals(1)) {
                if (baseFull.haveCode(text).equals(1)) {
                    return Result.getResultJson(0, "你的内容包含敏感代码，请修改后重试！", null);
                }
            }
            if (isMd.equals(1)) {
                text = text.replace("||rn||", "\n");
            }
            jsonToMap.put("text", text);
            jsonToMap.put("isMd", isMd);

            //如果用户不设置VIP折扣，则调用系统设置

            Double vipDiscount = Double.valueOf(apiconfig.getVipDiscount());
            if (jsonToMap.get("vipDiscount") == null) {
                jsonToMap.put("vipDiscount", vipDiscount);
            }

//            if(group.equals("administrator")||group.equals("editor")){
//                jsonToMap.put("status","1");
//            }
            //根据后台的开关判断
            Integer contentAuditlevel = apiconfig.getContentAuditlevel();
            if (contentAuditlevel.equals(0)) {
                jsonToMap.put("status", "1");
            }
            if (contentAuditlevel.equals(1)) {
                String forbidden = apiconfig.getForbidden();
                if (forbidden != null) {
                    if (forbidden.indexOf(",") != -1) {
                        String[] strarray = forbidden.split(",");
                        for (int i = 0; i < strarray.length; i++) {
                            String str = strarray[i];
                            if (text.indexOf(str) != -1) {
                                jsonToMap.put("status", "0");
                            }

                        }
                    } else {
                        if (text.indexOf(forbidden) != -1) {
                            jsonToMap.put("status", "0");
                        }
                    }
                } else {
                    jsonToMap.put("status", "1");
                }

            }
            if (contentAuditlevel.equals(2)) {
                //除管理员外，商品默认待审核
                String group = map.get("group").toString();
                if (!group.equals("administrator") && !group.equals("editor")) {
                    jsonToMap.put("status", "0");
                } else {
                    jsonToMap.put("status", "1");
                }
            }

            //判断是否开启邮箱验证
            Integer isEmail = apiconfig.getIsEmail();
            if (isEmail > 0) {
                //判断用户是否绑定了邮箱
                Users users = usersService.selectByKey(uid);
                if (users.getMail() == null) {
                    return Result.getResultJson(0, "发布商品前，请先绑定邮箱", null);
                }
            }
            insert = JSON.parseObject(JSON.toJSONString(jsonToMap), Shop.class);
            Long date = System.currentTimeMillis();
            String created = String.valueOf(date).substring(0, 10);
            insert.setCreated(Integer.parseInt(created));
            insert.setUid(uid);
        }

        int rows = service.insert(insert);
        //同步到动态
        if (isSpace.equals(1)) {
            Long date = System.currentTimeMillis();
            String created = String.valueOf(date).substring(0, 10);
            Space space = new Space();
            space.setType(5);
            space.setText("发布了新商品");
            space.setCreated(Integer.parseInt(created));
            space.setModified(Integer.parseInt(created));
            space.setUid(uid);
            space.setToid(insert.getId());
            spaceService.insert(space);
        }
        editFile.setLog("用户" + uid + "请求添加商品");
        JSONObject response = new JSONObject();
        response.put("code", rows);
        response.put("msg", rows > 0 ? "添加成功" : "添加失败");
        return response.toString();
    }

    /***
     * 修改商品
     */
    @XssCleanIgnore
    @RequestMapping(value = "/editShop")
    @ResponseBody
    public String editShop(@RequestParam(value = "params", required = false) String params,
                           @RequestParam(value = "token", required = false) String token,
                           @RequestParam(value = "text", required = false) String text,
                           @RequestParam(value = "isMd", required = false, defaultValue = "1") Integer isMd) {
        Integer uStatus = UStatus.getStatus(token, this.dataprefix, redisTemplate);
        if (uStatus == 0) {
            return Result.getResultJson(0, "用户未登录或Token验证失败", null);
        }
        Shop update = null;
        Map jsonToMap = null;
        Map map = redisHelp.getMapValue(this.dataprefix + "_" + "userInfo" + token, redisTemplate);
        Integer uid = Integer.parseInt(map.get("uid").toString());
        if (StringUtils.isNotBlank(params)) {
            Apiconfig apiconfig = UStatus.getConfig(this.dataprefix, apiconfigService, redisTemplate);
            jsonToMap = JSONObject.parseObject(JSON.parseObject(params).toString());
            //支持两种模式提交评论内容
            if (text == null) {
                text = jsonToMap.get("text").toString();
            }
            Integer price = 0;
            if (jsonToMap.get("price") != null) {
                price = Integer.parseInt(jsonToMap.get("price").toString());
                if (price < 0) {
                    return Result.getResultJson(0, "请输入正确的参数", null);
                }
            }

            // 查询发布者是不是自己，如果是管理员则跳过
            String group = map.get("group").toString();
            if (!group.equals("administrator") && !group.equals("editor")) {
                Integer sid = Integer.parseInt(jsonToMap.get("id").toString());
                Shop info = service.selectByKey(sid);
                Integer aid = info.getUid();
                if (!aid.equals(uid)) {
                    return Result.getResultJson(0, "你无权进行此操作", null);
                }
//                jsonToMap.put("status","0");
            }
            if (text.length() < 1) {
                return Result.getResultJson(0, "内容不能为空", null);
            } else {
                if (text.length() > 10000) {
                    return Result.getResultJson(0, "超出最大内容长度", null);
                }
            }
            //根据后台的开关判断
            Integer contentAuditlevel = apiconfig.getContentAuditlevel();
            if (contentAuditlevel.equals(0)) {
                jsonToMap.put("status", "1");
            }
            if (contentAuditlevel.equals(1)) {
                String forbidden = apiconfig.getForbidden();
                if (forbidden != null) {
                    if (forbidden.indexOf(",") != -1) {
                        String[] strarray = forbidden.split(",");
                        for (int i = 0; i < strarray.length; i++) {
                            String str = strarray[i];
                            if (text.indexOf(str) != -1) {
                                jsonToMap.put("status", "0");
                            }

                        }
                    } else {
                        if (text.indexOf(forbidden) != -1) {
                            jsonToMap.put("status", "0");
                        }
                    }
                } else {
                    jsonToMap.put("status", "1");
                }

            }
            if (contentAuditlevel.equals(2)) {
                //除管理员外，商品默认待审核
                if (!group.equals("administrator") && !group.equals("editor")) {
                    jsonToMap.put("status", "0");
                } else {
                    jsonToMap.put("status", "1");
                }
            }
            //是否开启代码拦截
            if (apiconfig.getDisableCode().equals(1)) {
                if (baseFull.haveCode(text).equals(1)) {
                    return Result.getResultJson(0, "你的内容包含敏感代码，请修改后重试！", null);
                }
            }
            if (isMd.equals(1)) {
                text = text.replace("||rn||", "\n");
            }
            jsonToMap.put("text", text);
            jsonToMap.remove("created");
            jsonToMap.put("isMd", isMd);
            update = JSON.parseObject(JSON.toJSONString(jsonToMap), Shop.class);
        }

        int rows = service.update(update);
        editFile.setLog("用户" + uid + "请求修改商品");
        JSONObject response = new JSONObject();
        response.put("code", rows);
        response.put("msg", rows > 0 ? "修改成功" : "修改失败");
        return response.toString();
    }

    /***
     * 删除商品
     */
    @RequestMapping(value = "/deleteShop")
    @ResponseBody
    public String deleteShop(@RequestParam(value = "key", required = false) String key, @RequestParam(value = "token", required = false) String token) {

        Integer uStatus = UStatus.getStatus(token, this.dataprefix, redisTemplate);
        if (uStatus == 0) {
            return Result.getResultJson(0, "用户未登录或Token验证失败", null);
        }
        // 查询发布者是不是自己，如果是管理员则跳过
        Map map = redisHelp.getMapValue(this.dataprefix + "_" + "userInfo" + token, redisTemplate);
        Integer uid = Integer.parseInt(map.get("uid").toString());
        String group = map.get("group").toString();
        Integer sid = Integer.parseInt(key);
        Shop info = service.selectByKey(sid);
        if (!group.equals("administrator") && !group.equals("editor")) {

            Integer aid = info.getUid();
            if (!aid.equals(uid)) {
                return Result.getResultJson(0, "你无权进行此操作", null);
            }
        } else {
            //发送消息
            Long date = System.currentTimeMillis();
            String created = String.valueOf(date).substring(0, 10);
            Inbox insert = new Inbox();
            insert.setUid(uid);
            insert.setTouid(info.getUid());
            insert.setType("system");
            insert.setText("你的商品【" + info.getTitle() + "】已被删除");
            insert.setCreated(Integer.parseInt(created));
            inboxService.insert(insert);
        }

        int rows = service.delete(key);
        editFile.setLog("用户" + uid + "请求删除商品" + key);
        JSONObject response = new JSONObject();
        response.put("code", rows);
        response.put("msg", rows > 0 ? "操作成功" : "操作失败");
        return response.toString();
    }

    /***
     * 生成的订单号
     */
    @RequestMapping(value = "/genOrder")
    @ResponseBody
    public String genOrder(@RequestParam(value = "token", required = true) String token,
                           @RequestParam(value = "product", required = true) Integer product,
                           @RequestParam(value = "specs", required = true) Integer specs,
                           @RequestParam(value = "address") String address) {
        try {
            // 验证用户登录状态
            Boolean isLogin = false;
            Map userInfo = new HashMap<>();
            Apiconfig apiconfig = new Apiconfig();
            if (UStatus.getStatus(token, this.dataprefix, redisTemplate) > 0) {
                isLogin = true;
                userInfo = redisHelp.getMapValue(this.dataprefix + "_" + "userInfo" + token, redisTemplate);
                apiconfig = UStatus.getConfig(this.dataprefix, apiconfigService, redisTemplate);
            } else {
                return Result.getResultJson(0, "用户未登录或Token验证失败", null);
            }
            // 获取到商品信息

            Shop userOrder = service.selectByKey(product);
            JSONObject spcesInfo = new JSONObject();
            // 不允许店主本人购买
            if (userOrder.getUid().equals(userInfo.get("uid"))) {
                return Result.getResultJson(0, "不允许购买自己的商品", null);
            }

            // 将 specs 的 JSON 字符串转换为对象列表

            JSONArray spcesList = JSONArray.parseArray(userOrder.getSpecs());
            for (int i = 0; i < spcesList.size(); i++) {
                JSONObject obj = spcesList.getJSONObject(i);
                if (obj.getInteger("id") != null && obj.getInteger("id") == specs) {
                    spcesInfo = obj;
                }
            }

            // 通过验证 写入商品信息
            Order newData = new Order();
            // 先计算价格，VIP价格以及商品打折
            Boolean isVip = false;
            Integer price = 0;
            long timeStamp = System.currentTimeMillis();
            long currentTime = timeStamp / 1000;
            if (Integer.parseInt(userInfo.get("vip").toString()) > currentTime) isVip = true;
            // 如果是VIP就开始计算折扣 商品折扣大于系统全局折扣 否则就不打折
            if (isVip) {
                // 商品折扣优先级大于系统折扣 先判断商品折扣是否小于1
                if (Float.parseFloat(userOrder.getVipDiscount()) < 1) {
                    // 先判断specs中有没有设置价格
                    if (spcesInfo.getInteger("price") > 0) {
                        price = (int) (spcesInfo.getInteger("price") * Float.parseFloat(userOrder.getVipDiscount()) + userOrder.getFreight());
                    } else {
                        price = (int) (userOrder.getPrice() * Float.parseFloat(userOrder.getVipDiscount()) + userOrder.getFreight());
                    }
                } else {
                    if (spcesInfo.getInteger("price") > 0) {
                        price = (int) (spcesInfo.getInteger("price") * Float.parseFloat(apiconfig.getVipDiscount()) + userOrder.getFreight());
                    } else {
                        price = (int) (userOrder.getPrice() * Float.parseFloat(apiconfig.getVipDiscount()) + userOrder.getFreight());
                    }
                }
            } else {
                if (spcesInfo.getInteger("price") > 0) {
                    price = spcesInfo.getInteger("price") + userOrder.getFreight();

                } else {
                    price = userOrder.getPrice() + userOrder.getFreight();
                }
            }

            // 处理商品订单 使用系统时间戳+格式化时间
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String orders = dateFormat.format(timeStamp) + timeStamp + userInfo.get("uid").toString();

            // 整合信息准备新增数据
            newData.setOrders(orders);
            newData.setPrice(price);
            newData.setPaid(0);
            newData.setUser_id(Integer.parseInt(userInfo.get("uid").toString()));
            newData.setBoss_id(userOrder.getUid());
            newData.setProduct(product);
            newData.setFreight(userOrder.getFreight());
            newData.setProduct_name(userOrder.getTitle());
            newData.setSpecs(spcesInfo.toString());
            newData.setCreated((int) (currentTime));
            newData.setAddress(address);

            Integer status = orderService.insert(newData);
            if (status > 0) {
                Map data = new HashMap<>();
                data.put("orderId", newData.getId());
                return Result.getResultJson(1, "订单生成成功", data);
            } else {
                return Result.getResultJson(0, "订单生成失败", null);
            }

        } catch (Exception err) {
            err.printStackTrace();
            return Result.getResultJson(0, "接口异常", null);
        }
    }

    /***
     * 查询订单
     */
    @RequestMapping(value = "/order")
    @ResponseBody
    public String order(@RequestParam(value = "id") Integer id,
                        @RequestParam(value = "token") String token) {
        try {
            Map userInfo = new HashMap<>();
            Apiconfig apiconfig = new Apiconfig();
            if (UStatus.getStatus(token, this.dataprefix, redisTemplate) > 0) {
                userInfo = redisHelp.getMapValue(this.dataprefix + "_" + "userInfo" + token, redisTemplate);
                apiconfig = UStatus.getConfig(this.dataprefix, apiconfigService, redisTemplate);
            } else {
                return Result.getResultJson(0, "用户未登录或Token验证失败", null);
            }
            // 验证订单所属
            Order orderInfo = orderService.selectByKey(id);
            if (orderInfo.toString().isEmpty()) {
                return Result.getResultJson(0, "订单不存在", null);
            }
            if (!userInfo.get("uid").equals(orderInfo.getUser_id())) {
                return Result.getResultJson(0, "你没有权限查看该订单", null);
            }

            Map<String, Object> data = new HashMap<>();
            Class<?> orderClass = orderInfo.getClass();
            Field[] fields = orderClass.getDeclaredFields();

            for (Field field : fields) {
                try {
                    field.setAccessible(true);
                    String fieldName = field.getName();
                    Object value = field.get(orderInfo);
                    data.put(fieldName, value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            // 查找该订单商品的主图片
            Shop product = service.selectByKey(orderInfo.getProduct());
            // 格式化Array

            JSONArray product_image = JSONArray.parseArray(product.getImgurl());

            // 格式化Object
            JSONObject specs = JSONObject.parseObject(orderInfo.getSpecs());
            JSONObject address = JSONObject.parseObject(orderInfo.getAddress());
            // 加入店主信息
            Users bossUser = usersService.selectByKey(orderInfo.getBoss_id());
            Map bossInfo = new HashMap<>();
            bossInfo.put("nickname", bossUser.getScreenName());
            bossInfo.put("username", bossUser.getName());
            bossInfo.put("uid", bossUser.getUid());
            bossInfo.put("avatar", bossUser.getAvatar());
            // 返回信息
            data.put("bossInfo", bossInfo);
            data.put("product_image", product_image);
            data.put("address", address);
            data.put("specs", specs);
            return Result.getResultJson(1, "获取成功", data);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.getResultJson(0, "接口异常", null);
        }
    }

    /***
     * 订单列表
     * @param type 0是购买订单 1是商家订单
     */
    @RequestMapping(value = "/orderList")
    @ResponseBody
    public String orderList(@RequestParam(value = "token") String token,
                            @RequestParam(value = "page", defaultValue = "1") Integer page,
                            @RequestParam(value = "limit", defaultValue = "15") Integer limit,
                            @RequestParam(value = "type", defaultValue = "0") Integer type,
                            @RequestParam(value = "searchParams", required = false) String searchParams,
                            @RequestParam(value = "order", defaultValue = "created desc") String order) {
        try {
            Map userInfo = new HashMap<>();
            Apiconfig apiconfig = new Apiconfig();
            if (UStatus.getStatus(token, this.dataprefix, redisTemplate) > 0) {
                userInfo = redisHelp.getMapValue(this.dataprefix + "_" + "userInfo" + token, redisTemplate);
                apiconfig = UStatus.getConfig(this.dataprefix, apiconfigService, redisTemplate);
            } else {
                return Result.getResultJson(0, "用户未登录或Token验证失败", null);
            }
            // 开始查询
            // 先将searchParams 格式化
            Order query = new Order();
            if (StringUtils.isNotBlank(searchParams)) {
                query = JSONObject.parseObject(searchParams).toJavaObject(Order.class);
                // query设置bossid和userid无效 只能在token获取
            }
            if (type.equals(1)) query.setBoss_id(Integer.parseInt(userInfo.get("uid").toString()));
            else query.setUser_id(Integer.parseInt(userInfo.get("uid").toString()));
            PageList<Order> orderList = orderService.selectPage(query, page, limit, "", order);
            List<Order> list = orderList.getList();
            JSONArray arrayList = new JSONArray();
            for (Order _order : list) {
                Map info = JSONObject.parseObject(JSONObject.toJSONString(_order),Map.class);
                // 格式化Obecjt
                JSONObject address = JSONObject.parseObject(info.get("address").toString());
                JSONObject specs = JSONObject.parseObject(info.get("specs").toString());
                JSONArray product_image = JSONArray.parseArray(service.selectByKey(info.get("product").toString()).getImgurl());
                Users bossUser = usersService.selectByKey(Integer.parseInt(info.get("boss_id").toString()));
                JSONObject bossInfo = new JSONObject();
                bossInfo.put("uid",bossUser.getUid());
                bossInfo.put("name",bossUser.getName());
                bossInfo.put("nickname",bossUser.getScreenName());
                bossInfo.put("avatar",bossUser.getAvatar());

                info.put("bossInfo",bossInfo);
                info.put("address",address);
                info.put("specs",specs);
                info.put("product_image",product_image);


                arrayList.add(info);
            }

            Map reslutData = new HashMap();
            reslutData.put("data", arrayList);
            reslutData.put("count", list.size());
            reslutData.put("total", orderService.total(query));
            return Result.getResultJson(1, "获取成功", reslutData);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.getResultJson(0, "接口异常", null);
        }

    }

    /***
     * 审核商品
     */
    @RequestMapping(value = "/auditShop")
    @ResponseBody
    public String auditShop(@RequestParam(value = "key", required = false) String key,
                            @RequestParam(value = "token", required = false) String token,
                            @RequestParam(value = "type", required = false) Integer type,
                            @RequestParam(value = "reason", required = false) String reason) {
        if (type == null) {
            type = 0;
        }
        try {
            Integer uStatus = UStatus.getStatus(token, this.dataprefix, redisTemplate);
            if (uStatus == 0) {
                return Result.getResultJson(0, "用户未登录或Token验证失败", null);
            }
            // 查询发布者是不是自己，如果是管理员则跳过
            Map map = redisHelp.getMapValue(this.dataprefix + "_" + "userInfo" + token, redisTemplate);
            Integer uid = Integer.parseInt(map.get("uid").toString());
            String group = map.get("group").toString();
            Integer sid = Integer.parseInt(key);
            Shop info = service.selectByKey(sid);
            if (!group.equals("administrator") && !group.equals("editor")) {

                Integer aid = info.getUid();
                if (!aid.equals(uid)) {
                    return Result.getResultJson(0, "你无权进行此操作", null);
                }
            }
            Shop shop = new Shop();
            shop.setId(Integer.parseInt(key));
            if (type.equals(0)) {
                shop.setStatus(1);
            } else {
                if (reason == "" || reason == null) {
                    return Result.getResultJson(0, "请输入拒绝理由", null);
                }
                shop.setStatus(2);
            }
            Integer rows = service.update(shop);
            //根据过审状态发送不同的内容
            if (type.equals(0)) {
                //发送消息
                Long date = System.currentTimeMillis();
                String created = String.valueOf(date).substring(0, 10);
                Inbox insert = new Inbox();
                insert.setUid(uid);
                insert.setTouid(info.getUid());
                insert.setType("system");
                insert.setText("你的商品【" + info.getTitle() + "】已审核通过");
                insert.setCreated(Integer.parseInt(created));
                inboxService.insert(insert);
            } else {
                //发送消息
                Long date = System.currentTimeMillis();
                String created = String.valueOf(date).substring(0, 10);
                Inbox insert = new Inbox();
                insert.setUid(uid);
                insert.setTouid(info.getUid());
                insert.setType("system");
                insert.setText("你的商品【" + info.getTitle() + "】未审核通过。理由如下：" + reason);
                insert.setCreated(Integer.parseInt(created));
                inboxService.insert(insert);
            }


            editFile.setLog("管理员" + uid + "请求审核商品" + key);
            JSONObject response = new JSONObject();
            response.put("code", rows);
            response.put("msg", rows > 0 ? "操作成功" : "操作失败");
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.getResultJson(0, "接口请求异常，请联系管理员", null);
        }

    }

    /***
     * 购买商品
     */
    @RequestMapping(value = "/buy")
    @ResponseBody
    public String buy(@RequestParam(value = "id", required = true) String id, @RequestParam(value = "token", required = true) String token) {

        try {
            Integer uStatus = UStatus.getStatus(token, this.dataprefix, redisTemplate);
            if (uStatus == 0) {
                return Result.getResultJson(0, "用户未登录或Token验证失败", null);
            }
            Map info = redisHelp.getMapValue(this.dataprefix + "_" + "userInfo" + token, redisTemplate);
            Apiconfig apiconfig = UStatus.getConfig(this.dataprefix, apiconfigService, redisTemplate);
            // 验证通过 获取用户信息
            Users userInfo = usersService.selectByKey(info.get("uid").toString());
            Integer money = userInfo.getAssets();
            // 获取订单信息
            Order orderInfo = orderService.selectByKey(id);
            // 获取商品信息

            Shop product = service.selectByKey(orderInfo.getProduct());

            // 判断库存
            if (product.getNum() < 1) {
                return Result.getResultJson(0, "商品库存不足", null);
            }

            // 检测订单属于权限
            if (!info.get("uid").equals(orderInfo.getUser_id())) {
                return Result.getResultJson(0, "订单错误", null);
            }

            if (orderInfo.getPaid().equals(1)) {
                return Result.getResultJson(0, "该订单已支付", null);
            }

            // 获取OK之后判断用户余额是否足够
            Integer payStatus = 0;
            if (money >= orderInfo.getPrice()) {
                money -= orderInfo.getPrice();
                // 设置订单状态
                orderInfo.setPaid(1);
                // 更新用户财产
                userInfo.setAssets(money);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                String orders = dateFormat.format(System.currentTimeMillis()) + System.currentTimeMillis() / 1000 + userInfo.getUid();
                Integer orderStatus = orderService.update(orderInfo);
                Integer userStatus = usersService.update(userInfo);
                if (orderStatus > 0 && userStatus > 0) {
                    // 写入购买信息
                    Paylog payInfo = new Paylog();
                    payInfo.setStatus(1);
                    payInfo.setUid(userInfo.getUid());
                    payInfo.setTotalAmount(String.valueOf(orderInfo.getPrice() * -1));
                    payInfo.setSubject("购买" + orderInfo.getProduct_name());
                    payInfo.setPaytype("product");
                    payInfo.setOutTradeNo(orders + userInfo.getUid());
                    payInfo.setCreated((int) (System.currentTimeMillis()));
                    payStatus = paylogService.insert(payInfo);

                    // 更新出售数量

                    Integer sellNum = product.getSellNum() + 1;
                    Integer productNum = product.getNum() - 1;
                    product.setNum(productNum);
                    product.setSellNum(sellNum);
                    service.update(product);


                    // 给卖家加米
                    Users bossInfo = usersService.selectByKey(orderInfo.getBoss_id());
                    Integer bossMoney = bossInfo.getAssets() + orderInfo.getPrice();
                    bossInfo.setAssets(bossMoney);
                    Integer bossStatus = usersService.update(bossInfo);

                    if (bossStatus > 0) {
                        // 给商家发送消息 然后添加记录
                        Paylog bossLog = new Paylog();
                        bossLog.setOutTradeNo(orders + bossInfo.getUid());
                        bossLog.setUid(bossInfo.getUid());
                        bossLog.setStatus(1);
                        bossLog.setCreated((int) (System.currentTimeMillis() / 1000));
                        bossLog.setSubject("出售" + orderInfo.getProduct_name());
                        bossLog.setTotalAmount(String.valueOf(orderInfo.getPrice()));
                        paylogService.insert(bossLog);
                        // 站内通知
                        Inbox inbox = new Inbox();
                        inbox.setText("你的商品【" + orderInfo.getProduct_name() + "】有新订单");
                        inbox.setUid(userInfo.getUid());
                        inbox.setTouid(orderInfo.getBoss_id());
                        inbox.setType("finance");

                        // 通知栏消息
                        if (apiconfig.getIsPush().equals(1)) {
                            if (!bossInfo.getClientId().isEmpty()) {
                                try {
                                    pushService.sendPushMsg(bossInfo.getClientId(), "消息通知", "你有新的商品订单！", "payload", "finance");
                                } catch (Exception e) {
                                    System.err.println("通知发送失败：" + e);
                                }
                            }
                        }
                    }

                }

            } else {
                return Result.getResultJson(201, "余额不足", null);
            }

            if (payStatus < 1) {
                return Result.getResultJson(0, "购买错误", null);
            }

            return Result.getResultJson(1, "购买成功", null);
        } catch (Exception e) {
            e.printStackTrace();

            return Result.getResultJson(0, "操作失败", null);
        }
    }

    /***
     * 商家修改订单号和地址
     */

    @RequestMapping(value = "/tracking")
    @ResponseBody
    public String tracking(@RequestParam(value = "id") Integer id,
                           @RequestParam(value = "token") String token,
                           @RequestParam(value = "price", required = false) Integer price,
                           @RequestParam(value = "address", required = false) String address,
                           @RequestParam(value = "tracking_number", required = false) String tracking_number,
                           @RequestParam(value = "isTracking", required = false) Integer isTracking) {
        try {
            Integer uStatus = UStatus.getStatus(token, this.dataprefix, redisTemplate);
            if (uStatus == 0) {
                return Result.getResultJson(0, "用户未登录或Token验证失败", null);
            }
            Map info = redisHelp.getMapValue(this.dataprefix + "_" + "userInfo" + token, redisTemplate);
            // 判断订单boss_id是否为infoID
            Order orderInfo = orderService.selectByKey(id);
            System.out.println(info.get("uid")+"草");
            System.out.println(orderInfo.getBoss_id() + orderInfo.getUser_id());
            System.out.println(orderInfo.getBoss_id().equals(info.get("uid"))+"草");
            System.out.println(orderInfo.getUser_id().equals(info.get("uid"))+"草");
            if (!orderInfo.getBoss_id().equals(info.get("uid")) && !orderInfo.getUser_id().equals(info.get("uid"))) {
                return Result.getResultJson(0, "你没有权限修改该订单", null);
            }
            Boolean isUser = false;
            if(orderInfo.getUser_id().equals(info.get("uid"))) isUser = true;
            // 修改数值
            if (price != null &&!isUser) {
                orderInfo.setPrice(price);
            }
            if (address!=null && !address.isEmpty()) {
                //判断用户 如果已支付则不可修改地址
                if(isUser&&orderInfo.getPaid().equals(1)){
                    return  Result.getResultJson(0,"已支付订单，不可修改地址，请联系卖家",null);
                }
                orderInfo.setAddress(address);
            }
            if (tracking_number != null && !tracking_number.isEmpty()) {
                orderInfo.setTracking_number(tracking_number);
            }
            if (isTracking != null&&!isUser) {
                orderInfo.setIsTracking(isTracking);
            }

            // 状态返回信息
            Integer orderStatus = orderService.update(orderInfo);
            if (orderStatus < 1) {
                return Result.getResultJson(0, "修改失败", null);
            }
            return Result.getResultJson(1, "修改成功", null);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.getResultJson(0, "接口错误", null);
        }
    }

    /***
     * 购买VIP
     */
    @RequestMapping(value = "/buyVIP")
    @ResponseBody
    public String buyVIP(@RequestParam(value = "day", required = false) Integer day, @RequestParam(value = "token", required = false) String token) {
        try {
            Integer uStatus = UStatus.getStatus(token, this.dataprefix, redisTemplate);
            if (uStatus == 0) {
                return Result.getResultJson(0, "用户未登录或Token验证失败", null);
            }
            if (day < 1) {
                return Result.getResultJson(0, "参数错误！", null);
            }
            Map map = redisHelp.getMapValue(this.dataprefix + "_" + "userInfo" + token, redisTemplate);
            Integer uid = Integer.parseInt(map.get("uid").toString());
            Apiconfig apiconfig = UStatus.getConfig(this.dataprefix, apiconfigService, redisTemplate);

            Long date = System.currentTimeMillis();
            String curTime = String.valueOf(date).substring(0, 10);
            Integer days = 86400;
            Users users = usersService.selectByKey(uid);
            Integer assets = users.getAssets();
            //判断用户是否为VIP，决定是续期还是从当前时间开始计算
            Integer vip = users.getVip();
            //默认是从当前时间开始相加
            Integer vipTime = Integer.parseInt(curTime) + days * day;
            if (vip.equals(1)) {
                return Result.getResultJson(0, "您已经是永久VIP，无需购买", null);
            }
            //如果已经是vip，走续期逻辑。
            if (vip > Integer.parseInt(curTime)) {
                vipTime = vip + days * day;
            }

            Integer AllPrice = day * apiconfig.getVipPrice();
            if (AllPrice > assets) {
                return Result.getResultJson(0, "当前资产不足，请充值", null);
            }


            if (day >= apiconfig.getVipDay()) {
                //如果时间戳为1就是永久会员
                vipTime = 1;
            }
            if (AllPrice < 0) {
                return Result.getResultJson(0, "参数错误！", null);
            }
            Integer newassets = assets - AllPrice;
            //更新用户资产与登录状态
            users.setAssets(newassets);
            users.setVip(vipTime);

            int rows = usersService.update(users);
            String created = String.valueOf(date).substring(0, 10);
            Paylog paylog = new Paylog();
            paylog.setStatus(1);
            paylog.setCreated(Integer.parseInt(created));
            paylog.setUid(uid);
            paylog.setOutTradeNo(created + "buyvip");
            paylog.setTotalAmount("-" + AllPrice);
            paylog.setPaytype("buyvip");
            paylog.setSubject("购买VIP");
            paylogService.insert(paylog);

            JSONObject response = new JSONObject();
            response.put("code", rows);
            response.put("msg", rows > 0 ? "开通VIP成功" : "操作失败");
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            JSONObject response = new JSONObject();
            response.put("code", 0);
            response.put("msg", "接口请求异常，请联系管理员");
            return response.toString();
        }


    }

    /***
     * VIP信息
     */
    @RequestMapping(value = "/vipInfo")
    @ResponseBody
    public String vipInfo() {
        JSONObject data = new JSONObject();
        Apiconfig apiconfig = UStatus.getConfig(this.dataprefix, apiconfigService, redisTemplate);
        data.put("vipDiscount", apiconfig.getVipDiscount());
        data.put("vipPrice", apiconfig.getVipPrice());
        data.put("scale", apiconfig.getScale());
        data.put("vipDay", apiconfig.getVipDay());
        JSONObject response = new JSONObject();
        response.put("code", 1);
        response.put("data", data);
        response.put("msg", "");
        return response.toString();
    }

    /**
     * 文章挂载商品
     */
    @RequestMapping(value = "/mountShop")
    @ResponseBody
    public String mountShop(@RequestParam(value = "cid", required = false) String cid, @RequestParam(value = "sid", required = false) String sid, @RequestParam(value = "token", required = false) String token) {

        Integer uStatus = UStatus.getStatus(token, this.dataprefix, redisTemplate);
        if (uStatus == 0) {
            return Result.getResultJson(0, "用户未登录或Token验证失败", null);
        }
        Map map = redisHelp.getMapValue(this.dataprefix + "_" + "userInfo" + token, redisTemplate);
        Integer uid = Integer.parseInt(map.get("uid").toString());
        //判断商品是不是自己的
        Shop shop = new Shop();
        shop.setUid(uid);
        shop.setId(Integer.parseInt(sid));
        Integer num = service.total(shop, null);
        if (num < 1) {
            return Result.getResultJson(0, "你无权限修改他人的商品", null);
        }
        shop.setCid(Integer.parseInt(cid));
        int rows = service.update(shop);
        JSONObject response = new JSONObject();
        response.put("code", rows);
        response.put("msg", rows > 0 ? "操作成功" : "操作失败");
        return response.toString();
    }

    /***
     * 查询商品是否已经购买过
     */
    @RequestMapping(value = "/isBuyShop")
    @ResponseBody
    public String isBuyShop(@RequestParam(value = "sid", required = false) String sid, @RequestParam(value = "token", required = false) String token) {

        Integer uStatus = UStatus.getStatus(token, this.dataprefix, redisTemplate);
        if (uStatus == 0) {
            return Result.getResultJson(0, "用户未登录或Token验证失败", null);
        }
        Map map = redisHelp.getMapValue(this.dataprefix + "_" + "userInfo" + token, redisTemplate);
        Integer uid = Integer.parseInt(map.get("uid").toString());

        Userlog log = new Userlog();
        log.setType("buy");
        log.setUid(uid);
        log.setCid(Integer.parseInt(sid));
        int rows = userlogService.total(log);
        JSONObject response = new JSONObject();
        response.put("code", rows > 0 ? 1 : 0);
        response.put("msg", rows > 0 ? "已购买" : "未购买");
        return response.toString();
    }

    /***
     * 添加商品分类
     */
    @RequestMapping(value = "/addShopType")
    @ResponseBody
    public String addShopType(@RequestParam(value = "params", required = false) String params, @RequestParam(value = "token", required = false) String token) {
        try {
            Integer uStatus = UStatus.getStatus(token, this.dataprefix, redisTemplate);
            if (uStatus == 0) {
                return Result.getResultJson(0, "用户未登录或Token验证失败", null);
            }
            // 查询发布者是不是自己，如果是管理员则跳过
            Map map = redisHelp.getMapValue(this.dataprefix + "_" + "userInfo" + token, redisTemplate);
            Integer uid = Integer.parseInt(map.get("uid").toString());
            String group = map.get("group").toString();
            if (!group.equals("administrator") && !group.equals("editor")) {
                return Result.getResultJson(0, "你无权进行此操作", null);
            }
            Shoptype insert = null;
            if (StringUtils.isNotBlank(params)) {
                JSONObject object = JSON.parseObject(params);
                insert = object.toJavaObject(Shoptype.class);
            } else {
                return Result.getResultJson(0, "参数不正确", null);
            }

            int rows = shoptypeService.insert(insert);
            editFile.setLog("管理员" + uid + "请求添加商品分类");
            JSONObject response = new JSONObject();
            response.put("code", rows);
            response.put("msg", rows > 0 ? "添加成功" : "添加失败");
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            JSONObject response = new JSONObject();
            response.put("code", 0);
            response.put("msg", "接口请求异常，请联系管理员");
            return response.toString();
        }

    }

    /***
     * 修改商品分类
     */
    @RequestMapping(value = "/editShopType")
    @ResponseBody
    public String editShopType(@RequestParam(value = "params", required = false) String params, @RequestParam(value = "token", required = false) String token) {
        try {
            Integer uStatus = UStatus.getStatus(token, this.dataprefix, redisTemplate);
            if (uStatus == 0) {
                return Result.getResultJson(0, "用户未登录或Token验证失败", null);
            }
            Map map = redisHelp.getMapValue(this.dataprefix + "_" + "userInfo" + token, redisTemplate);
            String group = map.get("group").toString();
            if (!group.equals("administrator")) {
                return Result.getResultJson(0, "你没有操作权限", null);
            }
            String logUid = map.get("uid").toString();
            Shoptype update = new Shoptype();
            Map jsonToMap = null;
            if (StringUtils.isNotBlank(params)) {
                jsonToMap = JSONObject.parseObject(JSON.parseObject(params).toString());
                update = JSON.parseObject(JSON.toJSONString(jsonToMap), Shoptype.class);
            } else {
                return Result.getResultJson(0, "参数不正确", null);
            }

            int rows = shoptypeService.update(update);
            editFile.setLog("管理员" + logUid + "请求修改商品分类" + jsonToMap.get("mid").toString());
            JSONObject response = new JSONObject();
            response.put("code", rows);
            response.put("msg", rows > 0 ? "操作成功" : "操作失败");
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.getResultJson(0, "接口请求异常，请联系管理员", null);
        }

    }

    /***
     * 删除分类
     */
    @RequestMapping(value = "/deleteShopType")
    @ResponseBody
    public String deleteShopType(@RequestParam(value = "id", required = false) String id,
                                 @RequestParam(value = "token", required = false) String token) {
        try {
            Integer uStatus = UStatus.getStatus(token, this.dataprefix, redisTemplate);
            if (uStatus == 0) {
                return Result.getResultJson(0, "用户未登录或Token验证失败", null);
            }
            Map map = redisHelp.getMapValue(this.dataprefix + "_" + "userInfo" + token, redisTemplate);
            String group = map.get("group").toString();
            String logUid = map.get("uid").toString();
            if (!group.equals("administrator")) {
                return Result.getResultJson(0, "你没有操作权限", null);
            }
            Shoptype typeInfo = shoptypeService.selectByKey(id);
            if (typeInfo == null) {
                return Result.getResultJson(0, "商品分类不存在", null);
            }
            Integer typeId = typeInfo.getId();
            //有下级分类的大类不能删除
            Shoptype shoptype = new Shoptype();
            shoptype.setParent(typeId);
            Integer total = shoptypeService.total(shoptype);
            if (total > 0) {
                return Result.getResultJson(0, "该分类存在下级分类，无法删除", null);
            }

            int rows = shoptypeService.delete(id);
            editFile.setLog("管理员" + logUid + "请求删除商品分类" + id);
            JSONObject response = new JSONObject();
            response.put("code", rows);
            response.put("msg", rows > 0 ? "操作成功" : "操作失败");
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.getResultJson(0, "接口请求异常，请联系管理员", null);
        }
    }

    /***
     * 查询商品分类
     */
    @RequestMapping(value = "/shopTypeList")
    @ResponseBody
    public String shopTypeList(@RequestParam(value = "searchParams", required = false) String searchParams,
                               @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                               @RequestParam(value = "limit", required = false, defaultValue = "15") Integer limit,
                               @RequestParam(value = "searchKey", required = false, defaultValue = "") String searchKey,
                               @RequestParam(value = "order", required = false, defaultValue = "") String order) {
        Shoptype query = new Shoptype();
        String sqlParams = "null";
        if (limit > 50) {
            limit = 50;
        }
        Integer total = 0;
        List jsonList = new ArrayList();

        if (StringUtils.isNotBlank(searchParams)) {
            JSONObject object = JSON.parseObject(searchParams);
            query = object.toJavaObject(Shoptype.class);
            Map paramsJson = JSONObject.parseObject(JSONObject.toJSONString(query), Map.class);
            sqlParams = paramsJson.toString();
        }
        List cacheList = redisHelp.getList(this.dataprefix + "_" + "shopTypeList_" + page + "_" + limit + "_" + searchKey + "_" + sqlParams, redisTemplate);

        total = shoptypeService.total(query);
        try {
            if (cacheList.size() > 0) {
                jsonList = cacheList;
            } else {
                PageList<Shoptype> pageList = shoptypeService.selectPage(query, page, limit, searchKey, order);
                jsonList = pageList.getList();
                if (jsonList.size() < 1) {
                    JSONObject noData = new JSONObject();
                    noData.put("code", 1);
                    noData.put("msg", "");
                    noData.put("data", new ArrayList());
                    noData.put("count", 0);
                    return noData.toString();
                }
                redisHelp.delete(this.dataprefix + "_" + "shopTypeList_" + page + "_" + limit + "_" + searchKey + "_" + sqlParams, redisTemplate);
                redisHelp.setList(this.dataprefix + "_" + "shopTypeList_" + page + "_" + limit + "_" + searchKey + "_" + sqlParams, jsonList, 10, redisTemplate);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (cacheList.size() > 0) {
                jsonList = cacheList;
            }
        }
        JSONObject response = new JSONObject();
        response.put("code", 1);
        response.put("msg", "");
        response.put("data", jsonList);
        response.put("count", jsonList.size());
        response.put("total", total);
        return response.toString();
    }

    /***
     * 商品分类信息
     */
    @RequestMapping(value = "/shopTypeInfo")
    @ResponseBody
    public String shopTypeInfo(@RequestParam(value = "id", required = false) Integer id,
                               @RequestParam(value = "token", required = false) String token) {
        try {
            Integer uid = 0;
            Integer cacheTime = 20;
            Integer uStatus = UStatus.getStatus(token, this.dataprefix, redisTemplate);
            if (!uStatus.equals(0)) {
                Map map = redisHelp.getMapValue(this.dataprefix + "_" + "userInfo" + token, redisTemplate);
                uid = Integer.parseInt(map.get("uid").toString());
                cacheTime = 3;
            }
            Map shopTypeInfoJson = new HashMap<String, String>();
            Map cacheInfo = redisHelp.getMapValue(this.dataprefix + "_" + "shopTypeInfoJson_" + id, redisTemplate);
            if (cacheInfo.size() > 0) {
                shopTypeInfoJson = cacheInfo;
            } else {
                Shoptype shoptype = shoptypeService.selectByKey(id);
                if (shoptype == null) {
                    return Result.getResultJson(0, "板块不存在", null);
                }

                shopTypeInfoJson = JSONObject.parseObject(JSONObject.toJSONString(shoptype), Map.class);

                redisHelp.delete(this.dataprefix + "_" + "shopTypeInfoJson_" + id + '_' + uid, redisTemplate);
                redisHelp.setKey(this.dataprefix + "_" + "shopTypeInfoJson_" + id + '_' + uid, shopTypeInfoJson, cacheTime, redisTemplate);
            }
            JSONObject response = new JSONObject();
            response.put("code", 1);
            response.put("msg", "");
            response.put("data", shopTypeInfoJson);
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            JSONObject response = new JSONObject();
            response.put("code", 1);
            response.put("msg", "");
            response.put("data", null);

            return response.toString();
        }
    }
}
