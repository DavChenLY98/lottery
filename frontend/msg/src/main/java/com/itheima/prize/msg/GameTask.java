package com.itheima.prize.msg;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.itheima.prize.commons.config.RedisKeys;
import com.itheima.prize.commons.db.entity.*;
import com.itheima.prize.commons.db.service.CardGameProductService;
import com.itheima.prize.commons.db.service.CardGameRulesService;
import com.itheima.prize.commons.db.service.CardGameService;
import com.itheima.prize.commons.db.service.GameLoadService;
import com.itheima.prize.commons.utils.RedisUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.format.DateTimeFormatters;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 活动信息预热，每隔1分钟执行一次
 * 查找未来1分钟内（含），要开始的活动
 */
@Component
public class GameTask {
    private final static Logger log = LoggerFactory.getLogger(GameTask.class);
    @Autowired
    private CardGameService gameService;
    @Autowired
    private CardGameProductService gameProductService;
    @Autowired
    private CardGameRulesService gameRulesService;
    @Autowired
    private GameLoadService gameLoadService;
    @Autowired
    private RedisUtil redisUtil;

    @Scheduled(cron = "0 * * * * ?")
    public void execute() {
        //TODO
        //首先输出当前方法的执行时间
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();


        //获取需要预热的活动列表,小于活动开始时间,且大于活动开始时间前的1分钟则进行预热
        List<CardGame> cardGameList = gameService.lambdaQuery()
                .gt(CardGame::getStarttime, now.format(dateTimeFormatter))
                .le(CardGame::getStarttime, now.plusMinutes(1).format(dateTimeFormatter))
                .list();

        if(cardGameList.isEmpty()){
            return;
        }

        //缓存活动进入redis
        cacheCardGame(cardGameList);

        //缓存活动规则
        cacheCardGameRules(cardGameList);

        //奖品令牌设计
        productsTokenCreate(cardGameList);


    }

    /**
     * 奖品令牌设计
     *
     * @param cardGameList
     */
    private void productsTokenCreate(List<CardGame> cardGameList) {
        List tokens = new ArrayList();
        for (CardGame cardGame : cardGameList) {
            //根据活动id获取奖品信息
            List<CardProductDto> byGameId = gameLoadService.getByGameId(cardGame.getId());
            Date starttime = cardGame.getStarttime();
            Date endtime = cardGame.getEndtime();
            long duration=endtime.getTime()-starttime.getTime();
            for (CardProductDto cardProductDto : byGameId) {
                int productsNum = cardProductDto.getAmount();
                for (int i = 0; i < productsNum; i++) {
                    //活动持续时间（ms）
                    long rnd = starttime.getTime() + new Random().nextInt((int) duration);
                    //为什么乘1000，再额外加一个随机数呢？ - 防止时间段奖品多时重复
                    long token = rnd * 1000 + new Random().nextInt(999);
                    tokens.add(token);
                    CardProduct cardProduct = cardProductDto;
                    //将令牌和奖品信息对应起来进行存储
                    redisUtil.set(RedisKeys.TOKEN + cardGame.getId() + "_" + token, cardProduct, duration / 1000);
                }
            }
            Collections.sort(tokens);
            redisUtil.rightPushAll(RedisKeys.TOKENS + cardGame.getId(), tokens);
        }
    }

    /**
     * 缓存活动规则，包括每个活动对应的会员等级以及不同会员等级对应的最大抽奖次数和最大中奖次数。
     *
     * @param cardGameList
     */
    private void cacheCardGameRules(List<CardGame> cardGameList) {
        for (CardGame cardGame : cardGameList) {
            //通过gameid查询gameRules，并返回规则列表
            List<CardGameRules> cardGameRules = gameRulesService.getRulesByGameId(cardGame.getId());
            //遍历规则列表中的规则，并通过hash数据结构在redis中存储活动规则，包括每个活动对应的会员等级以及不同会员等级对应的最大抽奖次数和最大中奖次数
            for (CardGameRules cardGameRule : cardGameRules) {
                redisUtil.hset(RedisKeys.MAXGOAL + cardGame.getId(), cardGameRule.getUserlevel() + "", cardGameRule.getGoalTimes());
                redisUtil.hset(RedisKeys.MAXENTER + cardGame.getId(), cardGameRule.getUserlevel() + "", cardGameRule.getEnterTimes());
            }
        }
    }


    /**
     * 将每一个遍历到的活动按照k-v的形式利用String类型的redis数据类型存入redis，过期时间设置为永久
     *
     * @param cardGameList
     */
    private void cacheCardGame(List<CardGame> cardGameList) {
        for (CardGame cardGame : cardGameList) {
            redisUtil.set(RedisKeys.INFO + cardGame.getId(), cardGame, -1);
        }
    }
}
