package cc.ncu.edu.tw.sop_v1;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class ballot extends AppCompatActivity {

    EditText stepExamine,unit,people,place;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ballot);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(onMenuItemClick);
/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        */
        stepExamine = (EditText)findViewById(R.id.editStepExamine);
        unit = (EditText)findViewById(R.id.editUnit);
        people = (EditText)findViewById(R.id.editPeople);
        place = (EditText)findViewById(R.id.editPlace);

        //初始EditText內容
        stepExamine.setText("去抽籤");
        unit.setText("場地組");
        people.setText("王小明");
        place.setText("行政大樓");

    }


    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            String msg = "";

            switch (menuItem.getItemId()) {
                case R.id.action_edit:
                    msg += "Click edit";
                    stepExamine.setEnabled(true);
                    unit.setEnabled(true);
                    people.setEnabled(true);
                    place.setEnabled(true);
                    break;

                case R.id .action_upload:
                    msg += "Click upload";
                    stepExamine.setEnabled(false);
                    unit.setEnabled(false);
                    people.setEnabled(false);
                    place.setEnabled(false);

                case R.id.action_settings:
                    msg += "Click setting";
                    break;
            }

            if(!msg.equals("")) {
                Toast.makeText(ballot.this, msg, Toast.LENGTH_SHORT).show();
            }
            return true;
        }
    };


    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ballot, menu);
        return true;
    }




}
