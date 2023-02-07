package com.company.bboxprinter;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.company.bboxprinter.databinding.ActivityMainBinding;

// add
import android.content.Context;
import android.app.AlertDialog;
import android.provider.Settings;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

// printer import
import com.sunmi.peripheral.printer.InnerPrinterCallback;
import com.sunmi.peripheral.printer.InnerPrinterException;
import com.sunmi.peripheral.printer.InnerPrinterManager;
import com.sunmi.peripheral.printer.SunmiPrinterService;

public class MainActivity extends AppCompatActivity {

    // printer varible
    //---------------------------------
    public static int NoSunmiPrinter = 0x00000000;
    public static int CheckSunmiPrinter = 0x00000001;
    public static int FoundSunmiPrinter = 0x00000002;
    public static int LostSunmiPrinter = 0x00000003;

    public static int giEnablePrinter = 0;  // 0-disable , 1-enable

    public int sunmiPrinter = CheckSunmiPrinter;
    private SunmiPrinterService sunmiPrinterService;

    Intent intent;

    // main varible
    //---------------------------------
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // background service
        //----------------------------------------
        //EventBus.getDefault().register(this);

        Log.v("Jeffrey","Call Foreground");
        intent = new Intent(MainActivity.this, ForegroundService.class);
        startService(intent);
        Log.v("Jeffrey", "服務已建立 .. 01");


        // on create
        //----------------------------------------
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // printer init
        //-----------------
        if (giEnablePrinter==1)
            initSunmiPrinterService(this); //this);

        /*
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        switch(sunmiPrinter)
        {
        case 0:
            builder.setTitle("列表機狀態");
            builder.setMessage("沒發現列表機");
            builder.create().show();
            break;
        case 1:
            builder.setTitle("列表機狀態");
            builder.setMessage("偵測列表機 : 查詢中");
            break;
        case 2:
            builder.setTitle("列表機狀態");
            builder.setMessage("偵測列表機 : 開啟");
            break;
        case 3:
            builder.setTitle("列表機狀態");
            builder.setMessage("偵測列表機 : 關閉");
            break;
        default:
            builder.setTitle("列表機狀態");
            builder.setMessage("偵測列表機 : 未知狀況");
            break;

        }
        builder.create().show();

         */
}



    // Method to start the service
    public void startService(View view) {
        startService(new Intent(getBaseContext(), ForegroundService.class));
    }

    // Method to stop the service
    public void stopService(View view) {
        stopService(new Intent(getBaseContext(), ForegroundService.class));
    }

    public void initSunmiPrinterService(Context context) {
        try {
            boolean ret =  InnerPrinterManager.getInstance().bindService(context,
                    innerPrinterCallback);
            if(!ret){
                sunmiPrinter = NoSunmiPrinter;
            }
        } catch (InnerPrinterException e) {
            e.printStackTrace();
        }
    }

    public void deInitSunmiPrinterService(Context context){
        try {
            if(sunmiPrinterService != null){
                InnerPrinterManager.getInstance().unBindService(context, innerPrinterCallback);
                sunmiPrinterService = null;
                sunmiPrinter = LostSunmiPrinter;
            }
        } catch (InnerPrinterException e) {
            e.printStackTrace();
        }
    }

    // #002 printer functions
    //----------------------------------------------------------------
    public void initPrinter(){
        if(sunmiPrinter == NoSunmiPrinter){ //sunmiPrinterService == null){
            //TODO Service disconnection processing
            return;
        }
        try {
            sunmiPrinterService.printerInit(null);
        } catch (RemoteException e) {
            handleRemoteException(e);
        }
    }


    public void PrinterSelfCheck(){
        if(sunmiPrinter == NoSunmiPrinter){ //sunmiPrinterService == null){
            //TODO Service disconnection processing
            return;
        }
        try {
            sunmiPrinterService.printerSelfChecking(null);
        } catch (RemoteException e) {
            handleRemoteException(e);
        }
    }

    public void printerInfo(){
        if(sunmiPrinter == NoSunmiPrinter){ //sunmiPrinterService == null){
            Log.v("Jeffrey", "no printer...");
            return;
        }
        try {
            Log.v("Jeffrey", String.format("列表機接口類型 : %s",sunmiPrinterService.getPrinterModal()));
            Log.v("Jeffrey", String.format("列表機機板序號 : %s",sunmiPrinterService.getPrinterSerialNo()));
            Log.v("Jeffrey", String.format("列表機固件版號 : %s",sunmiPrinterService.getPrinterVersion()));
            Log.v("Jeffrey", String.format("列表機最新狀態 : %d",sunmiPrinterService.updatePrinterState()));
            Log.v("Jeffrey", String.format("列表機紙張規格 : %d",sunmiPrinterService.getPrinterPaper()));
            //Log.v("Jeffrey", String.format("列表機打印長度 : %s",sunmiPrinterService.getPrintedLength(null)));
            Log.v("Jeffrey", "-------------------------------------------");
            Log.v("Jeffrey", String.format("走紙距離   : %d",sunmiPrinterService.getPrinterBBMDistance()));
            Log.v("Jeffrey", String.format("列表機模式 : %d",sunmiPrinterService.getPrinterMode()));
        } catch (RemoteException e) {
            handleRemoteException(e);
        }
    }


    private void handleRemoteException(RemoteException e){
        //TODO process when get one exception
    }


    //-----------------------------------------------------------------------------------
    InnerPrinterCallback innerPrinterCallback = new InnerPrinterCallback(){
        @Override
        protected void onConnected(SunmiPrinterService service){
            //這⾥即獲取到綁定服務成功連接後的遠端服務接⼝控制碼
            //可以通過service調⽤⽀持的列印⽅法
            sunmiPrinterService = service;
            checkSunmiPrinterService(service);

            //Log.v("Jeffrey", String.format("sunmiPrinterService = %x", sunmiPrinterService));

            initPrinter();
            //PrinterSelfCheck();
            printerInfo();
            printText();
            printBarcode();
            printGoLine();

            //AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            //builder.setTitle("列表機狀態");
            //builder.setMessage("連結列表機.");
            //builder.create().show();


        }
        @Override
        protected void onDisconnected() {
            //當服務異常斷開後，會回檔此⽅法，建議在此做重連策略
            sunmiPrinterService = null;
            sunmiPrinter = LostSunmiPrinter;

            Log.v("Jeffrey", "列表機狀態 : 列表機斷開!!");

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("列表機狀態");
            builder.setMessage("列表機斷開!!");
            builder.create().show();
        }
    };


    private void checkSunmiPrinterService(SunmiPrinterService service){
        boolean ret = false;
        try {
            ret = InnerPrinterManager.getInstance().hasPrinter(service);

            if (ret)
            {
                Log.v("Jeffrey", "偵測到列表機");
            }
            else
            {
                Log.v("Jeffrey", "沒發現列表機");
            }


        } catch (InnerPrinterException e) {
            e.printStackTrace();
        }
        sunmiPrinter = ret?FoundSunmiPrinter:NoSunmiPrinter;
    }


    // printer functions
    //------------------------------------------------------------
    public void printText() {
        try {
            sunmiPrinterService.sendRAWData(new byte[]{0x1B, 0x45, 0x1},null);
            sunmiPrinterService.printText("洗衣櫃名 : 松平六門測試\n", null);
            sunmiPrinterService.printText("洗衣櫃號 : 099004\n", null);
            sunmiPrinterService.printText("洗衣格門 : 1\n", null);
            sunmiPrinterService.printText("送洗時間 : 2022-06-21 17:56:35\n", null);
            sunmiPrinterService.printText("客人姓名 : 王小明\n", null);
            sunmiPrinterService.printText("客人手機 : 0933333333\n", null);
            sunmiPrinterService.printText("列印張數 : 0\n", null);
        }
        catch (RemoteException e){
            AlertDialog.Builder builder99 = new AlertDialog.Builder(MainActivity.this);
            builder99.setTitle("列表機狀態");
            builder99.setMessage("列表發生問題!!");
            builder99.create().show();
        }
    }

    public void printBarcode() {
        try {
            sunmiPrinterService.printText("   ", null);
            sunmiPrinterService.printBarCode("{C12345678901234567890",8,100,2 ,2,null);
        }
        catch (RemoteException e){
            AlertDialog.Builder builder99 = new AlertDialog.Builder(MainActivity.this);
            builder99.setTitle("列表機狀態");
            builder99.setMessage("列表發生問題!!");
            builder99.create().show();
        }
    }


    public void printGoLine() {
        try {
            sunmiPrinterService.lineWrap(3,null);
        }
        catch (RemoteException e){
            AlertDialog.Builder builder99 = new AlertDialog.Builder(MainActivity.this);
            builder99.setTitle("列表機狀態");
            builder99.setMessage("列表發生問題!!");
            builder99.create().show();
        }
    }

    //sunmiPrinterService.printText("要列印的內容\n",null);
        /*
        try{
            sunmiPrinterService.printText("要列印的內容\n", new InnerResultCallbcak()
            {
                @Override public void onRunResult(boolean isSuccess)
                        throws RemoteException
                {
                    //返回接口執行的情況(並非真實列印):成功或失敗
                }
                @Override
                public void onReturnString(String result)
                        throws RemoteException
                {
                    //部分接口會非同步返回查詢資料
                }
                @Override
                public void onRaiseException(int code, String msg)
                        throws RemoteException
                {
                    //接口執行失敗時，返回的異常狀態說明
                }
                @Override
                public void onPrintResult(int code, String msg)
                throws RemoteException
                {
                    //事務模式下真實的列印結果返回
                }
            }
        });
    } catch (RemoteException e) {
        //如部分接口只能用於指定機型所以會跑出調用接口異常，如錢箱接口只能用於桌上型電腦
    }
    */


    /*
    public void printText(String content, float size, boolean isBold, boolean isUnderLine,
                          String typeface) {
        if(sunmiPrinterService == null){
            //TODO Service disconnection processing
            return;
        }

        try {
            try {
                sunmiPrinterService.setPrinterStyle(WoyouConsts.ENABLE_BOLD, isBold?
                        WoyouConsts.ENABLE:WoyouConsts.DISABLE);
            } catch (RemoteException e) {
                if (isBold) {
                    sunmiPrinterService.sendRAWData(ESCUtil.boldOn(), null);
                } else {
                    sunmiPrinterService.sendRAWData(ESCUtil.boldOff(), null);
                }
            }
            try {
                sunmiPrinterService.setPrinterStyle(WoyouConsts.ENABLE_UNDERLINE, isUnderLine?
                        WoyouConsts.ENABLE:WoyouConsts.DISABLE);
            } catch (RemoteException e) {
                if (isUnderLine) {
                    sunmiPrinterService.sendRAWData(ESCUtil.underlineWithOneDotWidthOn(), null);
                } else {
                    sunmiPrinterService.sendRAWData(ESCUtil.underlineOff(), null);
                }
            }
            sunmiPrinterService.printTextWithFont(content, typeface, size, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
    */




}