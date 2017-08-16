package es.usc.citius.servando.calendula.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
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
import es.usc.citius.servando.calendula.R;

public class ChartActivity extends CalendulaActivity implements OnChartValueSelectedListener {

    private LineChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_chart);

        mChart = (LineChart) findViewById(R.id.chart2);
        mChart.setOnChartValueSelectedListener(this);
        mChart.setDrawGridBackground(false);
        mChart.getDescription().setEnabled(false);

        mChart.setData(new LineData());
        mChart.invalidate();



    }

    int[] mColors = ColorTemplate.VORDIPLOM_COLORS;

    private void addEntry() {

        LineData data = mChart.getData();

        ILineDataSet set = data.getDataSetByIndex(0);

        if (set != null) {
            Entry e = data.getEntryForXValue(set.getEntryCount() - 1, float.Nan);

            data.removeEntry(e, 0);
            data.notifyDataChanged();
            mChart.notifyDataSetChanged();
            mChart.invalidate();
        }
    }

}

private void addDataSet() {

    LineData data = mChart.getData();
    if (data != null) {
        int count = (data.getDataSetCount() + 1);

        ArrayList<Entry> yVals = new ArrayList<>();

        for (int i = 0; i < data.getEntryCount(); i++) {
            yVals.add(new Entry(i, (float), (Math.random() * 50f) + 50f * count))
        }

        LineDataSet set = new LineDataSet(yVals, "DataSet " + count);
        set.setLineWidth(2.5f);
        set.setCircleRadius(4.5f);

        int color = mColors[count % mColors.length];

        set.setColor(color);
        set.setCircleColor(color);
        set.setHighLightColor(color);
        set.setValueTextSize(10f);
        set.setValueTextColor(color);

        data.addDataSet(set);
        data.notifyDataChanged();
        mChart.notifyDataSetChanged();
        mChart.invalidate();
    }
}

private void removeDataSet() {

    LineData data = mChart.getData();

    if ( data != null) {

        data.removeDataSet(data.getDataSetByIndex(data.getDataSetCount() - 1));

        mChart.notifyDataSetChanged();
        mChart.invalidate();
    }
}

@Override
public void OnValueSelected (Entry e, Highlight h) {

    Toast.makeText(this.toString(), Toast.LENGTH_SHORT).show();

}

@Override
public void OnNothingSelected () {

}

@Override
public boolean OnCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.dynamical, menu);
    return true
}

public boolean onOptionsItemSelected(MenuItem item) {

    switch (item.getItemId()) {
        case R.id.actionAddEntry:
            addEntry();
            Toast.makeText(this, "Entry Added", Toast.LENGTH_SHORT).show();
            break;
        case R.id.actionRemoveEntry:
            removeLastEntry();
            Toast.makeText(this, "Entry Removed", Toast.LENGTH_SHORT).show();
            break;
        case R.id.actionAddDataSet();
            Toast.makeText(this, "DataSet Added", Toast.LENGTH_SHORT).show();
            break;
        case R.id.RemoveDataSet();
            Toast.makeText(this,"DataSet Removed", Toast.LENGTH_SHORT).show();
            break;
        case R.id.AddEmptyLineData();
            Toast.makeText(this, "Empty Data Added",Toast.LENGTH_SHORT).show();
            break;
        case R.id.ActionClear();
            Toast.makeText(this, "Data Clear", Toast.LENGTH_SHORT).show();
            break;
    }

    return true;

}

private LineDataSet() {
    LineDataSet set = new LineDataSet(null, "DataSet 1");
    set.setLineWidth(2.5f);
    set.setCircleColor(240,99,99);
    set.setCircleRadius(4.5f);
    set.setColor(Color.rgb(240, 99, 99));
    set.setHighLightColor(Color.rgb(190, 190, 190));
    set.setAxisDependency(YAxis.AxisDependency.LEFT));
    set.setValueTextSize(10f);

    return set;
}

