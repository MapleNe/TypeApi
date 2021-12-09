package com.RuleApi.web;

import com.RuleApi.common.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.RuleApi.entity.*;
import com.RuleApi.service.*;
import com.mysql.jdbc.exceptions.MySQLDataException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

/**
 * 控制层
 * TypechoContentsController
 * @author buxia97
 * @date 2021/11/29
 */
@Component
@Controller
@RequestMapping(value = "/typechoContents")
public class TypechoContentsController {

    @Autowired
    TypechoContentsService service;

    @Autowired
    private TypechoFieldsService fieldsService;

    @Autowired
    private TypechoRelationshipsService relationshipsService;

    @Autowired
    private TypechoMetasService metasService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${webinfo.contentCache}")
    private Integer contentCache;

    @Value("${webinfo.contentInfoCache}")
    private Integer contentInfoCache;

    @Value("${webinfo.pexelsKey}")
    private String pexelsKey;

    RedisHelp redisHelp =new RedisHelp();
    ResultAll Result = new ResultAll();
    baseFull baseFull = new baseFull();
    UserStatus UStatus = new UserStatus();
    HttpClient HttpClient = new HttpClient();

    /**
     * 查询文章详情
     *
     */
    @RequestMapping(value = "/contentsInfo")
    @ResponseBody
    public String contentsInfo (@RequestParam(value = "key", required = false) String  key,@RequestParam(value = "isMd" , required = false, defaultValue = "0") Integer isMd) {
        TypechoContents typechoContents = null;
        Map contensjson = new HashMap<String, String>();
        Map cacheInfo = redisHelp.getMapValue("contentsInfo_"+key+"_"+isMd,redisTemplate);
        try{
            if(cacheInfo.size()>0){
                contensjson = cacheInfo;
            }else{
                typechoContents = service.selectByKey(key);
                if(typechoContents==null){
                    return Result.getResultJson(0,"该文章不存在",null);
                }
                String text = typechoContents.getText();
                //要做处理将typecho的图片插入格式变成markdown
                List imgList = baseFull.getImageSrc(text);
                List codeList = baseFull.getImageCode(text);



                for(int c = 0; c < codeList.size(); c++){
                    String codeimg = codeList.get(c).toString();
                    String urlimg = imgList.get(c).toString();
                    text=text.replace(codeimg,"![image"+c+"]("+urlimg+")");
                }
                text=text.replace("<!--markdown-->","");
                List codeImageMk = baseFull.getImageMk(text);
                for(int d = 0; d < codeImageMk.size(); d++){
                    String mk = codeImageMk.get(d).toString();
                    text=text.replace(mk,"");
                }
                if(isMd==1){
                    //如果isMd等于1，则输出解析后的md代码
                    Parser parser = Parser.builder().build();
                    Node document = parser.parse(text);
                    HtmlRenderer renderer = HtmlRenderer.builder().build();
                    text = renderer.render(document);

                }
                //获取文章id，从而获取自定义字段，和分类标签
                String cid = typechoContents.getCid().toString();
                List<TypechoFields> fields = fieldsService.selectByKey(cid);
                List<TypechoRelationships> relationships = relationshipsService.selectByKey(cid);

                List metas = new ArrayList();
                List tags = new ArrayList();
                for (int i = 0; i < relationships.size(); i++) {
                    Map json = JSONObject.parseObject(JSONObject.toJSONString(relationships.get(i)), Map.class);
                    if(json!=null){
                        String mid = json.get("mid").toString();
                        TypechoMetas metasList  = metasService.selectByKey(mid);
                        Map metasInfo = JSONObject.parseObject(JSONObject.toJSONString(metasList), Map.class);
                        String type = metasInfo.get("type").toString();
                        if(type.equals("category")){
                            metas.add(metasInfo);
                        }
                        if(type.equals("tag")){
                            tags.add(metasInfo);
                        }
                    }

                }
                contensjson = JSONObject.parseObject(JSONObject.toJSONString(typechoContents), Map.class);

                //转为map，再加入字段
                contensjson.remove("password");
                contensjson.put("images",imgList);
                contensjson.put("fields",fields);
                contensjson.put("category",metas);
                contensjson.put("tag",tags);
                contensjson.put("text",text);
            }

        }catch (Exception e){
            if(cacheInfo.size()>0){
                contensjson = cacheInfo;
            }
        }
        redisHelp.delete("contentsInfo_"+key+"_"+isMd,redisTemplate);
        redisHelp.setKey("contentsInfo_"+key+"_"+isMd,contensjson,this.contentInfoCache,redisTemplate);
        JSONObject concentInfo = JSON.parseObject(JSON.toJSONString(contensjson),JSONObject.class);
        return concentInfo.toJSONString();
        //return new ApiResult<>(ResultCode.success.getCode(), typechoContents, ResultCode.success.getDescr(), request.getRequestURI());
    }


    /***
     * 表单查询请求
     * @param searchParams Bean对象JSON字符串
     * @param page         页码
     * @param limit        每页显示数量
     */
    @RequestMapping(value = "/contensList")
    @ResponseBody
    public String contensList (@RequestParam(value = "searchParams", required = false) String  searchParams,
                            @RequestParam(value = "page"        , required = false, defaultValue = "1") Integer page,
                            @RequestParam(value = "limit"       , required = false, defaultValue = "15") Integer limit,
                            @RequestParam(value = "searchKey"        , required = false, defaultValue = "") String searchKey,
                            @RequestParam(value = "order"        , required = false, defaultValue = "") String order,
                               @RequestParam(value = "random"        , required = false, defaultValue = "0") Integer random,
                               @RequestParam(value = "token"        , required = false, defaultValue = "") String token){
        TypechoContents query = new TypechoContents();
        if (StringUtils.isNotBlank(searchParams)) {
            JSONObject object = JSON.parseObject(searchParams);
            //如果不是登陆状态，那么只显示开放状态文章。如果是，则查询自己发布的文章
            Integer uStatus = UStatus.getStatus(token,redisTemplate);
            if(token==""||uStatus==0){

                object.put("status","publish");
            }else{
                String aid = redisHelp.getValue("userInfo"+token,"uid",redisTemplate).toString();
                object.put("authorId",aid);
            }

            query = object.toJavaObject(TypechoContents.class);

        }
        List jsonList = new ArrayList();

        List cacheList = redisHelp.getList("contensList_"+page+"_"+limit+"_"+searchParams+"_"+order+"_"+searchKey+"_"+random,redisTemplate);
        //监听异常，如果有异常则调用redis缓存中的list，如果无异常也调用redis，但是会更新数据
        try{
            if(cacheList.size()>0){
                jsonList = cacheList;
            }else{
                PageList<TypechoContents> pageList = service.selectPage(query, page, limit, searchKey,order,random);
                List list = pageList.getList();
                for (int i = 0; i < list.size(); i++) {
                    Map json = JSONObject.parseObject(JSONObject.toJSONString(list.get(i)), Map.class);
                    //加入自定义字段信息，这里取消注释即可开启，但是数据库查询会消耗性能
                    String cid = json.get("cid").toString();
                    List<TypechoFields> fields = fieldsService.selectByKey(cid);
                    json.put("fields",fields);

                    List<TypechoRelationships> relationships = relationshipsService.selectByKey(cid);

                    List metas = new ArrayList();
                    List tags = new ArrayList();
                    for (int j = 0; j < relationships.size(); j++) {
                        Map info = JSONObject.parseObject(JSONObject.toJSONString(relationships.get(j)), Map.class);
                        if(info!=null){
                            String mid = info.get("mid").toString();

                            TypechoMetas metasList  = metasService.selectByKey(mid);
                            Map metasInfo = JSONObject.parseObject(JSONObject.toJSONString(metasList), Map.class);
                            String type = metasInfo.get("type").toString();
                            if(type.equals("category")){
                                metas.add(metasInfo);
                            }
                            if(type.equals("tag")){
                                tags.add(metasInfo);
                            }
                        }

                    }


                    String text = json.get("text").toString();
                    List imgList = baseFull.getImageSrc(text);
                    text=text.replaceAll("(\\\r\\\n|\\\r|\\\n|\\\n\\\r)", "");
                    text=text.replaceAll("\\s*", "");
                    text=text.replaceAll("</?[^>]+>", "");
                    //去掉文章开头的图片插入
                    text=text.replaceAll("((!\\[)[\\s\\S]+?(\\]\\[)[\\s\\S]+?(\\]))+?","");
                    json.put("images",imgList);
                    json.put("text",text.length()>200 ? text.substring(0,200) : text);
                    json.put("category",metas);
                    json.put("tag",tags);
                    json.remove("password");



                    jsonList.add(json);
                    redisHelp.delete("contensList_"+page+"_"+limit+"_"+searchParams+"_"+order+"_"+searchKey+"_"+random,redisTemplate);
                    redisHelp.setList("contensList_"+page+"_"+limit+"_"+searchParams+"_"+order+"_"+searchKey+"_"+random,jsonList,this.contentCache,redisTemplate);
                }
            }
        }catch (Exception e){

            if(cacheList.size()>0){
                jsonList = cacheList;
            }

        }

        JSONObject response = new JSONObject();
        response.put("code" , 1);
        response.put("msg"  , "");
        response.put("data" , null != jsonList ? jsonList : new JSONArray());
        response.put("count", jsonList.size());
        return response.toString();
    }

    /***
     * 发布文章
     * @param params Bean对象JSON字符串
     */
    @RequestMapping(value = "/contensAdd")
    @ResponseBody
    public String contensAdd(@RequestParam(value = "params", required = false) String  params, @RequestParam(value = "token", required = false) String  token) {
        TypechoContents insert = null;
        Integer uStatus = UStatus.getStatus(token,redisTemplate);
        Map jsonToMap =null;
        String category = "";
        String tag = "";
        if(uStatus==0){
            return Result.getResultJson(0,"用户未登录或Token验证失败",null);
        }
        if (StringUtils.isNotBlank(params)) {
            jsonToMap =  JSONObject.parseObject(JSON.parseObject(params).toString());
            //获取发布者信息
            Map map =redisHelp.getMapValue("userInfo"+token,redisTemplate);
            String uid = map.get("uid").toString();
            //生成typecho数据库格式的创建时间戳
            Long date = System.currentTimeMillis();
            String userTime = String.valueOf(date).substring(0,10);
            //验证文章类型(只允许post)
//            String type = jsonToMap.get("type").toString();
//            if(!type.equals("page")&&!type.equals("post")){
//                return Result.getResultJson(0,"请传入正确的文章类型",null);
//            }
            //获取参数中的分类和标签
            category = jsonToMap.get("category").toString();
            if(jsonToMap.get("tag")!=null){
                tag = jsonToMap.get("tag").toString();
            }


            //写入创建时间和作者
            jsonToMap.put("created",userTime);
            jsonToMap.put("authorId",uid);
            //文章默认待审核
            jsonToMap.put("status","waiting");
            //部分字段不允许定义
            jsonToMap.put("type","post");
            jsonToMap.put("commentsNum",0);
            jsonToMap.put("allowPing",1);
            jsonToMap.put("allowFeed",1);
            jsonToMap.put("allowComment",1);
            jsonToMap.put("orderKey",0);
            jsonToMap.put("parent",0);
            jsonToMap.remove("password");
            insert = JSON.parseObject(JSON.toJSONString(jsonToMap), TypechoContents.class);

        }

        int rows = service.insert(insert);

        Integer cid = insert.getCid();
        //文章添加完成后，再处理分类和标签
        if(rows > 0) {
            if (category != "") {
                TypechoRelationships toCategory = new TypechoRelationships();
                Integer mid = Integer.parseInt(category);
                toCategory.setCid(cid);
                toCategory.setMid(mid);
                List<TypechoRelationships> mList = relationshipsService.selectList(toCategory);
                if (mList.size() == 0) {
                    relationshipsService.insert(toCategory);
                }

            }
            if (tag != "") {
                Integer result = tag.indexOf(",");
                if (result != -1) {
                    String[] tagList = tag.split(",");
                    List list = Arrays.asList(baseFull.threeClear(tagList));
                    for (int v = 0; v < list.size(); v++) {
                        TypechoRelationships toTag = new TypechoRelationships();
                        String id = list.get(v).toString();
                        Integer mid = Integer.parseInt(id);
                        toTag.setCid(cid);
                        toTag.setMid(mid);
                        List<TypechoRelationships> mList = relationshipsService.selectList(toTag);
                        if (mList.size() == 0) {
                            relationshipsService.insert(toTag);
                        }

                    }
                }
            }
        }
        JSONObject response = new JSONObject();
        response.put("code" ,rows > 0 ? 1: 0 );
        response.put("data" , rows);
        response.put("msg"  , rows > 0 ? "添加成功" : "添加失败");
        return response.toString();
    }

    /***
     * 文章修改
     * @param params Bean对象JSON字符串
     */
    @RequestMapping(value = "/contensUpdate")
    @ResponseBody
    public String contensUpdate(@RequestParam(value = "params", required = false) String  params, @RequestParam(value = "token", required = false) String  token) {
        TypechoContents update = null;
        Map jsonToMap =null;
        String category = "";
        String tag = "";
        Integer uStatus = UStatus.getStatus(token,redisTemplate);
        if(uStatus==0){
            return Result.getResultJson(0,"用户未登录或Token验证失败",null);
        }
        if (StringUtils.isNotBlank(params)) {
            jsonToMap =  JSONObject.parseObject(JSON.parseObject(params).toString());
            //生成typecho数据库格式的修改时间戳
            Long date = System.currentTimeMillis();
            String userTime = String.valueOf(date).substring(0,10);
            jsonToMap.put("modified",userTime);
            //验证文章类型(废除，普通用户无这个权限)
//            String type = jsonToMap.get("type").toString();
//            if(!type.equals("page")&&!type.equals("post")){
//                return Result.getResultJson(0,"请传入正确的文章类型",null);
//            }
            //获取参数中的分类和标签（暂时不允许定义）
            category = jsonToMap.get("category").toString();
            if(jsonToMap.get("tag")!=null){
                tag = jsonToMap.get("tag").toString();
            }


            //部分字段不允许定义
            jsonToMap.remove("authorId");
            jsonToMap.remove("commentsNum");
            jsonToMap.remove("allowPing");
            jsonToMap.remove("allowFeed");
            jsonToMap.remove("allowComment");
            jsonToMap.remove("password");
            jsonToMap.remove("orderKey");
            jsonToMap.remove("parent");
            jsonToMap.remove("created");
            jsonToMap.remove("slug");

            jsonToMap.remove("type");
            //状态重新变成待审核
            jsonToMap.put("status","waiting");
            update = JSON.parseObject(JSON.toJSONString(jsonToMap), TypechoContents.class);
        }

        int rows = service.update(update);
        //处理标签和分类
        Integer cid = Integer.parseInt(jsonToMap.get("cid").toString());
        //删除原本的分类标签映射，反正都会更新，那就一起更新
        relationshipsService.delete(cid);

        //文章添加完成后，再处理分类和标签，只有文章能设置标签和分类
        if(rows > 0){
            if(category!=""){
                TypechoRelationships toCategory = new TypechoRelationships();
                Integer mid = Integer.parseInt(category);
                toCategory.setCid(cid);
                toCategory.setMid(mid);
                List<TypechoRelationships> mList =  relationshipsService.selectList(toCategory);
                if (mList.size()==0){
                    relationshipsService.insert(toCategory);
                }

            }
            if(tag!=""){
                Integer result = tag.indexOf(",");
                if(result != -1){
                    String[] tagList = tag.split(",");
                    List list = Arrays.asList(baseFull.threeClear(tagList));
                    for (int v = 0; v < list.size(); v++) {
                        TypechoRelationships toTag = new TypechoRelationships();
                        String id = list.get(v).toString();
                        Integer mid = Integer.parseInt(id);
                        toTag.setCid(cid);
                        toTag.setMid(mid);
                        List<TypechoRelationships> mList =  relationshipsService.selectList(toTag);
                        if (mList.size()==0){
                            relationshipsService.insert(toTag);
                        }

                    }
                }
            }
        }


        JSONObject response = new JSONObject();
        response.put("code" ,rows > 0 ? 1: 0 );
        response.put("data" , rows);
        response.put("msg"  , rows > 0 ? "修改成功" : "修改失败");
        return response.toString();
    }

    /***
     * 文章删除
     */
    @RequestMapping(value = "/contensDelete")
    @ResponseBody
    public String formDelete(@RequestParam(value = "key", required = false) String  key, @RequestParam(value = "token", required = false) String  token) {
        Integer uStatus = UStatus.getStatus(token,redisTemplate);
        if(uStatus==0){
            return Result.getResultJson(0,"用户未登录或Token验证失败",null);
        }
        //String group = (String) redisHelp.getValue("userInfo"+token,"group",redisTemplate);
        Map map =redisHelp.getMapValue("userInfo"+token,redisTemplate);
        String group = map.get("group").toString();
        if(!group.equals("administrator")){
            return Result.getResultJson(0,"你没有操作权限",null);
        }
        int rows = service.delete(key);
        //删除与分类的映射
        int st = relationshipsService.delete(key);
        JSONObject response = new JSONObject();
        response.put("code" ,rows > 0 ? 1: 0 );
        response.put("data" , rows);
        response.put("msg"  , rows > 0 ? "操作成功" : "操作失败");
        return response.toString();
    }

    /**
     * pexels图库
     * */
    @RequestMapping(value = "/ImagePexels")
    @ResponseBody
    public String ImagePexels() {
        String cacheImage = redisHelp.getRedis("ImagePexels",redisTemplate);
        String imgList = "";
        if(cacheImage==null){
            imgList = HttpClient.doGetImg("https://api.pexels.com/v1/curated?per_page=40",this.pexelsKey);
            if(imgList==null){
                return Result.getResultJson(0,"图片接口异常",null);
            }
            redisHelp.delete("ImagePexels",redisTemplate);
            redisHelp.setRedis("ImagePexels",imgList,43200,redisTemplate);
        }else{
            imgList = cacheImage;
        }
        return imgList;
    }
}
