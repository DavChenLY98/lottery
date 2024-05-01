package com.itheima.prize.commons.db.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.prize.commons.db.entity.CardUser;
import com.itheima.prize.commons.db.entity.ViewCardUserHit;
import com.itheima.prize.commons.db.mapper.ViewCardUserHitMapper;
import com.itheima.prize.commons.db.service.ViewCardUserHitService;
import com.itheima.prize.commons.utils.PageBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author shawn
* @description 针对表【view_card_user_hit】的数据库操作Service实现
* @createDate 2023-12-26 11:58:48
*/
@Service
public class ViewCardUserHitServiceImpl extends ServiceImpl<ViewCardUserHitMapper, ViewCardUserHit>
    implements ViewCardUserHitService{


    @Autowired
    private ViewCardUserHitMapper viewCardUserHitMapper;

    @Override
    public PageBean<ViewCardUserHit> getPageBeam(int gameid, int curpage, int limit, CardUser cardUser) {
        QueryWrapper<ViewCardUserHit> hitMsg=new QueryWrapper<ViewCardUserHit>()
                .eq("userid",cardUser.getId());
        if(gameid!=-1){
            hitMsg.eq("gameid",gameid);
        }
        List<ViewCardUserHit> viewCardUserHits = viewCardUserHitMapper.selectList(hitMsg);
        PageBean<ViewCardUserHit> viewCardUserHitPageBean =
                new PageBean<>(curpage, limit, 0, viewCardUserHits);
        return viewCardUserHitPageBean;
    }
}




