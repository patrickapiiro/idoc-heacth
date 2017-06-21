package com.dchealth.VO;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/6/21.
 */
public class Form {
    private List<FormData> form_data = new ArrayList<>() ;

    public List<FormData> getForm_data() {
        return form_data;
    }

    public void setForm_data(List<FormData> form_data) {
        this.form_data = form_data;
    }
}
