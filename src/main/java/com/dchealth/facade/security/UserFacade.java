package com.dchealth.facade.security;

import com.dchealth.entity.YunUsers;
import com.dchealth.facade.common.BaseFacade;
import org.apache.commons.collections.ArrayStack;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/6/6.
 */
@Component
public class UserFacade extends BaseFacade {

    public YunUsers getYunUsersByUserId(String userName) throws Exception {
        String hql = "from YunUsers as user where user.userId='"+userName+"'";
        List<YunUsers> resultList = createQuery(YunUsers.class, hql, new ArrayList<>()).getResultList();
        if(resultList.size()>0){
            return resultList.get(0);
        }else{
            throw new Exception("不存在的用户名");
        }
    }


    /**
     * 根据ID获取用户信息
     * @param id
     * @return
     * @throws Exception
     */
    public YunUsers getYunUserById(long id) throws Exception {
        String hql = "from YunUsers as user where user.id="+id;
        List<YunUsers> resultList = createQuery(YunUsers.class, hql, new ArrayList<>()).getResultList();
        if(resultList.size()>0){
            return resultList.get(0);
        }else{
            throw new Exception("不存在的用户名");
        }
    }
}
