// IBookManager.aidl
package com.example.aidl_library;
// Declare any non-default types here with import statements
import com.example.sourcelib.model.Book;
import com.example.aidl_library.IOnNewBookArrivedListener;

interface IBookManager {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    List<Book> getBookList();
    void addBook(in Book book);
    void registerListener(in IOnNewBookArrivedListener listener);
    void unRegisterListener(in IOnNewBookArrivedListener listener);

}
