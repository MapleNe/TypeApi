package com.RuleApi.entity;

import java.io.Serializable;
import lombok.Data;
import java.util.Date;
import java.util.List;

/**
 * TypechoUserlog
 * @author buxia97 2022-01-06
 */
@Data
public class TypechoTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Integer id;

    /**
     * uid
     */
    private Integer uid;

    /**
     * comment（每日任务评论）
     */
    private Integer comment;

    /**
     * follow（每日任务关注）
     */
    private Integer follow;

    /**
     * reward（每日任务打赏）
     */
    private Integer reward;

    /**
     * contents（每日任务帖子）
     */
    private Integer contents;

    /**
     * time(每日任务时间)
     */
    private Integer time;

}