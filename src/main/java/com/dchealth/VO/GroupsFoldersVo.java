package com.dchealth.VO;

public class GroupsFoldersVo {

    private String researchGroupName;
    private long num;

    public GroupsFoldersVo(String researchGroupName, long num) {
        this.researchGroupName = researchGroupName;
        this.num = num;
    }

    public GroupsFoldersVo() {
    }

    public String getResearchGroupName() {
        return researchGroupName;
    }

    public void setResearchGroupName(String researchGroupName) {
        this.researchGroupName = researchGroupName;
    }

    public long getNum() {
        return num;
    }

    public void setNum(long num) {
        this.num = num;
    }
}
