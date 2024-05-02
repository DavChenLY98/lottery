package com.itheima.prize.commons.db.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.prize.commons.db.entity.CardGame;
import com.itheima.prize.commons.db.mapper.CardGameMapper;
import com.itheima.prize.commons.db.service.CardGameService;
import com.itheima.prize.commons.utils.PageBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author shawn
* @description 针对表【card_game】的数据库操作Service实现
* @createDate 2023-12-26 11:58:48
*/
@Service
public class CardGameServiceImpl extends ServiceImpl<CardGameMapper, CardGame>
    implements CardGameService{

    @Autowired
    private CardGameMapper cardGameMapper;


    /**
     * 根据状态筛选活动列表
     * @param status
     * @param curpage
     * @param limit
     * @return
     */
    @Override
    public PageBean getCardGame(int status, int curpage, int limit) {
        QueryWrapper<CardGame> cardgameQueryWrapper=new QueryWrapper<CardGame>();
        if(status!=-1){
            cardgameQueryWrapper.eq("status",status);
        }
        Page<CardGame> page= Page.of(curpage,limit);
        Page<CardGame> pageRes = page(page, cardgameQueryWrapper);
        return new PageBean<CardGame>(pageRes);
    }

    /**
     * 根据活动id获取活动信息
     * @param gameid
     * @return
     */
    @Override
    public CardGame getCardGameMsg(int gameid) {
        QueryWrapper<CardGame> gameidQueryWrapper = new QueryWrapper<CardGame>()
                .eq("id", gameid);
        CardGame cardGame = cardGameMapper.selectOne(gameidQueryWrapper);
        return cardGame;
    }


}




