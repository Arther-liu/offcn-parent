package com.offcn.user.controller;

import com.netflix.discovery.converters.Auto;
import com.offcn.dycommon.response.AppResponse;
import com.offcn.user.component.SmsTemplate;
import com.offcn.user.po.TMember;
import com.offcn.user.service.UserService;
import com.offcn.user.vo.req.UserRegistVo;
import com.offcn.user.vo.resp.UserRespVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Api(tags = "用户登录注册模块")
@RestController
@RequestMapping("/user")
@Slf4j
public class UserLoginController {
    @Autowired
    private SmsTemplate smsTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    UserService userService;

    @ApiOperation(value = "获取短信验证码")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "手机号码", name = "phoneNo", required = true)
    })
    @GetMapping("/sendCode")
    public AppResponse<Object> sendCode(String phoneNo) {
        UUID uuid = UUID.randomUUID();
        //1、生成验证码保存到服务器，准备用户提交上来进行对比
        String code = uuid.toString().substring(0, 6);
        //2、保存验证码和手机号的对应关系,设置验证码过期时间
        stringRedisTemplate.opsForValue().set(phoneNo, code, 60 * 60 * 24 * 7, TimeUnit.SECONDS);
        //3、短信发送构造参数
        Map<String, String> querys = new HashMap<>();
        querys.put("mobile", phoneNo);
        querys.put("param", "code:" + code);
        querys.put("tpl_id", "TP1711063");
        //开始发短信
        String s = smsTemplate.sendCode(querys);
        if (s.equals("fail")) {
            return AppResponse.fail("短信发送失败");
        }
        return AppResponse.ok(s);
    }

    //用户注册
    @ApiOperation("用户注册")
    @PostMapping("/regist")
    public AppResponse<Object> regist(UserRegistVo registVo) {
        //1、校验验证码
        String code = stringRedisTemplate.opsForValue().get(registVo.getLoginacct());
        if (!StringUtils.isEmpty(code)) {
            //redis中有验证码
            boolean b = code.equalsIgnoreCase(registVo.getCode());
            if (b) {
                //2、将vo转业务能用的数据对象
                TMember member = new TMember();
                BeanUtils.copyProperties(registVo, member);
                //3、将用户信息注册到数据库
                try {
                    userService.registerUser(member);
                    log.debug("用户信息注册成功：{}", member.getLoginacct());
                    //4、注册成功后，删除验证码
                    stringRedisTemplate.delete(registVo.getLoginacct());
                    return AppResponse.ok("注册成功...");
                } catch (Exception e) {
                    log.error("用户信息注册失败：{}", member.getLoginacct());
                    return AppResponse.fail(e.getMessage());
                }
            } else {
                return AppResponse.fail("验证码错误");
            }
        } else {
            return AppResponse.fail("验证码过期，请重新获取");
        }

    }

    @ApiOperation("用户登录")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "用户名",name = "username",required = true),
            @ApiImplicitParam(value = "密码",name = "password",required = true)
    })
    @PostMapping("/login")
    public AppResponse<UserRespVo> login(String username,String password){
        //1.尝试登录
        TMember member = userService.login(username,password);
        if(member==null){//登录失败
            AppResponse<UserRespVo> fail = AppResponse.fail(null);
            fail.setMsg("用户名或密码错误");
            return fail;
        }
        //2.登陆成功，生成令牌
        String token = UUID.randomUUID().toString().replace("-","");
        UserRespVo vo = new UserRespVo();
        BeanUtils.copyProperties(member,vo);//将member中的相关数据拷贝到vo对象中，避免了getset方法的使用
        vo.setAccessToken(token);
        //3.经常根据令牌查询用户的id信息
        stringRedisTemplate.opsForValue().set(token,member.getId()+"",10,TimeUnit.DAYS);//令牌和用户id对应
        return AppResponse.ok(vo);
    }

    @ApiOperation("根据用户id查询")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "用户id",name = "id",required = true),
    })
    @GetMapping("/findUser/{id}")
    public AppResponse<UserRespVo> findUser(@PathVariable("id")Integer id){
        TMember member = userService.findMemberById(id);
        UserRespVo userRespVo = new UserRespVo();
        BeanUtils.copyProperties(member,userRespVo);
        return AppResponse.ok(userRespVo);
    }
}
