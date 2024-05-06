package com.itheima.prize.api.action;

import com.alibaba.fastjson.JSON;
import com.itheima.prize.api.config.LuaScript;
import com.itheima.prize.commons.config.RabbitKeys;
import com.itheima.prize.commons.config.RedisKeys;
import com.itheima.prize.commons.db.entity.*;
import com.itheima.prize.commons.utils.ApiResult;
import com.itheima.prize.commons.utils.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;

@RestController
@RequestMapping("/api/act")
@Api(tags = {"抽奖模块"})
public class ActController {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private LuaScript luaScript;

    @GetMapping("/go/{gameid}")
    @ApiOperation(value = "抽奖")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "gameid", value = "活动id", example = "1", required = true)
    })
    public ApiResult<Object> act(@PathVariable int gameid, HttpServletRequest request) {
        //根据gameid获取活动信息
        CardGame cardGame = (CardGame) redisUtil.get(RedisKeys.INFO + gameid);
        if (cardGame == null) {
            return new ApiResult<>(-1, "活动未加载进缓存", null);
        }

        //定义时间信息
        Date starttime = cardGame.getStarttime();
        Date endtime = cardGame.getEndtime();
        Date date = new Date();
        long duration = endtime.getTime() - starttime.getTime() + 60*1000;//将预热的一分钟也包含进去

        //判断活动状态，未开始，进行中，已结束？
        if (date.before(starttime)) {
            return new ApiResult(-1, "活动未开始", null);
        }
        if (date.after(endtime)) {
            return new ApiResult(-1, "活动已结束", null);
        }

        //获取用户信息
        HttpSession session = request.getSession();
        CardUser cardUser = (CardUser) session.getAttribute("user");

        //用户资格判断逻辑
        ApiResult apiResult = checkForGameUser(cardGame, cardUser);
        if (apiResult != null) {
            return apiResult;
        }

        //用户参与活动信息的异步更新
        nSynchronizedGameMQ(cardGame, cardUser, date);

        //用户进行了一次奖品的抽取
        ApiResult hitRes = gameProductHit(cardGame, cardUser, date, duration);

        return hitRes;
    }

    /**
     * 用户抽奖逻辑
     *
     * @param cardGame
     * @param cardUser
     * @param date
     * @param duration
     * @return
     */
    private ApiResult gameProductHit(CardGame cardGame, CardUser cardUser,
                                     Date date, long duration) {
        redisUtil.incr(RedisKeys.USERENTER + cardGame.getId() + "_" + cardUser.getId(),
                1L);
        redisUtil.expire(RedisKeys.USERENTER + cardGame.getId() + "_" + cardUser.getId(),
                duration / 1000);

        Long res = luaScript.tokenCheck(RedisKeys.TOKENS + cardGame.getId()
                , date.getTime() + "");

        if (res == 1) {
            return new ApiResult<>(0, "未中奖", null);
        } else if (res == 0) {
            return new ApiResult<>(-1, "奖品已抽光", null);
        } else {
            redisUtil.incr(RedisKeys.USERHIT + cardGame.getId() + "_" + cardUser.getId(), 1L);
            redisUtil.expire(RedisKeys.USERHIT + cardGame.getId() + "_" + cardUser.getId(),
                    duration / 1000);
            CardProduct cardProduct = (CardProduct) redisUtil.get(RedisKeys.TOKEN +
                    cardGame.getId() + "_" + res);

            nSynchronizedGameProductsMQ(cardUser, cardGame, cardProduct, date);
            return new ApiResult<>(1, "恭喜中奖", cardProduct);
        }
    }

    /**
     * 用户参与活动信息的异步更新
     *
     * @param cardGame
     * @param cardUser
     * @param date
     */
    private void nSynchronizedGameMQ(CardGame cardGame, CardUser cardUser, Date date) {
        //获取当前用户抽奖次数
        Integer curEnter = (Integer) redisUtil.get(RedisKeys.USERENTER +
                cardGame.getId() + "_" + cardUser.getId());

        //异步将用户参与活动的信息更新到db中
        if (curEnter == null) {
            CardUserGame cardUserGame = new CardUserGame();
            cardUserGame.setCreatetime(date);
            cardUserGame.setGameid(cardGame.getId());
            cardUserGame.setUserid(cardUser.getId());
            String msg = JSON.toJSONString(cardUserGame);
            rabbitTemplate.convertAndSend(RabbitKeys.EXCHANGE_DIRECT,
                    RabbitKeys.QUEUE_PLAY, msg);
        }
    }

    /**
     * 用于执行用户能否参加活动的逻辑判断
     *
     * @param cardGame
     * @param cardUser
     */
    private ApiResult checkForGameUser(CardGame cardGame, CardUser cardUser) {
        //该用户级别能够参与抽奖次数
        Integer maxEnter = (Integer) redisUtil.hget(RedisKeys.MAXENTER + cardGame.getId()
                , cardUser.getLevel() + "");

        //获取当前用户抽奖次数
        Integer curEnter = (Integer) redisUtil.get(RedisKeys.USERENTER +
                cardGame.getId() + "_" + cardUser.getId());


        if (curEnter != null && curEnter >= maxEnter) {
            return new ApiResult<>(-1, "您的抽奖次数已用完", null);
        }

        //该用户能够中奖次数
        Integer maxGoal = (Integer) redisUtil.hget(RedisKeys.MAXGOAL + cardGame.getId()
                , cardUser.getLevel() + "");

        //获取当前用户抽奖次数
        Integer curGoal = (Integer) redisUtil.get(RedisKeys.USERHIT +
                cardGame.getId() + "_" + cardUser.getId());

        if (curGoal != null && curGoal >= maxGoal) {
            return new ApiResult<>(-1, "您已达到最大中奖数", null);
        }
        return null;

    }

    /**
     * 用于异步更新数据库中用户的获奖信息
     *
     * @param cardUser
     * @param cardGame
     * @param cardProduct
     * @param date
     */
    private void nSynchronizedGameProductsMQ(CardUser cardUser, CardGame cardGame,
                                             CardProduct cardProduct, Date date) {
        CardUserHit cardUserHit = new CardUserHit();
        cardUserHit.setGameid(cardGame.getId());
        cardUserHit.setHittime(date);
        cardUserHit.setProductid(cardProduct.getId());
        cardUserHit.setUserid(cardUser.getId());
        String msg = JSON.toJSONString(cardUserHit);
        rabbitTemplate.convertAndSend(RabbitKeys.EXCHANGE_DIRECT,
                RabbitKeys.QUEUE_HIT, msg);

    }

    @GetMapping("/info/{gameid}")
    @ApiOperation(value = "缓存信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "gameid", value = "活动id", example = "1", required = true)
    })
    public ApiResult info(@PathVariable int gameid) {
        //TODO
        return null;
    }
}
