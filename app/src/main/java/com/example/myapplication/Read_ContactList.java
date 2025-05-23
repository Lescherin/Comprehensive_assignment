package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent; 
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.provider.ContactsContract; // 确保 ContactsContract 被导入
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;


public class Read_ContactList extends AppCompatActivity {

    private Button buttonSyncToSystem;
    private Button buttonViewSystemContacts;
    private ListView contactsView;
    private EditText editTextName;
    private EditText editTextPhone;
    private EditText editTextEmail;
    private Button buttonAddContact;
    private Button buttonUpdateContact;
    private Button buttonDeleteContact;

    private ArrayAdapter<String> adapter;
    private List<String> contactsList = new ArrayList<>();
    private List<Long> contactIds = new ArrayList<>(); // 用于存储联系人的ID，方便更新和删除

    private ContactsDbHelper dbHelper;
    private SQLiteDatabase database;

    private ContactSyncHelper contactSyncHelper; // 用于本地应用中的同步联系人

    private long selectedContactId = -1; // 用于存储当前选中的联系人ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.read_contactlist);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("本地联系人管理");
            actionBar.setDisplayHomeAsUpEnabled(true); // 显示返回按钮
        }



        dbHelper = new ContactsDbHelper(this);
        database = dbHelper.getWritableDatabase(); // 获取可写数据库文件
        contactSyncHelper = new ContactSyncHelper(this, database);
        // 初始化 ContactSyncHelper用于同步联系人

        editTextName = findViewById(R.id.edit_text_name);
        editTextPhone = findViewById(R.id.edit_text_phone);
        editTextEmail = findViewById(R.id.edit_text_email);

        buttonAddContact = findViewById(R.id.button_add_contact);
        buttonUpdateContact = findViewById(R.id.button_update_contact);
        buttonDeleteContact = findViewById(R.id.button_delete_contact);

        buttonSyncToSystem = findViewById(R.id.button_sync_to_system);
        buttonViewSystemContacts = findViewById(R.id.button_view_system_contacts);

        contactsView = findViewById(R.id.contacts_view);

        //用适配器做一个列表显示数据逐条记录
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, contactsList);
        contactsView.setAdapter(adapter);

        loadContactsFromDb(); // 从数据库加载联系人

        buttonAddContact.setOnClickListener(v -> addContact());
        buttonUpdateContact.setOnClickListener(v -> updateContact());
        buttonDeleteContact.setOnClickListener(v -> deleteContact());

        buttonSyncToSystem.setOnClickListener(v -> contactSyncHelper.requestContactsPermissions());
        // 使用 helper
        buttonViewSystemContacts.setOnClickListener(v -> openSystemContactsApp());



        contactsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 当列表项被点击时，填充 EditText 并记录所选联系人的 ID
                selectedContactId = contactIds.get(position);
                // 直接从数据库加载该联系人的信息
                loadContactDetailsToForm(selectedContactId);
            }
        });
    }
    
    //跳转到联系人
    private void openSystemContactsApp() {
        Intent intent = new Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "无法打开联系人应用", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 将结果传递给 ContactSyncHelper 进行处理
        contactSyncHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


//数据库部分:
    //查询所选记录
    private void loadContactDetailsToForm(long contactId) {
        Cursor cursor = database.query(
                ContactsDbHelper.TABLE_CONTACTS,
                new String[]{ContactsDbHelper.COLUMN_NAME, ContactsDbHelper.COLUMN_PHONE,ContactsDbHelper.COLUMN_EMAIL},
                ContactsDbHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(contactId)},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsDbHelper.COLUMN_NAME));
            @SuppressLint("Range") String phone = cursor.getString(cursor.getColumnIndex(ContactsDbHelper.COLUMN_PHONE));
            @SuppressLint("Range") String email = cursor.getString(cursor.getColumnIndex(ContactsDbHelper.COLUMN_EMAIL));
            editTextName.setText(name);
            editTextPhone.setText(phone);
            editTextEmail.setText(email);
            cursor.close();
        }
    }

    //加载数据库中的联系人信息
    private void loadContactsFromDb() {
        contactsList.clear();
        contactIds.clear(); // 清空ID列表

        Cursor cursor = database.query(
                ContactsDbHelper.TABLE_CONTACTS,
                null, // null 表示选择所有列
                null, null, null, null,
                ContactsDbHelper.COLUMN_NAME + " ASC" // 按姓名顺序排序
        );
        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") long id = cursor.getLong(cursor.getColumnIndex(ContactsDbHelper.COLUMN_ID));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsDbHelper.COLUMN_NAME));
                @SuppressLint("Range") String phone = cursor.getString(cursor.getColumnIndex(ContactsDbHelper.COLUMN_PHONE));
                @SuppressLint("Range") String email = cursor.getString(cursor.getColumnIndex(ContactsDbHelper.COLUMN_EMAIL));

                contactsList.add("姓名: " + name + "\n电话: " + phone +"\n邮箱: " + email);
                contactIds.add(id); // 添加对应的ID
            }
            cursor.close();
        }
        adapter.notifyDataSetChanged();
    }

    //添加一个记录
    private void addContact() {
        String name = editTextName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();


        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "请输入姓名", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "请输入电话号码", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(ContactsDbHelper.COLUMN_NAME, name);
        values.put(ContactsDbHelper.COLUMN_PHONE, phone);
        values.put(ContactsDbHelper.COLUMN_EMAIL, email);

        try {
            long newRowId = database.insertOrThrow(ContactsDbHelper.TABLE_CONTACTS, null, values);
            // 如果执行到这里，说明插入成功
            Toast.makeText(this, "联系人已添加", Toast.LENGTH_SHORT).show();
            loadContactsFromDb(); // 重新加载列表

        } catch (SQLiteConstraintException e) {
            // 捕获数据库中的约束异常
             // 检查错误信息是否与 UNIQUE(name) 相关
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE constraint failed: " + ContactsDbHelper.TABLE_CONTACTS + "." + ContactsDbHelper.COLUMN_NAME)) {
                 Toast.makeText(this, "添加失败：姓名 '" + name + "' 已存在。", Toast.LENGTH_LONG).show();
            } else {
                // 其他约束错误，例如 NOT NULL 约束
                Toast.makeText(this, "添加失败：数据不符合约束。", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            // 捕获其他可能的数据库异常
            Toast.makeText(this, "添加失败，发生未知数据库错误。", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    //更新所选记录
    private void updateContact() {
        if (selectedContactId == -1) {
            Toast.makeText(this, "请先选择一个联系人进行更新", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = editTextName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();

        //对联系人,号码做非空校验检查
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "姓名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "电话号码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }


        ContentValues values = new ContentValues();
        values.put(ContactsDbHelper.COLUMN_NAME, name);
        values.put(ContactsDbHelper.COLUMN_PHONE, phone);
        values.put(ContactsDbHelper.COLUMN_EMAIL, email);

        int rowsAffected = database.update(
                ContactsDbHelper.TABLE_CONTACTS,
                values,
                ContactsDbHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(selectedContactId)}
        );

        if (rowsAffected > 0) {
            Toast.makeText(this, "联系人已更新", Toast.LENGTH_SHORT).show();
            loadContactsFromDb(); // 重新加载列表
        } else {
            Toast.makeText(this, "更新失败或未找到联系人", Toast.LENGTH_SHORT).show();
        }
    }

    //删除所选记录
    private void deleteContact() {
        if (selectedContactId == -1) {
            Toast.makeText(this, "请先选择一个联系人进行删除", Toast.LENGTH_SHORT).show();
            return;
        }


        int rowsAffected = database.delete(
                ContactsDbHelper.TABLE_CONTACTS,
                ContactsDbHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(selectedContactId)}
        );


        if (rowsAffected > 0) {
            Toast.makeText(this, "联系人已删除", Toast.LENGTH_SHORT).show();
            loadContactsFromDb(); // 重新加载列表
        } else {
            Toast.makeText(this, "删除失败或未找到联系人", Toast.LENGTH_SHORT).show();
        }
    }



    // 当 Activity 销毁时，关闭数据库连接
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

}