package com.itheima.prize.msg;

import com.itheima.prize.commons.config.RabbitKeys;
import com.itheima.prize.commons.db.entity.CardUserGame;
import com.itheima.prize.commons.db.service.CardUserGameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = RabbitKeys.QUEUE_PLAY)
public class PrizeGameReceiver {

    private final static Logger logger = LoggerFactory.getLogger(PrizeGameReceiver.class);

    @Autowired
    private CardUserGameService cardUserGameService;

    /**
     * 用于记录用户参与的活动
     * @param cardUserGame
     */
    @RabbitHandler
    public void processMessage(CardUserGame cardUserGame) {
        logger.info("user play : msg={}" , cardUserGame);
        //TODO
        cardUserGameService.save(cardUserGame);
    }

}
