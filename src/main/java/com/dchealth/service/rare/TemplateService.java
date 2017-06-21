package com.dchealth.service.rare;

import com.dchealth.entity.YunDisTemplet;
import com.dchealth.facade.common.BaseFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/6/21.
 */
@Controller
@Produces("application/json")
@Path("tempalte")
public class TemplateService {

    @Autowired
    private BaseFacade baseFacade ;


    /**
     * 获取模板列表
     * @param dcode
     * @param doctorId
     * @param title
     * @param mblx
     * @return
     */
    @GET
    @Path("list")
    public List<YunDisTemplet> getDisTemplate(@QueryParam("dcode") String dcode,@QueryParam("doctorId") String doctorId,
                                              @QueryParam("title")String title,@QueryParam("mblx")String mblx){
        String hql = "from YunDisTemplate as t where 1=1" ;
        //所属疾病
        if(!"".equals(dcode)&&dcode!=null){
            hql+=" and t.dcode='"+dcode+"'" ;
        }
        //所属用户
        if(!"".equals(doctorId)&&doctorId!=null){
            hql+=" and t.doctorId='"+doctorId+"'" ;
        }
        //标题
        if(!"".equals(title)&&title!=null){
            hql+=" and t.title='"+title+"'" ;
        }
        //模板类型
        if(!"".equals(mblx)&&mblx!=null){
            hql+=" and t.mblx='"+mblx+"'" ;
        }

        List<YunDisTemplet> yunDisTemplets = baseFacade.createQuery(YunDisTemplet.class, hql, new ArrayList<Object>()).getResultList();
        return yunDisTemplets;
    }


    /**
     * 添加或者修改模板信息
     * @param yunDisTemplet
     * @return
     */
    @POST
    @Path("merge")
    @Transactional
    public Response mergeYunDisTemplate(YunDisTemplet yunDisTemplet){
        YunDisTemplet merge = baseFacade.merge(yunDisTemplet);
        return Response.status(Response.Status.OK).entity(merge).build();
    }


}
