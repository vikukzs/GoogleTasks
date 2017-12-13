package com.example.quickstart;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.tasks.TasksScopes;


import com.google.api.services.tasks.model.*;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.content.ContentValues.TAG;
import static com.example.quickstart.AppUtility.isDeviceOnline;
import static com.example.quickstart.Const.REQUEST_ACCOUNT_PICKER;
import static com.example.quickstart.Const.REQUEST_AUTHORIZATION;
import static com.example.quickstart.Const.REQUEST_GOOGLE_PLAY_SERVICES;
import static com.example.quickstart.Const.REQUEST_PERMISSION_GET_ACCOUNTS;
import static com.example.quickstart.GoogleServicesHelper.acquireGooglePlayServices;
import static com.example.quickstart.GoogleServicesHelper.isGooglePlayServicesAvailable;
import static com.example.quickstart.GoogleServicesHelper.showGooglePlayServicesAvailabilityErrorDialog;

public class MainActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks {

    ProgressDialog mProgress;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {TasksScopes.TASKS_READONLY};

    private GoogleAccountCredential mCredential;

    private List<TaskList> taskListList = new ArrayList<>();

    private com.google.api.services.tasks.Tasks taskService = null;

    HttpTransport transport = AndroidHttp.newCompatibleTransport();
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

    private CompositeDisposable disposable = null;
    DrawerAdapter drawerAdapter;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.drawer_recview)
    RecyclerView drawerRecView;

    private List<Task> allTasksList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.bind(this);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Google Tasks API ...");

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        taskService = new com.google.api.services.tasks.Tasks.Builder(
                transport, jsonFactory, mCredential)
                .setApplicationName("Google Tasks API Android Quickstart")
                .build();
        checkGooglePlayAndNetConnection();



        getSupportActionBar().hide();

        initDrawerLayout();
        drawerAdapter.notifyDataSetChanged();

        openTasksFragment();
    }

    private void initDrawerLayout() {
        drawerAdapter = new DrawerAdapter(taskListList);
        drawerRecView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        drawerRecView.setAdapter(drawerAdapter);
    }

    private void openTasksFragment() {
        TasksFragment fragment = new TasksFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }


    public GoogleAccountCredential getmCredential() {
        return mCredential;
    }

    private void checkGooglePlayAndNetConnection() {
        if (!isGooglePlayServicesAvailable(getApplicationContext())) {
            acquireGooglePlayServices(getApplicationContext());
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline(getApplicationContext())) {
            Toast.makeText(this, "No network connection available.", Toast.LENGTH_LONG).show();
        } else {
            getTaskListListFromObservable();
        }
    }

    public List<Task> getAllTasksList() {
        return allTasksList;
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                checkGooglePlayAndNetConnection();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(this,
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.", Toast.LENGTH_LONG).show();
                } else {
                    checkGooglePlayAndNetConnection();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        checkGooglePlayAndNetConnection();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    checkGooglePlayAndNetConnection();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    private void getTaskListListFromObservable() {
        disposable = new CompositeDisposable();
        disposable.add(taskListObservable()
                // Run on a background thread
                .subscribeOn(Schedulers.io())
                // Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<List<TaskList>>() {
                    @Override
                    public void onComplete() {
                        drawerAdapter.notifyDataSetChanged();
                        disposable.dispose();
                        getAllTaskListFromObservable();
                    }

                    @Override
                    public void onError(Throwable e) {

                        if (e instanceof GooglePlayServicesAvailabilityIOException) {
                            showGooglePlayServicesAvailabilityErrorDialog(getApplicationContext(),
                                    ((GooglePlayServicesAvailabilityIOException) e)
                                            .getConnectionStatusCode());
                        } else if (e instanceof UserRecoverableAuthIOException) {
                            startActivityForResult(
                                    ((UserRecoverableAuthIOException) e).getIntent(),
                                    REQUEST_AUTHORIZATION);
                        } else {
//                            mOutputText.setText("The following error occurred:\n"
//                                    + e.getMessage());
                        }
                        Log.e(TAG, "onError()", e);
                    }

                    @Override
                    public void onNext(List<TaskList> taskLists) {
                        taskListList.addAll(taskLists);
                    }
                }));
    }

    private void getAllTaskListFromObservable() {
        disposable = new CompositeDisposable();
        disposable.add(taskObservable()
                // Run on a background thread
                .subscribeOn(Schedulers.io())
                // Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<List<Task>>() {
                    @Override
                    public void onComplete() {
                        EventBus.getDefault().post(new MessageEvent(allTasksList));
                        disposable.dispose();
                    }

                    @Override
                    public void onError(Throwable e) {

                        if (e instanceof GooglePlayServicesAvailabilityIOException) {
                            showGooglePlayServicesAvailabilityErrorDialog(getApplicationContext(),
                                    ((GooglePlayServicesAvailabilityIOException) e)
                                            .getConnectionStatusCode());
                        } else if (e instanceof UserRecoverableAuthIOException) {
                            startActivityForResult(
                                    ((UserRecoverableAuthIOException) e).getIntent(),

                                    REQUEST_AUTHORIZATION);
                        } else {
//                            mOutputText.setText("The following error occurred:\n"
//                                    + e.getMessage());
                        }
                        Log.e(TAG, "onError()", e);
                    }

                    @Override
                    public void onNext(List<Task> tasks) {
                        allTasksList.addAll(tasks);
                    }
                }));
    }

    private List<TaskList> getDataFromApi() throws IOException {

        TaskLists result = taskService.tasklists().list()
                .execute();
        return result.getItems();
    }

    private Observable<List<TaskList>> taskListObservable() {
        return Observable.defer(new Callable<ObservableSource<? extends List<TaskList>>>() {
            @Override
            public ObservableSource<? extends List<TaskList>> call() throws Exception {
                // Do some long running operation
                return Observable.just(getDataFromApi());
            }
        });
    }

    private Observable<List<Task>> taskObservable() {
        return Observable.defer(new Callable<ObservableSource<? extends List<Task>>>() {
            @Override
            public ObservableSource<? extends List<Task>> call() throws Exception {
                // Do some long running operation
                return Observable.just(getAllTasksFromAllLists(taskListList));
            }
        });
    }

    private List<Task> getAllTasksFromAllLists(List<TaskList> taskLists) {
        List<Task> listOfTasks = new ArrayList<>();
        for (TaskList taskList : taskLists) {
            Tasks tasks = null;
            try {
                tasks = taskService.tasks().list(taskList.getId()).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            listOfTasks.addAll(tasks.getItems());
        }
        return listOfTasks;
    }
}

