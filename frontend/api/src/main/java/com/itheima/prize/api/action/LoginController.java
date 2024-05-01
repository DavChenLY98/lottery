package com.itheima.prize.api.action;

import com.itheima.prize.commons.config.RedisKeys;
import com.itheima.prize.commons.db.entity.CardUser;
import com.itheima.prize.commons.db.service.CardUserService;
import com.itheima.prize.commons.utils.ApiResult;
import com.itheima.prize.commons.utils.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping(value = "/api")
@Api(tags = {"登录模块"})
public class LoginController {
    @Autowired
    private CardUserService userService;

    @Autowired
    private RedisUtil redisUtil;

    @PostMapping("/login")
    @ApiOperation(value = "登录")
    @ApiImplicitParams({
            @ApiImplicitParam(name="account",value = "用户名",required = true),
            @ApiImplicitParam(name="password",value = "密码",required = true)
    })
    public ApiResult login(HttpServletRequest request, @RequestParam String account,@RequestParam String password) {
        //redis中的key值
        String key= RedisKeys.USERLOGINTIMES+account;
        Integer count=(Integer) redisUtil.get(key);
        if(count!=null&&count>=5){
            return new ApiResult(0,"密码错误5次，请5分钟后再登录",null);
        }
        //去数据库根据用户名和密码查询信息
        CardUser cardUser=userService.judgeByAccountAndPassWord(account,password);
        //判断查询结果是否为空？
        if(cardUser!=null){
            //如果不为空，则提取信息并创建session
            cardUser.setPasswd(null);
            cardUser.setIdcard(null);
            HttpSession session = request.getSession();
            session.setAttribute("user",cardUser);

            return new ApiResult(1,"登录成功",cardUser);
        }else{
            redisUtil.incr(key,1L);
            if(count!=null&&count>=4){
                redisUtil.expire(key,5*60);
            }
            return new ApiResult(0,"账户名或密码错误",null);
        }
    }

    @GetMapping("/logout")
    @ApiOperation(value = "退出")
    public ApiResult logout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        if(session!=null){
            session.invalidate();
        }
        return new ApiResult(1,"退出成功",null);
    }

}