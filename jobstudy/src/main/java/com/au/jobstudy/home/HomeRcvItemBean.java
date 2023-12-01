package com.au.jobstudy.home;

/**
 * @author allan.jiang
 * @date :2023/12/1 16:46
 * @description:
 */
public class HomeRcvItemBean extends HomeRcvBean{
    public HomeRcvItemBean(int viewType, int colorId, String subject, String desc) {
        super(viewType);
        this.colorId = colorId;
        this.subject = subject;
        this.desc = desc;
    }

    public int colorId;
    public String subject;
    public String desc;
}
