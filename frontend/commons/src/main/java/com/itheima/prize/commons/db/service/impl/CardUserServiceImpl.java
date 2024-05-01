package com.itheima.prize.commons.db.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.prize.commons.db.entity.CardUser;
import com.itheima.prize.commons.db.entity.CardUserDto;
import com.itheima.prize.commons.db.entity.CardUserGame;
import com.itheima.prize.commons.db.entity.CardUserHit;
import com.itheima.prize.commons.db.mapper.CardUserGameMapper;
import com.itheima.prize.commons.db.mapper.CardUserHitMapper;
import com.itheima.prize.commons.db.mapper.CardUserMapper;
import com.itheima.prize.commons.db.service.CardUserService;
import com.itheima.prize.commons.utils.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
* @author shawn
* @description 针对表【card_user(会员信息表)】的数据库操作Service实现
* @createDate 2023-12-26 11:58:48
*/
@Service
public class CardUserServiceImpl extends ServiceImpl<CardUserMapper, CardUser>
    implements CardUserService{

    @Autowired
    private CardUserMapper cardUserMapper;
    @Autowired
    private CardUserGameMapper cardUserGameMapper;
    @Autowired
    private CardUserHitMapper cardUserHitMapper;


    @Override
    public CardUser judgeByAccountAndPassWord(String account, String password) {
        //将输入的密码进行md5加密
        String newPassWord= PasswordUtil.encodePassword(password);
        //创建QueryWrapper进行用户名与密码的匹配
        QueryWrapper<CardUser> cardUserQueryWrapper = new QueryWrapper<CardUser>()
                .eq("uname",account)
                .eq("passwd",newPassWord);
        //找出符合条件的cardUser对象
        CardUser cardUser = cardUserMapper.selectOne(cardUserQueryWrapper);
        return cardUser;
    }

    @Override
    public CardUserDto cardUserDTO(CardUser cardUser) {
        //定义通过用户id查询参与活动数量的匹配sql
        QueryWrapper<CardUserGame> gamesqueryWrapper=new QueryWrapper<CardUserGame>()
                .eq("userid",cardUser.getId());
        //根据匹配条件查询用户参与活动数量，这里mybatisplus进行了更新，selectCount方法返回值
        //更改为Long类型，因此需要做数据类型转换
        int games = cardUserGameMapper.selectCount(gamesqueryWrapper).intValue();
        //同上，设定根据用户id查询中奖数量的信息
        QueryWrapper<CardUserHit> hitsqueryWrapper = new QueryWrapper<CardUserHit>()
                .eq("userid",cardUser.getId());
        int hits = cardUserHitMapper.selectCount(hitsqueryWrapper).intValue();
        //创建cardUserDTO对象，并将相应的数据赋值给该对象
        CardUserDto cardUserDto=new CardUserDto(cardUser);
        cardUserDto.setGames(games);
        cardUserDto.setProducts(hits);
        return cardUserDto;
    }
}




