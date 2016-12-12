package com.github.karlicoss.fenum_android_demo;

import android.app.Activity;
import android.database.Cursor;
import android.util.Log;

import com.github.karlicoss.checker_example_annotations.FolderId;
import com.github.karlicoss.checker_example_annotations.MessageId;


public class MainActivity extends Activity {

    private static final String FENUM_RETURN_TYPE = "fenum:return.type.incompatible";

    /**
     * @return count of messages in the specified folder
     */
    public static long getMessagesCount(@FolderId long folderId) {
        Cursor result = null; // SQL count(*) query goes here
        return result.getLong(0);
    }

    /**
     * @return folder of the specified message
     */
    @SuppressWarnings(FENUM_RETURN_TYPE)
    @FolderId
    public static long getFolderId(@MessageId long messageId) {
        Cursor result = null; // SQL query filtering message by id and projecting folderId goes here
        return result.getLong(0);
    }

    @SuppressWarnings(FENUM_RETURN_TYPE)
    @MessageId
    public static long getLastUnreadMessage() {
        Cursor result = null; // SQL querying last unread message goes here
        return result.getLong(0);
    }

    public static void getMessagesCount_good() {
        long lastMessageId = getLastUnreadMessage();
        long folderId = getFolderId(lastMessageId);
        long count = getMessagesCount(folderId);
        Log.d("TAG", "Count: " + count);
    }

    public static void getMessagesCount_bad1() {
        long folderId = getFolderId(0);
        // oops, we passed an arbitrary long instead of actual message id
        long count = getMessagesCount(folderId);
        Log.d("TAG", "Count: " + count);
    }

    public static void getMessagesCount_bad2() {
        long lastMessageId = getLastUnreadMessage();
        long count = getMessagesCount(lastMessageId);
        // oops, we passed message id instead of folder id
        Log.d("TAG", "Count: " + count);
    }

    public static void getMessagesCount_bad3() {
        long lastMessageId = getLastUnreadMessage();
        long folderId = getFolderId(lastMessageId);
        long c = getMessagesCount(folderId);
        long count = getMessagesCount(c);
        // oops, we pased plain long instead of folder ID
        Log.d("TAG", "Count: " + count);
    }
}
