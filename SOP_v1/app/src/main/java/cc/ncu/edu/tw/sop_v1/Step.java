package cc.ncu.edu.tw.sop_v1;

import java.io.Serializable;

/**
 * Created by jason on 2016/5/29.
 */
public class Step implements Serializable
{
    //存放步驟顯示位置的資料
    //private boolean parent;  //是否為父層
    private int layer=0;        //為第幾層
    private int sequence;    //在層中的順序
    private String content;
    private int belong;     //所屬的project id
    private int id;         //該步驟在後端的id

    private boolean exist;
/*=================================================================*/
    //存放步驟內容的資料
    private String item;
    private String unit;
    private String person;
    private String place;


    public Step(int l,int s,String c,int b,int i)
    {
        //parent = p;
        layer = l;
        sequence = s;
        content = c;
        belong =b;
        id = i;
        exist = true;
    }

    public int getSequence() {return sequence;}
    public void setSequence(int i){sequence+=i;}

    public int getLayer() {return layer;}
    public void setLayer(int j){layer+=j;}

    public String getContent() {return content;}
    public void setContent(String item,String unit,String person,String place)
    {
        this.item = item;
        this.unit = unit;
        this.person = person;
        this.place = place;
    }

    public int getBelong(){return belong;}

    public int getId(){return id;}
    public void setId(int i){id=i;}

    public String getItem(){return item;}
    public void setItem(String s){item=s;}

    public String getUnit(){return unit;}
    public String getPerson(){return person;}
    public String getPlace(){return place;}

    public boolean getExist(){return exist;}
    public void setExist(){exist=false;}

}