package com.carter.graduation.design.music.activity;

import android.bluetooth.BluetoothHeadset;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.carter.graduation.design.music.R;
import com.carter.graduation.design.music.event.PlayingWayEvent;
import com.carter.graduation.design.music.fragment.MusicDynamicFragment;
import com.carter.graduation.design.music.fragment.MusicFragment;
import com.carter.graduation.design.music.fragment.TimingFragment;
import com.carter.graduation.design.music.global.GlobalConstants;
import com.carter.graduation.design.music.receiver.HeadsetReceiver;
import com.carter.graduation.design.music.service.MusicPlayerService;
import com.carter.graduation.design.music.utils.SpUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;


/**
 * 应用主界面
 */
public class HomeDetailActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        MusicDynamicFragment.OnFragmentInteractionListener, View.OnClickListener {

    private boolean isRandom;
    private static final String TAG = "HomeDetailActivity";
    private ArrayList<ImageView> tabs = new ArrayList<>();
    private ImageView barnet, barmusic;
    private ViewPager mMainViewPager;
    private long time = 0;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    private Context mContext;
    private FloatingActionButton mFab;
    private MusicFragment mMusicFragment;
    private ViewPager.OnPageChangeListener mPageChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_detail);
        mContext = this;
        Log.d(TAG, "onCreate: ");
        bindService();
        bindReceiver();
        initView();
        initUse();
    }

    private void initUse() {
        isRandom = (boolean) SpUtils.get(mContext, GlobalConstants.IS_RANDOM, false);
        Log.d(TAG, "initUse: " + isRandom);
        if (isRandom) {
            mFab.setImageResource(R.drawable.random);
        } else {
            mFab.setImageResource(R.drawable.single);
        }
        PlayingWayEvent wayEvent = PlayingWayEvent.getInstance();
        wayEvent.setRandom(isRandom);
        EventBus.getDefault().postSticky(wayEvent);
    }

    private void initView() {
        barnet = findViewById(R.id.bar_net);
        barmusic = findViewById(R.id.bar_music);
        ImageView ivSearch = findViewById(R.id.bar_search);
        ivSearch.setOnClickListener(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("我的音乐");
        setSupportActionBar(toolbar);

        mFab = findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/

                PlayingWayEvent wayEvent = PlayingWayEvent.getInstance();
                getThePlayWay(mFab);
                SpUtils.put(mContext, GlobalConstants.IS_RANDOM, isRandom);
                wayEvent.setRandom(isRandom);
                EventBus.getDefault().post(wayEvent);
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setViewPager();
        barmusic.setOnClickListener(this);
        barnet.setOnClickListener(this);

    }

    private void getThePlayWay(FloatingActionButton fab) {
        if (isRandom) {
            isRandom = false;
            fab.setImageResource(R.drawable.single);
            Toast.makeText(mContext, "单曲播放", Toast.LENGTH_SHORT).show();
        } else {
            isRandom = true;
            fab.setImageResource(R.drawable.random);
            Toast.makeText(mContext, "随机播放", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 绑定监听耳机状态的receiver
     */
    private void bindReceiver() {
        HeadsetReceiver headsetReceiver = new HeadsetReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        intentFilter.addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);
        registerReceiver(headsetReceiver, intentFilter);
    }

    private void setViewPager() {
        tabs.add(barnet);
        tabs.add(barmusic);
        mMainViewPager = findViewById(R.id.main_viewpager);
        MusicDynamicFragment musicDynamicFragment = new MusicDynamicFragment();
        MainViewPagerAdapter pagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(musicDynamicFragment);
        if (mMusicFragment == null) {
            mMusicFragment = new MusicFragment();
        }
        pagerAdapter.addFragment(mMusicFragment);

//        添加music页面 和  摇动界面
        mMainViewPager.setAdapter(pagerAdapter);
        mMainViewPager.setCurrentItem(1);
        barmusic.setSelected(true);
        mPageChangeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switchTabs(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };
        mMainViewPager.addOnPageChangeListener(mPageChangeListener);

    }

    private void switchTabs(int position) {
        for (int i = 0; i < tabs.size(); i++) {
            if (position == i) {
                tabs.get(i).setSelected(true);
            } else {
                tabs.get(i).setSelected(false);
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_detail, menu);
        MenuItem item = menu.findItem(R.id.action_share);
        ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        Intent sendIntent = new Intent();
        try {
            String appDir = getPackageManager().getApplicationInfo("com.carter.graduation.design.music", 0)
                    .sourceDir;
            appDir = "//" + appDir;
            Uri uri = Uri.parse(appDir);

            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
            sendIntent.setType("*/*");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        shareActionProvider.setShareIntent(sendIntent);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        return id == R.id.action_share || super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_about_app:
                Intent intent = new Intent(this, AboutAppActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_slideshow:
                TimingFragment timingFragment = new TimingFragment();
                timingFragment.show(getFragmentManager(), "timing");
                break;
            case R.id.nav_exit:
                finish();
                break;
        }

        /* else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }*/
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment != null) {
                    fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
                }
            }
        }

    }

    @Override
    public void onFragmentInteraction() {
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - time) > 1000) {
                Toast.makeText(this, "再按一次返回桌面", Toast.LENGTH_SHORT).show();
                time = System.currentTimeMillis();
            } else {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    /**
     * 绑定音乐播放的service
     */
    public void bindService() {
        Intent intent = new Intent(this, MusicPlayerService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMainViewPager.removeOnPageChangeListener(mPageChangeListener);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bar_search:
                Intent intent = new Intent(mContext, LocalSearchActivity.class);
                startActivity(intent);
                break;
            case R.id.bar_net:
                mMainViewPager.setCurrentItem(0);
                break;
            case R.id.bar_music:
                //默认页面
                mMainViewPager.setCurrentItem(1);
                break;
            default:
                break;
        }
    }

    public static class MainViewPagerAdapter extends FragmentPagerAdapter {

        ArrayList<Fragment> mFragments = new ArrayList<>();

        MainViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        void addFragment(Fragment fragment) {
            mFragments.add(fragment);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }
    }
}
