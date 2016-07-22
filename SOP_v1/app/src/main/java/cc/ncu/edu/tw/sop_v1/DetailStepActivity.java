package cc.ncu.edu.tw.sop_v1;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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

import java.util.HashMap;
import java.util.Map;

public class DetailStepActivity extends AppCompatActivity
{
    private Bundle bundle;
    RequestQueue mQueue;
    private String ACCESS_TOKEN;
    int stepID;

    //DetailStepActivity介面元件對應的宣告
    EditText stepExamine;
    AutoCompleteTextView person;
    private Spinner unitsSpinner;
    private Spinner placesSpinner;

    //經過轉換後得到的unit 、 places 、 people  ID  這是put 前做的轉換
    private String unit_no;
    private int places_id;
    private int people_id;

    private String personName;


    //載入時需知道要顯示Spinner裡面的第幾個item  這是get前做的轉換
    private int unitsIndexM;
    private int placesIndexM;


    private String[] unitsList;
    private String[] placesList;

    ArrayAdapter<String> unitsAdapter;
    ArrayAdapter<String> placesAdapter;

    HttpClient httpSwitchUnitClient ;
    HttpClient httpSwitchPlacesClient ;

    //轉換成unitsIndex在 Spinner中顯示
    HttpClient httpClientGetUnitsIndex;
    HttpClient httpClientGetPlacesIndex;
    HttpClient httpClientPerson;

    //AutoCompleteTextView元件
    private AutoCompleteTextView mAutoCompTextView;
    private ArrayAdapter<CharSequence> mAdapAutoCompText;


    private String[] PeopleName;

    @Override

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_step);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(onMenuItemClick);

        //DetailStepActivity見面元件設定 ID
        unitsSpinner = (Spinner)findViewById(R.id.units_spinner);
        placesSpinner =(Spinner)findViewById(R.id.places_spinner);
        stepExamine = (EditText)findViewById(R.id.editStepExamine);
        person = (AutoCompleteTextView)findViewById(R.id.autoCompTextView);

        //需要登入後才能使用
        stepExamine.setEnabled(false);
        person.setEnabled(false);
        placesSpinner.setEnabled(false);
        unitsSpinner.setEnabled(false);

        //初始化mQueue
        mQueue = Volley.newRequestQueue(DetailStepActivity.this);
        httpClientGetUnitsIndex = new HttpClient(mQueue);
        httpClientGetPlacesIndex = new HttpClient(mQueue);
        httpClientPerson = new HttpClient(mQueue);


        bundle =getIntent().getExtras();

        //取出從add_new_one.java中Intent所附帶的Step資料
        stepID = bundle.getInt("StepID");
        ACCESS_TOKEN =bundle.getString("ACCESS_TOKEN");

        //到後端去get 資料
        StringRequest apiRequest = new StringRequest("http://140.115.3.188:3000/sop/v1/steps/"+Integer.toString(stepID), new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.d("getStepSuccessful", response);
                try
                {
                    JSONObject object = new JSONObject(response);

                    //載入攜帶物品
                    stepExamine.setText(object.getString("items"), TextView.BufferType.EDITABLE);

                    //載入單位
                    httpClientGetUnitsIndex.ToUnitsIndexInSpinner(object.getString("UnitId"), new HttpClient.GetUnitResponseListener() {
                                @Override
                                public void setUnitSpinnerIndex(int index) {
                                    unitsIndexM = index;
                                    Log.v("unitsIndexM", Integer.toString(unitsIndexM));
                                    //設定unitsSpinner
                                    unitsSpinner.setSelection(unitsIndexM, true);
                                }

                            }
                    );


                    //載入地點
                    httpClientGetPlacesIndex.ToPlacesIndexInSpinner(Integer.parseInt(object.getString("PlaceId")), new HttpClient.GetPlacesResponseListener() {
                        public void setPlacesSpinnerIndex(int index) {
                            placesIndexM = index;
                            Log.v("placesIndexM", Integer.toString(placesIndexM));
                            //設定placesSpinner
                            placesSpinner.setSelection(placesIndexM, true);
                        }

                    });


                    //載入人員資料
                    httpClientPerson.switchPersonIdToName(Integer.parseInt(object.getString("PersonId")), new HttpClient.GetPeopleResponseListener() {
                        public void setPeopleIndex(String name) {
                            personName = name;
                            Log.v("people_id", Integer.toString(people_id));
                            //設定人員欄位
                            person.setText(personName, TextView.BufferType.EDITABLE);
                        }

                    });



                }
                catch(JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e("ErrorHappened", error.getMessage(), error);
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


        //把後端 units 、 places 載入到選單中
        StringRequest getUnitsRequest = new StringRequest("http://140.115.3.188:3000/sop/v1/units", new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.d("getUnitsSuccessful", response);
                try
                {
                    JSONArray array = new JSONArray(response);
                    unitsList = new String[array.length()];
                    for(int i=0;i<array.length();i++)
                    {
                        unitsList[i] = array.getJSONObject(i).getString("full_name");
                    }
                    unitsAdapter = new ArrayAdapter<>(DetailStepActivity.this,android.R.layout.simple_spinner_item,unitsList);
                    unitsSpinner.setAdapter(unitsAdapter);
                }
                catch(JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e("ErrorHappened", error.getMessage(), error);
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

        mQueue.add(getUnitsRequest);

        StringRequest getPlacesRequest = new StringRequest("http://140.115.3.188:3000/sop/v1/places", new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.d("getUnitsSuccessful", response);
                try
                {
                    JSONArray array = new JSONArray(response);
                    placesList = new String[array.length()];
                    for(int i=0;i<array.length();i++)
                    {
                        placesList[i] = array.getJSONObject(i).getString("cname");
                    }
                    placesAdapter = new ArrayAdapter<String>(DetailStepActivity.this,android.R.layout.simple_spinner_item,placesList);
                    placesSpinner.setAdapter(placesAdapter);
                }
                catch(JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e("ErrorHappened", error.getMessage(), error);
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

        mQueue.add(getPlacesRequest);


        mAutoCompTextView = (AutoCompleteTextView)findViewById(R.id.autoCompTextView);

        //設定PeopleName裡面的內容
        HttpClient HttpgetNameArray = new HttpClient(mQueue);
        HttpgetNameArray.getPeopleNameArray(new HttpClient.GetPeopleNameArrayResponseListener() {
            @Override
            public void setPersonNameArray(String[] peopleNameArray)
            {
                PeopleName = peopleNameArray;
                mAdapAutoCompText = new ArrayAdapter<CharSequence>(DetailStepActivity.this, android.R.layout.simple_dropdown_item_1line, PeopleName);
                //mAdapAutoCompText = ArrayAdapter.createFromResource(DetailStepActivity.this,R.array.PeopleName,android.R.layout.simple_dropdown_item_1line);
                mAutoCompTextView.setAdapter(mAdapAutoCompText);

            }
        });




        unitsSpinner.setOnItemSelectedListener(unitsSpinnerOnItemSelect);
        placesSpinner.setOnItemSelectedListener(placesSpinnerOnItemSelect);


    }

//==============================================================================================================
    AdapterView.OnItemSelectedListener unitsSpinnerOnItemSelect = new AdapterView.OnItemSelectedListener()
    {
        public void onItemSelected(AdapterView<?> adapterView, View view,int position, long arg3)
        {
            //將文字選單中的 unit 文字 轉成 unit_no
            httpSwitchUnitClient = new HttpClient(mQueue);
            httpSwitchUnitClient.switchToUnit_no(adapterView.getSelectedItem().toString());


            //Toast.makeText(DetailStepActivity.this, "你選的是"+adapterView.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent)
        {

        }

    };

    AdapterView.OnItemSelectedListener placesSpinnerOnItemSelect = new AdapterView.OnItemSelectedListener()
    {
        public void onItemSelected(AdapterView<?> adapterView, View view,int position, long arg3)
        {
            //將文字選單中的 places文字 轉換成places_id
            httpSwitchPlacesClient = new HttpClient(mQueue);
            httpSwitchPlacesClient.switchToPlaces_id(adapterView.getSelectedItem().toString());


            //Toast.makeText(DetailStepActivity.this, "你選的是"+adapterView.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent)
        {

        }

    };


    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener()
    {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem)
        {

            switch (menuItem.getItemId())
            {
                case R.id.action_edit:
                    if(ACCESS_TOKEN.length()==0)
                    {
                        Toast.makeText(DetailStepActivity.this,"必須先登入才能編輯步驟",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        stepExamine.setEnabled(true);
                        person.setEnabled(true);
                        placesSpinner.setEnabled(true);
                        unitsSpinner.setEnabled(true);
                    }


                    break;

                case R.id.action_upload:
                    if(ACCESS_TOKEN.length()==0)
                    {
                        Toast.makeText(DetailStepActivity.this,"必須先登入才能上傳步驟",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        stepExamine.setEnabled(false);
                        person.setEnabled(false);
                        placesSpinner.setEnabled(false);
                        unitsSpinner.setEnabled(false);

                        //將文字選單的文字轉換成  id
                        unit_no= httpSwitchUnitClient.getUnit_no();
                        Log.v("LookupForUnitNo",unit_no);
                        places_id= httpSwitchPlacesClient.getPlaces_id();
                        Log.v("LookupForPlaceID",Integer.toString(places_id));

                        //把 people 的所有資料 get 下來轉換成 people id
                        StringRequest getPeopleRequest = new StringRequest("http://140.115.3.188:3000/sop/v1/people", new Response.Listener<String>()
                        {
                            @Override
                            public void onResponse(String response)
                            {
                                Log.d("getPeopleSuccessful", response);
                                try
                                {
                                    JSONArray array = new JSONArray(response);
                                    for(int i=0;i<array.length();i++)
                                    {
                                        if(person.getText().toString().equals(array.getJSONObject(i).getString("cname")))
                                        {

                                            people_id = Integer.parseInt(array.getJSONObject(i).getString("id"));
                                            HttpClient httpClient =new HttpClient(mQueue);
                                            Log.v("LookupParam","Unit_no :"+unit_no+" places_id :"+places_id);
                                            httpClient.putEditStepResult(DetailStepActivity.this, stepExamine.getText().toString(), Integer.toString(people_id) , unit_no ,places_id , ACCESS_TOKEN , stepID);

                                            break;
                                        }

                                    }

                                }
                                catch(JSONException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error)
                            {
                                Log.e("getPeopleErrorHappen", error.getMessage(), error);
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

                        mQueue.add(getPeopleRequest);

                    }

                    break;



                case R.id.action_settings:
                    ;
                    break;
            }


            return true;
        }
    };


    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_ballot, menu);
        return true;
    }



}
