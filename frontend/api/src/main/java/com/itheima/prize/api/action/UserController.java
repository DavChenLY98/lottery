package com.itheima.prize.api.action;

import com.itheima.prize.commons.db.entity.CardUser;
import com.itheima.prize.commons.db.entity.CardUserDto;
import com.itheima.prize.commons.db.entity.ViewCardUserHit;
import com.itheima.prize.commons.db.service.CardUserService;
import com.itheima.prize.commons.db.service.GameLoadService;
import com.itheima.prize.commons.db.service.ViewCardUserHitService;
import com.itheima.prize.commons.utils.ApiResult;
import com.itheima.prize.commons.utils.PageBean;
import com.itheima.prize.commons.utils.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping(value = "/api/user")
@Api(tags = {"用户模块"})
public class UserController {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private ViewCardUserHitService viewCardUserHitService;
    @Autowired
    private GameLoadService loadService;
    @Autowired
    private CardUserService cardUserService;

    @GetMapping("/info")
    @ApiOperation(value = "用户信息")
    public ApiResult info(HttpServletRequest request) {
        //获取session
        HttpSession session = request.getSession();
        //通过session获取用户信息
        CardUser cardUser = (CardUser) session.getAttribute("user");
        if (cardUser == null) {
            return new ApiResult(0, "登陆超时", null);
        } else {
            CardUserDto cardUserDto = loadService.cardUserDTO(cardUser);
            return new ApiResult(1, "成功", cardUserDto);
        }

    }

    @GetMapping("/hit/{gameid}/{curpage}/{limit}")
    @ApiOperation(value = "我的奖品")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "gameid", value = "活动id（-1=全部）", dataType = "int", example = "1", required = true),
            @ApiImplicitParam(name = "curpage", value = "第几页", defaultValue = "1", dataType = "int", example = "1"),
            @ApiImplicitParam(name = "limit", value = "每页条数", defaultValue = "10", dataType = "int", example = "3")
    })
    public ApiResult hit(@PathVariable int gameid, @PathVariable int curpage, @PathVariable int limit, HttpServletRequest request) {
        HttpSession session = request.getSession();
        CardUser cardUser = (CardUser) session.getAttribute("user");
        PageBean<ViewCardUserHit> viewCardUserHitPageBean =
                viewCardUserHitService.getPageBeam(gameid, curpage, limit, cardUser);
        return new ApiResult(1, "成功", viewCardUserHitPageBean);

    }


}