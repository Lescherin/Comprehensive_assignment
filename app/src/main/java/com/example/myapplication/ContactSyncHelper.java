package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class ContactSyncHelper {

    private static final String TAG = "ContactSyncHelper";
    public static final int PERMISSIONS_REQUEST_CONTACTS = 101;

    private Activity activity;
    private SQLiteDatabase database;

    public ContactSyncHelper(Activity activity, SQLiteDatabase database) {
        this.activity = activity;
        this.database = database;
    }

    public void requestContactsPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissionsToRequest = new ArrayList<>();
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.WRITE_CONTACTS);
            }
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_CONTACTS);
            }

            if (!permissionsToRequest.isEmpty()) {
                ActivityCompat.requestPermissions(activity, permissionsToRequest.toArray(new String[0]), PERMISSIONS_REQUEST_CONTACTS);
            } else {
                // Permissions already granted
                syncContactsToSystem();
            }
        } else {
            // Pre-M, permissions are granted at install time
            syncContactsToSystem();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CONTACTS) {
            boolean writeGranted = false;
            boolean readGranted = false;

            for (int i = 0; i < permissions.length; i++) {
                if (Manifest.permission.WRITE_CONTACTS.equals(permissions[i])) {
                    writeGranted = (grantResults[i] == PackageManager.PERMISSION_GRANTED);
                }
                if (Manifest.permission.READ_CONTACTS.equals(permissions[i])) {
                    readGranted = (grantResults[i] == PackageManager.PERMISSION_GRANTED);
                }
            }

            if (writeGranted && readGranted) {
                syncContactsToSystem();
            } else {
                StringBuilder message = new StringBuilder("权限被拒绝：");
                if (!writeGranted) message.append("写入联系人 ");
                if (!readGranted) message.append("读取联系人 ");
                Toast.makeText(activity, message.toString().trim(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean isContactInSystem(String phoneNumber) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "没有读取联系人权限，无法可靠检查现有联系人。");
            // 如果没有权限，我们不能假设联系人不存在，但为了避免意外行为，这里返回false
            // 更好的做法是在请求权限时就处理好，确保有权限再调用此方法
            return false;
        }

        if (TextUtils.isEmpty(phoneNumber)) {
            return false; // 没有电话号码，难以唯一识别
        }

        Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        String[] projection = new String[]{ContactsContract.PhoneLookup._ID}; // 只需要ID来确认存在性

        Cursor c = activity.getContentResolver().query(lookupUri, projection, null, null, null);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    Log.d(TAG, "电话号码为 " + phoneNumber + " 的联系人已存在于系统中。");
                    return true; // 找到匹配项
                }
            } finally {
                c.close();
            }
        }
        return false;
    }

    public void syncContactsToSystem() {
        if (database == null || !database.isOpen()) {
            Toast.makeText(activity, "数据库未准备好", Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor cursor = database.query(
                ContactsDbHelper.TABLE_CONTACTS,
                new String[]{ContactsDbHelper.COLUMN_NAME, ContactsDbHelper.COLUMN_PHONE, ContactsDbHelper.COLUMN_EMAIL},
                null, null, null, null, null
        );

        if (cursor == null) {
            Toast.makeText(activity, "无法读取本地联系人数据库", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cursor.getCount() == 0) {
            Toast.makeText(activity, "本地数据库没有联系人可以同步", Toast.LENGTH_SHORT).show();
            cursor.close();
            return;
        }

        int contactsSynced = 0;
        int contactsSkipped = 0;
        int contactsFailed = 0;
        ContentResolver contentResolver = activity.getContentResolver();

        while (cursor.moveToNext()) {
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsDbHelper.COLUMN_NAME));
            @SuppressLint("Range") String phone = cursor.getString(cursor.getColumnIndex(ContactsDbHelper.COLUMN_PHONE));
            @SuppressLint("Range") String email = cursor.getString(cursor.getColumnIndex(ContactsDbHelper.COLUMN_EMAIL));

            if (isContactInSystem(phone)) {
                Log.d(TAG, "联系人 " + name + " (电话: " + phone + ") 已存在于系统中，跳过。");
                contactsSkipped++;
                continue;
            }

            ArrayList<ContentProviderOperation> ops = new ArrayList<>();

            ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());

            if (!TextUtils.isEmpty(name)) {
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                        .build());
            }

            if (!TextUtils.isEmpty(phone)) {
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                        .build());
            }

            if (!TextUtils.isEmpty(email)) {
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Email.DATA, email)
                        .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                        .build());
            }

            try {
                if (!ops.isEmpty()) {
                    contentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
                    contactsSynced++;
                }
            } catch (RemoteException | OperationApplicationException e) {
                Log.e(TAG, "同步联系人 " + name + " 失败: " + e.getMessage());
                contactsFailed++;
            }
        }
        cursor.close();

        StringBuilder resultMessage = new StringBuilder();
        if (contactsSynced > 0) resultMessage.append(contactsSynced).append(" 个联系人已同步。");
        if (contactsSkipped > 0) resultMessage.append(contactsSkipped).append(" 个联系人已存在并跳过。");
        if (contactsFailed > 0) resultMessage.append(contactsFailed).append(" 个联系人同步失败。");

        if (resultMessage.length() == 0 && (cursor.getCount() - contactsSkipped > 0)) { // 检查是否有实际尝试同步的联系人
            Toast.makeText(activity, "所有可同步的本地联系人似乎已存在于系统中或同步失败。", Toast.LENGTH_LONG).show();
        } else if (resultMessage.length() > 0) {
            Toast.makeText(activity, resultMessage.toString(), Toast.LENGTH_LONG).show();
        } else if (cursor.getCount() > 0 && contactsSkipped == cursor.getCount()){
            Toast.makeText(activity, "所有本地联系人已存在于系统中。", Toast.LENGTH_LONG).show();
        }
    }
}