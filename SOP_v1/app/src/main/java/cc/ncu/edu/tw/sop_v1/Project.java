package cc.ncu.edu.tw.sop_v1;

/**
 * Created by jason on 2016/5/29.
 */
public class Project
{
    private int position;     //在listview中的第幾個位置
    private int id;          //在後端的id
    private String content; //project的內容

    public Project(int p,int i,String c)
    {
        position = p;
        id = i;
        content =c ;
    }

    public String getProjectContent(){return content;}
    public int getProjectId(){return id;}
    public void setProjectId(int pid){id =pid;}
    public void setContent(String s ){content = s;}
}
