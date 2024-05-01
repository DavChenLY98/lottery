package com.itheima.prize.commons.db.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.prize.commons.db.entity.CardUser;
import com.itheima.prize.commons.db.entity.ViewCardUserHit;
import com.itheima.prize.commons.db.mapper.ViewCardUserHitMapper;
import com.itheima.prize.commons.db.service.ViewCardUserHitService;
import com.itheima.prize.commons.utils.PageBean;
import org.springframework.stereotype.Service;

/**
* @author shawn
* @description 针对表【view_card_user_hit】的数据库操作Service实现
* @createDate 2023-12-26 11:58:48
*/
@Service
public class ViewCardUserHitServiceImpl extends ServiceImpl<ViewCardUserHitMapper, ViewCardUserHit>
    implements ViewCardUserHitService{


    /**
     * 用户根据活动id获取用户中奖信息
     * @param gameid
     * @param curpage
     * @param limit
     * @param cardUser
     * @return
     */
    @Override
    public PageBean<ViewCardUserHit> getPageBeam(int gameid, int curpage, int limit, CardUser cardUser) {
        QueryWrapper<ViewCardUserHit> hitMsg=new QueryWrapper<ViewCardUserHit>()
                .eq("userid",cardUser.getId());
        if(gameid!=-1){
            hitMsg.eq("gameid",gameid);
        }
        Page<ViewCardUserHit> page=Page.of(curpage,limit);
        Page<ViewCardUserHit> p = page(page, hitMsg);
        return new PageBean<>(p);
    }

    /**
     * 用于获取制定活动的中奖信息
     * @param gameid
     * @param curpage
     * @param limit
     * @return
     */
    @Override
    public PageBean getPageBeamCardGameList(int gameid, int curpage, int limit) {
        QueryWrapper<ViewCardUserHit> hitMsg=new QueryWrapper<ViewCardUserHit>()
                .eq("gameid",gameid);
        Page<ViewCardUserHit> page=Page.of(curpage,limit);
        Page<ViewCardUserHit> p = page(page, hitMsg);
        return new PageBean<ViewCardUserHit>(page);

    }
}




