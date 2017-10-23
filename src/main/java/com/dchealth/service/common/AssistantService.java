package com.dchealth.service.common;

import com.dchealth.entity.common.ResearchAssistant;
import com.dchealth.entity.common.YunUsers;
import com.dchealth.facade.common.BaseFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Produces("application/json")
@Path("assistant")
@Controller
public class AssistantService {

    @Autowired
    private BaseFacade baseFacade;

    /**
     * 添加一个人成为研究助手
     * @param researchAssistant
     * @return
     */
    @POST
    @Path("add-assistant")
    @Transactional
    public Response addAssistant(ResearchAssistant researchAssistant){
        ResearchAssistant merge = baseFacade.merge(researchAssistant);
        return Response.status(Response.Status.OK).entity(merge).build();
    }

    /**
     * 移除一个研究助手
     * @param researchAssistant
     * @return
     */
    @POST
    @Path("remove-assistant")
    @Transactional
    public Response removeAssistant(ResearchAssistant researchAssistant){
        baseFacade.remove(researchAssistant);
        return Response.status(Response.Status.OK).entity(researchAssistant).build();
    }

    /**
     * 获取研究助手列表
     * @param userId
     * @return
     */
    @GET
    @Path("get-assistants")
    public List<YunUsers> getAssistants(@QueryParam("userId") String userId){
        String hql=" from YunUsers where id in " +
                "(select assistant from ResearchAssistant where userId='"+userId+"')";
        List<YunUsers> yunUsersList = baseFacade.createQuery(YunUsers.class, hql, new ArrayList<>()).getResultList();
        return yunUsersList;
    }
}
