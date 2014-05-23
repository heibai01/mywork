package com.led.weatherdemo;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.led.weatherdemo.bean.WeatherConfig;
import com.led.weatherdemo.bean.WeatherInfo;
import com.led.weatherdemo.engine.WeatherEngine;
import com.led.weatherdemo.net.HttpClientUtil;
import com.led.weatherdemo.util.BeanFactory;
import com.led.weatherdemo.util.DensityUtil;
import com.led.weatherdemo.util.GetYahooWeatherSaxTools;

/**
 * 天气预报显示
 * 
 * @author heibai
 * @company http://www.bts-led.com/
 * @date 2014年5月21日
 */
public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";
	private String[] mShowTypeOfTemperature = { "°C", "°F" };
	SharedPreferences sp;
	private List<String> mWeekList;
	/**
	 * 安卓终端唯一id标识
	 */
	String mAndroidId;
	/**
	 * 显示天气的容器，水平方向
	 */
	private LinearLayout mWeatherContainer;
	WeatherEngine mWeatherEngine;
	View view = null;
	private WeatherConfig config;
	/**
	 * 语言
	 */
	private static final int DEFAULT = 0;
	private static final int CHINESE = 1;
	private static final int ENGLISH = 2;
	private static final int WEATHER_IS_READY = 0;

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == WEATHER_IS_READY) {// 利用handler发送延时消息,根据用户指定更新时间,更新天气
				if (mWeatherContainer.getChildCount() != 0) {
					mWeatherContainer.removeAllViews();
				}
				// String cityCode = config.getCityCode();
				String temperature = config.getShowType() > 0 ? "f" : "c";
				showWeatherInfos("2161853", temperature);
				handler.sendEmptyMessageDelayed(WEATHER_IS_READY,
						config.getUpdateFrequency() * 60 * 60);
			}
			super.handleMessage(msg);
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		sp = getSharedPreferences("weather_config", Context.MODE_PRIVATE);
		init();

	}

	/**
	 * 初始化,查找ID,获取天气配置文件,设置容器属性
	 */
	private void init() {
		mWeatherContainer = (LinearLayout) findViewById(R.id.ll_weather);
		// 利用工厂模式,获取天气业务对象,得到天气配置信息
		mWeatherEngine = BeanFactory.getImpl(WeatherEngine.class);
		config = mWeatherEngine.getWeatherConfig();
		// 设置语言
		int id = config.getLanguage();
		Log.i(TAG, "langauge_id=" + id);

		if (sp.getInt("language", 0) != id) {
			// 应用内配置语言
			Resources resources = getResources();// 获得res资源对象
			Configuration lanConfig = resources.getConfiguration();// 获得设置对象
			DisplayMetrics dm = resources.getDisplayMetrics();// 获得屏幕参数：主要是分辨率，像素等。
			switch (id) {
			case DEFAULT:
				lanConfig.locale = Locale.getDefault(); // 系统默认语言
				break;
			case CHINESE:
				lanConfig.locale = Locale.SIMPLIFIED_CHINESE; // 简体中文
				break;
			case ENGLISH:
				lanConfig.locale = Locale.ENGLISH; // 英文
				break;
			case 3:
				// lanConfig.locale = Locale.ar;//阿拉伯语
				lanConfig.locale = new Locale("ar");
				break;
			default:
				lanConfig.locale = Locale.getDefault();
				break;
			}
			resources.updateConfiguration(lanConfig, dm);
			finish();
			Intent intent = new Intent(this, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getApplicationContext().startActivity(intent);
			sp.edit().putInt("language", id).commit();
		}
		mAndroidId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
		// 周列表,获取对应在数字,好直接从res/arrays获取不同国家的语言
		mWeekList = new ArrayList<String>();
		mWeekList.add("Sun");
		mWeekList.add("Mon");
		mWeekList.add("Tue");
		mWeekList.add("Wed");
		mWeekList.add("Thu");
		mWeekList.add("Fri");
		mWeekList.add("Sat");

		// 容器的大小及位置由用户指定
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				DensityUtil.dip2px(MainActivity.this, config.getWidth()),
				DensityUtil.dip2px(MainActivity.this, config.getHeight()));
		mWeatherContainer.setLayoutParams(params);
		// mWeatherContainer.layout(config.getStartX(), config.getStartY(),
		// DensityUtil.px2dip(MainActivity.this, config.getWidth()),
		// DensityUtil.px2dip(MainActivity.this, config.getHeight()));
		mWeatherContainer.setBackgroundColor(Color.YELLOW);
	}

	@Override
	protected void onResume() {
		if (mWeatherContainer.getChildCount() != 0) {
			mWeatherContainer.removeAllViews();
		}
		String temperature = config.getShowType() > 0 ? "f" : "c";
		showWeatherInfos("2161853", temperature);
		handler.sendEmptyMessageDelayed(WEATHER_IS_READY,
				config.getUpdateFrequency() * 60 * 60 * 1000);
		super.onResume();
	}

	/**
	 * 根据城市代号与温度显示方式从雅虎服务器异步获取数据,显示天气预报
	 * 
	 * @param cityCode
	 *            城市代号
	 * @param temperature
	 *            f/c
	 */
	private void showWeatherInfos(String cityCode, String temperature) {
		String url = ConstantValue.YAHOO_WEATHER_URI + "?w=" + cityCode + "&u="
				+ temperature;
		new AsyncTask<String, Void, String>() {
			private List<WeatherInfo> infos;
			SAXParserFactory factory = null;
			SAXParser saxParser = null;
			XMLReader xmlReader = null;
			GetYahooWeatherSaxTools tools = null;

			protected void onPreExecute() {
				try {
					factory = SAXParserFactory.newInstance();
					saxParser = factory.newSAXParser();
					xmlReader = saxParser.getXMLReader();
					infos = new ArrayList<WeatherInfo>();
					tools = new GetYahooWeatherSaxTools(infos);
				} catch (Exception e) {
					e.printStackTrace();
				}

			};

			protected String doInBackground(String[] params) {
				HttpClientUtil http = new HttpClientUtil();
				return http.sendDataByGet(params[0]);
			};

			protected void onPostExecute(String result) {
				if (result != null) {
					Log.i("onPostExecute", "成功了");
					try {
						xmlReader.setContentHandler(tools);
						xmlReader.parse(new InputSource(
								new StringReader(result)));
						// 获取当前是周几,用数字表示
						int currentDay = mWeekList.indexOf(infos.get(0)
								.getDay());
						int forecastDay = 0;
						// 将得到的list天气数据,展示到屏幕上
						for (int i = 0; i < config.getForecastDays(); i++) {
							view = View.inflate(MainActivity.this,
									R.layout.weather_item, null);
							TextView tvDay = (TextView) view
									.findViewById(R.id.tv_day);
							TextView tvLow = (TextView) view
									.findViewById(R.id.tv_temperature_low);
							TextView tvHigh = (TextView) view
									.findViewById(R.id.tv_temperature_high);
							TextView tvWeather = (TextView) view
									.findViewById(R.id.tv_weather);
							ImageView ivWeatherIcon = (ImageView) view
									.findViewById(R.id.iv_weather_icon);
							forecastDay = currentDay + i;
							if (forecastDay >= 7) {
								forecastDay = forecastDay % 7;
							}
							tvDay.setText(getResources().getStringArray(
									R.array.weather_day_normalform)[forecastDay]);
							tvLow.setText(infos.get(i).getLow()
									+ mShowTypeOfTemperature[config
											.getShowType()]);
							tvHigh.setText(infos.get(i).getHigh()
									+ mShowTypeOfTemperature[config
											.getShowType()]);
							tvWeather.setText(getResources().getStringArray(
									R.array.weather_condition)[infos.get(i)
									.getCode()]);
							ivWeatherIcon.setImageResource(R.drawable.w0);

							int width = config.getWidth()
									/ config.getForecastDays();
							int height = config.getHeight();
							float wScale = width / 100f;
							float hScale = height / 100f;
							float squarScale = width * height / (100 * 100);

							LayoutParams viewChildParams = (LayoutParams) tvDay
									.getLayoutParams();
							viewChildParams.height *= hScale;
							viewChildParams.width *= wScale;
							tvDay.setLayoutParams(viewChildParams);

							viewChildParams = (LayoutParams) tvLow
									.getLayoutParams();
							viewChildParams.height *= hScale;
							viewChildParams.width *= wScale;
							tvLow.setLayoutParams(viewChildParams);
							tvHigh.setLayoutParams(viewChildParams);

							viewChildParams = (LayoutParams) tvWeather
									.getLayoutParams();
							viewChildParams.height *= hScale;
							viewChildParams.width *= wScale;
							tvWeather.setLayoutParams(viewChildParams);

							viewChildParams = (LayoutParams) ivWeatherIcon
									.getLayoutParams();
							viewChildParams.height *= hScale;
							viewChildParams.width *= wScale;
							ivWeatherIcon.setLayoutParams(viewChildParams);

							tvDay.setTextSize(DensityUtil.dip2px(
									MainActivity.this, 14 * squarScale));
							tvLow.setTextSize(DensityUtil.dip2px(
									MainActivity.this, 14 * squarScale));
							tvHigh.setTextSize(DensityUtil.dip2px(
									MainActivity.this, 14 * squarScale));
							tvWeather.setTextSize(DensityUtil.dip2px(
									MainActivity.this, 14 * squarScale));

							LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(
									LayoutParams.MATCH_PARENT,
									LayoutParams.MATCH_PARENT);
							viewParams.weight = 1;

							mWeatherContainer.addView(view, viewParams);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
		}.execute(url);
	}

	@Override
	protected void onDestroy() {
		mWeatherContainer.removeAllViews();
		mWeatherContainer = null;
		mWeekList = null;
		super.onDestroy();
	}
}