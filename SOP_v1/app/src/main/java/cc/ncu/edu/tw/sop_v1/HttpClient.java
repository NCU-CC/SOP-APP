package cc.ncu.edu.tw.sop_v1;

import android.content.Context;
import android.location.GpsStatus;
import android.util.Log;
import android.widget.Spinner;
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

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.ErrorListener;

/**
 * Created by jason on 2016/7/2.
 */
public class HttpClient
{
    private Step[] step =new Step[30];
    private int changeIndex;

    private RequestQueue mQueue;
    private String ACCESS_TOKEN;

    //經過轉換後得到的unit 、 places 、 people  ID
    private String unit_no;
    private int places_id;
    private int people_id;

    //紀錄在units 、 places Spinner中的第幾個位置
    private int unitsIndex;
    private int placesIndex;

    private String PersonName;

    //用在DetailStepActivity的HttpClient
    public HttpClient(RequestQueue mQueue)
    {
        this.mQueue = mQueue;
    }

    //用在MyAdapter中的HttpClient
    public HttpClient(RequestQueue mQueue,String accessToken)
    {
        this.mQueue = mQueue;
        ACCESS_TOKEN = accessToken;
    }

    //用在add_new_one的HttpClient
    public HttpClient(Step step[],String accessToken,RequestQueue mQueue)
    {
        this.step = step;
        this.ACCESS_TOKEN = accessToken;
        this.mQueue = mQueue;
    }
//======================================================================
    //Listener
    private GetUnitResponseListener getResponseListener;
    public interface GetUnitResponseListener
    {
        public void setUnitSpinnerIndex(int index);
    }

    private GetPlacesResponseListener getPlacesResponseListener;
    public interface GetPlacesResponseListener
    {
        public void setPlacesSpinnerIndex(int index);
    }

    private GetPeopleResponseListener getPeopleResponseListener;
    public interface GetPeopleResponseListener
    {
        public void setPeopleIndex(String name);
    }
//========================================================================

    //刪除平步驟做的調整
    public void putParaDeletChange(int changeindex)
    {
        changeIndex = changeindex;

        StringRequest stringRequest = new StringRequest(Request.Method.PUT, "http://140.115.3.188:3000/sop/v1/steps/"+Integer.toString(step[changeIndex].getId()), new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.d("putchangeSuccessful", response);
            }
        }, new Response.ErrorListener()
        {
            public void onErrorResponse(VolleyError error)
            {
                Log.e("putchangeErrorHappen", error.getMessage(), error);
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
                //Log.v("e[changeIndex].inner", Integer.toString(step[changeIndex].getLayer()));
                //Log.v("changeIndex",Integer.toString(changeIndex));
                map.put("prev", Integer.toString(step[changeIndex].getLayer()));
                return map;
            }
        };
        mQueue.add(stringRequest);

    };

    //刪除一般步驟做的調整
    public void putComDeletChange(int changeindex)
    {
        //做完調整後上傳的動作 (put)
        changeIndex = changeindex;

        StringRequest stringRequest = new StringRequest(Request.Method.PUT, "http://140.115.3.188:3000/sop/v1/steps/"+Integer.toString(step[changeIndex].getId()), new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.d("CStepPutSuccessful", response);
            }
        }, new Response.ErrorListener()
        {
            public void onErrorResponse(VolleyError error)
            {
                Log.e("CStepPutErrorHappen", error.getMessage(), error);
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
                map.put("next", Integer.toString(step[changeIndex].getSequence()));

                return map;
            }
        };

        mQueue.add(stringRequest);
    }


    //新增平行步驟做的調整
    public void putParaAddChange(int changeindex)
    {
        changeIndex = changeindex;
        //做完調整後上傳的動作 (put)
        StringRequest stringRequest = new StringRequest(Request.Method.PUT, "http://140.115.3.188:3000/sop/v1/steps/"+Integer.toString(step[changeIndex].getId()), new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.d("PstepPutSuccessful", response);

            }
        }, new Response.ErrorListener()
        {
            public void onErrorResponse(VolleyError error)
            {
                Log.e("PstepPutErrorHappen", error.getMessage(), error);
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
                map.put("prev", Integer.toString(step[changeIndex].getLayer()));
                //Log.v("prev", Integer.toString(step[changeIndex].getLayer()));
                return map;
            }
        };

        mQueue.add(stringRequest);
    }

    //新增一般步驟所做的調整
    public void putComAddChange(int changeindex)
    {
        changeIndex = changeindex;

        //做完調整後上傳的動作 (put)
        StringRequest stringRequest = new StringRequest(Request.Method.PUT, "http://140.115.3.188:3000/sop/v1/steps/"+Integer.toString(step[changeIndex].getId()), new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.d("putSuccessful", response);
            }
        }, new Response.ErrorListener()
        {
            public void onErrorResponse(VolleyError error)
            {
                Log.e("putErrorHappen", error.getMessage(), error);
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
                map.put("next", Integer.toString(step[changeIndex].getSequence()));

                return map;
            }
        };

        mQueue.add(stringRequest);
    }







    //做文字選單 變成 unit_no 的轉換
    public void switchToUnit_no(String text)
    {
        final String full_name;
        full_name =text;
        StringRequest stringRequest = new StringRequest("http://140.115.3.188:3000/sop/v1/units", new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.d("getUnitsSuccess", response);
                try
                {
                    JSONArray array = new JSONArray(response);
                    for(int i=0;i<array.length();i++)
                    {
                        if( full_name.equals(array.getJSONObject(i).getString("full_name")))
                        {
                            unit_no = array.getJSONObject(i).getString("unit_no");
                            Log.v("unit_no",unit_no);

                            break;
                        }
                    }

                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e("getUnitsErrorHappen", error.getMessage(), error);
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

        mQueue.add(stringRequest);

    }

    public String getUnit_no()
    {
        return unit_no;
    }


    //做文字選單 變成 places  id 的轉換
    public void switchToPlaces_id(String text)
    {
        final String cname =text;
        StringRequest stringRequest = new StringRequest("http://140.115.3.188:3000/sop/v1/places", new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.d("getPlacesSuccess", response);

                try
                {
                    JSONArray array = new JSONArray(response);
                    for(int i=0;i<array.length();i++)
                    {
                        if( cname.equals(array.getJSONObject(i).getString("cname")))
                        {
                            places_id = Integer.parseInt(array.getJSONObject(i).getString("id"));
                            Log.v("places_id: ",Integer.toString(places_id));
                        }
                    }


                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e("getPlacesErrorHappen", error.getMessage(), error);

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

        mQueue.add(stringRequest);
    }

    public int getPlaces_id()
    {
        return places_id;
    }

    private searchCopyStepAndPostListener searchCopyStepAndPostListener;
    public interface searchCopyStepAndPostListener
    {
        public void getCopyPostProjectId(int id);
    }


    //post複製的專案到後端並取得專案的ID (步驟的Flow id)
    public void searchCopyStepAndPost(final String copyProjectName,final searchCopyStepAndPostListener searchCopyStepAndPostListener)
    {
        StringRequest postCopyProjectRequest = new StringRequest(Request.Method.POST, "http://140.115.3.188:3000/sop/v1/processes/", new Response.Listener<String>() {
            @Override
            public void onResponse(String response)
            {
                try
                {
                    Log.d("postProjectRequestSucce", response);
                    JSONObject object =new JSONObject(response);
                    searchCopyStepAndPostListener.getCopyPostProjectId(Integer.parseInt(object.getString("id").toString()));

                } catch (JSONException e)
                {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener()
        {
            public void onErrorResponse(VolleyError error)
            {
                Log.e("postPrjRequestErrHappen", error.getMessage(), error);
            }

        })
        {
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> map = new HashMap<String, String>();
                map.put("Authorization", "Bearer" + " " + ACCESS_TOKEN);
                return map;
            }


            public Map<String, String> getParams() throws AuthFailureError
            {
                Map<String, String> map = new HashMap<>();
                map.put("name",copyProjectName+"(複製)");
                return map;
            }
        };

        mQueue.add(postCopyProjectRequest);
    }




    //get Step資料時將PersonId 轉換成人名
    public void switchPersonIdToName(int id,final GetPeopleResponseListener getPeopleResponseListener)
    {
        final int ID =id;
        StringRequest stringRequest = new StringRequest("http://140.115.3.188:3000/sop/v1/people", new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.d("getPeopleSuccess", response);

                try
                {
                    JSONArray array = new JSONArray(response);
                    for(int i=0;i<array.length();i++)
                    {
                        if(ID == Integer.parseInt(array.getJSONObject(i).getString("id")))
                        {
                            PersonName = array.getJSONObject(i).getString("cname");
                            Log.v("PersonName",PersonName);
                            getPeopleResponseListener.setPeopleIndex(PersonName);

                            break;
                        }

                    }



                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e("getPeopleError", error.getMessage(), error);
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

        mQueue.add(stringRequest);

    }

    public String getPersonName()
    {
        return PersonName;
    }


    public void ToUnitsIndexInSpinner(String text,final GetUnitResponseListener getResponseListener)
    {
        final String string = text;

        StringRequest stringRequest = new StringRequest("http://140.115.3.188:3000/sop/v1/units", new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.d("GetUnitsIndexSuccess", response);

                try
                {
                    JSONArray array = new JSONArray(response);
                    for(int i=0;i<array.length();i++)
                    {
                        if(string.equals(array.getJSONObject(i).getString("unit_no")))
                        {
                            unitsIndex = i;
                            Log.v("unitsIndexInSpinner", Integer.toString(unitsIndex));
                            getResponseListener.setUnitSpinnerIndex(unitsIndex);
                            break;
                        }
                    }

                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e("GetUnitsIndexError", error.getMessage(), error);
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

        mQueue.add(stringRequest);

    }

    public void setUnitsIndex(int index)
    {
        unitsIndex =index;
    }

    public int getUnitsIndex()
    {
        Log.v("ReturnUnitsIndex", Integer.toString(unitsIndex));
        return unitsIndex;
    }

    public void ToPlacesIndexInSpinner(int id,final GetPlacesResponseListener getResponseListener)
    {
        final int ID = id;
        StringRequest apiRequest = new StringRequest(" http://140.115.3.188:3000/sop/v1/places", new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.d("GetPlacesIndexSuccess", response);

                try {
                    JSONArray array = new JSONArray(response);
                    for(int i=0;i<array.length();i++)
                    {
                        if(ID ==Integer.parseInt(array.getJSONObject(i).getString("id")))
                        {
                            placesIndex =i;
                            Log.v("placesIndexInSpinner",Integer.toString(placesIndex));
                            getResponseListener.setPlacesSpinnerIndex(placesIndex);
                            break;
                        }
                    }



                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e("GetPlacesIndexError", error.getMessage(), error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("X-Ncu-Api-Token", "e763cac7e011b72f1e5d8668cb661070bd130f2109c920a76ca4adb3e540018fcf69115961abae35b0c23a4d27dd7782acce7b75c9dd066053eb0408cb4575b9");
                return map;
            }
        };

        mQueue.add(apiRequest);

    }

    public int getPlacesIndex()
    {
        Log.v("ReturnPlacesIndex", Integer.toString(placesIndex));
        return placesIndex;
    }





    //將編輯後的結果上傳 put 後端
    public void putEditStepResult(Context mContext,String items,String PersonId, String UnitId, int PlaceId, String accesstoken,int stepId)
    {
        final String item =items;
        final String PID =PersonId;

        final String UID =UnitId;
        final int PlaceID =PlaceId;
        final String ACCESS_TOKEN = accesstoken;
        int stepID = stepId;
        final Context context = mContext;


        //將編輯後的結果上傳 put 後端
        StringRequest putRequest = new StringRequest(Request.Method.PUT, "http://140.115.3.188:3000/sop/v1/steps/"+Integer.toString(stepID), new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.d("putEditSuccessful", response);
                Toast.makeText(context, "上傳成功", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener()
        {
            public void onErrorResponse(VolleyError error)
            {
                Log.e("putEditErrorHappen", error.getMessage(), error);
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
                Map<String, String> map = new HashMap<>();
                map.put("items",  item);
                Log.v("items",  item);
                map.put("PersonId", PID);
                Log.v("PersonId", PID);
                map.put("UnitId", UID);
                Log.v("UnitId", UID);
                map.put("PlaceId", Integer.toString(PlaceID));
                Log.v("PlaceId", Integer.toString(PlaceID));

                return map;
            }
        };
        mQueue.add(putRequest);

    }


    //將被複製的專案內的步驟上傳到後端
    public void upLoadCopySteps(final Step step)
    {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://140.115.3.188:3000/sop/v1/steps", new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                try
                {
                    Log.d("PostCopyItemSuccessful", response);

                    JSONObject object = new JSONObject(response);
                    step.setId(Integer.parseInt(object.getString("id")));
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener()
        {
            public void onErrorResponse(VolleyError error)
            {
                Log.e("PostCopyItemErrorHappen", error.getMessage(), error);
            }
        })
        {
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + ACCESS_TOKEN);
                //Log.v("GetAccessToken", ACCESS_TOKEN);
                return map;
            }


            public Map<String, String> getParams() throws AuthFailureError
            {
                Map<String, String> map = new HashMap<>();
                map.put("action", step.getContent());
                map.put("items", step.getItem());
                map.put("prev", Integer.toString(step.getLayer()));
                map.put("next", Integer.toString(step.getSequence()));
                map.put("Flow_id", Integer.toString(step.getBelong()));
                map.put("PersonId", step.getPerson());
                map.put("UnitId", step.getUnit());
                map.put("PlaceId", step.getPlace());


                //Log測試
                Log.v("action", step.getContent());
                Log.v("items",step.getItem());
                Log.v("prevInner", Integer.toString(step.getLayer()));
                Log.v("nextInner",Integer.toString(step.getSequence()));
                Log.v("Flow_idInner", Integer.toString(step.getBelong()));
                Log.v("PersonId", step.getPerson());
                Log.v("UnitId", step.getUnit());
                Log.v("PlaceId",step.getPlace());

                return map;
            }
        };

        mQueue.add(stringRequest);
    }


    public void uploadMovedStep(final Step step,final int groupPosition,final int layer)
    {

        StringRequest stringRequest = new StringRequest(Request.Method.PUT, "http://140.115.3.188:3000/sop/v1/steps/"+Integer.toString(step.getId()), new Response.Listener<String>() {

            @Override
            public void onResponse(String response)
            {
                Log.d("uploadMovedStepSuccess", response);
            }
        }, new Response.ErrorListener(){
            public void onErrorResponse(VolleyError error)
            {
                Log.e("uploadMovedStepError", error.getMessage(), error);
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
                map.put("prev", Integer.toString(layer));


                return map;
            }
        };
        mQueue.add(stringRequest);

    }


    public String[] getPeopleNameArray(final GetPeopleNameArrayResponseListener getPeopleNameArrayResponseListener)
    {
        StringRequest getPeopleNameArrayRequest = new StringRequest("http://140.115.3.188:3000/sop/v1/people", new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.d("getPeopleNameSucc", response);
                try
                {

                    JSONArray array = new JSONArray(response);
                    String[] PeopleName = new String[array.length()];

                    for(int i=0;i<array.length();i++)
                    {
                        PeopleName[i] = array.getJSONObject(i).getString("cname");
                        getPeopleNameArrayResponseListener.setPersonNameArray(PeopleName);
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
                Log.e("getPeopleNameError", error.getMessage(), error);
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

        mQueue.add(getPeopleNameArrayRequest);
        return null;
    }

    public interface GetPeopleNameArrayResponseListener
    {
        public void setPersonNameArray(String[] peopleNameArray);
    }




    //刪除專案後,連帶刪除專按內的步驟
    public void deleteStepsInProject(int id)
    {
        StringRequest deleteStepsInProjectRequest = new StringRequest(Request.Method.DELETE, "http://140.115.3.188:3000/sop/v1/steps/"+String.valueOf(id), new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.d("deleteStepsInProject", response);
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error)
            {
                Log.e("deleteStepsInPrjError", error.getMessage(), error);
            }

        })
        {
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer"+ " "+ACCESS_TOKEN);
                return map;
            }
        };

        mQueue.add(deleteStepsInProjectRequest);
    }







}
