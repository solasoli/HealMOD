package es.usc.citius.servando.calendula.activities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


import java.util.ArrayList;
import java.util.Timer;

import es.usc.citius.servando.calendula.CalendulaActivity;
import es.usc.citius.servando.calendula.ChartDBHelper;
import es.usc.citius.servando.calendula.R;

public class ChartActivity extends CalendulaActivity  {

    Button button;
    EditText xInput,yInput;
    GraphView graph;
    LineGraphSeries<DataPoint> series;
    ChartDBHelper chartDB;
    SQLiteDatabase sqLiteDatabase;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        button=(Button) findViewById(R.id.button4);
        xInput=(EditText) findViewById(R.id.editText2);
        yInput=(EditText) findViewById(R.id.editText3);
        graph=(GraphView) findViewById(R.id.chartengine);

        chartDB= new ChartDBHelper(this);
        sqLiteDatabase=chartDB.getWritableDatabase();

        exqButton();


}

    private void exqButton() {
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                int xVal=Integer.parseInt(String.valueOf(xInput.getText()));
                int yVal=Integer.parseInt(String.valueOf(yInput.getText()));

                chartDB.insertData(xVal, yVal);

                series= new LineGraphSeries<DataPoint>(getData());
                graph.addSeries(series);
            }
        });
    }

    private DataPoint[] getData() {
        String[] columns={"xValues", "yValues"};
        Cursor cursor=sqLiteDatabase.query("MyTable", columns,null, null,null,null);

        DataPoint[] dp=new DataPoint[cursor.getCount()];

        for (int i=0;i<cursor.getCount();i++) {
            cursor.moveToNext();
            dp[i]=new DataPoint(cursor.getInt(0),cursor.getInt(1));
        }
        return dp;
    }

}