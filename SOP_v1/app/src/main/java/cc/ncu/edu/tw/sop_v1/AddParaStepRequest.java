package cc.ncu.edu.tw.sop_v1;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jason on 2016/6/24.
 */
public class AddParaStepRequest extends Request
{
    private final Response.Listener<String> mListener;

    public AddParaStepRequest(int method,String url,Response.Listener<String> listener, Response.ErrorListener errorListener)
    {
        super(method, url, errorListener);
        mListener = listener;
    }


    @Override
    protected Response parseNetworkResponse(NetworkResponse response)
    {

        return null;
    }

    @Override
    protected void deliverResponse(Object response)
    {

    }


    @Override
    public int compareTo(Object another)
    {
        return 0;
    }
}
