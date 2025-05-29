package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Read_ContactList Activity
 * 该活动负责管理应用本地存储的联系人列表。
 * 用户可以进行以下操作：
 * 1. 查看本地联系人列表。
 * 2. 添加新的联系人到本地数据库。
 * 3. 更新已存在的本地联系人信息。
 * 4. 删除本地联系人。
 * 5. 将本地联系人同步到系统通讯录（需要权限）。
 * 6. 跳转到系统通讯录应用查看联系人。
 * <p>
 * 联系人信息（姓名、电话、邮箱）存储在 SQLite 数据库中。
 * 通过 ListView 展示联系人，并通过 EditText 字段进行信息的输入和编辑。
 */
public class Read_ContactList extends AppCompatActivity {
    // UI 控件引用
    private Button buttonSyncToSystem; // 同步到系统通讯录按钮
    private Button buttonViewSystemContacts; // 查看系统联系人按钮
    private ListView contactsView; // 显示联系人列表的 ListView
    private EditText editTextName; // 输入/编辑联系人姓名的 EditText
    private EditText editTextPhone; // 输入/编辑联系人电话的 EditText
    private EditText editTextEmail; // 输入/编辑联系人邮箱的 EditText
    private Button buttonAddContact; // 添加联系人按钮
    private Button buttonUpdateContact; // 更新联系人按钮
    private Button buttonDeleteContact; // 删除联系人按钮

    // 数据和适配器
    private ArrayAdapter<String> adapter; //做用于显示数据库列表信息的ArrayList 的适配器
    private final List<String> contactsList = new ArrayList<>(); // 存储用于 ArrayList 显示的联系人信息字符串
    private final List<Long> contactIds = new ArrayList<>(); // 存储每个联系人在数据库中的 ID，与 contactsList 中的项一一对应

    // 数据库相关
    private ContactsDbHelper dbHelper; // 数据库帮助类实例
    private SQLiteDatabase database; // SQLite数据库实例
    private ContactSyncHelper contactSyncHelper; // 辅助类，用于处理联系人同步到系统的逻辑和权限请求

    // 状态变量
    private long selectedContactId = -1; // 当前在 ListView 中选中的联系人的数据库 ID，-1表示未选中

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.read_contactlist);

        // 设置 ActionBar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("本地联系人管理"); // 设置标题
            actionBar.setDisplayHomeAsUpEnabled(true); // 显示返回箭头按钮，依赖于 AndroidManifest.xml 中的 parentActivityName 设置
        }

        // 初始化数据库帮助类和数据库实例
        dbHelper = new ContactsDbHelper(this);
        database = dbHelper.getWritableDatabase(); // 获取可写的数据库引用

        // 初始化联系人同步帮助类
        contactSyncHelper = new ContactSyncHelper(this, database);

        // 绑定 UI 控件
        editTextName = findViewById(R.id.edit_text_name);
        editTextPhone = findViewById(R.id.edit_text_phone);
        editTextEmail = findViewById(R.id.edit_text_email);
        buttonAddContact = findViewById(R.id.button_add_contact);
        buttonUpdateContact = findViewById(R.id.button_update_contact);
        buttonDeleteContact = findViewById(R.id.button_delete_contact);
        buttonSyncToSystem = findViewById(R.id.button_sync_to_system);
        buttonViewSystemContacts = findViewById(R.id.button_view_system_contacts);
        contactsView = findViewById(R.id.contacts_view);

        // 初始化 ListView 的适配器
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, contactsList);
        contactsView.setAdapter(adapter);

        // 从数据库加载已有的联系人数据显示到 ListView
        loadContactsFromDb();

        // 设置按钮点击事件监听器
        buttonAddContact.setOnClickListener(v -> addContact());
        buttonUpdateContact.setOnClickListener(v -> updateContact());
        buttonDeleteContact.setOnClickListener(v -> deleteContact());
        buttonSyncToSystem.setOnClickListener(v -> contactSyncHelper.requestContactsPermissions()); // 点击同步按钮时，请求权限
        buttonViewSystemContacts.setOnClickListener(v -> openSystemContactsApp()); // 点击查看系统联系人按钮

        // 设置 ListView 的项目点击事件监听器
        contactsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 当列表中的某一项被点击时，执行以下操作：
                // 1. 获取被点击项对应的联系人数据库 ID
                selectedContactId = contactIds.get(position);
                // 2. 根据该 ID 从数据库加载完整的联系人详情到表单 (EditTexts) 中，方便用户查看和编辑
                loadContactDetailsToForm(selectedContactId);
            }
        });
    }

    /**
     * 打开系统联系人应用。
     * 使用 Intent.ACTION_VIEW 和 ContactsContract.Contacts.CONTENT_URI 来启动默认的联系人应用。
     */
    private void openSystemContactsApp() {
        Intent intent = new Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI);
        // 检查是否有应用可以处理此 Intent
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "无法打开联系人应用", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 处理权限请求的结果。
     * 此方法是 Activity 的回调，当用户响应权限请求对话框后被调用。
     * 将结果传递给 ContactSyncHelper 类进行后续处理（例如，如果权限被授予，则开始同步）。
     * @param requestCode 请求码，用于识别是哪个权限请求
     * @param permissions 请求的权限数组
     * @param grantResults 用户对每个权限的授权结果数组 (PERMISSION_GRANTED 或 PERMISSION_DENIED)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 将权限请求的结果传递给 contactSyncHelper 进行处理
        contactSyncHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


//数据库部分:

    /**
     * 根据指定的联系人 ID 从数据库加载联系人详细信息，并填充到表单的 EditText 控件中。
     * @param contactId 要加载详情的联系人的数据库 ID。
     */
    private void loadContactDetailsToForm(long contactId) {
        // 查询数据库获取指定 ID 的联系人信息
        Cursor cursor = database.query(
                ContactsDbHelper.TABLE_CONTACTS, // 表名
                new String[]{ContactsDbHelper.COLUMN_NAME, ContactsDbHelper.COLUMN_PHONE, ContactsDbHelper.COLUMN_EMAIL}, // 要查询的列
                ContactsDbHelper.COLUMN_ID + "=?", // 查询条件 (WHERE 子句)
                new String[]{String.valueOf(contactId)}, // 查询条件的参数
                null, null, null // 其他参数 (groupBy, having, orderBy)
        );

        // 如果查询到结果并且 cursor 成功移动到第一行
        if (cursor != null && cursor.moveToFirst()) {
            // 从 cursor 中提取姓名、电话和邮箱
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsDbHelper.COLUMN_NAME));
            @SuppressLint("Range") String phone = cursor.getString(cursor.getColumnIndex(ContactsDbHelper.COLUMN_PHONE));
            @SuppressLint("Range") String email = cursor.getString(cursor.getColumnIndex(ContactsDbHelper.COLUMN_EMAIL));
            // 将提取到的信息设置到对应的 EditText 中
            editTextName.setText(name);
            editTextPhone.setText(phone);
            editTextEmail.setText(email);
            cursor.close(); // 关闭 cursor 释放资源
        }
    }

    /**
     * 从本地 SQLite 数据库加载所有联系人信息，并更新 ListView。
     * 联系人信息包括姓名、电话和邮箱。
     * 结果会按姓名升序排序。
     */
    private void loadContactsFromDb() {
        contactsList.clear(); // 清空当前的联系人显示列表
        contactIds.clear();   // 清空当前的联系人 ID 列表

        // 查询数据库获取所有联系人记录
        Cursor cursor = database.query(
                ContactsDbHelper.TABLE_CONTACTS, // 表名
                null, // 查询所有列
                null, null, null, null, // 无特定查询条件、分组或聚合
                ContactsDbHelper.COLUMN_NAME + " ASC" // 按姓名升序排序 (ORDER BY)
        );

        if (cursor != null) {
            // 遍历查询结果
            while (cursor.moveToNext()) {
                // 从 cursor 中提取联系人信息
                @SuppressLint("Range") long id = cursor.getLong(cursor.getColumnIndex(ContactsDbHelper.COLUMN_ID));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsDbHelper.COLUMN_NAME));
                @SuppressLint("Range") String phone = cursor.getString(cursor.getColumnIndex(ContactsDbHelper.COLUMN_PHONE));
                @SuppressLint("Range") String email = cursor.getString(cursor.getColumnIndex(ContactsDbHelper.COLUMN_EMAIL));

                // 将格式化后的联系人信息字符串添加到显示列表
                contactsList.add("姓名: " + name + "\n电话: " + phone +"\n邮箱: " + email);
                // 将对应的数据库 ID 添加到 ID 列表
                contactIds.add(id);
            }
            cursor.close(); // 关闭 cursor
        }
        // 通知适配器数据已改变，ListView 需要刷新
        adapter.notifyDataSetChanged();
    }

    /**
     * 添加新的联系人到本地数据库。
     * 从 EditText 控件获取姓名、电话和邮箱信息。
     * 对姓名和电话进行非空校验。
     * 如果姓名已存在（数据库 UNIQUE 约束），会提示用户。
     */
    private void addContact() {
        // 从 EditText 获取用户输入的联系人信息，并去除首尾空格
        String name = editTextName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();

        // 非空校验：姓名
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "请输入姓名", Toast.LENGTH_SHORT).show();
            return;
        }
        // 非空校验：电话号码
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "请输入电话号码", Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建 ContentValues 对象，用于存储要插入数据库的数据
        ContentValues values = new ContentValues();
        values.put(ContactsDbHelper.COLUMN_NAME, name);
        values.put(ContactsDbHelper.COLUMN_PHONE, phone);
        values.put(ContactsDbHelper.COLUMN_EMAIL, email);

        try {
            // 尝试向数据库插入新行，如果表定义了主键自增，则返回新行的 ID
            // insertOrThrow 会在发生约束冲突时抛出 SQLiteConstraintException
            long newRowId = database.insertOrThrow(ContactsDbHelper.TABLE_CONTACTS, null, values);

            Toast.makeText(this, "联系人已添加", Toast.LENGTH_SHORT).show();
            loadContactsFromDb(); // 添加成功后，重新从数据库加载联系人列表以更新 ListView
            // 清空输入框
            editTextName.setText("");
            editTextPhone.setText("");
            editTextEmail.setText("");

        } catch (SQLiteConstraintException e) {
            // 捕获因违反数据库约束（如 UNIQUE 约束）而抛出的异常
            // 检查是否是因为姓名重复（违反了 ContactsDbHelper 中定义的姓名 UNIQUE 约束）
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE constraint failed: " + ContactsDbHelper.TABLE_CONTACTS + "." + ContactsDbHelper.COLUMN_NAME)) {
                 Toast.makeText(this, "添加失败：姓名 '" + name + "' 已存在。", Toast.LENGTH_LONG).show();
            }else if (e.getMessage().contains("UNIQUE constraint failed: " + ContactsDbHelper.TABLE_CONTACTS + "." + ContactsDbHelper.COLUMN_PHONE)) {
                Toast.makeText(this, "添加失败：电话号码 '" + phone + "' 已存在。", Toast.LENGTH_LONG).show();
            } else {
                // 其他类型的约束冲突，例如某个字段设置了 NOT NULL 但插入时为 null
                Toast.makeText(this, "添加失败：数据不符合约束。", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            // 捕获其他所有可能的数据库操作异常
            Toast.makeText(this, "添加失败，发生未知数据库错误。", Toast.LENGTH_SHORT).show();
            e.printStackTrace(); // 打印堆栈跟踪，方便调试
        }
    }

    /**
     * 更新当前选中的联系人信息。
     * 如果未选中任何联系人 (selectedContactId == -1)，则提示用户。
     * 从 EditText 获取更新后的信息，并对姓名和电话进行非空校验。
     */
    private void updateContact() {
        // 检查是否已通过点击 ListView 中的项来选择了一个联系人
        if (selectedContactId == -1) {
            Toast.makeText(this, "请先选择一个联系人进行更新", Toast.LENGTH_SHORT).show();
            return;
        }

        // 从 EditText 获取更新后的联系人信息
        String name = editTextName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();

        // 非空校验
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "姓名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "电话号码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建 ContentValues 存储要更新的数据
        ContentValues values = new ContentValues();
        values.put(ContactsDbHelper.COLUMN_NAME, name);
        values.put(ContactsDbHelper.COLUMN_PHONE, phone);
        values.put(ContactsDbHelper.COLUMN_EMAIL, email);

        // 执行数据库更新操作
        // database.update 返回受影响的行数
        int rowsAffected = database.update(
                ContactsDbHelper.TABLE_CONTACTS, // 表名
                values, // 要更新的值
                ContactsDbHelper.COLUMN_ID + "=?", // WHERE 子句，匹配指定 ID 的行
                new String[]{String.valueOf(selectedContactId)} // WHERE 子句的参数
        );

        if (rowsAffected > 0) {
            Toast.makeText(this, "联系人已更新", Toast.LENGTH_SHORT).show();
            loadContactsFromDb(); // 更新成功后，刷新 ListView
            // 清空输入框并将 selectedContactId 重置
            editTextName.setText("");
            editTextPhone.setText("");
            editTextEmail.setText("");
            selectedContactId = -1;
        } else {
            // 如果 rowsAffected 为 0，可能是因为没有找到匹配 selectedContactId 的行，或者提供的值与现有值相同
            Toast.makeText(this, "更新失败或未找到联系人，或者信息未改变", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 删除当前选中的联系人。
     * 如果未选中任何联系人 (selectedContactId == -1)，则提示用户。
     */
    private void deleteContact() {
        // 检查是否已选择一个联系人
        if (selectedContactId == -1) {
            Toast.makeText(this, "请先选择一个联系人进行删除", Toast.LENGTH_SHORT).show();
            return;
        }

        // 执行数据库删除操作
        // database.delete 返回受影响的行数
        int rowsAffected = database.delete(
                ContactsDbHelper.TABLE_CONTACTS, // 表名
                ContactsDbHelper.COLUMN_ID + "=?", // WHERE 子句，匹配指定 ID 的行
                new String[]{String.valueOf(selectedContactId)} // WHERE 子句的参数
        );

        if (rowsAffected > 0) {
            Toast.makeText(this, "联系人已删除", Toast.LENGTH_SHORT).show();
            loadContactsFromDb(); // 删除成功后，刷新 ListView
            // 清空输入框并将 selectedContactId 重置
            editTextName.setText("");
            editTextPhone.setText("");
            editTextEmail.setText("");
            selectedContactId = -1;
        } else {
            // 如果 rowsAffected 为 0，可能是因为没有找到匹配 selectedContactId 的行
            Toast.makeText(this, "删除失败或未找到联系人", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 当 Activity 销毁时调用此方法。
     * 在这里关闭数据库连接，以释放资源并避免内存泄漏。
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 确保在 Activity 销毁时关闭数据库连接
        if (database != null) {
            database.close();
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
    }


}