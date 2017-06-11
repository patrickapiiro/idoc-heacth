package com.dchealth.service;

import com.dchealth.entity.YunUsers;
import com.dchealth.facade.common.BaseFacade;
import com.dchealth.facade.security.UserFacade;
import com.dchealth.security.PasswordAndSalt;
import com.dchealth.security.SystemPasswordService;
import com.dchealth.util.UserUtils;
import com.mysql.cj.x.protobuf.Mysqlx;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.jboss.logging.annotations.Pos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Date;

/**
 * Created by Administrator on 2017/6/6.
 */
@Controller
@Produces("application/json")
@Path("yun-user")
public class YunUserService {

    @Autowired
    private UserFacade userFacade ;

    /**
     * 注册用户信息
     * @param yunUsers
     * @return
     */
    @Path("regist")
    @POST
    @Transactional
    public Response registYunUser(YunUsers yunUsers){

        long id = new Date().getTime();
        yunUsers.setId(id);
        PasswordAndSalt passwordAndSalt = SystemPasswordService.enscriptPassword(yunUsers.getUserId(), yunUsers.getPassword());
        yunUsers.setPassword(passwordAndSalt.getPassword());
        yunUsers.setSalt(passwordAndSalt.getSalt());
        return Response.status(Response.Status.OK).entity(userFacade.merge(yunUsers)).build();
    }


    /**
     * 更新用户
     * @param yunUsers
     * @return
     */
    @POST
    @Transactional
    @Path("update")
    public Response updateYunUser(YunUsers yunUsers) throws Exception {
        long id = yunUsers.getId();
        if(id==0){
            throw new Exception("获取不到原信息的ID");
        }
        return Response.status(Response.Status.OK).entity(userFacade.merge(yunUsers)).build();
    }

    /**
     * 修改密码
     * @param oldPassword
     * @param newPassowrd
     * @param userId
     * @return
     */
    @POST
    @Transactional
    @Path("change-pwd")
    public Response chagePassword(@QueryParam("oldPassword") String oldPassword, @QueryParam("newPassword") String newPassowrd,
                                  @QueryParam("userId") String userId) throws Exception {
        YunUsers yunUsers = userFacade.getYunUsersByUserId(userId);
        String passwordWithSalt = SystemPasswordService.enscriptPasswordWithSalt(yunUsers.getSalt(), userId, oldPassword);
        String oldDbPassword = yunUsers.getPassword() ;
        if(passwordWithSalt.equals(oldDbPassword)){
            PasswordAndSalt passwordAndSalt = SystemPasswordService.enscriptPassword(userId, newPassowrd);
            yunUsers.setPassword(passwordAndSalt.getPassword());
            yunUsers.setSalt(passwordAndSalt.getSalt());
            Subject subject = SecurityUtils.getSubject();
            subject.logout();
            return Response.status(Response.Status.OK).entity(userFacade.merge(yunUsers)).build();
        }else{
            throw new Exception("原密码错误！");
        }
    }

    @Produces("text/plain")
    @POST
    @Path("logout")
    public Response logOut(){
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        return Response.status(Response.Status.OK).entity("success").build();
    }
    /**
     * 获取当前登录用户
     * @return
     * @throws Exception
     */
    @GET
    @Path("current-user")
    public YunUsers getCurrentUser() throws Exception {
        YunUsers yunUsers = UserUtils.getYunUsers();
        return yunUsers;
    }

}
