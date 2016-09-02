package cc.ncu.edu.tw.sop_v1;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddNewOneActivity extends ExpandableListActivity
{
    //建造AppCompat class中的取的actionbar的方法,為了解決繼承ExpandableListActivity不能使用setSupportActionbar()
    private AppCompatDelegate mDelegate;

    //增加ExpandableListView的參數key值
    private static final String ITEM_NAME = "Item Name";
    private static final String ITEM_SUBNAME = "Item Subname";

    private ExpandableListView mExpandableListView = null;

    private Step[] e = new Step[30];
    private Context mContext;

    //執行時存入的list
    private  List<Map<String,Object>> groupList = new ArrayList<>();
    private  List<List<Map<String,String>>> childList2D = new ArrayList<>();

    private Bundle bundle;

    //紀錄  長按按到的groupPosition、childPosition
    private int groupPosition;
    private int childPosition;

    RequestQueue mQueue;

    //紀錄步驟是屬於第幾個project的
    private int Flow_id;
    private String ACCESS_TOKEN;

    //此專案的創建者和目前的使用者是否為同一個人
    private boolean isEditalbe;

    //紀錄該專案中有多少步驟、及索引值
    private int StepCount=0;//為實體化e的個數
    private int index=0;//目前被實體化Step的索引

    int MaxLayer =0;

    private EditText editStepName;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        getDelegate().setContentView(R.layout.activity_add_new_one);
        super.onCreate(savedInstanceState);

        mExpandableListView = getExpandableListView();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionbar(toolbar);
        getDelegate().getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //初始化mQueue
        mQueue = Volley.newRequestQueue(AddNewOneActivity.this);

        //取出從MainActivity中Intent所附帶的Flow_id資料
        Intent it = getIntent();
        bundle = it.getExtras();
        Flow_id = bundle.getInt("Flow_id");
        ACCESS_TOKEN = bundle.getString("Access_token");
        isEditalbe =bundle.getBoolean("isEditable");
        getDelegate().setTitle(bundle.getString("selectProject"));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_add_new_one);

        if(ACCESS_TOKEN.length()!=0 && isEditalbe)
        {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                        final View dialogLayout = LayoutInflater.from(AddNewOneActivity.this).inflate(R.layout.add_step_dialog, null);
                        AlertDialog.Builder builder = new AlertDialog.Builder(AddNewOneActivity.this);
                        builder.setTitle("新增步驟");
                        builder.setView(dialogLayout);
                        builder.setCancelable(false);

                        builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            EditText mEdtStepName;

                            public void onClick(DialogInterface dialog, int id)
                            {
                                mEdtStepName = (EditText) ((AlertDialog) dialog).findViewById(R.id.edtStepName);

                                StepCount++;


                                e[index] = new Step(groupList.size() + 1, 0, mEdtStepName.getText().toString(), Flow_id, 0);
                                //e[index].setContent(mEdtStepExam.getText().toString(), mSelectUnit.getSelectedItem().toString(), mEdtPeople.getText().toString(), mSelectPlace.getSelectedItem().toString());
                                index++;

                                Map<String, Object> group = new HashMap<>();
                                group.put(ITEM_NAME, mEdtStepName.getText().toString());
                                groupList.add(group);

                                List<Map<String, String>> childList = new ArrayList<>();

                                childList2D.add(childList);

                                //設定MyExpandListAdapter
                                initData();

                                //新增步驟到後端(post)
                                StringRequest postRequest = new StringRequest(Request.Method.POST, "http://140.115.3.188:3000/sop/v1/steps", new Response.Listener<String>()
                                {
                                    @Override
                                    public void onResponse(String response)
                                    {
                                        try
                                        {
                                            JSONObject object = new JSONObject(response);
                                            Log.d("postSuccessful", response);
                                            e[StepCount - 1].setId(Integer.parseInt(object.getString("id")));

                                        } catch (JSONException e)
                                        {
                                            e.printStackTrace();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                                    public void onErrorResponse(VolleyError error)
                                    {
                                        Log.e("postErrorHappen", error.getMessage(), error);
                                    }

                                })
                                {
                                    public Map<String, String> getHeaders() throws AuthFailureError
                                    {
                                        Map<String, String> map = new HashMap<>();
                                        map.put("Authorization", "Bearer" + " " + ACCESS_TOKEN);
                                        return map;
                                    }


                                    public Map<String, String> getParams() throws AuthFailureError
                                    {
                                        Map<String, String> map = new HashMap<>();
                                        map.put("action", mEdtStepName.getText().toString());
                                        map.put("items", "");
                                        map.put("prev", Integer.toString(groupList.size()));
                                        map.put("next", Integer.toString(0));
                                        map.put("Flow_id", Integer.toString(Flow_id));
                                        map.put("PersonId", "0");
                                        map.put("UnitId", "A100");
                                        map.put("PlaceId", "1");

                                        return map;
                                    }
                                };

                                mQueue.add(postRequest);
                            }
                        });

                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id)
                            {

                            }
                        });

                        AlertDialog alert = builder.create();
                        alert.show();
                }
            });

        }
        else
        {
            CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
            p.setAnchorId(View.NO_ID);
            fab.setLayoutParams(p);
            fab.setVisibility(View.GONE);
        }

        //從後端載入(get)資料
        StringRequest apiRequest = new StringRequest("http://140.115.3.188:3000/sop/v1/steps", new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.d("API_response", response);
                int k=0;

                try
                {
                    JSONArray array = new JSONArray(response);
                    for(int i=0;i<array.length();i++)
                    {
                        if(Integer.parseInt(array.getJSONObject(i).getString("flow_id")) == Flow_id)
                        {
                            e[index] = new Step(Integer.parseInt(array.getJSONObject(i).getString("prev")),Integer.parseInt(array.getJSONObject(i).getString("next")),array.getJSONObject(i).getString("action"),Integer.parseInt(array.getJSONObject(i).getString("flow_id")),Integer.parseInt(array.getJSONObject(i).getString("id")));

                            //設定MaxLayer
                            if(e[index].getLayer() > MaxLayer)
                            {
                                MaxLayer = e[index].getLayer();
                            }
                            Log.v("This is step",e[index].getContent());
                            StepCount++;
                            index++;
                        }

                    }



                    for(int i=1;i<=MaxLayer;i++)
                    {
                        List<Map<String, String>> childList = new ArrayList<>();
                        childList2D.add(childList);
                    }
                    Log.v("childList2DSize", Integer.toString(childList2D.size()));


                    //載入ExpandableListView選單的文字
                    while(k<StepCount)
                    {
                            Map<String, Object> group = new HashMap<>();

                            //ExpandableListView第一層
                            if (e[k].getSequence() == 0)
                            {
                                group.put(ITEM_NAME, e[k].getContent());
                                groupList.add(group);
                                k++;
                            }
                            else
                            {
                                //ExpandableListView第二層
                                Map<String, String> child = new HashMap<>();

                                child.put(ITEM_SUBNAME, e[k].getContent());

                                childList2D.get(e[k].getLayer()-1).add(child);
                                k++;
                            }
                    }
                    Log.v("groupListSize", Integer.toString(groupList.size()));
                    for(int n=0;n<groupList.size();n++)
                    {
                        Log.v("childList"+Integer.toString(n)+"Size",Integer.toString(childList2D.get(n).size()));
                    }


                    //調整ChildList 、 GroupList 中的順序
                    int n=0;
                    while(n<StepCount)
                    {
                        //調整GroupList 內的順序
                        if(e[n].getSequence()==0)
                        {
                            Map<String, Object> group = new HashMap<>();
                            group.put(ITEM_NAME,e[n].getContent());
                            groupList.set(e[n].getLayer()-1,group);

                        }
                        //調整ChildList內的順序
                        else
                        {
                            //ExpandableListView第二層
                            Map<String, String> child = new HashMap<>();
                            child.put(ITEM_SUBNAME, e[n].getContent());
                            childList2D.get(e[n].getLayer()-1).set(e[n].getSequence()-1,child);

                        }
                        n++;
                    }




                } catch (JSONException e)
                {
                    e.printStackTrace();
                }

                //設定MyExpandListAdapter
                initData();

            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e("TAG", error.getMessage(), error);
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("X-Ncu-Api-Token", "e763cac7e011b72f1e5d8668cb661070bd130f2109c920a76ca4adb3e540018fcf69115961abae35b0c23a4d27dd7782acce7b75c9dd066053eb0408cb4575b9");
                return map;
            }
        };

        mQueue.add(apiRequest);

        //註冊能夠接收Context Menu事件的元件
        registerForContextMenu(mExpandableListView);
        mContext = AddNewOneActivity.this;

        //設判斷長按父層或子層的監聽器
        mExpandableListView.setOnItemLongClickListener(expandListViewOnItemLongClick);

        mExpandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id)
            {
                int temp=0;
                //找出所按位置step的 e[i]
                for(int i=0;i<StepCount;i++)
                {
                    if(e[i].getLayer() == groupPosition+1)
                    {
                        temp =i;
                        break;
                    }
                }
                Intent it = new Intent();
                it.setClass(AddNewOneActivity.this, DetailStepActivity.class);
                Bundle bundleNext = new Bundle();
                bundleNext.putInt("StepID", e[temp].getId());
                bundleNext.putString("ACCESS_TOKEN", ACCESS_TOKEN);


                bundleNext.putString("selectProjectAndStep",e[temp].getContent());



                bundleNext.putBoolean("isEditable",isEditalbe);
                it.putExtras(bundleNext);

                startActivity(it);
                return true; // 設為 true為不能收合
            }
        });
    }
//========================================================  OnCreate 結束  =======================================================================//

private void initData()
{
    String[] groupItems = new String[groupList.size()];
    String[][] childItems = new String[groupList.size()][];

    //把groupList 和childList2D載入 groupItems 、childItems
    for(int i=0;i<groupList.size();i++)
    {
        groupItems[i] = groupList.get(i).get(ITEM_NAME).toString();
        childItems[i] =new String[childList2D.get(i).size()];
        for(int j=0;j<childList2D.get(i).size();j++)
        {
                childItems[i][j] = childList2D.get(i).get(j).get(ITEM_SUBNAME);
                Log.v("childItems"+"["+i+"]"+"["+j+"]=",childItems[i][j]);

        }

        Log.v("groupItems"+"["+i+"]",groupItems[i]);
    }

    MyExpandListAdapter adapter = new MyExpandListAdapter(this, groupItems);

    for (int i = 0; i < groupItems.length; i++)
    {
        adapter.addChild(i, childItems[i]);
    }

    mExpandableListView.setAdapter(adapter);

    //默認項目全部展開
    for (int i = 0; i < groupList.size(); i++)
    {
        mExpandableListView.expandGroup(i);
    }

    mExpandableListView.setGroupIndicator(null);
}

//=================================================================================================================//

    //判斷選到ExpandableListView選單中哪個選項(短按)
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
    {
        // TODO Auto-generated method stub
        int temp=0;

        switch(groupPosition)
        {
            default:
                switch(childPosition)
                {
                    default:

                        Log.v("Debug","GroupPosition"+groupPosition+"  ChildPosition"+childPosition);
                        Intent it = new Intent();
                        it.setClass(AddNewOneActivity.this, DetailStepActivity.class);

                        //找出所按位置step的 e[i]
                        for(int i=0;i<StepCount;i++)
                        {
                            if(e[i].getLayer()==groupPosition+1 && e[i].getSequence()==childPosition+1)
                            {
                                temp =i;
                                break;
                            }
                        }

                        Bundle bundleNext = new Bundle();
                        bundleNext.putInt("StepID", e[temp].getId());
                        bundleNext.putString("ACCESS_TOKEN", ACCESS_TOKEN);
                        bundleNext.putString("selectProjectAndStep",e[temp].getContent());
                        bundleNext.putBoolean("isEditable",isEditalbe);
                        it.putExtras(bundleNext);
                        startActivity(it);
                        break;
                }
                break;
        }
        return super.onChildClick(parent, v, groupPosition, childPosition, id);
    }



    //發生"長按"情況,系統呼叫此方法建立Context Menu,並顯示在畫面上
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        if(v==mExpandableListView && ACCESS_TOKEN.length() != 0 && isEditalbe)
        {
            if(menu.size()==0)
            {
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.context_menu_expandable_listview,menu);
            }
        }
        else if(ACCESS_TOKEN.length() == 0)
        {
            Toast.makeText(this, "必須先登入才能對步驟進行操作", Toast.LENGTH_SHORT).show();
        }
        else if(!isEditalbe)
        {
            Toast.makeText(this, "必須為創建者才能對步驟進行操作", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //判斷使用者點選Context Menu中的項目,執行對應的工作
    public boolean onContextItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch (id)
        {
                //新增平行步驟
                case R.id.contextMenuItemAddParallel:
                        final View addCommonStepLayout = LayoutInflater.from(AddNewOneActivity.this).inflate(R.layout.add_step_dialog, null);
                        AlertDialog.Builder builder = new AlertDialog.Builder(AddNewOneActivity.this);
                        builder.setTitle("新增平行步驟");
                        builder.setView(addCommonStepLayout);
                        builder.setCancelable(false);

                        builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            EditText mEdtStepName;

                            //暫時紀錄第幾個step的順序要改變
                            int temp;


                            public void onClick(DialogInterface dialog, int id)
                            {
                                mEdtStepName = (EditText) ((AlertDialog) dialog).findViewById(R.id.edtStepName);
                                e[index] = new Step(groupPosition, childPosition + 1, mEdtStepName.getText().toString(), Flow_id, 0);
                                index++;

                                //新增一般步驟後post到後端的動作
                                StringRequest postRequest = new StringRequest(Request.Method.POST, "http://140.115.3.188:3000/sop/v1/steps", new Response.Listener<String>()
                                {
                                    @Override
                                    public void onResponse(String response)
                                    {
                                        try
                                        {
                                            JSONObject object = new JSONObject(response);
                                            Log.d("postSuccessful", response);
                                            e[StepCount - 1].setId(Integer.parseInt(object.getString("id")));
                                        } catch (JSONException e)
                                        {
                                            e.printStackTrace();
                                        }

                                        //插入新步驟後其他item做的調整  (後端)
                                        for (int i = 0; i < StepCount - 1; i++)
                                        {
                                            if (e[i].getExist())
                                            {
                                                if (e[i].getLayer() == groupPosition && e[i].getSequence() >= childPosition + 1)
                                                {

                                                    e[i].setSequence(1);
                                                    temp= i;
                                                    HttpClient httpClient = new HttpClient(e, ACCESS_TOKEN, mQueue);
                                                    httpClient.putComAddChange(temp);

                                                }
                                            }
                                        }


                                    }
                                }, new Response.ErrorListener() {
                                    public void onErrorResponse(VolleyError error)
                                    {
                                        Log.e("postErrorHappen", error.getMessage(), error);
                                    }

                                }) {
                                    public Map<String, String> getHeaders() throws AuthFailureError
                                    {
                                        Map<String, String> map = new HashMap<String, String>();
                                        map.put("Authorization", "Bearer" + " " + ACCESS_TOKEN);
                                        return map;
                                    }


                                    public Map<String, String> getParams() throws AuthFailureError
                                    {
                                        Map<String, String> map = new HashMap<String, String>();
                                        map.put("action", mEdtStepName.getText().toString());
                                        //map.put("items", mEdtStepExam.getText().toString());
                                        map.put("items", "");
                                        map.put("prev", Integer.toString(e[StepCount - 1].getLayer()));
                                        map.put("next", Integer.toString(e[StepCount - 1].getSequence()));
                                        map.put("Flow_id", Integer.toString(Flow_id));
                                        //map.put("PersonId", mEdtPeople.getText().toString());
                                        map.put("PersonId", "0");
                                        //map.put("UnitId", mSelectUnit.getSelectedItem().toString());
                                        map.put("UnitId", "A100");
                                        //map.put("PlaceId", mSelectPlace.getSelectedItem().toString());
                                        map.put("PlaceId", "1");
                                        return map;
                                    }
                                };

                                mQueue.add(postRequest);

                                StepCount++;


                                Map<String, String> child = new HashMap<>();
                                child.put(ITEM_SUBNAME, mEdtStepName.getText().toString());
                                childList2D.get(groupPosition - 1).add(childPosition, child);

                                //設定MyExpandListAdapter
                                initData();

                                Log.v("childList2DSize", Integer.toString(childList2D.size()));
                                Log.v("groupListSize", Integer.toString(groupList.size()));

                                /*
                                for (int n = 0; n < groupList.size(); n++)
                                {
                                    Log.v("childList" + Integer.toString(n) + "Size", Integer.toString(childList2D.get(n).size()));
                                }
                                */
                            }
                        });

                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });

                        AlertDialog alert = builder.create();
                        alert.show();


                    break;


                case R.id.contextMenuItemDelete:
                        AlertDialog.Builder builder3 = new AlertDialog.Builder(AddNewOneActivity.this);

                        //點擊到父層
                        if(childPosition == 0)
                        {
                            builder3.setMessage("確定要刪除整個平行步驟?");
                        }
                        else
                        {
                            builder3.setMessage("確定要刪除此步驟?");
                        }
                        builder3.setCancelable(false);

                        builder3.setPositiveButton("確定", new DialogInterface.OnClickListener()
                        {
                            StringRequest deleteRequest[] = new StringRequest[10];

                            int deletedIndex = 0;      //紀錄要被刪除的步驟在e[]中的index
                            int changeIndex =0;       //紀錄要被調整的步驟在e[]中的index
                            int counter=0;

                            public void onClick(DialogInterface dialog, int id)
                            {
                                if (childPosition == 0)    //刪除平行步驟
                                {
                                    for(int i = 0;i<StepCount ;i++)
                                    {
                                        if(e[i].getExist())
                                        {
                                            //刪掉父層在groupList中做的調整
                                            if (e[i].getLayer() == groupPosition && e[i].getSequence() == 0)
                                            {
                                                deletedIndex = i;
                                                groupList.remove(groupPosition - 1);
                                                Log.v("groupListSize",Integer.toString(groupList.size()));

                                                e[deletedIndex].setExist();

                                                //刪除平行步驟 後端所做的處理
                                                deleteRequest[counter] = new StringRequest(Request.Method.DELETE, "http://140.115.3.188:3000/sop/v1/steps/" + Integer.toString(e[deletedIndex].getId()), new Response.Listener<String>()
                                                {
                                                    @Override
                                                    public void onResponse(String response)
                                                    {
                                                        Log.d("deletePstepSuccessF", response);
                                                    }
                                                }, new Response.ErrorListener()
                                                {
                                                    public void onErrorResponse(VolleyError error)
                                                    {
                                                        Log.e("deletePstepErrorHappen", error.getMessage(), error);
                                                    }
                                                })
                                                {
                                                    public Map<String, String> getHeaders() throws AuthFailureError
                                                    {
                                                        Map<String, String> map = new HashMap<String, String>();
                                                        map.put("Authorization", "Bearer" + " " + ACCESS_TOKEN);
                                                        return map;
                                                    }
                                                };
                                                mQueue.add(deleteRequest[counter]);
                                                counter++;

                                            }
                                            //刪掉子層在childList中做的調整
                                            else if (e[i].getLayer() == groupPosition && e[i].getSequence() != 0)
                                            {
                                                deletedIndex = i;
                                                childList2D.get(groupPosition - 1).remove(0);
                                                e[deletedIndex].setExist();

                                                //刪除平行步驟 後端所做的處理
                                                deleteRequest[counter] = new StringRequest(Request.Method.DELETE, "http://140.115.3.188:3000/sop/v1/steps/" + Integer.toString(e[deletedIndex].getId()), new Response.Listener<String>()
                                                {
                                                    @Override
                                                    public void onResponse(String response)
                                                    {
                                                        Log.d("deletePstepSuccessC", response);
                                                    }
                                                }, new Response.ErrorListener()
                                                {
                                                    public void onErrorResponse(VolleyError error)
                                                    {
                                                        Log.e("deletePstepErrorHappen", error.getMessage(), error);
                                                    }
                                                })
                                                {
                                                    public Map<String, String> getHeaders() throws AuthFailureError
                                                    {
                                                        Map<String, String> map = new HashMap<String, String>();
                                                        map.put("Authorization", "Bearer" + " " + ACCESS_TOKEN);
                                                        return map;
                                                    }
                                                };
                                                mQueue.add(deleteRequest[counter]);
                                                counter++;

                                            }

                                        }

                                    }

                                    //做完後要把空的childList刪除掉
                                    childList2D.remove(groupPosition - 1);

                                    //刪除掉平行步驟後端所做的調整
                                    for (int j=0;j<StepCount;j++)
                                    {
                                        if(e[j].getExist())
                                        {
                                            if(e[j].getLayer() > groupPosition )
                                            {
                                                e[j].setLayer(-1);
                                                changeIndex =j;

                                                Log.v("e[changeIndex].out",Integer.toString(e[changeIndex].getLayer()));
                                                Log.v("changeIndex.out",Integer.toString(changeIndex));

                                                HttpClient httpClient = new HttpClient(e,ACCESS_TOKEN,mQueue);
                                                httpClient.putParaDeletChange(changeIndex);
                                            }
                                        }

                                    }


                                }


                                //刪除一般步驟
                                if(childPosition!=0)
                                {
                                    //刪除被點擊的步驟
                                    for (int i = 0; i < StepCount; i++)
                                    {
                                        if(e[i].getExist())
                                        {
                                            if (e[i].getLayer() == groupPosition && e[i].getSequence() == childPosition)
                                            {
                                                deletedIndex = i;
                                                e[deletedIndex].setExist();

                                                //刪除一般步驟 後端所做的處理
                                                StringRequest stringRequest = new StringRequest(Request.Method.DELETE, "http://140.115.3.188:3000/sop/v1/steps/"+Integer.toString(e[deletedIndex].getId()), new Response.Listener<String>()
                                                {
                                                    @Override
                                                    public void onResponse(String response)
                                                    {
                                                        Log.d("deleteCstepSuccessful", response);
                                                    }
                                                }, new Response.ErrorListener()
                                                {
                                                    public void onErrorResponse(VolleyError error)
                                                    {
                                                        Log.e("deleteCstepErrorHappen", error.getMessage(), error);
                                                    }

                                                })
                                                {
                                                    public Map<String, String> getHeaders() throws AuthFailureError
                                                    {
                                                        Map<String, String> map = new HashMap<String, String>();
                                                        map.put("Authorization", "Bearer"+" "+ACCESS_TOKEN);
                                                        return map;
                                                    }
                                                };

                                                mQueue.add(stringRequest);
                                                break;
                                            }
                                        }

                                    }

                                    childList2D.get(groupPosition - 1).remove(e[deletedIndex].getSequence()-1);

                                    //刪除一般步驟後其他步驟做的調整
                                    for(int j=0;j<StepCount;j++)
                                    {
                                        if(e[j].getExist())
                                        {
                                            if(e[j].getLayer() == groupPosition && e[j].getSequence()>childPosition)
                                            {
                                                changeIndex =j;
                                                e[changeIndex].setSequence(-1);

                                                HttpClient httpClient = new HttpClient(e,ACCESS_TOKEN,mQueue);
                                                httpClient.putComDeletChange(changeIndex);
                                            }
                                        }

                                    }

                                }

                                //設定MyExpandListAdapter
                                initData();

                                Log.v("childList2DSize", Integer.toString(childList2D.size()));
                                Log.v("groupListSize", Integer.toString(groupList.size()));

                                /*
                                for(int n=0;n<groupList.size();n++)
                                {
                                    Log.v("childList"+Integer.toString(n)+"Size",Integer.toString(childList2D.get(n).size()));
                                }
                                */
                            }

                        });


                        builder3.setNegativeButton("取消", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int id)
                            {
                            }

                        });

                        builder3.show();


                    break;

                case R.id.contextMenuItemEdit:
                        AlertDialog.Builder builder4 = new AlertDialog.Builder(AddNewOneActivity.this);
                        builder4.setTitle("編輯步驟名稱");
                        builder4.setView(R.layout.edit_step_dialog);
                        builder4.setCancelable(false);

                        builder4.setPositiveButton("確定", new DialogInterface.OnClickListener()
                        {
                            int editIndex;

                            public void onClick(DialogInterface dialog, int id)
                            {
                                //Log.v("successful edit step", "新的步驟名稱為:" + editStepName.getText().toString());
                                //editStepName = (EditText) ((AlertDialog) dialog).findViewById(R.id.editStepName);
                                if (childPosition == 0)
                                {
                                    Map<String, Object> newEditItem = new HashMap<String, Object>();
                                    newEditItem.put(ITEM_NAME, editStepName.getText().toString());
                                    groupList.set(groupPosition - 1, newEditItem);



                                    for(int i=0;i<StepCount;i++)
                                    {
                                        if(e[i].getLayer() == groupPosition)
                                        {
                                            e[i].setProjectContent(editStepName.getText().toString());
                                            break;
                                        }
                                    }


                                    //e[groupPosition - 1].setProjectContent(editStepName.getText().toString());
                                }
                                else
                                {
                                    Map<String, String> newEditSubItem = new HashMap<String, String>();
                                    newEditSubItem.put(ITEM_SUBNAME, editStepName.getText().toString());
                                    childList2D.get(groupPosition - 1).set(childPosition - 1, newEditSubItem);



                                    for(int i=0;i<StepCount;i++)
                                    {
                                        if(e[i].getLayer() == groupPosition && e[i].getSequence() == childPosition)
                                        {
                                            e[i].setProjectContent(editStepName.getText().toString());
                                            break;
                                        }
                                    }




                                }

                                //編輯步驟名稱後端所做的處理(put)
                                for (int i = 0; i < StepCount; i++)
                                {
                                    if (e[i].getLayer() == groupPosition && e[i].getSequence() == childPosition)
                                    {
                                        editIndex = i;
                                        //做完調整後上傳的動作 (put)
                                        StringRequest putRequest = new StringRequest(Request.Method.PUT, "http://140.115.3.188:3000/sop/v1/steps/" + Integer.toString(e[editIndex].getId()), new Response.Listener<String>() {

                                            @Override
                                            public void onResponse(String response)
                                            {
                                                Log.d("Successful", response);
                                            }
                                        }, new Response.ErrorListener() {
                                            public void onErrorResponse(VolleyError error) {
                                                Log.e("ErrorHappen", error.getMessage(), error);
                                            }

                                        }) {
                                            public Map<String, String> getHeaders() throws AuthFailureError
                                            {
                                                Map<String, String> map = new HashMap<String, String>();
                                                map.put("Authorization", "Bearer" + " " + ACCESS_TOKEN);
                                                return map;
                                            }

                                            public Map<String, String> getParams() throws AuthFailureError
                                            {
                                                Map<String, String> map = new HashMap<String, String>();
                                                map.put("action", editStepName.getText().toString());
                                                return map;
                                            }
                                        };
                                        mQueue.add(putRequest);
                                        break;
                                    }
                                }
                                //設定MyExpandListAdapter
                                initData();
                            }

                        });


                        builder4.setNegativeButton("取消", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int id)
                            {

                            }

                        });

                        AlertDialog alert4 = builder4.create();
                        alert4.show();
                        editStepName = (EditText) alert4.findViewById(R.id.editStepName);
                        editStepName.setText(getEditStepName());

                    break;


                case R.id.contextMenuItemMoveUp:

                        final int adjustPosition = groupPosition-1;
                        final Step[] stepToBeChange = new Step[StepCount];

                        AlertDialog.Builder builder5 = new AlertDialog.Builder(AddNewOneActivity.this);
                        builder5.setMessage("確定要將步驟上移?");
                        builder5.setCancelable(false);
                        builder5.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id)
                            {
                                //表示不能在上移了
                                if(groupPosition == 1)
                                {
                                    Toast.makeText(AddNewOneActivity.this,"此步驟為最上層,無法上移",Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    int counter=0;
                                    int layer;
                                    //做groupList的調整
                                    Map<String,Object> selectedGroupListItem = groupList.get(groupPosition-1);
                                    Map<String,Object> adjustGroupListItem = groupList.get(adjustPosition-1);
                                    groupList.set(adjustPosition-1,selectedGroupListItem);
                                    groupList.set(groupPosition-1,adjustGroupListItem);

                                    //做child2Dlist的調整
                                    List<Map<String,String>> selectedChildList = childList2D.get(groupPosition-1);
                                    List<Map<String,String>> adjustChildList = childList2D.get(adjustPosition - 1);
                                    childList2D.set(groupPosition-1,adjustChildList);
                                    childList2D.set(adjustPosition - 1, selectedChildList);

                                    //將調整後的順序put 到後端
                                    for(int i=0;i<StepCount;i++)
                                    {
                                        Log.v("stepTEST",e[i].getContent());
                                        if(e[i].getLayer() == groupPosition)
                                        {
                                            stepToBeChange[counter] = e[i];
                                            layer =groupPosition-1;
                                            HttpClient httpClient =new HttpClient(stepToBeChange,ACCESS_TOKEN,mQueue);
                                            httpClient.uploadMovedStep(stepToBeChange[counter],groupPosition,layer);
                                            e[i].setLayer(-1);
                                            Log.v("e["+i+"]:layer",Integer.toString(e[i].getLayer()));
                                            counter++;
                                        }
                                        else if(e[i].getLayer() == groupPosition-1)
                                        {
                                            stepToBeChange[counter] = e[i];
                                            layer =groupPosition;
                                            HttpClient httpClient =new HttpClient(stepToBeChange,ACCESS_TOKEN,mQueue);
                                            httpClient.uploadMovedStep(stepToBeChange[counter],groupPosition,layer);
                                            e[i].setLayer(1);
                                            Log.v("e[" + i + "]:layer", Integer.toString(e[i].getLayer()));
                                            counter++;
                                        }
                                    }

                                    //設定MyExpandListAdapter
                                    initData();

                                }


                            }

                        });


                        builder5.setNegativeButton("取消", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int id)
                            {

                            }

                        });


                        AlertDialog alert5 = builder5.create();
                        alert5.show();


                    break;


                case R.id.contextMenuItemMoveDown:

                        final int adjustPosition2 = groupPosition + 1;
                        final Step[] stepToBeChange2 = new Step[StepCount];

                        AlertDialog.Builder builder6 = new AlertDialog.Builder(AddNewOneActivity.this);
                        builder6.setMessage("確定要將步驟下移?");
                        builder6.setCancelable(false);
                        builder6.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id)
                            {
                                //表示不能再下移
                                if(groupPosition == groupList.size())
                                {
                                    Toast.makeText(AddNewOneActivity.this,"此步驟為最下層,無法下移",Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    int counter=0;
                                    int layer;

                                    //做groupList的調整
                                    Map<String,Object> selectedGroupListItem = groupList.get(groupPosition-1);
                                    Map<String,Object> adjustGroupListItem = groupList.get(adjustPosition2-1);
                                    groupList.set(adjustPosition2-1,selectedGroupListItem);
                                    groupList.set(groupPosition-1,adjustGroupListItem);

                                    //做child2Dlist的調整
                                    List<Map<String,String>> selectedChildList = childList2D.get(groupPosition-1);
                                    List<Map<String,String>> adjustChildList = childList2D.get(adjustPosition2 - 1);
                                    childList2D.set(groupPosition-1,adjustChildList);
                                    childList2D.set(adjustPosition2 - 1, selectedChildList);

                                    //將調整後的順序put 到後端
                                    for(int i=0;i<StepCount;i++)
                                    {
                                        Log.v("stepTEST",e[i].getContent());
                                        if(e[i].getLayer() == groupPosition)
                                        {
                                            stepToBeChange2[counter] = e[i];
                                            layer =groupPosition+1;
                                            HttpClient httpClient =new HttpClient(stepToBeChange2,ACCESS_TOKEN,mQueue);
                                            httpClient.uploadMovedStep(stepToBeChange2[counter],groupPosition,layer);
                                            e[i].setLayer(1);
                                            Log.v("e["+i+"]:layer",Integer.toString(e[i].getLayer()));
                                            counter++;
                                        }
                                        else if(e[i].getLayer() == groupPosition+1)
                                        {
                                            stepToBeChange2[counter] = e[i];
                                            layer =groupPosition;
                                            HttpClient httpClient =new HttpClient(stepToBeChange2,ACCESS_TOKEN,mQueue);
                                            httpClient.uploadMovedStep(stepToBeChange2[counter],groupPosition,layer);
                                            e[i].setLayer(-1);
                                            Log.v("e[" + i + "]:layer", Integer.toString(e[i].getLayer()));
                                            counter++;
                                        }
                                    }

                                    //設定MyExpandListAdapter
                                    initData();
                                }


                            }
                        });

                        builder6.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id)
                            {

                            }

                        });


                        AlertDialog alert6 = builder6.create();
                        alert6.show();

                    break;

        }

        return super.onContextItemSelected(item);
    }


    //為了解決繼承ExpandableListActivity不能使用setSupportActionbar()
    private AppCompatDelegate getDelegate()
    {
        if (mDelegate == null)
        {
            mDelegate = AppCompatDelegate.create(this, null);
        }
        return mDelegate;
    }

    public void setSupportActionbar(Toolbar toolbar)
    {
        getDelegate().setSupportActionBar(toolbar);
    }



    //設定長按父層或子層的監聽事件
    private  AdapterView.OnItemLongClickListener expandListViewOnItemLongClick =new AdapterView.OnItemLongClickListener()
    {
        public boolean onItemLongClick(AdapterView<?> parent, View childView, int flatPos, long id)
        {
                long packedPosition = ((ExpandableListView) parent).getExpandableListPosition(flatPos);
                int groupposition = ExpandableListView.getPackedPositionGroup(packedPosition)+1;
                int childposition = ExpandableListView.getPackedPositionChild(packedPosition)+1;
                groupPosition = groupposition;
                childPosition = childposition;

                if (childPosition ==0) //選到父層
                {

                }
                else                    //選到子層
                {

                }
                //Log.v("測試", packedPosition+" ");
                Log.v("測試", " 長按的组群位置：" + groupPosition);
                Log.v("測試", "長按的子項位置:" + childPosition);

            return false;
        }


    };

    public String getEditStepName()
    {
        for (int i = 0; i < StepCount; i++)
        {
            if (e[i].getLayer() == groupPosition && e[i].getSequence() == childPosition)
            {
                 return  e[i].getContent();
            }
        }

        return "";
    }


}
