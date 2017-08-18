package es.usc.citius.servando.calendula;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

/**
 * Created by alfabet on 8/18/17.
 */

public class ChartDBHelper extends SQLiteOpenHelper {

    private Context con;
    public ChartDBHelper(Context context) {
        super(context, "MDatabase", null, 1);
        con=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createTable="create table MyTable (xValues INTEGER, yValues INTEGER);";
        db.execSQL(createTable);
        Toast.makeText(con, "Table Created", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

    }

    public void insertData(int x, int y)
    {
        SQLiteDatabase db=this.getWritableDatabase();


        ContentValues contentValues=new ContentValues();

        contentValues.put("xValues", x);
        contentValues.put("yValues", y);

        db.insert("MyTable",null, contentValues);
        Toast.makeText(con, "Data Submitted", Toast.LENGTH_SHORT).show();
    }
}
