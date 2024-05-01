package com.itheima.prize.commons.db.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.prize.commons.db.entity.CardGame;
import com.itheima.prize.commons.utils.PageBean;

import java.util.List;

/**
* @author shawn
* @description 针对表【card_game】的数据库操作Service
* @createDate 2023-12-26 11:58:48
*/
public interface CardGameService extends IService<CardGame> {


    void getCard(List<CardGame> cardGameList);

    PageBean getCardGame(int status, int curpage, int limit);

    CardGame getCardGameMsg(int gameid);
}
