package com.endpoint.gably.activities_fragments.activity_home.client_home.fragments.fragment_orders;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.endpoint.gably.R;
import com.endpoint.gably.activities_fragments.activity_home.client_home.activity.ClientHomeActivity;
import com.endpoint.gably.adapters.OrdersAdapter;
import com.endpoint.gably.models.OrderDataModel;
import com.endpoint.gably.models.UserModel;
import com.endpoint.gably.preferences.Preferences;
import com.endpoint.gably.remote.Api;
import com.endpoint.gably.singletone.UserSingleTone;
import com.endpoint.gably.tags.Tags;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Fragment_Client_New_Orders extends Fragment {

    private ProgressBar progBar;
    private RecyclerView recView;
    private RecyclerView.LayoutManager manager;
    private ClientHomeActivity activity;
    private TextView tv_no_orders;
    private List<OrderDataModel.OrderModel> orderModelList;
    private OrdersAdapter adapter;
    private UserModel userModel;
    private UserSingleTone userSingleTone;
    private boolean isLoading = false;
    private int current_page = 1;
    private Call<OrderDataModel> call;

    private boolean isFirstTime = true;
    private Preferences preferences;

    @Override
    public void onStart() {
        super.onStart();
        if (!isFirstTime&&adapter!=null)
        {
            adapter.notifyDataSetChanged();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_type,container,false);
        initView(view);
        return view;
    }

    public static Fragment_Client_New_Orders newInstance()
    {
        return new Fragment_Client_New_Orders();
    }
    private void initView(View view) {
        orderModelList = new ArrayList<>();
preferences=Preferences.getInstance();
        activity = (ClientHomeActivity) getActivity();
        userSingleTone = UserSingleTone.getInstance();
        userModel = userSingleTone.getUserModel();
        userModel=preferences.getUserData(activity);
        tv_no_orders = view.findViewById(R.id.tv_no_orders);
        progBar = view.findViewById(R.id.progBar);
        progBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(activity,R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        recView = view.findViewById(R.id.recView);
        manager = new LinearLayoutManager(activity);
        recView.setLayoutManager(manager);
        adapter = new OrdersAdapter(orderModelList,activity,userModel.getData().getUser_type(),this);
        recView.setAdapter(adapter);

        recView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy>0)
                {
                    int lastVisibleItem = ((LinearLayoutManager)manager).findLastCompletelyVisibleItemPosition();
                    int totalItems = manager.getItemCount();

                    if (lastVisibleItem>=(totalItems-5)&&!isLoading)
                    {
                        isLoading = true;
                        orderModelList.add(null);
                        adapter.notifyItemInserted(orderModelList.size()-1);
                        int next_page = current_page+1;
                        loadMore(next_page);
                    }
                }
            }
        });
        getOrders();

    }

    public void getOrders()
    {
        userModel=preferences.getUserData(activity);

        Log.e("utype",userModel.getData().getUser_type()+"_");
        if (userModel.getData().getUser_type().equals(Tags.TYPE_CLIENT))
        {
            call  = Api.getService(Tags.base_url).getClientOrders(userModel.getData().getUser_id(),"new", 1);
            Log.e("1111","gggg");

        }else if (userModel.getData().getUser_type().equals(Tags.TYPE_DELEGATE))
        {
            call  = Api.getService(Tags.base_url).getDelegateOrders(userModel.getData().getUser_id(),"new", 1);

            Log.e("sss","dddd");
        }


        call.enqueue(new Callback<OrderDataModel>() {
            @Override
            public void onResponse(Call<OrderDataModel> call, Response<OrderDataModel> response) {
                progBar.setVisibility(View.GONE);
                if (response.isSuccessful())
                {
                    orderModelList.clear();

                    if (response.body()!=null&&response.body().getData().size()>0)
                    {
                        tv_no_orders.setVisibility(View.GONE);
                        orderModelList.addAll(response.body().getData());
                        adapter.notifyDataSetChanged();
                        isFirstTime = false;
                    }else
                    {
                        tv_no_orders.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }


                }else
                {

                    Toast.makeText(activity,R.string.failed, Toast.LENGTH_SHORT).show();
                    try {
                        Log.e("Error_code",response.code()+"_"+response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<OrderDataModel> call, Throwable t) {
                try {
                    progBar.setVisibility(View.GONE);
                    Toast.makeText(activity, getString(R.string.something), Toast.LENGTH_SHORT).show();
                    Log.e("Error",t.getMessage());
                }catch (Exception e){}
            }
        });
    }

    private void loadMore(int page_index)
    {



        if (userModel.getData().getUser_type().equals(Tags.TYPE_CLIENT))
        {
            call  = Api.getService(Tags.base_url).getClientOrders(userModel.getData().getUser_id(),"new", page_index);
        }else if (userModel.getData().getUser_type().equals(Tags.TYPE_DELEGATE))
        {
            call  = Api.getService(Tags.base_url).getDelegateOrders(userModel.getData().getUser_id(),"new", page_index);

        }


        call.enqueue(new Callback<OrderDataModel>() {
            @Override
            public void onResponse(Call<OrderDataModel> call, Response<OrderDataModel> response) {
                orderModelList.remove(orderModelList.size()-1);
                adapter.notifyDataSetChanged();
                isLoading = false;

                if (response.isSuccessful())
                {

                    if (response.body()!=null&&response.body().getData().size()>0)
                    {
                        orderModelList.addAll(response.body().getData());
                        adapter.notifyDataSetChanged();
                        current_page = response.body().getMeta().getCurrent_page();


                    }
                }else
                {


                    Toast.makeText(activity,R.string.failed, Toast.LENGTH_SHORT).show();
                    try {
                        Log.e("Error_code",response.code()+"_"+response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<OrderDataModel> call, Throwable t) {
                try {
                    isLoading = false;
                    if (orderModelList.get(orderModelList.size()-1)==null)
                    {
                        orderModelList.remove(orderModelList.size()-1);
                        adapter.notifyDataSetChanged();
                    }
                    progBar.setVisibility(View.GONE);
                    Toast.makeText(activity, getString(R.string.something), Toast.LENGTH_SHORT).show();
                    Log.e("Error",t.getMessage());
                }catch (Exception e){}
            }
        });
    }

    public void setItemData(OrderDataModel.OrderModel orderModel) {

        if (userModel.getData().getUser_type().equals(Tags.TYPE_CLIENT))
        {
            activity.DisplayFragmentClientOrderDetails(orderModel);
        }else
        {
            activity.DisplayFragmentDelegateAddOffer(orderModel);
        }
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (ClientHomeActivity) context;
    }
}
