/*
 * Copyright (C) 2013 Andreas Stuetz <andreas.stuetz@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qzl.mymusic;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.astuetz.PagerSlidingTabStrip;
import com.qzl.mymusic.application.CodingkePlayerApp;
import com.qzl.mymusic.fragment.MyMusicListFragment;
import com.qzl.mymusic.fragment.NetMusicListFragment;
import com.qzl.mymusic.serivice.PlayService;

public class MainActivity extends BaseActivity {

	private final Handler handler = new Handler();

	private PagerSlidingTabStrip tabs;
	private ViewPager pager;
	private MyPagerAdapter adapter;

	//当前所选中的颜色值
	private Drawable oldBackground = null;
	private int currentColor = 0xFF3F9FE0;

	private MyMusicListFragment myMusicListFragment;
	private NetMusicListFragment netMusicListFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		pager = (ViewPager) findViewById(R.id.pager);
		adapter = new MyPagerAdapter(getSupportFragmentManager());

		pager.setAdapter(adapter);

		final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
				.getDisplayMetrics());
		pager.setPageMargin(pageMargin);

		tabs.setViewPager(pager);

		changeColor(currentColor);

	}

	//自定义的接口
	@Override
	public void publish(int progress) {
		//更新进度条
	}

	@Override
	public void change(int position) {
		//切换状态播放位置
		if(pager.getCurrentItem() == 0){
			myMusicListFragment.loadDate();
			//调运MymusicListFragment里的方法
			myMusicListFragment.changeUIStatusOnPlay(position);
		}else if(pager.getCurrentItem() == 1){

		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case R.id.action_ilike:
				startActivity(new Intent(this,MyLikeMusicListActivity.class));
				break;
			case R.id.action_record:
				startActivity(new Intent(this,PlayRecordListActivity.class));
				break;
			case R.id.action_about:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setIcon(R.mipmap.music);
				builder.setTitle("关于");
				builder.setMessage("1.本应用原型为扣丁音乐\n2.仅供学习，严禁用于商业交易\n3.作者：QZL强哥\n4.QQ：2538096489");
				builder.setCancelable(true);
				builder.setNeutralButton("我知道了", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.show();
				break;
			case R.id.action_help:
				builder = new AlertDialog.Builder(this);
				builder.setIcon(R.mipmap.music);
				builder.setTitle("帮助");
				builder.setMessage("点击左下角专辑封面图标加载音乐播放界面\n网络推荐来源百度音乐热歌榜\n关于退出后音乐仍在播放的问题，可以手动关闭后台进程");
				builder.setCancelable(true);
				builder.setNeutralButton("我知道了", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.show();
				break;
			case R.id.action_exit:
				stopService(new Intent(this, PlayService.class));
				finish();
				break;
		}

		return true;
	}

	private void changeColor(int newColor) {

		tabs.setIndicatorColor(newColor);

		// change ActionBar color just if an ActionBar is available
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

			Drawable colorDrawable = new ColorDrawable(newColor);
			Drawable bottomDrawable = getResources().getDrawable(R.drawable.actionbar_bottom);
			LayerDrawable ld = new LayerDrawable(new Drawable[] { colorDrawable, bottomDrawable });

			if (oldBackground == null) {

				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
					ld.setCallback(drawableCallback);
				} else {
					getActionBar().setBackgroundDrawable(ld);
				}

			} else {

				TransitionDrawable td = new TransitionDrawable(new Drawable[] { oldBackground, ld });

				// workaround for broken ActionBarContainer drawable handling on
				// pre-API 17 builds
				// https://github.com/android/platform_frameworks_base/commit/a7cc06d82e45918c37429a59b14545c6a57db4e4
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
					td.setCallback(drawableCallback);
				} else {
					getActionBar().setBackgroundDrawable(td);
				}

				td.startTransition(200);

			}

			oldBackground = ld;

			// http://stackoverflow.com/questions/11002691/actionbar-setbackgrounddrawable-nulling-background-from-thread-handler
			getActionBar().setDisplayShowTitleEnabled(false);
			getActionBar().setDisplayShowTitleEnabled(true);

		}

		currentColor = newColor;

	}

	public void onColorClicked(View v) {

		int color = Color.parseColor(v.getTag().toString());
		changeColor(color);

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("currentColor", currentColor);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		currentColor = savedInstanceState.getInt("currentColor");
		changeColor(currentColor);
	}

	private Drawable.Callback drawableCallback = new Drawable.Callback() {
		@Override
		public void invalidateDrawable(Drawable who) {
			getActionBar().setBackgroundDrawable(who);
		}

		@Override
		public void scheduleDrawable(Drawable who, Runnable what, long when) {
			handler.postAtTime(what, when);
		}

		@Override
		public void unscheduleDrawable(Drawable who, Runnable what) {
			handler.removeCallbacks(what);
		}
	};


	public class MyPagerAdapter extends FragmentPagerAdapter {

		//标题部分
		private final String[] TITLES = { "我的音乐", "网络推荐"};

		public MyPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return TITLES[position];
		}

		@Override
		public int getCount() {
			return TITLES.length;
		}

		@Override
		public Fragment getItem(int position) {
			if(position == 0){
				//本地音乐
				if( myMusicListFragment== null){
					myMusicListFragment = MyMusicListFragment.newInstance();
				}
				return myMusicListFragment;
			}else if(position == 1){
				//网络音乐
				if(netMusicListFragment == null){
					netMusicListFragment = NetMusicListFragment.newInstance();
				}
				return netMusicListFragment;
			}
			return null;
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//保存当前播放的一些状态值
		CodingkePlayerApp app = (CodingkePlayerApp) getApplication();
		SharedPreferences.Editor editor = app.sp.edit();
		editor.putInt("currentPostion",playService.getCurrentPosition());
		editor.putInt("play_mode",playService.getPlay_mode());
		editor.commit();
	}
}