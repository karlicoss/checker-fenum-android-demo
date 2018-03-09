package com.github.karlicoss.fenum_android_demo;

import android.app.Activity;
import android.database.Cursor;
import android.util.Log;

import com.github.karlicoss.checker_example_annotations.FolderId;
import com.github.karlicoss.checker_example_annotations.MessageId;

import java.util.Collection;


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
        long folderId = getFolderId(0); // CI_ERROR_TAG
        // oops, we passed an arbitrary long instead of actual message id
        long count = getMessagesCount(folderId);
        Log.d("TAG", "Count: " + count);
    }

    public static void getMessagesCount_bad2() {
        long lastMessageId = getLastUnreadMessage();
        long count = getMessagesCount(lastMessageId); // CI_ERROR_TAG
        // oops, we passed message id instead of folder id
        Log.d("TAG", "Count: " + count);
    }

    public static void getMessagesCount_bad3() {
        long lastMessageId = getLastUnreadMessage();
        long folderId = getFolderId(lastMessageId);
        long c = getMessagesCount(folderId);
        long count = getMessagesCount(c); // CI_ERROR_TAG
        // oops, we passed plain long instead of folder ID
        Log.d("TAG", "Count: " + count);
    }

    // still, checker framework processes comments
    public static long getTotalMessagesCount_good(Collection<@FolderId Long> ids) {
        long sum = 0;
        // everything ok, with no annotations in for loop, Fenum checker is able to infer @FolderId
        for (Long id : ids) {
            sum += getMessagesCount(id);
        }
        return sum;
    }

    public static long getTotalMessagesCount_bad1(Collection<Long> ids) {
        long sum = 0;
        // oops, we forgot to specify @FolderId on collection elements
        for (@FolderId Long id : ids) {  // CI_ERROR_TAG
            sum += getMessagesCount(id);
        }
        return sum;
    }

    public static long getTotalMessagesCount_bad2(Collection<@FolderId Long> ids) {
        long sum = 0;
        for (Long id : ids) {
            // oops, we passed folder id for function expecting message ID
            sum += getFolderId(id);  // CI_ERROR_TAG
        }
        // oops, we were adding some crap to long instead of regular longs
        return sum; // CI_ERROR_TAG
    }
}
