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
    public static final int PERMISSIONS_REQUEST_CONTACTS = 101; // 用于识别联系人权限请求的请求码

    private final Activity activity;
    private final SQLiteDatabase database;

    public ContactSyncHelper(Activity activity, SQLiteDatabase database) {
        this.activity = activity;
        this.database = database;
    }

    /*
     请求读取和写入联系人的权限。
     */
    public void requestContactsPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissionsToRequest = new ArrayList<>();
            // 检查是否已授予写入联系人权限
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.WRITE_CONTACTS);
            }
            // 检查是否已授予读取联系人权限
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_CONTACTS);
            }

            if (!permissionsToRequest.isEmpty()) {
                // 如果有尚未授予的权限，则发起请求
                ActivityCompat.requestPermissions(activity, permissionsToRequest.toArray(new String[0]), PERMISSIONS_REQUEST_CONTACTS);
            } else {
                // 所有必需的权限都已授予
                syncContactsToSystem();
            }
        } else {
            // Android 6.0 以下版本，权限在安装时授予
            syncContactsToSystem();
        }
    }

    /**
     * 处理权限请求的结果。
     * 当用户响应权限请求对话框后，此方法被调用。
     *
     * @param requestCode  权限请求的请求码
     * @param permissions  请求的权限数组
     * @param grantResults 授权结果数组
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CONTACTS) {
            boolean writeGranted = false;
            boolean readGranted = false;

            // 遍历权限请求结果
            for (int i = 0; i < permissions.length; i++) {
                if (Manifest.permission.WRITE_CONTACTS.equals(permissions[i])) {
                    writeGranted = (grantResults[i] == PackageManager.PERMISSION_GRANTED);
                }
                if (Manifest.permission.READ_CONTACTS.equals(permissions[i])) {
                    readGranted = (grantResults[i] == PackageManager.PERMISSION_GRANTED);
                }
            }

            if (writeGranted && readGranted) {
                // 如果读取和写入权限都被授予，则开始同步
                syncContactsToSystem();
            } else {
                // 如果有任何一个权限被拒绝，则提示用户
                StringBuilder message = new StringBuilder("权限被拒绝：");
                if (!writeGranted) message.append("写入联系人 ");
                if (!readGranted) message.append("读取联系人 ");
                Toast.makeText(activity, message.toString().trim(), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * 检查指定的电话号码是否已存在于系统通讯录中。
     *
     * @param phoneNumber 要检查的电话号码
     * @return 如果联系人存在于系统中，则返回 true；否则返回 false。
     * 如果电话号码为空或没有读取联系人权限，也返回 false。
     */
    private boolean isContactInSystem(String phoneNumber) {
        // 检查读取联系人权限，如果未授予，则无法可靠检查，并记录警告
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "没有读取联系人权限，无法可靠检查现有联系人。");
            // 在没有权限的情况下，返回 false，避免潜在的重复添加，但这不是最佳实践。
            // 理想情况下，应确保在调用此方法前已获得权限。
            return false;
        }

        // 如果电话号码为空，则无法进行有效查找
        if (TextUtils.isEmpty(phoneNumber)) {
            return false;
        }

        // 构建用于查询联系人的 URI，通过电话号码过滤
        Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        // 定义查询结果中需要返回的列，这里只需要 _ID 来确认联系人是否存在
        String[] projection = new String[]{ContactsContract.PhoneLookup._ID};

        Cursor c = null;
        try {
            // 执行查询
            c = activity.getContentResolver().query(lookupUri, projection, null, null, null);
            if (c != null && c.moveToFirst()) {
                // 如果查询结果不为空且可以移动到第一条记录，说明找到了匹配的联系人
                Log.d(TAG, "电话号码为 " + phoneNumber + " 的联系人已存在于系统中。");
                return true;
            }
        } finally {
            // 确保 Cursor 被关闭，释放资源
            if (c != null) {
                c.close();
            }
        }
        // 未找到匹配的联系人
        return false;
    }

    /**
     * 将本地 SQLite 数据库中的联系人同步到系统通讯录。
     * 此方法会遍历本地数据库中的所有联系人，
     * 对于每个联系人，首先检查其是否已存在于系统通讯录中（通过电话号码）。
     * 如果不存在，则将其添加到系统通讯录。
     * 最后会提示同步结果（成功、跳过、失败的数量）。
     */
    public void syncContactsToSystem() {
        // 检查数据库是否已准备好
        if (database == null || !database.isOpen()) {
            Toast.makeText(activity, "数据库未准备好", Toast.LENGTH_SHORT).show();
            return;
        }

        // 从本地数据库查询所有联系人
        Cursor cursor = database.query(
                ContactsDbHelper.TABLE_CONTACTS, // 表名
                new String[]{ContactsDbHelper.COLUMN_NAME, ContactsDbHelper.COLUMN_PHONE, ContactsDbHelper.COLUMN_EMAIL}, // 要查询的列
                null, null, null, null, null // 其他查询参数（selection, selectionArgs, groupBy, having, orderBy）
        );

        // 检查查询结果是否有效
        if (cursor == null) {
            Toast.makeText(activity, "无法读取本地联系人数据库", Toast.LENGTH_SHORT).show();
            return;
        }

        // 检查本地数据库是否有联系人
        if (cursor.getCount() == 0) {
            Toast.makeText(activity, "本地数据库没有联系人可以同步", Toast.LENGTH_SHORT).show();
            cursor.close();
            return;
        }

        int contactsSynced = 0; // 同步成功的联系人数量
        int contactsSkipped = 0; // 因已存在而跳过的联系人数量
        int contactsFailed = 0; // 同步失败的联系人数量
        ContentResolver contentResolver = activity.getContentResolver(); // 获取 ContentResolver 用于与系统通讯录交互

        // 遍历本地数据库中的每个联系人
        while (cursor.moveToNext()) {
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsDbHelper.COLUMN_NAME));
            @SuppressLint("Range") String phone = cursor.getString(cursor.getColumnIndex(ContactsDbHelper.COLUMN_PHONE));
            @SuppressLint("Range") String email = cursor.getString(cursor.getColumnIndex(ContactsDbHelper.COLUMN_EMAIL));

            // 检查联系人是否已存在于系统通讯录
            if (isContactInSystem(phone)) {
                Log.d(TAG, "联系人 " + name + " (电话: " + phone + ") 已存在于系统中，跳过。");
                contactsSkipped++;
                continue; // 跳过当前联系人，处理下一个
            }

            // 创建一个 ContentProviderOperation 列表，用于批量插入联系人数据
            ArrayList<ContentProviderOperation> ops = new ArrayList<>();

            // 插入原始联系人记录 (Raw Contact)
            // 这是联系人数据的容器，一个联系人可以有多个原始联系人（例如，来自不同账户的同一个联系人）
            ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null) // 对于本地联系人，账户类型和名称通常为 null
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());

            // 如果姓名不为空，则插入姓名数据
            if (!TextUtils.isEmpty(name)) {
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0) // 引用上面插入的 RawContact 的 ID
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE) // 数据类型为结构化姓名
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name) // 设置显示名称
                        .build());
            }

            // 如果电话号码不为空，则插入电话号码数据
            if (!TextUtils.isEmpty(phone)) {
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0) // 引用 RawContact ID
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE) // 数据类型为电话号码
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone) // 设置电话号码
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) // 设置电话类型为手机
                        .build());
            }

            // 如果电子邮件不为空，则插入电子邮件数据
            if (!TextUtils.isEmpty(email)) {
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0) // 引用 RawContact ID
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE) // 数据类型为电子邮件
                        .withValue(ContactsContract.CommonDataKinds.Email.DATA, email) // 设置电子邮件地址
                        .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK) // 设置电子邮件类型为工作
                        .build());
            }

            try {
                // 如果操作列表不为空（即有数据需要插入），则执行批量操作
                if (!ops.isEmpty()) {
                    contentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
                    contactsSynced++;
                }
            } catch (RemoteException | OperationApplicationException e) {
                // 捕获并记录同步过程中可能发生的异常
                Log.e(TAG, "同步联系人 " + name + " 失败: " + e.getMessage());
                contactsFailed++;
            }
        }
        cursor.close(); // 关闭数据库游标

        // 构建并显示同步结果信息
        StringBuilder resultMessage = new StringBuilder();
        if (contactsSynced > 0) resultMessage.append(contactsSynced).append(" 个联系人已同步。");
        if (contactsSkipped > 0) resultMessage.append(contactsSkipped).append(" 个联系人已存在并跳过。");
        if (contactsFailed > 0) resultMessage.append(contactsFailed).append(" 个联系人同步失败。");

        // 根据同步结果显示不同的提示信息
        if (resultMessage.length() == 0 && (cursor.getCount() - contactsSkipped > 0)) {
            // 如果没有成功、跳过或失败的记录，但确实尝试了同步（总数 - 跳过数 > 0），
            // 这可能意味着所有尝试同步的联系人都失败了，或者存在其他未处理的情况。
            Toast.makeText(activity, "所有可同步的本地联系人似乎已存在于系统中或同步失败。", Toast.LENGTH_LONG).show();
        } else if (resultMessage.length() > 0) {
            // 显示汇总的同步结果
            Toast.makeText(activity, resultMessage.toString(), Toast.LENGTH_LONG).show();
        } else if (cursor.getCount() > 0 && contactsSkipped == cursor.getCount()){
            // 如果所有本地联系人都已存在于系统中
            Toast.makeText(activity, "所有本地联系人已存在于系统中。", Toast.LENGTH_LONG).show();
        }
        // 如果 cursor.getCount() == 0，在方法开头已经处理了，这里不需要额外处理。
        // 如果 resultMessage.length() == 0 且 (cursor.getCount() - contactsSkipped == 0)，
        // 意味着所有联系人都被跳过了，或者没有联系人。这种情况会被上面的 "所有本地联系人已存在于系统中" 或"本地数据库没有联系人可以同步" 覆盖
    }
}