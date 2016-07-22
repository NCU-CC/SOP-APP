package cc.ncu.edu.tw.sop_v1;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jason on 2016/7/16.
 */
public class MyExpandListAdapter extends BaseExpandableListAdapter
{
    Context ctxt = null;
    //SparseArray<String[]> data = new SparseArray<>();
    List<String[]> data = new ArrayList<>();
    String[] groups = null;



    public MyExpandListAdapter(Context c, String[] groupitems)
    {
        super();
        ctxt = c;
        groups = groupitems;

    }

    @Override
    public String getChild(int groupPosition, int childPosition)
    {
        String[] childs = data.get(groupPosition);
        /*
        if (childs!=null)
        {
            Log.v("getChild",childs[childPosition]);
        }
        */

        if (childs == null)
        {
            return null;
        }
        if (childPosition >= childs.length)
        {
            return null;
        }
        return childs[childPosition];
    }

    @Override
    public long getChildId(int groupPosition, int childPosition)
    {
        return childPosition;
    }






    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
    {
        View row = convertView;
        Log.v("groupPosition",Integer.toString(groupPosition));
        Log.v("childPosition",Integer.toString(childPosition));

        if (row == null)
        {
            LayoutInflater inflater = (LayoutInflater) ctxt.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.child_row, parent, false);
        }


        TextView tv = (TextView) row.findViewById(R.id.childlabel);
        String child = getChild(groupPosition, childPosition);



        if (child != null)
        {
            tv.setText(child);
            Log.v("ChildContent", child);
        }



        //決定顏色
        if(groupPosition % 3==0)
        {
            row.setBackgroundColor(Color.parseColor("#4F9D9D"));
        }
        else if(groupPosition % 3==1)
        {
            row.setBackgroundColor(Color.parseColor("#95CACA"));
        }
        else if(groupPosition % 3==2)
        {
            row.setBackgroundColor(Color.parseColor("#D1E9E9"));
        }




        return row;
    }







    @Override
    public int getChildrenCount(int groupPosition)
    {
        String[] childs = data.get(groupPosition);
        //String[] childs = childsContent[groupPosition];
        if (childs == null)
        {
            return 0;
        }
        return childs.length;
    }

    @Override
    public String getGroup(int groupPosition)
    {
        if (groupPosition >= groups.length)
        {
            return null;
        }
        return groups[groupPosition];
    }

    @Override
    public int getGroupCount()
    {
        return groups.length;
    }

    @Override
    public long getGroupId(int groupPosition)
    {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
    {
        View row = convertView;

        if (row == null)
        {
            LayoutInflater inflater = (LayoutInflater) ctxt.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.group_row, parent, false);
        }

        TextView tv = (TextView) row.findViewById(R.id.grouplabel);

        String group = getGroup(groupPosition);

        if (group != null)
        {
            tv.setText(group);
        }

        //決定顏色
        if(groupPosition % 3==0)
        {
            row.setBackgroundColor(Color.parseColor("#4F9D9D"));
        }
        else if(groupPosition % 3==1)
        {
            row.setBackgroundColor(Color.parseColor("#95CACA"));
        }
        else if(groupPosition % 3==2)
        {
            row.setBackgroundColor(Color.parseColor("#D1E9E9"));
        }
        return row;
    }

    @Override
    public boolean hasStableIds()
    {
        return false;
    }


    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition)
    {

        return true;
    }

    public void addChild(int group, String[] childs)
    {

        data.add(group, childs);
        notifyDataSetChanged();
    }

}
