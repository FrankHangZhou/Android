package com.android.green;

import static android.view.Window.PROGRESS_VISIBILITY_OFF;
import static android.view.Window.PROGRESS_VISIBILITY_ON;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

public class GreenActivity extends TabActivity implements OnTabChangeListener {
	/** Called when the activity is first created. */
	private static final String NAME = "name", NUMBER = "number",
			SORT_KEY = "sort_key";
	private ListView listview = null;
	private ListView personList;
	private ListView phoneList;
	private BaseAdapter adapter;
	private BaseAdapter adapterphone;// phone
	private TextView mEmptyText;
	private TextView mEditText; // PHONE
	private List<ContentValues> listData;
	private List<ContentValues> listDataphone;// phone
	private AsyncQueryHandler asyncQuery;
	private AsyncQueryHandler asyncQueryphone;// phone
	private List list;

	private Map<String, Object> listTemp;
	private EditText et;
	private EditText et1;
	private CheckBox whiteCb, defWhiteCb;
	private int index = 0;
	private AlertDialog addDlg, CopyDlg, removeDlg;
	/*-- configGrid�ײ��˵�ѡ���±�--*/
	private final int CONFIG_ITEM_APPLY = 0;// Ӧ��
	private final int CONFIG_ITEM_EXIT = 1;// �˳�
	/*-- managerGrid�ײ��˵�ѡ���±�--*/
	private final int MANAGE_ITEM_ADD = 0; // ���
	private final int MANAGE_ITEM_REMOVE = 1; // ɾ��
	private final int MANAGE_ITEM_EXIT = 2; // �˳�

	// menu ����SIM����ϵ��ֵ������Ӻ�ɾ���Լ���ʼ���ӿ�
	protected final static int MENU_ADD = Menu.FIRST;// ���
	protected final static int MENU_DELETE = Menu.FIRST + 1;// ɾ��

	protected int myMenuSettingTag = 0;
	protected Menu myMenu;
	private static final int myMenuResources[] = { R.menu.a_menu, R.menu.b_menu };
	// ȫ��hashmap ��Ϊ��� frank
	private HashMap<Integer, Boolean> isSelected;
	private HashMap<Integer, Boolean> isSelectedphone;// PHONE

	// �첽������ӿ�
	// *********************************************//
	protected static final int QUERY_TOKEN = 0;// ʵ��
	protected static final int INSERT_TOKEN = 1;// ʵ��
	protected static final int UPDATE_TOKEN = 2;
	protected static final int DELETE_TOKEN = 3;// ʵ��
	protected static final int DELETE_TOKEN_ALL = 4;
	protected static final int SEARCH_TOKEN = 5;

	protected static final String ADD = "add";
	protected static final String DELETE = "delete";

	View layout;// SIM
	LayoutInflater inflater;

	// *********************************************//
	/*
	 * UI����
	 */
	@SuppressWarnings("unused")
	private Handler hand = new Handler() {

		public void handleMessage(Message msg) {
			if (!Thread.currentThread().isInterrupted()) {
				switch (msg.what) {
				case 0:
					Toast.makeText(getApplicationContext(), "û��SIM����ϵ������", 0)
							.show();
					break;
				case 1:
					Toast.makeText(getApplicationContext(), "��ѡ����Ҫɾ��������~", 0)
							.show();
					break;

				case 2:
					Toast.makeText(getApplicationContext(), "ɾ���ɹ�~", 0).show();
					break;
					
					
				case 3:
					Toast.makeText(getApplicationContext(), "û���ֻ���ϵ������", 0)
							.show();
					break;
				case 4:
					Toast.makeText(getApplicationContext(), "��ѡ����Ҫ����������~", 0)
							.show();
					break;

				case 5:
					Toast.makeText(getApplicationContext(), "�����ɹ�~", 0).show();
					break;

				default:
					break;
				}
			}
		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);
		mEmptyText = (TextView) findViewById(android.R.id.empty);// ���SIM����ϵ�˲�����ʾ
		personList = (ListView) findViewById(R.id.listview);// SIM����ϵ��
		phoneList = (ListView) findViewById(R.id.listview1);// �绰��ϵ��
		mEditText = (TextView) findViewById(android.R.id.edit);// ����ֻ���ϵ�˲�����ʾ
		inflater = (LayoutInflater) this
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		layout = inflater.inflate(R.layout.add, null);

		// �Ի������
		addDlg = new AlertDialog.Builder(GreenActivity.this).setTitle("���")
				.setView(layout)
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						insert();

					}

				}).setNegativeButton("ȡ��", null).create();

		// ɾ��ȷ�϶Ի���
		removeDlg = new AlertDialog.Builder(this).setTitle("ɾ��")
				.setMessage("ȷ��ɾ��ѡ�е�������")
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						// delete();

						deletesim del = new deletesim();
						del.execute();

					}
				}).setNegativeButton("ȡ��", null).create();
		// COPY�����Ի���
		CopyDlg = new AlertDialog.Builder(this).setTitle("������SIM��")
				.setMessage("ȷ������ѡ������ô��")
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						copytosim cp=new copytosim();
						cp.execute();

					}
				}).setNegativeButton("ȡ��", null).create();

		// TabHost���������
		int width = 55;
		int height = 60;
		final TabHost tabhost = getTabHost();
		tabhost.setBackgroundColor(Color.argb(150, 22, 70, 150));
		// frank
		Drawable localDrawable1 = getResources().getDrawable(
				R.drawable.sim_icon);
		Drawable localDrawable2 = getResources().getDrawable(
				R.drawable.phone_icon);
		tabhost.addTab(tabhost.newTabSpec("sim")
				.setIndicator("SIM����ϵ��", localDrawable1).setContent(R.id.cb));
		tabhost.addTab(tabhost.newTabSpec("phone")
				.setIndicator("�ֻ���ϵ��", localDrawable2).setContent(R.id.list));

		final TabWidget tabwidget = tabhost.getTabWidget();

		for (int i = 0; i < tabwidget.getChildCount(); i++) {
			// ����ҳǩ�߶Ⱥ�ҳǩ����������
			TextView tv = (TextView) tabwidget.getChildAt(i).findViewById(
					android.R.id.title);
			tabwidget.getChildAt(i).getLayoutParams().height = height;
			tabwidget.getChildAt(i).getLayoutParams().width = width;
			// tabwidget.setBackgroundResource(R.drawable.toolbar_menu_bg);
			tv.setTextSize(15);
			tv.setTextColor(Color.BLACK);
			tv.setGravity(Gravity.CENTER_HORIZONTAL);
		}
		// ����
		tabhost.setOnTabChangedListener(this);

		/************** Ĭ��SIM����ϵ��*******************/
		asyncQuery = new MyAsyncQueryHandler(getContentResolver());
		// �첽��ȡ��ϵ�˵���Ϣ
		asyncQueryContact();
		/************************************************/
		


	}

	/*
	 * SIM����ϵ�˶�ȡ
	 */
	private void asyncQueryContact() {
		// TODO Auto-generated method stub
		// for SIM����ϵ��
		Uri uri = Uri.parse("content://icc/adn");
		String[] projection = new String[] { "name", "phone" };
		String selection = null;
		String[] selectionArgs = null;
		String sortOrder = "name" + " COLLATE LOCALIZED ASC";
		asyncQuery.startQuery(0, null, uri, projection, selection,
				selectionArgs, sortOrder);
		Log.i("frank", "asyncQueryContact-----0 " + mEmptyText);
		displayProgress(true);
	}

	// ˢ��
	private void reQuery() {
		asyncQueryContact();
	}

	/*
	 * �첽�ӿڲ���������ݿ��ֹANR����
	 */
	private class MyAsyncQueryHandler extends AsyncQueryHandler {

		public MyAsyncQueryHandler(ContentResolver cr) {
			super(cr);

		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			if (cursor != null && cursor.getCount() > 0) {
				Log.i("frank", "----------------------------" + cursor);
				listData = new ArrayList<ContentValues>();
				// cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					ContentValues cv = new ContentValues();
					cursor.moveToPosition(i);
					String name = cursor.getString(0);
					String number = cursor.getString(1);
					String sortKey = Integer.toString(i);// cursor.getString(2);//��I����������ֵfrank
					// for �ֻ���ϵ��
					// String name = cursor.getString(1);
					// String number = cursor.getString(2);
					// String sortKey = cursor.getString(3);

					/********************************/
					Log.i("frank", "----name:" + name + " number:" + number
							+ " sortKey:" + sortKey);
					if (number.startsWith("+86")) { // �����Ȳ�����
						cv.put(NAME, name);
						// process (+86)
						cv.put(NUMBER, number.substring(3));
						cv.put(SORT_KEY, sortKey);
					} else {
						cv.put(NAME, name);
						cv.put(NUMBER, number);
						cv.put(SORT_KEY, sortKey);
					}
					listData.add(cv);
				}
				if (listData.size() > 0) {
					Log.i("frank", "---listData.size()" + listData.size());
					mEmptyText.setVisibility(View.GONE);// ��ʧ
					setAdapter(listData);
				}
				cursor.close();
			} else {// û������
				listData = null;// ���
				displayProgress(false);
			}
		}

	}

	private void setAdapter(List<ContentValues> listData) {
		adapter = new ListAdapter(this, listData);
		personList.setAdapter(adapter);
		// frank
		personList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Log.i("frank", "----------------------------setAdapter arg2: "
						+ arg2);
				CheckBox cbx = (CheckBox) arg1
						.findViewById(R.id.widget_checkbox);
				if (cbx != null) {
					cbx.toggle();
					((ListAdapter) adapter).select(arg2, cbx.isChecked());
				}
				//
			}

		});

	}

	/*
	 * �ڲ��� ����������listview ���SIM����ϵ��
	 */
	private class ListAdapter extends BaseAdapter {

		private LayoutInflater inflater;
		private List<ContentValues> list;

		public ListAdapter(Context context, List<ContentValues> list) {

			this.inflater = LayoutInflater.from(context);
			this.list = list;
			isSelected = null;// frank ���
			isSelected = new HashMap<Integer, Boolean>(); // frank
			selectAll(false); // ��ʼ��
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;

			if (convertView == null) {
				convertView = inflater.inflate(R.layout.list_item, null);
				holder = new ViewHolder();
				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.number = (TextView) convertView
						.findViewById(R.id.number);
				holder.cb = (CheckBox) convertView
						.findViewById(R.id.widget_checkbox);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			ContentValues cv = list.get(position);
			holder.name.setText(cv.getAsString(NAME));
			holder.number.setText(cv.getAsString(NUMBER));
			Log.i("frank",
					"--------------------getViewsortkey: "
							+ cv.getAsString(SORT_KEY));

			Boolean b = isSelected.get(position);
			if (null == b)
				b = false;
			if (convertView instanceof ViewGroup) {
				ViewGroup g = (ViewGroup) convertView;
				for (int i = 0; i < g.getChildCount(); i++) {
					View v = g.getChildAt(i);
					if (v instanceof CheckBox) {
						((CheckBox) v).setChecked(b);
						break;
					}
				}
			}

			return convertView;
		}

		public void select(int postion, boolean isChecked) {
			isSelected.put(postion, isChecked);
		}

		public void selectAll(boolean isChecked) {
			int a = this.getCount();
			for (int b = 0; b < a; b++) {
				select(b, isChecked);
			}
		}

		private class ViewHolder {
			TextView name;
			TextView number;
			CheckBox cb;
		}

	}

	// UI״̬
	private void displayProgress(boolean flag) {
		mEmptyText.setVisibility(View.VISIBLE);// ʹ��
		mEmptyText.setText(flag ? "���ڶ�ȡSIM�������Ժ�..." : "����SIM����û��SIM����ϵ�ˣ�");
		getWindow().setFeatureInt(Window.FEATURE_INDETERMINATE_PROGRESS,
				flag ? PROGRESS_VISIBILITY_ON : PROGRESS_VISIBILITY_OFF);
	}

	// �����ϵ��
	private void insert() {
		Log.d("frank", "insert()�����ϵ��---");
		EditText name = (EditText) layout.findViewById(R.id.etname);
		EditText tel = (EditText) layout.findViewById(R.id.etphonenumber);
		if (name.getText().toString().trim().equals("")
				|| tel.getText().toString().trim().equals("")) {
			Toast.makeText(this, "���������룬����Ϊ�գ�", Toast.LENGTH_SHORT).show();
		} else {
			ContentValues values = new ContentValues();
			ContentResolver localContentResolver = getContentResolver();
			values.put("tag", name.getText().toString().trim());
			values.put("number", tel.getText().toString().trim());
			Uri localUri = Uri.parse("content://icc/adn");
			Uri newSimContactUri = localContentResolver
					.insert(localUri, values);
			Log.d("frank", ">>>>>>" + "new sim contact uri, "
					+ newSimContactUri.toString());

			name.setText("");
			tel.setText("");
			reQuery();// ����������

		}
	}

	// ɾ��ѡ�е� SIM����ϵ�ˣ�����������첽����Ȼ�϶��Ῠ��

	class deletesim extends AsyncTask<Void, Void, Integer> {

		private ProgressDialog progressDialog;

		protected void onPreExecute() {
			// TODO Auto-generated method stub
			progressDialog = ProgressDialog.show(GreenActivity.this, "���Ե�",
					"����ɾ��......", true);
		}

		protected Integer doInBackground(Void... params) {
			// TODO Auto-generated method stub
			int mark = delete();
			Log.i("frank", "doInBackground  mark;  " + mark);
			return mark;
		}

		protected void onPostExecute(Integer result) {
			if (result == 0) {
				progressDialog.dismiss();
				Log.i("frank", "Handler onPostExecute");
				Message message = new Message();
				message.what = 0;
				hand.sendMessage(message);

			} else if (result == 1) {// һ����ûѡ��

				progressDialog.dismiss();
				Message message = new Message();
				message.what = 1;
				hand.sendMessage(message);

			} else if (result == 2) {// ��ѡ��

				progressDialog.dismiss();
				Message message = new Message();
				message.what = 2;
				hand.sendMessage(message);
				reQuery();// ����������
			}

		}

	}

	private int delete() {

		Boolean mark = false;
		if (listData == null) {
			// û������
			return 0;

		}

		if (listData.size() <= 0) {
			// û������
			return 0;
		}
		if (((ListAdapter) adapter).getCount() <= 0) {
			// û������
			return 0;

		}

		for (int i = 0; i < ((ListAdapter) adapter).getCount(); i++) {
			Log.i("frank", "delete: " + isSelected.get(i) + " i: " + i);
			Log.i("frank", "listData.get(i).getAsString(NAME)��   "
					+ listData.get(i).getAsString(NAME));
			if (isSelected.get(i) == true) {// ɾ��
				Log.i("frank", "delete ɾ��: " + i + "name���֣� "
						+ listData.get(i).getAsString(NAME));
				ContentResolver localContentResolver = getContentResolver();
				Uri localUri = Uri.parse("content://icc/adn");
				String where = "tag='" + listData.get(i).getAsString(NAME)
						+ "'";
				where += " AND number='" + listData.get(i).getAsString(NUMBER)
						+ "'";// ���� AND �绰

				localContentResolver.delete(localUri, where, null);
				// query();
				mark = true;// ֻҪ��ѡ�е� ��Ϊ��
			}

		}
		// ɾ������������ˢ�½��������� frank
		if (mark == false) {

			return 1; // һ����ûѡ��
		} else {
			return 2;// ��ѡ��
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 * �������ֽ��洦����tabҳ����Ӧ��ͬ��menu frank
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		// Hold on to this
		myMenu = menu;
		myMenu.clear();// ���MENU�˵�
		// Inflate the currently selected menu XML resource.
		MenuInflater inflater = getMenuInflater();
		// ��TabActivity�����ȡһ��MENU������
		switch (myMenuSettingTag) {
		case 1:
			inflater.inflate(myMenuResources[0], menu);
			Log.i("frank", "onCreateOptionsMenu--0");
			// ��̬���������ж�Ӧ��XML MENU�˵�
			break;
		case 2:
			inflater.inflate(myMenuResources[1], menu);
			Log.i("KKKKKKK", "onCreateOptionsMenu--1");
			break;
		default:
			inflater.inflate(myMenuResources[0], menu);
			break;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		super.onOptionsItemSelected(item);
		Log.i("frank",
				"onCreateOptionsMenu--item.getItemId():    "
						+ item.getTitleCondensed());
		if (item.getTitleCondensed().toString().contains("ɾ��")) {
			Log.i("frank", "delete");
			removeDlg.show();
		}
		if (item.getTitleCondensed().toString().contains("���")) {
			Log.i("frank", "ADD");
			addDlg.show();
		}
		if (item.getTitleCondensed().toString().contains("���Ƶ�SIM��")) {
			Log.i("frank", "COPY");
			CopyDlg.show();
		}

		return true;
	}

	@Override
	public void onTabChanged(String tabId) {
		// TODO Auto-generated method stub
		if (tabId.equals("sim")) {
			myMenuSettingTag = 1;
		}
		if (tabId.equals("phone")) {
			myMenuSettingTag = 2;
			/************** �ֻ���ϵ��************************/
			asyncQueryphone = new MyAsyncQueryHandlerphone(getContentResolver());
			asyncQueryContactphone();
			Log.i("kkkkkkkkkkk", "myMenuSettingTag----------------------------"
					+ myMenuSettingTag);

			/************************************************/

		}
		if (myMenu != null) {
			onCreateOptionsMenu(myMenu);
		}

	}

	/******************************************************** �ֻ���ϵ�˲��֣��������Ϸ��㣬ȫ��һ��class���� frank**********************************************************/

	private void reQueryphone() {

		asyncQueryContactphone();
	}

	/*
	 * �ֻ��绰��ϵ�˶�ȡ
	 */
	private void asyncQueryContactphone() {
		// TODO Auto-generated method stub
		// for �ֻ��绰����ϵ��
		Uri uri = Uri.parse("content://com.android.contacts/data/phones");
		String[] projection = { "_id", "display_name", "data1", "sort_key" };
		asyncQueryphone.startQuery(0, null, uri, projection, null, null,
				"sort_key COLLATE LOCALIZED asc");
		displayProgressphone(true);
	}

	private void displayProgressphone(boolean flag) {
		mEditText.setVisibility(View.VISIBLE);// ʹ��
		mEditText.setText(flag ? "���ڶ�ȡ�ֻ���ϵ�ˣ����Ժ�..." : "�����ֻ���û����ϵ�ˣ�");
		getWindow().setFeatureInt(Window.FEATURE_INDETERMINATE_PROGRESS,
				flag ? PROGRESS_VISIBILITY_ON : PROGRESS_VISIBILITY_OFF);
	}

	private void setAdapterphone(List<ContentValues> listData) {
		adapterphone = new ListAdapterphone(this, listData);
		phoneList.setAdapter(adapterphone);
		// frank
		phoneList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Log.i("kkkkkkkkkkk",
						"----------------------------setAdapter arg2: " + arg2);
				CheckBox cbx = (CheckBox) arg1
						.findViewById(R.id.widget_checkbox1);// phone
				if (cbx != null) {
					cbx.toggle();
					((ListAdapterphone) adapterphone).select(arg2,
							cbx.isChecked());
				}
				//
			}

		});

	}

	/*
	 * �ڲ��� ����������listview ����ֻ���ϵ��
	 */
	private class ListAdapterphone extends BaseAdapter {

		private LayoutInflater inflater;
		private List<ContentValues> list;// ˽��

		public ListAdapterphone(Context context, List<ContentValues> list) {

			this.inflater = LayoutInflater.from(context);
			this.list = list;
			isSelectedphone = null;// frank ���
			isSelectedphone = new HashMap<Integer, Boolean>(); // frank
			selectAll(false); // ��ʼ��
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;

			if (convertView == null) {
				convertView = inflater.inflate(R.layout.phonelistview, null);// phone
				holder = new ViewHolder();
				holder.name = (TextView) convertView
						.findViewById(R.id.namephone);
				holder.number = (TextView) convertView
						.findViewById(R.id.numberphone);
				holder.cb = (CheckBox) convertView
						.findViewById(R.id.widget_checkbox1);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			ContentValues cv = list.get(position);
			holder.name.setText(cv.getAsString(NAME));
			holder.number.setText(cv.getAsString(NUMBER));
			Log.i("kkkkkkkkkkk",
					"--------------------getViewsortkey: "
							+ cv.getAsString(SORT_KEY));

			Boolean b = isSelectedphone.get(position);
			if (null == b)
				b = false;
			if (convertView instanceof ViewGroup) {
				ViewGroup g = (ViewGroup) convertView;
				for (int i = 0; i < g.getChildCount(); i++) {
					View v = g.getChildAt(i);
					if (v instanceof CheckBox) {
						((CheckBox) v).setChecked(b);
						break;
					}
				}
			}

			return convertView;
		}

		public void select(int postion, boolean isChecked) {
			isSelectedphone.put(postion, isChecked);
		}

		public void selectAll(boolean isChecked) {
			int a = this.getCount();
			for (int b = 0; b < a; b++) {
				select(b, isChecked);
			}
		}

		private class ViewHolder {
			TextView name;
			TextView number;
			CheckBox cb;
		}

	}

	/*
	 * �첽�ӿڲ���������ݿ��ֹANR���� phone ���ڵ绰��ϵ�˶���
	 */
	private class MyAsyncQueryHandlerphone extends AsyncQueryHandler {

		public MyAsyncQueryHandlerphone(ContentResolver cr) {
			super(cr);

		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			if (cursor != null && cursor.getCount() > 0) {
				Log.i("kkkkkkkkkkk", "----------------------------" + cursor);
				listDataphone = new ArrayList<ContentValues>();
				// cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					ContentValues cv = new ContentValues();
					cursor.moveToPosition(i);
					// for �ֻ���ϵ��
					String name = cursor.getString(1);
					String number = cursor.getString(2);
					String sortKey = Integer.toString(i);// cursor.getString(2);//��I����������ֵfrank

					/********************************/
					Log.i("kkkkkkkkkkk", "----name:" + name + " number:"
							+ number + " sortKey:" + sortKey);
					if (number.startsWith("+86")) { // �����Ȳ�����
						cv.put(NAME, name);
						// process (+86)
						cv.put(NUMBER, number.substring(3));
						cv.put(SORT_KEY, sortKey);
					} else {
						cv.put(NAME, name);
						cv.put(NUMBER, number);
						cv.put(SORT_KEY, sortKey);
					}
					listDataphone.add(cv);
				}
				if (listDataphone.size() > 0) {
					Log.i("kkkkkkkkkkk",
							"---listData.size()" + listDataphone.size());
					mEditText.setVisibility(View.GONE);// ��ʧ
					setAdapterphone(listDataphone);
				}
				cursor.close();
			} else {// û������
				listDataphone = null;// ���
				displayProgressphone(false);
			}
		}

	}

	/*
	 * ���Ƶ�SIM��
	 */

	class copytosim extends AsyncTask<Void, Void, Integer> {

		private ProgressDialog progressDialog;

		protected void onPreExecute() {
			// TODO Auto-generated method stub
			progressDialog = ProgressDialog.show(GreenActivity.this, "���Ե�",
					"���ڿ���......", true);
		}

		protected Integer doInBackground(Void... params) {
			// TODO Auto-generated method stub
			int mark = copytosim();
			Log.i("KKKKKKKKKKK", "doInBackground  mark;  " + mark);
			Log.i("KKKKKKKKKKK", "doInBackground  mark;  " + mark);
			Log.i("KKKKKKKKKKK", "doInBackground  mark;  " + mark);
			return mark;
		}

		protected void onPostExecute(Integer result) {
			if (result == 0) {
				progressDialog.dismiss();
				Log.i("frank", "Handler onPostExecute");
				Message message = new Message();
				message.what = 3;
				hand.sendMessage(message);

			} else if (result == 1) {// һ����ûѡ��

				progressDialog.dismiss();
				Message message = new Message();
				message.what = 4;
				hand.sendMessage(message);

			} else if (result == 2) {// ��ѡ��

				progressDialog.dismiss();
				Message message = new Message();
				message.what = 5;
				hand.sendMessage(message);
				reQuery();// ����������
			}

		}

	}

	private int copytosim() {

		Boolean mark = false;
		if (listDataphone == null) {
			// û������
			return 0;

		}

		if (listDataphone.size() <= 0) {
			// û������
			return 0;
		}
		if (((ListAdapterphone) adapterphone).getCount() <= 0) {
			// û������
			return 0;

		}

		for (int i = 0; i < ((ListAdapterphone) adapterphone).getCount(); i++) {
			Log.i("kkkkkkkkkkk", "delete: " + isSelectedphone.get(i) + " i: "
					+ i);
			Log.i("kkkkkkkkkkk", "listDataphone.get(i).getAsString(NAME)��   "
					+ listDataphone.get(i).getAsString(NAME));
			if (isSelectedphone.get(i) == true) {// ���
				Log.i("kkkkkkkkkkk", "��� : " + i + "name���֣� "
						+ listDataphone.get(i).getAsString(NAME));
				ContentResolver localContentResolver = getContentResolver();
				ContentValues values = new ContentValues();
				values.put("tag", listDataphone.get(i).getAsString(NAME));
				values.put("number", listDataphone.get(i).getAsString(NUMBER));
				Uri localUri = Uri.parse("content://icc/adn");
				Uri newSimContactUri = localContentResolver.insert(localUri,
						values);
				Log.d("kkkkkkkkkkk", ">>>>>>" + "new sim contact uri, "
						+ newSimContactUri.toString());
				mark = true;// ֻҪ��ѡ�е� ��Ϊ��
			}

		}
		// ɾ������������ˢ�½��������� frank
		if (mark == false) {
			return 1; // һ����ûѡ��
		} else {
			return 2;// ��ѡ��
		}

	}

	/******************************************************************************************************************/

}