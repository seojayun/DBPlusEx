package com.example.dbplusex;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ListIterator;

public class MainActivity extends AppCompatActivity {

//1.파일을 DB로 변환
//2.변환 후 테이블 설정
//3. 안드로이드 스튜디오에서 assets폴더에 복사
//4. 프로그램으로 assets 폴더에 있는 DB를  /data/data/패키지이름/dataabases폴더 에 복사
//5.   /data/data/패키지이름/dataabases 에있는 DB 활용


class  dataSet {   // 필드의 갯수에 맞게 클래스 선언
    String Data[] = new String[10];
}

    SQLiteDatabase sqlDB;
    Spinner spinnerAdd,spinnerName;

    String path = "/data/data/com.example.dbplusex/databases/patHDB.db";
    ArrayList<dataSet> arrayList = new ArrayList<>();
    ArrayList<String> arrayListSP1 = new ArrayList<>();
    Integer tvResult[] = {R.id.tv1, R.id.tv2, R.id.tv3, R.id.tv4, R.id.tv5, R.id.tv6, R.id.tv7};
    TextView tv[] = new TextView[tvResult.length];





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spinnerAdd = (Spinner)findViewById(R.id.spinnerAdd);
        spinnerName = (Spinner)findViewById(R.id.spinnerName);
        for(int i =0; i<tvResult.length; i++) {
            tv[i] = (TextView)findViewById(tvResult[i]);
        }
        boolean bResult = isCheckDB(this);
        try {
        if(bResult==false) {
            copyDB(this);
        }
        }catch (Exception e) {//파일을 못 읽어 올때 처리
           showToast("파일을 읽을 수 없습니다.");
        }

        sqlDB=SQLiteDatabase.openDatabase(path,null,SQLiteDatabase.OPEN_READONLY);  //일기장에서는 db를 불러들여왔지만 지금은 db를 생성하는 것이 아니고 카피해서 사용한다.
                                        //(db경로,factory값,읽기전용);
        Cursor cursor = sqlDB.rawQuery("select * from patHTBL", null);

        while (cursor.moveToNext() != false) {
            dataSet d = new dataSet();
            for (int i = 0; i < d.Data.length; i++) {
                d.Data[i] = cursor.getString(i);
                if (i == 0) {
                    if (!arrayListSP1.contains(cursor.getString(i))) {
                        arrayListSP1.add(cursor.getString(i));
                    }
                }
            }
            arrayList.add(d);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_dropdown_item, arrayListSP1);
        spinnerAdd.setAdapter(adapter);
        spinnerAdd.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ArrayList<String> arrayListSP2 = new ArrayList<>();
                ListIterator<dataSet> lit = arrayList.listIterator();
                while (lit.hasNext()) {
                    dataSet d = lit.next();
                    if (arrayListSP1.get(position).equals(d.Data[0])) {
                        arrayListSP2.add(d.Data[1]);
                    }
                }
                Collections.sort(arrayListSP2);
                ArrayAdapter<String> adapter1 = new ArrayAdapter<>(getApplicationContext(),
                        android.R.layout.simple_spinner_dropdown_item, arrayListSP2);
                spinnerName.setAdapter(adapter1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ListIterator<dataSet> lit = arrayList.listIterator();
                while (lit.hasNext()) {
                    dataSet d = lit.next();
                    if (d.Data[1].equals(spinnerName.getAdapter().getItem(position))) {
                        tv[0].setText("소재지=" + d.Data[0]);
                        tv[1].setText("병원이름=" + d.Data[1]);
                        tv[2].setText("개업일=" + d.Data[2]);
                           if(d.Data[3].equals("폐업")) {
                               tv[3].setText("현재상태=" + d.Data[3] + "  폐업일자 : " + d.Data[4]);
                           }else if(d.Data[3].equals("말소")){
                               tv[3].setText("현재상태=" + d.Data[3] + "  말소일자 : " + d.Data[4]);
                           }else if(d.Data[3].equals("휴업")){
                               tv[3].setText("현재상태=" + d.Data[3] + "현재 휴업 중입니다.");
                           }else {
                               tv[3].setText("현재상태=" + d.Data[3]);
                           }

                        tv[4].setText("전화번호=" + d.Data[5]);
                        tv[5].setText("우편번호=" + d.Data[6]);
                        tv[6].setText("주소=" + d.Data[7]);


                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



    }
    public boolean isCheckDB(Context context) { // db가 있는지 없는지 확인해주는 메소드 + db가 갱신(늘어나거나 줄어드는 경우)되면 db를 복사 해주는 역할도 한다.
        String filePath = "/data/data/com.example.dbplusex/databases/patHDB.db";
        File file = new File(filePath);
        long newdb_size = 0;
        long olddb_size = file.length();  //파일의 크기 값이 들어간다.
        AssetManager manager = context.getAssets();  //asset 폴더에 접근시킬 수 있는 클래스
        try {
            InputStream is = manager.open("patHDB.db");   //파일을 읽어오는 명령어 중에 앱 안에 읽어오는 명령어는 InputStream
            newdb_size=is.available();

        }catch (IOException e) {  //파일을 못 읽어 올때 처리
            showToast("파일을 읽을 수 없습니다.");
        }
        if(file.exists()) {  //파일이 존재한다면
            if(newdb_size!=olddb_size) { //폴더안에 있는  patHDB.db와 assets폴더 안에 있는 patHDB.db 크기가 다른 경우
                return false;
            }else {
                return true;
            }
        }return  false;
    }
    public void copyDB(Context context) {  // assets폴더안의 db파일을      /data/data/패키지이름/dataabases폴더 생성 후 db파일을 복사해주는 역활을 하는 메소드
        AssetManager manager = context.getAssets();
        String folderPath = "/data/data/com.example.dbplusex/databases";
        String filePath = "/data/data/com.example.dbplusex/databases/patHDB.db";
        File folder = new File(folderPath);
        File file = new File(filePath);
        FileOutputStream  fos = null;  //파일을 복사에서 보내야되기 때문에 FileOutputStream
        BufferedOutputStream bos = null;  //파일을 옮기는 역할을 하는 것이 Buffer 이다. BufferedOutputStream
        try {
            InputStream is = manager.open("patHDB.db");
            BufferedInputStream bis = new BufferedInputStream(is);
            if(! folder.exists()) { //처음에 폴더가 존재하지 않는다면
                folder.mkdir(); //폴더 생성
            }
            if(file.exists()) { //파일이 존재한다면,   //폴더안에 있는  patHDB.db와 assets폴더 안에 있는 patHDB.db 크기가 다른 경우
                file.delete();  //기존 파일 삭제
                file.createNewFile(); //새로운 파일 생성해준다.
            }

            //파일처리 구문
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            int read = -1;
            byte buffer[] = new byte[1024];   //파일의 크기가 1mb를 넘어가면 안되기 때문
            while ((read=bis.read(buffer,0,1024)) != -1) {  //bis에는 현재 patHDB.db 정보가 있음. read 담아주는 역할
                bos.write(buffer,0,read); //bos에도 현재 patHDB.db 정보가 다 담겨있다.
            }
            bos.flush(); //정리  -->비워놔야 다음에 또 사용이 가능하다.
            bos.close(); //닫기
            fos.close(); //닫기
            bis.close(); //닫기
            is.close(); //닫기
        }catch (IOException e) { //파일을 복사 할 수 없을 때 처리
            showToast("파일을 복사 할 수 없습니다.");
        }
    }
    void showToast (String msg) {
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }

}
