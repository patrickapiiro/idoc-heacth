package com.dchealth.service;

import com.dchealth.entity.YunDiseaseList;
import com.dchealth.entity.YunUserDisease;
import com.dchealth.facade.common.BaseFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/6/16.
 */
@Controller
@Produces("application/json")
@Path("disease")
public class DiseaseService {

    @Autowired
    private BaseFacade baseFacade ;

    /**
     * 获取所有的用户列表
     * @return
     */
    @GET
    @Path("list")
    public List<YunDiseaseList> listAllYunDiseaseList(){
        return baseFacade.findAll(YunDiseaseList.class) ;
    }


    /**
     * 创建新的疾病信息
     * @param yunDiseaseList
     * @return
     */
    @POST
    @Path("add-new")
    @Transactional
    public Response mergeYunDiseaseList(YunDiseaseList yunDiseaseList){
        String hql = "select max(a.id) from YunDiseaseList a " ;
        long id = 0 ;
        List<Integer> resultList = baseFacade.createQuery(Integer.class, hql, new ArrayList<Object>()).getResultList();
        if(resultList.size()==0){
            id=0;
        }else{
            id=resultList.get(0)+1;
        }
        yunDiseaseList.setId(id);
        YunDiseaseList merge = baseFacade.merge(yunDiseaseList);
        return Response.status(Response.Status.OK).entity(merge).build();
    }

    /**
     * 修改疾病列表
     * @param yunDiseaseList
     * @return
     */
    @Transactional
    @Path("update")
    @POST
    public Response updateYunDiseaseList(YunDiseaseList yunDiseaseList){
        YunDiseaseList merge = baseFacade.merge(yunDiseaseList);
        return Response.status(Response.Status.OK).entity(merge).build();
    }


}
