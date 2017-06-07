package com.dchealth.service;

import com.dchealth.entity.YunUsers;
import com.dchealth.util.UserUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Controller;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Created by Administrator on 2017/6/6.
 */
@Path("login")
@Produces("application/json")
@Controller
public class LoginService {
    @Context
    private HttpServletRequest httpServletRequest;
    @Context
    private HttpServletResponse httpServletResponse;

    @GET
    public void login() throws IOException, ServletException {
        httpServletResponse.sendRedirect("../index.html");
    }

    @GET
    @Path("success")
    public YunUsers loinSuccess() throws Exception {
      YunUsers yunUsers = UserUtils.getYunUsers();
      return yunUsers;
    }

    /**
     * 登录成功后返回当前用户
     * @param username
     * @param password
     * @return
     * @throws Exception
     */
    @POST
    public Response loign(@QueryParam("username") String username, @QueryParam("password") String password) throws Exception {
        String exceptionClassName = (String) httpServletRequest.getAttribute("shiroLoginFailure");
        if (exceptionClassName != null) {
            if (UnknownAccountException.class.getName().equals(exceptionClassName)) {
                //最终会抛给异常处理器
                throw new Exception("账号不存在");
            } else if (IncorrectCredentialsException.class.getName().equals(exceptionClassName)) {
                throw new Exception("用户名/密码错误");
            } else if ("randomCodeError".equals(exceptionClassName)) {
                throw new Exception("验证码错误 ");
            } else {
                throw new Exception();//最终在异常处理器生成未知错误
            }
        }
        YunUsers yunUsers = UserUtils.getYunUsers();
        return Response.status(Response.Status.OK).entity(yunUsers).build();
    }
}
