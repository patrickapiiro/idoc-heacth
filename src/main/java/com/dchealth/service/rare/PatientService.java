package com.dchealth.service.rare;

import com.dchealth.VO.Page;
import com.dchealth.entity.rare.YunPatient;
import com.dchealth.facade.common.BaseFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.persistence.TypedQuery;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/6/23.
 */
@Controller
@Path("pat")
@Produces("application/json")
public class PatientService {

    @Autowired
    private BaseFacade baseFacade ;

    /**
     * 获取某个疾病的、某个医生的病人列表
     * @param doctorId
     * @param dcode
     * @param perPage
     * @param currentPage
     * @return
     */
    @GET
    @Path("list-pat")
    public Page<YunPatient> listYunPatient(@QueryParam("doctorId") String doctorId, @QueryParam("dcode") String dcode,
                                           @QueryParam("nc") String nc,@QueryParam("tel2") String tel2,@QueryParam("pid") String pid,
                                           @QueryParam("email")String email,@QueryParam("perPage")int perPage,@QueryParam("currentPage")int currentPage){
        String hql = "select p from YunPatient as p ,YunFolder as f where f.patientId =p.id " ;
        String hqlCount = "select count(p) from YunPatient as p ,YunFolder as f where f.patientId =p.id " ;
        if(!"".equals(dcode)&&null!=dcode){
            hql +=" and f.diagnosisCode='"+dcode+"'" ;
            hqlCount +=" and f.diagnosisCode='"+dcode+"'" ;
        }
        if(!"".equals(doctorId)&&null!=doctorId){
            hql+=" and p.doctorId = '"+doctorId+"'" ;
            hqlCount+=" and p.doctorId = '"+doctorId+"'" ;
        }
        if(!"".equals(nc)&&null!=nc){
            hql+=" and p.nc = '"+nc+"'" ;
            hqlCount+=" and p.nc = '"+nc+"'" ;
        }
        if(!"".equals(tel2)&&null!=tel2){
            hql+=" and p.tel2 = '"+tel2+"'" ;
            hqlCount+=" and p.tel2 = '"+tel2+"'" ;
        }
        if(!"".equals(pid)&&null!=pid){
            hql+=" and p.pid = '"+pid+"'" ;
            hqlCount+=" and p.pid = '"+pid+"'" ;
        }
        if(!"".equals(email)&&null!=email){
            hql+=" and p.email = '"+email+"'" ;
            hqlCount+=" and p.email = '"+email+"'" ;
        }
        hql+=" order by p.id";
        hqlCount+=" order by p.id";
        TypedQuery<YunPatient> baseFacadeQuery = baseFacade.createQuery(YunPatient.class, hql, new ArrayList<Object>());
        Long count = baseFacade.createQuery(Long.class, hqlCount, new ArrayList<Object>()).getSingleResult();
        Page<YunPatient> patientPage= new Page<>();
        patientPage.setCounts(count);
        if(perPage>0){
            baseFacadeQuery.setFirstResult(currentPage*perPage) ;
            baseFacadeQuery.setMaxResults(perPage);
        }
        List<YunPatient> yunPatients = baseFacadeQuery.getResultList();
        patientPage.setData(yunPatients);
        return patientPage;
    }


    /**
     * 获取随诊病人列表
     * @param doctorId 医生ID信息
     * @param followDateBegin 随诊时间范围之开始时间
     * @param followDateEnd   随诊时间范围之结束时间
     * @param remindDateBegin 随诊提醒时间范围之开始时间
     * @param remindDateEnd   随诊提醒时间范围之结束时间
     * @param dcode           疾病代码
     * @param hstatus         随诊状态
     * @param perPage         每页显示数目
     * @param currentPage     当前页
     * @return
     */
    @GET
    @Path("list-follow-pat")
    public Page<YunPatient> listFollowPatient(@QueryParam("doctorId") String doctorId, @QueryParam("followDateBegin") String followDateBegin,
                                              @QueryParam("followDateEnd") String followDateEnd,@QueryParam("remindDateBegin") String remindDateBegin,
                                              @QueryParam("remindDateEnd")String remindDateEnd,@QueryParam("dcode")String dcode,
                                              @QueryParam("hstatus")String hstatus,@QueryParam("perPage")int perPage,@QueryParam("currentPage")int currentPage){
        Page<YunPatient> yunPatientPage = new Page<>();
        String hql = "select p from YunPatient as p ,YunFollowUp as yf where p.id=yf.patientId" ;
        String hqlCount = "select count(p) from YunPatient as p ,YunFollowUp as yf where p.id=yf.patientId" ;
        if(!"".equals(doctorId)&null!=doctorId){
            hql += " and p.doctorId='"+doctorId+"'" ;
            hqlCount += " and p.doctorId='"+doctorId+"'" ;
        }
        if(!"".equals(followDateBegin)&null!=followDateBegin){
            hql += " and to_days(yf.followDate)>=to_days('"+followDateBegin+"')" ;
            hqlCount += " and to_days(yf.followDate)>=to_days('"+followDateBegin+"')" ;
        }
        if(!"".equals(followDateEnd)&null!=followDateEnd){
            hql += " and to_days(yf.followDate)<=to_days('"+followDateEnd+"')" ;
            hqlCount += " and to_days(yf.followDate)<=to_days('"+followDateEnd+"')" ;
        }
        if(!"".equals(remindDateBegin)&null!=remindDateBegin){
            hql += " and to_days(yf.remindDate)>=to_days('"+remindDateBegin+"')" ;
            hqlCount += " and to_days(yf.remindDate)>=to_days('"+remindDateBegin+"')" ;
        }
        if(!"".equals(remindDateEnd)&null!=remindDateEnd){
            hql += " and to_days(yf.remindDate)<=to_days('"+remindDateEnd+"')" ;
            hqlCount += " and to_days(yf.remindDate)<=to_days('"+remindDateEnd+"')" ;
        }

        if(!"".equals(dcode)&null!=dcode){
            hql += " and yf.dcode='"+dcode+"'" ;
            hqlCount += " and yf.dcode='"+dcode+"'" ;
        }

        if(!"".equals(hstatus)&null!=hstatus){
            hql += " and yf.hstatus='"+hstatus+"'" ;
            hqlCount += " and yf.hstatus='"+hstatus+"'" ;
        }
        Long aLong = baseFacade.createQuery(Long.class, hqlCount, new ArrayList<Object>()).getSingleResult();
        TypedQuery<YunPatient> baseFacadeQuery = baseFacade.createQuery(YunPatient.class, hql, new ArrayList<Object>());

        yunPatientPage.setCounts(aLong);
        if(perPage>0){
            baseFacadeQuery.setFirstResult(currentPage*perPage) ;
            baseFacadeQuery.setMaxResults(perPage);
        }
        yunPatientPage.setData(baseFacadeQuery.getResultList());
        return yunPatientPage;
    }


}
