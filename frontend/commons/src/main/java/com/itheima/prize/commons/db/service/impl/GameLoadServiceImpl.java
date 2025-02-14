package com.itheima.prize.commons.db.service.impl;

import com.itheima.prize.commons.db.entity.CardProductDto;
import com.itheima.prize.commons.db.entity.CardUser;
import com.itheima.prize.commons.db.entity.CardUserDto;
import com.itheima.prize.commons.db.mapper.GameLoadMapper;
import com.itheima.prize.commons.db.service.GameLoadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameLoadServiceImpl implements GameLoadService {
    @Autowired
    private GameLoadMapper loadMapper;

    @Override
    public List<CardProductDto> getByGameId(int gameId) {
        return loadMapper.getByGameId(gameId);
    }

    @Override
    public Integer getGamesNumByUserId(int userid) {
        return loadMapper.getGamesNumByUserId(userid);
    }

    @Override
    public Integer getPrizesNumByUserId(int userid) {
        return loadMapper.getPrizesNumByUserId(userid);
    }

    @Override
    public CardUserDto cardUserDTO(CardUser cardUser) {
        Integer gamesNumByUserId = getGamesNumByUserId(cardUser.getId());
        Integer prizesNumByUserId = getPrizesNumByUserId(cardUser.getId());
        CardUserDto cardUserDto=new CardUserDto(cardUser);
        cardUserDto.setGames(gamesNumByUserId);
        cardUserDto.setProducts(prizesNumByUserId);
        return cardUserDto;

    }
}
