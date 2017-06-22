package com.dchealth.service.rare;

import com.dchealth.entity.common.HospitalDict;
import com.dchealth.facade.common.BaseFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Created by Administrator on 2017/6/16.
 */

@Produces("application/json")
@Path("hospital")
@Controller
public class HospitalService {


    @Autowired
    private BaseFacade baseFacade;

    /**
     * 获取所有的医院
     *
     * @return
     */
    @GET
    public List<HospitalDict> listAllHospitalDict() {
        List<HospitalDict> hospitalDicts = baseFacade.findAll(HospitalDict.class);
        return hospitalDicts;

    }


    /**
     * 修改医院信息
     *
     * @param hospitalDict
     * @return
     */
    @POST
    @Transactional
    public Response mergeHospitalDict(HospitalDict hospitalDict) {
        HospitalDict mergeHospitalDict = baseFacade.merge(hospitalDict);
        return Response.status(Response.Status.OK).entity(mergeHospitalDict).build();
    }

}

