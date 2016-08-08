package cc.ncu.edu.tw.sop_v1;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.support.v7.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialStore;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.json.jackson.JacksonFactory;
import com.wuman.android.auth.AuthorizationFlow;
import com.wuman.android.auth.AuthorizationUIController;
import com.wuman.android.auth.DialogFragmentController;
import com.wuman.android.auth.OAuthManager;
import com.wuman.android.auth.oauth2.store.SharedPreferencesCredentialStore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,ListView.OnItemClickListener
{
    private MainActivity mainActivity;
    private ArrayList<Map<String,Object>> mList = new ArrayList<>(); //儲存每個sop project items中的各個物件型態(TextView、三個ImageButton)
    private CookieManager cookieManager;
    private boolean logoutSuccess = false;

    private ListView listView;
    private MyAdapter adapter;
    private Context context;
    private OAuthManager oAuthManager;
    private String ACCESS_TOKEN = "";

    private List<Project> projectList = new ArrayList<>();
    private int projectNum = 0;


    private TextView userName;
    private TextView userId;
    private EditText editText;
    private EditText searchEdt;


    private RequestQueue mQueue;

    //紀錄點進去專案的Flow_id
    private int flowId;
    private boolean searchFlag=false;

    //編輯步驟在projectList中真正的位置
    private int realPosition;
    private int realId;
    //private MyAdapter saveProjectId;


    //紀錄目前登入的使用者ID
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(onMenuItemClick);

        cookieManager = CookieManager.getInstance();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(ACCESS_TOKEN == "")
                {
                    Toast.makeText(MainActivity.this,R.string.oauth_create_certificate,Toast.LENGTH_LONG).show();
                }

                else
                {
                    final View dialogLayout = LayoutInflater.from(MainActivity.this).inflate(R.layout.add_project_dialog, null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("新增專案");
                    builder.setView(dialogLayout);
                    builder.setCancelable(false);

                    builder.setPositiveButton("確定", new DialogInterface.OnClickListener()
                            {
                                private String newProjectName;
                                public void onClick(DialogInterface dialog, int id)
                                {
                                    editText =(EditText) ((AlertDialog) dialog).findViewById(R.id.edtProjectName);
                                    newProjectName = editText.getText().toString();
                                    if(!editText.getText().toString().equals(""))
                                    {
                                        Map<String,Object> item = new HashMap<String,Object>();
                                        item.put("txtView",editText.getText().toString());
                                        item.put("delete", R.drawable.delete);
                                        item.put("edit", R.drawable.edit);
                                        item.put("copy", R.drawable.copy);

                                        mList.add(item);
                                        projectList.add(new Project(projectNum, 0, newProjectName));


                                        if(searchEdt.getText().toString().length()==0)
                                        {
                                            //adapter.setProjectList(projectList);
                                            //adapter.setmList(mList);
                                            adapter = new MyAdapter(MainActivity.this,mList,R.layout.sop_list_items,new String[] {"txtView","delete","edit","copy"}, new int[] {R.id.txtView,R.id.delete,R.id.edit,R.id.copy},mainActivity,projectList);
                                            listView.setAdapter(adapter);
                                        }
                                        else
                                        {
                                            searchEdt.setText("");
                                            adapter = new MyAdapter(MainActivity.this,mList,R.layout.sop_list_items,new String[] {"txtView","delete","edit","copy"}, new int[] {R.id.txtView,R.id.delete,R.id.edit,R.id.copy},mainActivity,projectList);
                                            listView.setAdapter(adapter);
                                        }

                                        Toast.makeText(getApplicationContext(), "新增了專案"+editText.getText().toString(), Toast.LENGTH_SHORT).show();
                                    }


                                    //post新增的內容到後端
                                    StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://140.115.3.188:3000/sop/v1/processes/", new Response.Listener<String>() {

                                        @Override
                                        public void onResponse(String response)
                                        {
                                            try
                                            {
                                                Log.d("Successful", response);
                                                JSONObject object = new JSONObject(response);
                                                projectList.get(projectNum).setProjectId(Integer.parseInt(object.getString("id")));
                                                projectNum++;
                                            }
                                            catch(JSONException e)
                                            {
                                                e.printStackTrace();
                                            }
                                        }
                                    }, new Response.ErrorListener()
                                    {
                                        public void onErrorResponse(VolleyError error)
                                        {
                                            Log.e("ErrorHappen", error.getMessage(), error);
                                        }
                                    })
                                    {
                                        public Map<String, String> getHeaders() throws AuthFailureError
                                        {
                                            Map<String, String> map = new HashMap<String, String>();
                                            map.put("Authorization", "Bearer"+" "+ACCESS_TOKEN);
                                            return map;
                                        }

                                        public Map<String, String> getParams() throws AuthFailureError
                                        {
                                            Map<String, String> map = new HashMap<String, String>();
                                            map.put("name", newProjectName);
                                            return map;
                                        }
                                    };

                                    mQueue.add(stringRequest);



                                }
                            }
                    );

                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {

                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });

        //DrawerLayout的相關設定
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.inflateHeaderView(R.layout.nav_header_main);
        userName = (TextView) header.findViewById(R.id.userName);
        userId =(TextView)header.findViewById(R.id.userId);

        context = this;
        mainActivity = this;
        //取得MainActivity context內容

        //在sop project items中加入許多物件(Image、TextView、三個ImageButton)
        listView = (ListView)findViewById(R.id.listView);
        listView.setTextFilterEnabled(true);
        searchEdt = (EditText) findViewById(R.id.search_box);

        //初始化mQueue
        mQueue = Volley.newRequestQueue(context);

        //從後端取得專案(get)
        StringRequest apiRequest = new StringRequest("http://140.115.3.188:3000/sop/v1/processes/", new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.d("TAG", response);

                try
                {
                    JSONArray array = new JSONArray(response);

                    for(int i=0;i<array.length();i++)
                    {
                        //初始化project相關的資訊
                        projectList.add(new Project(i,Integer.parseInt(array.getJSONObject(i).getString("id")), array.getJSONObject(i).getString("name")));

                        Map<String,Object> item = new HashMap<>();
                        item.put("txtView", projectList.get(i).getProjectContent());
                        item.put("delete", R.drawable.delete);
                        item.put("edit",R.drawable.edit);
                        item.put("copy", R.drawable.copy);
                        mList.add(item);

                        projectNum++;

                    }
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }

                //用自定義的MyAdapter把上面建立好的選單陣列存入此物件,再顯示
                adapter = new MyAdapter(MainActivity.this,mList,R.layout.sop_list_items,new String[] {"txtView","delete","edit","copy"}, new int[] {R.id.txtView,R.id.delete,R.id.edit,R.id.copy},mainActivity,projectList);
                listView.setAdapter(adapter);

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

        searchEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                MainActivity.this.adapter.getFilter().filter(arg0);
                searchFlag = true;
                Log.v("afterTextChanged", "there is text change");
            }
        });


        listView.setOnItemClickListener(listViewOnItemClick);

    }


    //type參數 0 :刪除  /  1:修改   /   2:複製
    public void setMyAdapterToMainActivity(int position,int type,String s)
    {
        //當沒有使用Filter的情況
        if(searchEdt.getText().toString().length() == 0)
        {
            switch(type)
            {
                //刪除專案後的setAdapter
                case 0:
                    if(projectList.get(position).getProjectId()==0)
                    {
                        //realId=saveProjectId.getCopiedProjectId();
                        realId=adapter.getCopiedProjectId();
                    }
                    else
                    {
                        realId = projectList.get(position).getProjectId();
                    }

                    mList.remove(position);
                    projectList.remove(position);
                    adapter = new MyAdapter(MainActivity.this,mList,R.layout.sop_list_items,new String[] {"txtView","delete","edit","copy"},new int[]{R.id.txtView, R.id.delete, R.id.edit, R.id.copy},mainActivity,projectList);
                    listView.setAdapter(adapter);

                    projectNum--;

                    break;

                //編輯專案後的setAdapter
                case 1:
                    realPosition =position;
                    Map<String,Object> newItem = new HashMap();
                    newItem.put("txtView",s);
                    newItem.put("delete",R.drawable.delete);
                    newItem.put("edit", R.drawable.edit);
                    newItem.put("copy", R.drawable.copy);
                    mList.set(position, newItem);
                    projectList.get(position).setContent(s);
                    adapter = new MyAdapter(MainActivity.this, mList, R.layout.sop_list_items, new String[]{"txtView", "delete", "edit", "copy"}, new int[]{R.id.txtView, R.id.delete, R.id.edit, R.id.copy}, mainActivity,projectList);
                    listView.setAdapter(adapter);
                    Log.v("searchEdt","is empty");

                    break;

                //複製步驟後的setAdapter
                case 2:
                    //saveProjectId = adapter;
                    realPosition =position;

                    Map<String, Object> copyItem = new HashMap<>();
                    copyItem.put("txtView", projectList.get(realPosition).getProjectContent()+"(複製)");
                    copyItem.put("delete", R.drawable.delete);
                    copyItem.put("edit", R.drawable.edit);
                    copyItem.put("copy", R.drawable.copy);
                    mList.add(copyItem);
                    projectList.add(new Project(projectList.size(), 0 , projectList.get(position).getProjectContent()+"(複製)"));
                    adapter = new MyAdapter(MainActivity.this, mList, R.layout.sop_list_items, new String[]{"txtView", "delete", "edit", "copy"}, new int[]{R.id.txtView, R.id.delete, R.id.edit, R.id.copy}, mainActivity,projectList);

                    listView.setAdapter(adapter);

                    projectNum++;
                    break;

            }

        }
        //當有使用Filter的情況
        else
        {
            ArrayList<Map<String,Object>> list = MainActivity.this.adapter.getAfterFilterList();

            for(int i=0;i<projectList.size();i++)
            {
                if(list.get(position).get("txtView").toString().equals(projectList.get(i).getProjectContent()))
                {
                    realPosition =i;
                    switch(type)
                    {
                        //刪除步驟設定Adapter
                        case 0:
                            realId = projectList.get(realPosition).getProjectId();
                            mList.remove(realPosition);
                            projectList.remove(realPosition);

                            searchEdt.setText("");
                            adapter = new MyAdapter(MainActivity.this, mList, R.layout.sop_list_items, new String[]{"txtView", "delete", "edit", "copy"}, new int[]{R.id.txtView, R.id.delete, R.id.edit, R.id.copy}, mainActivity,projectList);
                            listView.setAdapter(adapter);
                            projectNum--;

                            break;

                        //編輯步驟設定Adapter
                        case 1:
                            Map<String,Object> newItem = new HashMap();
                            newItem.put("txtView",s);
                            newItem.put("delete",R.drawable.delete);
                            newItem.put("edit", R.drawable.edit);
                            newItem.put("copy", R.drawable.copy);

                            mList.set(realPosition, newItem);
                            projectList.get(i).setContent(s);
                            searchEdt.setText("");
                            adapter = new MyAdapter(MainActivity.this, mList, R.layout.sop_list_items, new String[]{"txtView", "delete", "edit", "copy"}, new int[]{R.id.txtView, R.id.delete, R.id.edit, R.id.copy}, mainActivity,projectList);
                            listView.setAdapter(adapter);
                            Log.v("searchEdt", "has something");
                            break;

                        //複製步驟設定Adapter
                        case 2:
                            //saveProjectId = adapter;
                            Map<String, Object> copyItem = new HashMap<>();
                            copyItem.put("txtView", projectList.get(realPosition).getProjectContent()+"(複製)");
                            copyItem.put("delete", R.drawable.delete);
                            copyItem.put("edit", R.drawable.edit);
                            copyItem.put("copy", R.drawable.copy);

                            mList.add(copyItem);
                            projectList.add(new Project(projectList.size(), 0, projectList.get(realPosition).getProjectContent()+"(複製)"));
                            searchEdt.setText("");
                            adapter = new MyAdapter(MainActivity.this, mList, R.layout.sop_list_items, new String[]{"txtView", "delete", "edit", "copy"}, new int[]{R.id.txtView, R.id.delete, R.id.edit, R.id.copy}, mainActivity,projectList);
                            listView.setAdapter(adapter);
                            projectNum ++;


                            break;
                    }

                    break;
                }
            }
        }

    }

    public List<Project> getProjectList(){return projectList;}
    public int getRealId(){return realId;}
    public int getRealPosition() {return realPosition;}
    public MyAdapter getMainActivityAdapter(){return adapter;}
    public void setCopiedProjectId(int newProjectId){projectList.get(projectList.size()-1).setProjectId(newProjectId);}

    //Toolbar上Menu Item 被按下後執行對應的動作
    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId())
            {
                case R.id.action_certificate:
                    //oauth 認證
                    //oauth要用到的參數
                    final String CREDENTIAL_FILE_NAME = "credential.file";

                    final String AUTH_ENDPOINT_PATH  = "http://140.115.3.188/oauth/oauth/authorize";//拿authorization code(grant)的路徑
                    final String TOKEN_ENDPOINT_PATH = "http://140.115.3.188/oauth/oauth/token";//拿access token的路徑(及refresh token)

                    final String CLIENT_ID = "ZDMzNTYzMjQtMDQ0MC00NzNkLWEzN2UtNzIyYTlmZTI0MzNi";
                    final String CLIENT_SECRET = "d0d1f03e89d305eabbb1a76a670818500e931dee9cbd9260727975a1b145bdbca7eb2002e0c40d5d7573b6d22c89d973d673e0383e82cd7eba43da6d90223279";
                    final String CALL_BACK = "https://github.com/NCU-CC";
                    String scope = "user.info.basic.read";

                    //以JSON格式刊登access token到SharedPreferencesCredentialStore(多型:CredentialStore為SharedPreferencesCredentialStore的父類別)
                    CredentialStore credentialStore = new SharedPreferencesCredentialStore( context, CREDENTIAL_FILE_NAME, new JacksonFactory() );

                    //實作類別OAuthManager需要兩個參數
                    AuthorizationFlow authorizationFlow = null;
                    AuthorizationUIController authorizationUIController = null;

                    //實作AuthorizationFlow的程式碼(需要透過Builder)
                    AuthorizationFlow.Builder builder = new AuthorizationFlow.Builder(
                            BearerToken.authorizationHeaderAccessMethod(),
                            AndroidHttp.newCompatibleTransport(),
                            new JacksonFactory(),
                            new GenericUrl(TOKEN_ENDPOINT_PATH),
                            new ClientParametersAuthentication(CLIENT_ID, CLIENT_SECRET),
                            CLIENT_ID,
                            AUTH_ENDPOINT_PATH);
                    builder.setCredentialStore(credentialStore);
                    builder.setScopes(Arrays.asList(scope));
                    authorizationFlow = builder.build();

                    //實作AuthorizationUIController的程式碼
                    authorizationUIController = new DialogFragmentController( getSupportFragmentManager() ) {
                        @Override
                        public boolean isJavascriptEnabledForWebView() {
                            return true;
                        }
                        @Override
                        public String getRedirectUri() throws IOException {
                            return CALL_BACK;
                        }
                    };

                    oAuthManager = new OAuthManager(authorizationFlow, authorizationUIController);
                    oAuthManager.deleteCredential("user", null, null);


                    new AuthTask().execute();
                    break;

            }
            return true;
        }
    };






    private class AuthTask extends AsyncTask<Void, Void, Void>
    {
        //private boolean authSuccess = true;
        private String accessToken=null;

        @Override
        protected Void doInBackground(Void... params)
        {
            if(logoutSuccess)
            {
                cookieManager.setCookie("portal.ncu.edu.tw", "JSESSIONID=");
            }
            try
            {
                //Credential:存放密碼的密碼庫(放access token)
                //每次使用authorizeExplicitly時會自動檢查access token有沒有過期
                Credential authResult = oAuthManager.authorizeExplicitly("user",null,null).getResult();
                accessToken = authResult.getAccessToken();
                //String refreshToken = authResult.getRefreshToken();
                Log.e("debug","access Token: " + accessToken);
                ACCESS_TOKEN = accessToken;
                setNavDrawerHeaderInfo();
                logoutSuccess = false;
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            catch (CancellationException e)
            {
                cookieManager.setCookie("portal.ncu.edu.tw", "JSESSIONID=");
                ACCESS_TOKEN ="";
                logoutSuccess =true;
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid)
        {
            showLoginToast(ACCESS_TOKEN);
        }
    }

    public void showLoginToast(String access_token)
    {
        if(access_token.length()!=0)
        {
            Toast.makeText(MainActivity.this,"登入成功",Toast.LENGTH_SHORT).show();
        }
        else
        {
            cookieManager.setCookie("portal.ncu.edu.tw", "JSESSIONID=");
            ACCESS_TOKEN="";
            Toast.makeText(MainActivity.this,"登入失敗",Toast.LENGTH_SHORT).show();
        }
    }


    //監聽ListView中哪個選項被選到
    private AdapterView.OnItemClickListener listViewOnItemClick = new  AdapterView.OnItemClickListener() {

        public void onItemClick(AdapterView parent,View view,int position,long id)
        {
            ArrayList<Map<String,Object>> list ;
            int temp= 0;

            //沒有使用Filter時
            if(searchEdt.getText().length() == 0)
            {
                temp = position;
                flowId = projectList.get(position).getProjectId();
                Log.v("ShowPosition",Integer.toString(position));
            }
            //有使用Filter時
            else
            {
                list = MainActivity.this.adapter.getAfterFilterList();
                for (int i = 0; i < projectNum; i++)
                {
                    if (list.get(position).get("txtView").toString().equals(projectList.get(i).getProjectContent()))
                    {
                        temp =i;
                        //記錄點到的project id(flow_id)是多少
                        flowId = projectList.get(temp).getProjectId();
                        break;
                    }

                }
            }

            switch(temp)
            {
                default:
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, add_new_one.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("Flow_id", flowId);
                    bundle.putString("Access_token", ACCESS_TOKEN);
                    bundle.putString("selectProject",projectList.get(temp).getProjectContent());
                    intent.putExtras(bundle);
                    startActivity(intent);
                    Log.v("ShowClickFlow_id", Integer.toString(flowId));
                    break;
            }

        }
    };


    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        } else
        {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
/*
        if (id == R.id.action_settings)
        {
            return true;
        }
        */

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")

    //Navigation Item被點擊的監聽器
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout)
        {
            //cookieManager.setCookie("portal.ncu.edu.tw", "JSESSIONID=");
            logoutSuccess =true;
            ACCESS_TOKEN ="";
            userId.setText("");
            userName.setText("");
            Toast.makeText(MainActivity.this,"登出成功",Toast.LENGTH_SHORT).show();
        }
        else if (id == R.id.nav_share)
        {
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
            sendIntent.setType("plain/text");
            sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { "mobile@cc.ncu.edu.tw" });
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "意見反映與回饋信件");
            startActivity(sendIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
    }


    public String getACCESS_TOKEN()
    {
        return ACCESS_TOKEN;
    }


    //使用者登入後,設定NavDrawer上目前登入使用者的名稱和學號
    public void setNavDrawerHeaderInfo()
    {
        StringRequest setNavDrawerHeaderInfoRequest = new StringRequest("http://140.115.3.188/personnel/v1/info", new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.d("NavDrawerHeaderInfoSuc", response);
                try
                {
                    JSONObject object = new JSONObject(response);
                    currentUserId = object.getString("id");
                    userName.setText(object.getString("unit")+"  "+object.getString("name"));
                    userId.setText(object.getString("number"));
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e("NavDrawerHeaderInfoErr", error.getMessage(), error);
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("Authorization","Bearer "+ACCESS_TOKEN);
                return map;
            }
        };

        mQueue.add(setNavDrawerHeaderInfoRequest);

    }

}
