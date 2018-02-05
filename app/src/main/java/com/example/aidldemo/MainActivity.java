package com.example.aidldemo;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.aidl_library.BookManagerService;
import com.example.aidl_library.IBookManager;
import com.example.aidl_library.IOnNewBookArrivedListener;
import com.example.sourcelib.model.Book;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private IBookManager bookManager;
    private boolean bindService;

    private static final int MESSAGE_NEW_BOOK_ARRIVED = 1;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_NEW_BOOK_ARRIVED:
                    Log.d(TAG, "收到新书:" + msg.obj);

                    break;
            }
        }
    };

    private IOnNewBookArrivedListener onNewBookArrivedListener = new IOnNewBookArrivedListener.Stub() {

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public void onNewBookArrived(Book book) throws RemoteException {
            mHandler.obtainMessage(MESSAGE_NEW_BOOK_ARRIVED, book).sendToTarget();

        }
    };

    /**
     * Binder死亡的回调
     */
    IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient(){
        @Override
        public void binderDied() {

        }
    };

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bookManager = IBookManager.Stub.asInterface(service);
            try {
                service.linkToDeath(mDeathRecipient, 0);// binder死亡回调
                bookManager.addBook(new Book(3, "书籍"));
                List<Book> list = bookManager.getBookList();
                Log.i(TAG, "list type:" + list.getClass().getCanonicalName());
                Log.i(TAG, list.toString());

                bookManager.registerListener(onNewBookArrivedListener);

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bookManager = null;
            Log.d("bookManager::::::::", bookManager + "");
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        final Intent in = new Intent();
        in.setClassName(this, "com.example.aidl_library.BookManagerService");
        in.setPackage("com.example.aidl_library");
        in.setAction("com.example.aidl_library.BookManagerService");

        bindService = bindService(in, conn, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (conn != null && bookManager != null && bookManager.asBinder().isBinderAlive()) {
            try {
                Log.d(TAG, "unRegisterListener:" + onNewBookArrivedListener);
                bookManager.unRegisterListener(onNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            unbindService(conn);
        }
    }

    public void clickButton(View v) {
        if (bookManager == null) {
        } else {
            try {
                Toast.makeText(this, bookManager.getBookList().toString(), Toast.LENGTH_SHORT).show();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
