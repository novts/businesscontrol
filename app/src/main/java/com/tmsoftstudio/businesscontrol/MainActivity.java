package com.tmsoftstudio.businesscontrol;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.AccountPicker;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.tmsoftstudio.businesscontrol.backend.myApi.MyApi;
import com.tmsoftstudio.businesscontrol.backend.myApi.model.MyBean;
import com.tmsoftstudio.businesscontrol.backend.myApi.model.ReportBean;
import com.tmsoftstudio.businesscontrol.backend.myApi.model.TaskBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static int REQUEST_CODE_ACCOUNT = 1;
    private static int REQUEST_CODE_PHOTO = 2;
    private static SharedPreferences mSettings;
    private static Activity context;
    private static FloatingActionButton fab;
    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        context = this;
        mSettings = context.getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE);

        if (!mSettings.contains("APP_PREFERENCES_START_ALARM")) {
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putBoolean("APP_PREFERENCES_START_ALARM", false);
            editor.commit();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        StartFragment fragmentStart = StartFragment.newInstance();
        fragmentTransaction.replace(R.id.content, fragmentStart);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.show();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                ViewMapFragment fragmentMap = new ViewMapFragment();
                fragmentTransaction.replace(R.id.content, fragmentMap);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                fab.hide();

            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mSettings.contains("APP_PREFERENCES_ACCOUNT")) {
            Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"}, false, null, null, null, null);
            startActivityForResult(intent, REQUEST_CODE_ACCOUNT);
        }
    }

    protected void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        if (requestCode == REQUEST_CODE_ACCOUNT && resultCode == RESULT_OK) {
            String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putString("APP_PREFERENCES_ACCOUNT", accountName);
            editor.commit();

            if (isOnline()) {
                MyBean bean = new MyBean();
                bean.setAccount(accountName);
                Calendar c = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("dd:MMMM:yyyy H:mm:ss");
                String dateCurrent = sdf.format(c.getTime());
                bean.setDateEnter(dateCurrent);

                AccountAsyncTask task = new AccountAsyncTask();
                task.execute(bean);
            } else {
                Toast.makeText(context.getApplicationContext(), "Интернет соединение отсутствует", Toast.LENGTH_SHORT).show();
            }

        }
        if (requestCode == REQUEST_CODE_PHOTO && resultCode == RESULT_OK) {
            if (mSettings.contains("APP_PREFERENCES_IMAGES")) {
                try {
                    String jsonString = mSettings.getString("APP_PREFERENCES_IMAGES", "");
                    JSONObject json = new JSONObject(jsonString);
                    JSONArray jsonArray = json.getJSONArray("uris");
                    Uri selectedImage = data.getData();
                    JSONObject uriJ = new JSONObject();
                    uriJ.put("uri", selectedImage.toString());
                    jsonArray.put(uriJ);
                    json.put("uris", jsonArray);
                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putString("APP_PREFERENCES_IMAGES", json.toString());
                    editor.commit();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {

                try {
                    Uri selectedImage = data.getData();
                    JSONObject json = new JSONObject();
                    JSONArray urisJ = new JSONArray();
                    JSONObject uriJ = new JSONObject();
                    uriJ.put("uri", selectedImage.toString());
                    urisJ.put(uriJ);
                    json.put("uris", urisJ);
                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putString("APP_PREFERENCES_IMAGES", json.toString());
                    editor.commit();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        fab.show();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        if (mSettings.getBoolean("APP_PREFERENCES_START_ALARM", true) == true) {
            menu.findItem(R.id.action_online).setChecked(true);
            menu.findItem(R.id.action_show_status).setTitle("Задание выполняется");
        } else {
            menu.findItem(R.id.action_offline).setChecked(true);
            menu.findItem(R.id.action_show_status).setTitle("Задание не выполняется");
        }
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_online) {
            if (isOnline()) {

                Boolean start = mSettings.getBoolean("APP_PREFERENCES_START_ALARM", false);

                if (start == false) {
                    long interval = 15L * 1000 * 60;
                    StartAlarmIntentService.startActionAlarm(context, interval);

                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putBoolean("APP_PREFERENCES_START_ALARM", true);
                    editor.commit();
                    item.setChecked(true);
                    mMenu.findItem(R.id.action_show_status).setTitle("Задание выполняется");

                    View view = (View) findViewById(R.id.content);
                    Snackbar snack = Snackbar.make(view, "Выполнение задания запущено с отслеживанием в " + interval / (1000 * 60) + " minutes interval", Snackbar.LENGTH_LONG);
                    View snackbarView = snack.getView();
                    snackbarView.setBackgroundColor(Color.parseColor("#ffffff"));
                    TextView tv = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
                    tv.setGravity(Gravity.CENTER);
                    snack.setAction("Action", null).show();
                } else {
                    long interval = 15L * 1000 * 60;
                    View view = (View) findViewById(R.id.content);
                    Snackbar snack = Snackbar.make(view, "Выполнение задания уже запущено с отслеживанием в " + interval / (1000 * 60) + " minutes interval", Snackbar.LENGTH_LONG);
                    View snackbarView = snack.getView();
                    snackbarView.setBackgroundColor(Color.parseColor("#ffffff"));
                    TextView tv = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
                    tv.setGravity(Gravity.CENTER);
                    snack.setAction("Action", null).show();
                }

            } else {
                Toast.makeText(context.getApplicationContext(), "Интернет соединение отсутствует", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        if (id == R.id.action_offline) {
            Intent intent = new Intent(getApplicationContext(), LocationReceiver.class);
            final PendingIntent pIntent = PendingIntent.getBroadcast(getApplicationContext(), LocationReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager alarm = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            alarm.cancel(pIntent);

            SharedPreferences.Editor editor = mSettings.edit();
            editor.putBoolean("APP_PREFERENCES_START_ALARM", false);
            editor.commit();
            item.setChecked(true);
            mMenu.findItem(R.id.action_show_status).setTitle("Задание не выполняется");

            View view = (View) findViewById(R.id.content);
            Snackbar snack = Snackbar.make(view, "Выполнение задания остановлено", Snackbar.LENGTH_LONG);
            View snackbarView = snack.getView();
            snackbarView.setBackgroundColor(Color.parseColor("#ffffff"));
            TextView tv = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
            tv.setGravity(Gravity.CENTER);
            snack.setAction("Action", null).show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_task) {
            if (mSettings.contains("APP_PREFERENCES_ACCOUNT")) {
                String accountName = mSettings.getString("APP_PREFERENCES_ACCOUNT", "");
                if (isOnline()) {
                    MyBean bean = new MyBean();
                    bean.setAccount(accountName);
                    TaskAsyncTask task = new TaskAsyncTask();
                    task.execute(bean);
                } else {
                    Toast.makeText(context.getApplicationContext(), "Интернет соединение отсутствует", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (id == R.id.nav_view_task) {
            TaskDialogFragment dialog = new TaskDialogFragment();
            dialog.show(getSupportFragmentManager(), "TaskDialogFragment");

        } else if (id == R.id.nav_start_task) {
            if (isOnline()) {

                Boolean start = mSettings.getBoolean("APP_PREFERENCES_START_ALARM", false);

                if (start == false) {
                    long interval = 15L * 1000 * 60;
                    StartAlarmIntentService.startActionAlarm(context, interval);

                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putBoolean("APP_PREFERENCES_START_ALARM", true);
                    editor.commit();
                    mMenu.findItem(R.id.action_online).setChecked(true);
                    mMenu.findItem(R.id.action_show_status).setTitle("Задание выполняется");

                    View view = (View) findViewById(R.id.content);
                    Snackbar snack = Snackbar.make(view, "Выполнение задания запущено с отслеживанием в " + interval / (1000 * 60) + " minutes interval", Snackbar.LENGTH_LONG);
                    View snackbarView = snack.getView();
                    snackbarView.setBackgroundColor(Color.parseColor("#ffffff"));
                    TextView tv = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
                    tv.setGravity(Gravity.CENTER);
                    snack.setAction("Action", null).show();
                } else {
                    long interval = 15L * 1000 * 60;
                    View view = (View) findViewById(R.id.content);
                    Snackbar snack = Snackbar.make(view, "Выполнение задания уже запущено с отслеживанием в " + interval / (1000 * 60) + " minutes interval", Snackbar.LENGTH_LONG);
                    View snackbarView = snack.getView();
                    snackbarView.setBackgroundColor(Color.parseColor("#ffffff"));
                    TextView tv = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
                    tv.setGravity(Gravity.CENTER);
                    snack.setAction("Action", null).show();
                }

            } else {
                Toast.makeText(context.getApplicationContext(), "Интернет соединение отсутствует", Toast.LENGTH_SHORT).show();
            }

        } else if (id == R.id.nav_end_task) {
            Intent intent = new Intent(getApplicationContext(), LocationReceiver.class);
            final PendingIntent pIntent = PendingIntent.getBroadcast(getApplicationContext(), LocationReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager alarm = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            alarm.cancel(pIntent);

            SharedPreferences.Editor editor = mSettings.edit();
            editor.putBoolean("APP_PREFERENCES_START_ALARM", false);
            editor.commit();
            mMenu.findItem(R.id.action_offline).setChecked(true);
            mMenu.findItem(R.id.action_show_status).setTitle("Задание не выполняется");

            View view = (View) findViewById(R.id.content);
            Snackbar snack = Snackbar.make(view, "Выполнение задания остановлено", Snackbar.LENGTH_LONG);
            View snackbarView = snack.getView();
            snackbarView.setBackgroundColor(Color.parseColor("#ffffff"));
            TextView tv = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
            tv.setGravity(Gravity.CENTER);
            snack.setAction("Action", null).show();

        } else if (id == R.id.nav_report) {
            ReportDialogFragment dialog = new ReportDialogFragment();
            dialog.show(getSupportFragmentManager(), "ReportDialogFragment");

        } else if (id == R.id.nav_send_report) {
            ReportSendDialogFragment dialog = new ReportSendDialogFragment();
            dialog.show(getSupportFragmentManager(), "ReportSendDialogFragment");

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private static class AccountAsyncTask extends AsyncTask<MyBean, Integer, MyBean> {
        private MyApi myApiService = null;
        private ProgressBar pb;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb = (ProgressBar) context.findViewById(R.id.progress);
            pb.setVisibility(View.VISIBLE);

        }

        @Override
        protected void onPostExecute(MyBean myBean) {
            super.onPostExecute(myBean);
            Toast.makeText(context.getApplicationContext(), "Вы вошли в систему", Toast.LENGTH_SHORT).show();

            pb.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected MyBean doInBackground(MyBean... params) {
            MyBean profile = params[0];
            MyBean response = null;
            if (myApiService == null) {
                MyApi.Builder builder = new MyApi.Builder(AndroidHttp.newCompatibleTransport(),
                        new AndroidJsonFactory(), null)
                        .setRootUrl("https://buisness-control.appspot.com/_ah/api/")
                        .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                            @Override
                            public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                                abstractGoogleClientRequest.setDisableGZipContent(true);
                            }
                        });
                myApiService = builder.build();
            }

            try {
                response = myApiService.setAccount(profile).execute();
            } catch (IOException e) {
            }

            return response;
        }

    }

    private static class TaskAsyncTask extends AsyncTask<MyBean, Integer, List<TaskBean>> {
        private MyApi myApiService = null;
        private ProgressBar pb;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb = (ProgressBar) context.findViewById(R.id.progress);
            pb.setVisibility(View.VISIBLE);

        }

        @Override
        protected void onPostExecute(List<TaskBean> list) {
            super.onPostExecute(list);
            try {
                SharedPreferences.Editor editor = mSettings.edit();
                JSONObject jsonTask;
                JSONArray jsonArrayTask = new JSONArray();
                JSONObject json = new JSONObject();
                for (TaskBean bean : list) {
                    jsonTask = new JSONObject();
                    jsonTask.put("name", bean.getName());
                    jsonTask.put("date", bean.getDate());
                    jsonTask.put("descs", bean.getDescs());
                    jsonTask.put("route", bean.getRoute());
                    jsonArrayTask.put(jsonTask);
                }
                json.put("tasks", jsonArrayTask);

                editor.putString("APP_PREFERENCES_TASKS", json.toString());
                editor.commit();
                pb.setVisibility(View.INVISIBLE);
                Toast.makeText(context.getApplicationContext(), "Задание получено", Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected List<TaskBean> doInBackground(MyBean... params) {
            MyBean profile = params[0];
            List<TaskBean> list = new ArrayList<TaskBean>();
            if (myApiService == null) {
                MyApi.Builder builder = new MyApi.Builder(AndroidHttp.newCompatibleTransport(),
                        new AndroidJsonFactory(), null)
                        .setRootUrl("https://buisness-control.appspot.com/_ah/api/")
                        .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                            @Override
                            public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                                abstractGoogleClientRequest.setDisableGZipContent(true);
                            }
                        });
                myApiService = builder.build();
            }

            try {
                list = myApiService.getTask(profile).execute().getItems();
                ;
            } catch (IOException e) {
            }

            return list;
        }

    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static class TaskDialogFragment extends DialogFragment {

        private View view;
        private AlertDialog alert;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            view = (View) inflater.inflate(R.layout.dialog_task, null);


            builder.setView(view)
                    .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            TaskDialogFragment.this.getDialog().cancel();
                        }
                    });

            if (mSettings.contains("APP_PREFERENCES_TASKS")) {
                try {
                    String jsonString = mSettings.getString("APP_PREFERENCES_TASKS", "");
                    JSONObject json = new JSONObject(jsonString);
                    JSONArray jsonArrayTask = json.getJSONArray("tasks");
                    ArrayList<Map<String, String>> taskNameList = new ArrayList<Map<String, String>>();
                    Map<String, String> mapTaskName;
                    ArrayList<Map<String, String>> taskDetailList;
                    Map<String, String> mapTaskDetail;
                    ArrayList<ArrayList<Map<String, String>>> сhildDataList = new ArrayList<>();
                    for (int i = 0; i < jsonArrayTask.length(); i++) {
                        JSONObject jsonTask = jsonArrayTask.getJSONObject(i);
                        mapTaskName = new HashMap<>();
                        mapTaskName.put("taskName", jsonTask.getString("name"));
                        taskNameList.add(mapTaskName);

                        taskDetailList = new ArrayList<Map<String, String>>();

                        mapTaskDetail = new HashMap<>();
                        mapTaskDetail.put("taskDetail", jsonTask.getString("date"));
                        taskDetailList.add(mapTaskDetail);
                        mapTaskDetail = new HashMap<>();
                        mapTaskDetail.put("taskDetail", jsonTask.getString("descs"));
                        taskDetailList.add(mapTaskDetail);
                        mapTaskDetail = new HashMap<>();
                        mapTaskDetail.put("taskDetail", jsonTask.getString("route"));
                        taskDetailList.add(mapTaskDetail);

                        сhildDataList.add(taskDetailList);
                    }

                    String groupFrom[] = new String[]{"taskName"};
                    int groupTo[] = new int[]{android.R.id.text1};
                    String childFrom[] = new String[]{"taskDetail"};
                    int childTo[] = new int[]{android.R.id.text1};

                    SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(
                            context, taskNameList,
                            android.R.layout.simple_expandable_list_item_1, groupFrom,
                            groupTo, сhildDataList, android.R.layout.simple_list_item_1,
                            childFrom, childTo);
                    ExpandableListView listview = (ExpandableListView) view.findViewById(R.id.view_task);
                    listview.setAdapter(adapter);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            alert = builder.create();
            alert.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button btnNegative = alert.getButton(Dialog.BUTTON_NEGATIVE);
                    btnNegative.setTextSize(TypedValue.COMPLEX_UNIT_PX, 25.0f);
                    btnNegative.setTextColor(Color.parseColor("#303F9F"));
                    btnNegative.setGravity(Gravity.CENTER);
                }
            });

            return alert;
        }

    }

    public static class ReportDialogFragment extends DialogFragment {

        private View view;
        private AlertDialog alert;
        private EditText report;


        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            view = (View) inflater.inflate(R.layout.dialog_report_layout, null);


            if (mSettings.contains("APP_PREFERENCES_IMAGES")) {
                String jsonString = mSettings.getString("APP_PREFERENCES_IMAGES", "");

                try {
                    JSONObject json = new JSONObject(jsonString);
                    final JSONArray jsonArray = json.getJSONArray("uris");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        final int k=i;
                        JSONObject uriJ = jsonArray.getJSONObject(i);
                        String uri = uriJ.getString("uri");
                        Uri imageUri = Uri.parse(uri);
                        InputStream imageStream = null;
                        imageStream = getContext().getContentResolver().openInputStream(imageUri);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 2;
                        Bitmap bitmap = BitmapFactory.decodeStream(imageStream, null, options);
                        LinearLayout layout = (LinearLayout) view.findViewById(R.id.linearlayout_report);
                        ImageView imageView = new ImageView(view.getContext());
                        imageView.setImageBitmap(bitmap);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(0, 20, 0, 0);
                        imageView.setLayoutParams(params);
                        layout.addView(imageView);
                        imageStream = null;
                        Button button = new Button(view.getContext());
                        button.setText(R.string.button_delete_photo);
                        button.setTextColor(Color.parseColor("#3F51B5"));
                        button.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        button.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {

                                JSONObject json = new JSONObject();
                                try {
                                    JSONArray array = new JSONArray();
                                    for (int j = 0; j < jsonArray.length(); j++) {
                                        if (j != k)
                                            array.put(jsonArray.get(j));
                                    }
                                    json.put("uris", array);
                                    SharedPreferences.Editor editor = mSettings.edit();
                                    editor.putString("APP_PREFERENCES_IMAGES", json.toString());
                                    editor.commit();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                alert.dismiss();
                            }
                        });
                        layout.addView(button);

                        View viewDiv = new View(view.getContext());
                        viewDiv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2));
                        viewDiv.setBackgroundColor(Color.parseColor("#3F51B5"));
                        layout.addView(viewDiv);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            builder.setView(view)
                    .setPositiveButton(R.string.button_submit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    })
                    .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ReportDialogFragment.this.getDialog().cancel();
                        }
                    });


            report = (EditText) view.findViewById(R.id.text_report);

            if (mSettings.contains("APP_PREFERENCES_REPORT")) {
                report.setText(mSettings.getString("APP_PREFERENCES_REPORT", ""));
            }

            alert = builder.create();
            alert.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button btnPositive = alert.getButton(Dialog.BUTTON_POSITIVE);
                    btnPositive.setTextSize(TypedValue.COMPLEX_UNIT_PX, 25.0f);
                    btnPositive.setTextColor(Color.parseColor("#3F51B5"));
                    btnPositive.setGravity(Gravity.CENTER);

                    Button btnNegative = alert.getButton(Dialog.BUTTON_NEGATIVE);
                    btnNegative.setTextSize(TypedValue.COMPLEX_UNIT_PX, 25.0f);
                    btnNegative.setTextColor(Color.parseColor("#3F51B5"));
                    btnNegative.setGravity(Gravity.CENTER);
                }
            });

            Button imageButton = (Button) view.findViewById(R.id.report_photo);
            imageButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    getActivity().startActivityForResult(photoPickerIntent, REQUEST_CODE_PHOTO);
                    alert.dismiss();
                }
            });

            return alert;
        }

        @Override
        public void onStart() {
            super.onStart();
            alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Boolean wantToCloseDialog = false;

                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putString("APP_PREFERENCES_REPORT", report.getText().toString());
                    editor.commit();

                    wantToCloseDialog = true;
                    if (wantToCloseDialog) {
                        alert.dismiss();
                    }
                }
            });
        }


    }
    public static class ReportSendDialogFragment extends DialogFragment {

        private View view;
        private AlertDialog alert;
        private EditText report;


        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            view = (View) inflater.inflate(R.layout.dialog_report_send_layout, null);


            if (mSettings.contains("APP_PREFERENCES_IMAGES")) {
                String jsonString = mSettings.getString("APP_PREFERENCES_IMAGES", "");

                try {
                    JSONObject json = new JSONObject(jsonString);
                    final JSONArray jsonArray = json.getJSONArray("uris");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        final int k=i;
                        JSONObject uriJ = jsonArray.getJSONObject(i);
                        String uri = uriJ.getString("uri");

                        Uri imageUri = Uri.parse(uri);
                        InputStream imageStream = null;
                        imageStream = getContext().getContentResolver().openInputStream(imageUri);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 2;
                        Bitmap bitmap = BitmapFactory.decodeStream(imageStream, null, options);

                        LinearLayout layout = (LinearLayout) view.findViewById(R.id.linearlayout_report_send);
                        ImageView imageView = new ImageView(view.getContext());
                        imageView.setImageBitmap(bitmap);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(0, 20, 0, 0);
                        imageView.setLayoutParams(params);
                        layout.addView(imageView);

                        Button button = new Button(view.getContext());
                        button.setText(R.string.button_delete_photo);
                        button.setTextColor(Color.parseColor("#3F51B5"));
                        button.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        button.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {

                                JSONObject json = new JSONObject();
                                try {
                                    JSONArray array = new JSONArray();
                                    for (int j = 0; j < jsonArray.length(); j++) {
                                        if (j != k)
                                            array.put(jsonArray.get(j));
                                    }
                                    json.put("uris", array);
                                    SharedPreferences.Editor editor = mSettings.edit();
                                    editor.putString("APP_PREFERENCES_IMAGES", json.toString());
                                    editor.commit();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                alert.dismiss();
                            }
                        });
                        layout.addView(button);

                        View viewDiv = new View(view.getContext());
                        viewDiv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2));
                        viewDiv.setBackgroundColor(Color.parseColor("#3F51B5"));
                        layout.addView(viewDiv);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            builder.setView(view)
                    .setPositiveButton(R.string.button_submit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    })
                    .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ReportSendDialogFragment.this.getDialog().cancel();
                        }
                    });


            report = (EditText) view.findViewById(R.id.text_report_send);

            if (mSettings.contains("APP_PREFERENCES_REPORT")) {
                report.setText(mSettings.getString("APP_PREFERENCES_REPORT", ""));
            }

            alert = builder.create();
            alert.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button btnPositive = alert.getButton(Dialog.BUTTON_POSITIVE);
                    btnPositive.setTextSize(TypedValue.COMPLEX_UNIT_PX, 25.0f);
                    btnPositive.setTextColor(Color.parseColor("#3F51B5"));
                    btnPositive.setGravity(Gravity.CENTER);

                    Button btnNegative = alert.getButton(Dialog.BUTTON_NEGATIVE);
                    btnNegative.setTextSize(TypedValue.COMPLEX_UNIT_PX, 25.0f);
                    btnNegative.setTextColor(Color.parseColor("#3F51B5"));
                    btnNegative.setGravity(Gravity.CENTER);
                }
            });

            Button imageButton = (Button) view.findViewById(R.id.report_photo_send);
            imageButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    getActivity().startActivityForResult(photoPickerIntent, REQUEST_CODE_PHOTO);
                    alert.dismiss();
                }
            });

            Button sendButton = (Button) view.findViewById(R.id.report_send);
            sendButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                ReportBean bean = new ReportBean();

                    if (mSettings.contains("APP_PREFERENCES_IMAGES")) {

                        List<String> images=new ArrayList<String>();
                        String jsonString = mSettings.getString("APP_PREFERENCES_IMAGES", "");

                        try {
                            JSONObject json = new JSONObject(jsonString);
                            final JSONArray jsonArray = json.getJSONArray("uris");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                final int k=i;
                                JSONObject uriJ = jsonArray.getJSONObject(i);
                                String uri = uriJ.getString("uri");

                                Uri imageUri = Uri.parse(uri);
                                InputStream imageStream = null;
                                imageStream = getContext().getContentResolver().openInputStream(imageUri);
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inSampleSize = 2;
                                Bitmap bitmap = BitmapFactory.decodeStream(imageStream, null, options);

                                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 10, outputStream);
                                bitmap.recycle();

                                bitmap = null;
                                byte[] bitmapByte = outputStream.toByteArray();
                                outputStream = null;
                                String stringEncodedImage = Base64.encodeToString(bitmapByte, Base64.DEFAULT);
                                images.add(stringEncodedImage);
                                bitmapByte = null;
                                imageStream = null;
                            }
                            bean.setImages(images);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                bean.setReport(report.getText().toString());
                String accountName = mSettings.getString("APP_PREFERENCES_ACCOUNT", "");
                bean.setAccount(accountName);
                    List<String> tasks=new ArrayList<String>();
                    if (mSettings.contains("APP_PREFERENCES_TASKS")) {
                        try {
                            String jsonString = mSettings.getString("APP_PREFERENCES_TASKS", "");
                            JSONObject json = new JSONObject(jsonString);
                            JSONArray jsonArrayTask = json.getJSONArray("tasks");

                            for (int i = 0; i < jsonArrayTask.length(); i++) {
                                JSONObject jsonTask = jsonArrayTask.getJSONObject(i);
                                String taskName=jsonTask.getString("name");
                                tasks.add(taskName);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        bean.setTasks(tasks);
                        ReportAsyncTask task = new ReportAsyncTask();
                        task.execute(bean);
                    }
                    alert.dismiss();
                }
            });

            return alert;
        }

        @Override
        public void onStart() {
            super.onStart();
            alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Boolean wantToCloseDialog = false;

                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putString("APP_PREFERENCES_REPORT", report.getText().toString());
                    editor.commit();

                    wantToCloseDialog = true;
                    if (wantToCloseDialog) {
                        alert.dismiss();
                    }
                }
            });
        }
    }
    private static class ReportAsyncTask extends AsyncTask<ReportBean, Integer, ReportBean> {
        private MyApi myApiService = null;
        private ProgressBar pb;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb = (ProgressBar) context.findViewById(R.id.progress);
            pb.setVisibility(View.VISIBLE);

        }

        @Override
        protected void onPostExecute(ReportBean result) {
            super.onPostExecute(result);
            if(result.getState()==true) {
                Toast.makeText(context.getApplicationContext(), "Отчет послан", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(context.getApplicationContext(), "Ошибка отправки. Пошлите отчет заново", Toast.LENGTH_SHORT).show();
            }
            pb.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected ReportBean doInBackground(ReportBean... params) {
            ReportBean report = params[0];
            ReportBean res=new ReportBean();
            if (myApiService == null) {
                MyApi.Builder builder = new MyApi.Builder(AndroidHttp.newCompatibleTransport(),
                        new AndroidJsonFactory(), null)
                        .setRootUrl("https://buisness-control.appspot.com/_ah/api/")
                        .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                            @Override
                            public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                                abstractGoogleClientRequest.setDisableGZipContent(true);
                            }
                        });
                myApiService = builder.build();
            }

            try {
                res=myApiService.setReport(report).execute();
            } catch (IOException e) {
            }

            return res;
        }

    }
}