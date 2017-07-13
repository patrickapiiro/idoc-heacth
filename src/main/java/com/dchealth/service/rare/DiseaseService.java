package com.dchealth.service.rare;

import com.dchealth.VO.DiseasePatInfo;
import com.dchealth.entity.common.RoleVsUser;
import com.dchealth.entity.rare.YunDiseaseList;
import com.dchealth.facade.common.BaseFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.*;

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
     * 获取疾病列表
     * @param name
     * @param dcode
     * @param bw
     * @param xt
     * @return
     */
    @GET
    @Path("list")
    public List<YunDiseaseList> listAllYunDiseaseList(@QueryParam("name") String name,@QueryParam("dcode")String dcode, @QueryParam("bw") String bw, @QueryParam("xt") String xt){

        String hql = "from YunDiseaseList as list where 1=1" ;
        if(null!=name&&!"".equals(name)){
            hql+=" and list.name like '%"+name+"%'" ;
        }
        if(null!=dcode&&!"".equals(dcode)){
            hql+=" and list.name = '"+dcode+"'" ;
        }
        if(null!=bw&&!"".equals(bw)){
            hql+=" and list.bw = '"+bw+"'" ;
        }
        if(null!=xt&&!"".equals(xt)){
            hql+=" and list.bw = '"+xt+"'" ;
        }

        return baseFacade.createQuery(YunDiseaseList.class,hql,new ArrayList<Object>()).getResultList() ;
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
        try {
            YunDiseaseList merge = baseFacade.merge(yunDiseaseList);
            Response build = Response.status(Response.Status.OK).entity(merge).build();
            return build;
        } catch (Exception e) {
            e.printStackTrace();
            throw e ;
        }
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


    /**
     * 获取当前医生研究疾病的病例信息
     * @param doctorId
     * @return
     */
    @GET
    @Path("list-doctor-pat-info")
    public List<DiseasePatInfo> listDiseasePatInfo(@QueryParam("doctorId") String doctorId){
        List<DiseasePatInfo> diseasePatInfos = new ArrayList<>() ;
        Set<String> diseaseSet = getManageDisease(doctorId);
        String hql="select ydl from YunUserDisease yud ,YunDiseaseList ydl where ydl.dcode=yud.dcode "+
                    " and yud.userId='"+doctorId+"'" ;
        List<YunDiseaseList>  yunDiseaseLists = baseFacade.createQuery(YunDiseaseList.class, hql, new ArrayList<Object>()).getResultList();
        for(YunDiseaseList list:yunDiseaseLists){
            DiseasePatInfo diseasePatInfo = new DiseasePatInfo(list,Long.parseLong("0"),Long.parseLong("0"));
            Long patNumber = getPatNumber(doctorId, list.getDcode(),diseaseSet);
            diseasePatInfo.setPatNumber(patNumber);
            diseasePatInfo.setFollowNumber(getPatFollowUp(doctorId,list.getDcode(),diseaseSet));
            diseasePatInfos.add(diseasePatInfo);
        }
        return diseasePatInfos;
    }

    /**
     * 获取某个疾病当月的随访数
     * @param doctorId
     * @param dcode
     * @return
     */
    private Long getPatFollowUp(String doctorId, String dcode,Set diseaseSet) {
        String hql = "select count(*) from YunFollowUp as f,YunPatient as p  where YEAR(f.followDate)=YEAR(current_date()) and " +
                " MONTH(f.followDate)=MONTH(current_date()) and f.hstatus='R' and f.patientId=p.id" +
                " and f.dcode='"+dcode+"' ";
        if(diseaseSet==null || !diseaseSet.contains(dcode)){
            hql += " and p.doctorId='"+doctorId+"'" ;
        }
        return baseFacade.createQuery(Long.class,hql,new ArrayList<Object>()).getSingleResult();
    }

    /**
     * 获取某个研究院的某个疾病病人
     * @param doctorId
     * @param dcode
     * @return
     */
    private Long getPatNumber(String doctorId, String dcode,Set diseaseSet) {
        String hql = "select count(*) from YunFolder as yf ,YunPatient as yp where yf.patientId=yp.id " +
                     " and yf.diagnosisCode='"+dcode+"' " ;
        if(diseaseSet==null || !diseaseSet.contains(dcode)){
            hql += " and yp.doctorId='"+doctorId+"'";
        }
        return baseFacade.createQuery(Long.class,hql,new ArrayList<Object>()).getSingleResult();
    }

    public Set getManageDisease(String doctorId){
        Set<String> set = new HashSet<>();
        String hql = "select ydl from YunUserDiseaseManager yud ,YunDiseaseList ydl where ydl.dcode=yud.dcode "+
                " and yud.userId='"+doctorId+"'" ;
        List<YunDiseaseList> yunDiseaseLists = baseFacade.createQuery(YunDiseaseList.class, hql, new ArrayList<Object>()).getResultList();
        for(YunDiseaseList yunDiseaseList:yunDiseaseLists){
            set.add(yunDiseaseList.getDcode());
        }
        return set;
    }
}
