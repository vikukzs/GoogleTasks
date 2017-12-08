package com.example.quickstart;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
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

import static android.content.ContentValues.TAG;
import static com.example.quickstart.Const.REQUEST_AUTHORIZATION;
import static com.example.quickstart.GoogleServicesHelper.showGooglePlayServicesAvailabilityErrorDialog;

import com.google.api.services.tasks.model.*;


/**
 * Created by vikuk.zsuzsanna on 2017. 11. 20..
 */

public class TasksFragment extends Fragment {

    @BindView(R.id.output_text)
    TextView mOutputText;

    private List<String> taskListList = new ArrayList<>();
    private GoogleAccountCredential mCredential;

    private com.google.api.services.tasks.Tasks taskService = null;

    HttpTransport transport = AndroidHttp.newCompatibleTransport();
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

    private final CompositeDisposable disposable = new CompositeDisposable();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCredential = ((MainActivity) getActivity()).getmCredential();

//        getListFromObservable();
    }

    public static TasksFragment newInstance() {

        Bundle args = new Bundle();

        TasksFragment fragment = new TasksFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    private void getListFromObservable() {
        disposable.add(taskListObservable()
                // Run on a background thread
                .subscribeOn(Schedulers.io())
                // Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<List<String>>() {
                    @Override
                    public void onComplete() {
//                        adapter.notifyDataSetChanged();
                        mOutputText.setText(taskListList.toString());
                    }

                    @Override
                    public void onError(Throwable e) {

                        if (e instanceof GooglePlayServicesAvailabilityIOException) {
                            showGooglePlayServicesAvailabilityErrorDialog(getContext(),
                                    ((GooglePlayServicesAvailabilityIOException) e)
                                            .getConnectionStatusCode());
                        } else if (e instanceof UserRecoverableAuthIOException) {
                            startActivityForResult(
                                    ((UserRecoverableAuthIOException) e).getIntent(),
                                    REQUEST_AUTHORIZATION);
                        } else {
                            mOutputText.setText("The following error occurred:\n"
                                    + e.getMessage());
                        }
                        Log.e(TAG, "onError()", e);
                    }

                    @Override
                    public void onNext(List<String> taskListNames) {
                        taskListList.addAll(taskListNames);
                    }
                }));
    }

    private List<String> getDataFromApi() throws IOException {
        taskService = new com.google.api.services.tasks.Tasks.Builder(
                transport, jsonFactory, mCredential)
                .setApplicationName("Google Tasks API Android Quickstart")
                .build();

        List<String> taskListInfo = new ArrayList<String>();
        TaskLists result = taskService.tasklists().list()
                .execute();
        List<TaskList> tasklists = result.getItems();
        if (tasklists != null) {
            for (TaskList tasklist : tasklists) {
                taskListInfo.add(String.format("%s (%s)\n",
                        tasklist.getTitle(),
                        tasklist.getId()));
            }
        }
        return taskListInfo;
    }

    private Observable<List<String>> taskListObservable() {
        return Observable.defer(new Callable<ObservableSource<? extends List<String>>>() {
            @Override
            public ObservableSource<? extends List<String>> call() throws Exception {
                // Do some long running operation
                return Observable.just(getDataFromApi());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposable.clear();
    }


}
