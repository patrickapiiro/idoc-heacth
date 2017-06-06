package com.dchealth.service;

import com.dchealth.entity.YunUsers;
import com.dchealth.facade.common.BaseFacade;
import com.dchealth.facade.security.UserFacade;
import com.dchealth.security.PasswordAndSalt;
import com.dchealth.security.SystemPasswordService;
import com.mysql.cj.x.protobuf.Mysqlx;
import org.jboss.logging.annotations.Pos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
     * 修改用户信息
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

}
