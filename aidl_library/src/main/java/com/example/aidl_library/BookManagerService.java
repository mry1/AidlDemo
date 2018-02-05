package com.example.aidl_library;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.sourcelib.model.Book;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by louis on 18-1-17.
 */

public class BookManagerService extends Service {

    private static final String TAG = "BMS";
    private AtomicBoolean mIsServiceDestroyed = new AtomicBoolean();

    private CopyOnWriteArrayList<Book> mBookList = new CopyOnWriteArrayList<>();
    //    private CopyOnWriteArrayList<IOnNewBookArrivedListener> mBookListenerList = new CopyOnWriteArrayList<>();
    private RemoteCallbackList<IOnNewBookArrivedListener> mBookListenerList = new RemoteCallbackList<>();


    public BookManagerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBookList.add(new Book(1, "Android"));
        mBookList.add(new Book(2, "IOS"));
        new Thread(new ServiceWorker()).start();

    }

    @Override
    public void onDestroy() {
        mIsServiceDestroyed.set(true);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        int check = checkCallingOrSelfPermission("com.example.aidl_library.ACCESS_BOOK_SERVICE");
        if (check == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(getApplicationContext(), "onBind:没有权限", Toast.LENGTH_SHORT).show();
            return null;
        }
        return new MyBinder();
    }

    class MyBinder extends IBookManager.Stub {

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int check = checkCallingOrSelfPermission("com.example.aidl_library.ACCESS_BOOK_SERVICE");
            if (check == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(getApplicationContext(), "onTransact:没有权限", Toast.LENGTH_SHORT).show();
                return false;
            }
            String[] packages = getPackageManager().getPackagesForUid(getCallingUid());
            String packageName = null;
            if (packages != null && packages.length > 0) {
                packageName = packages[0];
            }
            if (!packageName.startsWith("com.example")) {
                Toast.makeText(getApplicationContext(), "onTransact:没有权限22", Toast.LENGTH_SHORT).show();
                return false;
            }
            return super.onTransact(code, data, reply, flags);
        }

        @Override
        public List<Book> getBookList() throws RemoteException {
            return mBookList;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            System.out.println("添加成功");
            mBookList.add(book);
        }

        @Override
        public void registerListener(IOnNewBookArrivedListener listener) throws RemoteException {
//            if (!mBookListenerList.contains(listener)) {
//                mBookListenerList.add(listener);
//            } else {
//                Log.d(TAG, "已经存在这个listener");
//            }
//            Log.d(TAG, "registerListener, size:" + mBookListenerList.size());

            mBookListenerList.register(listener);

            int n = mBookListenerList.beginBroadcast();
            mBookListenerList.finishBroadcast();
            Log.d(TAG, "registerListener, size:" + n);
        }

        @Override
        public void unRegisterListener(IOnNewBookArrivedListener listener) throws RemoteException {
//            if (mBookListenerList.contains(listener)) {
//                mBookListenerList.remove(listener);
//                Log.d(TAG, "成功移除");
//            } else {
//                Log.d(TAG, "unRegisterListener失败");
//            }
            mBookListenerList.unregister(listener);

            int n = mBookListenerList.beginBroadcast();
            mBookListenerList.finishBroadcast();
            Log.d(TAG, "unRegisterListener, size:" + n);


        }


    }

    private class ServiceWorker implements Runnable {

        @Override
        public void run() {
            while (!mIsServiceDestroyed.get()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int bookId = mBookList.size() + 1;
                Book newBook = new Book(bookId, "new book#" + bookId);
                try {
                    onNewBookArrived(newBook);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void onNewBookArrived(Book newBook) throws RemoteException {
        mBookList.add(newBook);
//        for (int i = 0; i < mBookListenerList.size(); i++) {
//            mBookListenerList.get(i).onNewBookArrived(newBook);
//        }

        int n = mBookListenerList.beginBroadcast();
        for (int i = 0; i < n; i++) {
            IOnNewBookArrivedListener l = mBookListenerList.getBroadcastItem(i);
            l.onNewBookArrived(newBook);
        }
        mBookListenerList.finishBroadcast();

    }
}
