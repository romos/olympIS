package com.example.client;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.view.View.OnClickListener;
import beans.CountryApplication;
import beans.CompetitionList;
import beans.Athlete;
import beans.Sex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class realize completing an application GUI for authorized country.
 *  @author danya
 */
public class CountryGUI extends Activity implements OnClickListener, View.OnLongClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.postrequest);

		// TODO должно быть получение уже имеющейся заявки от базы + число спортсменов
		competitionNamesList = getResources().getStringArray(R.array.sport_array);
		athleteNumberList = new int[competitionNamesList.length];
		for (int i = 0 ; i < athleteNumberList.length; i++) {
			athleteNumberList[i] = 2;
		}
		competitionList = new CompetitionList(competitionNamesList, athleteNumberList);

		forceEdit = false;
		oldAthleteName = "";
		text1 = (EditText)findViewById(R.id.text1);
		text2 = (EditText)findViewById(R.id.text2);
		text3 = (EditText)findViewById(R.id.text3);
		text4 = (EditText)findViewById(R.id.text4);
		horizontalScrollView = (HorizontalScrollView) findViewById(R.id.horizontalScrollView1);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		tableLayoutArrayList = new ArrayList<TableLayout>();
		for (int i = 0; i < competitionNamesList.length; i++) {
			TableLayout tl = (TableLayout) inflater.inflate(R.layout.tablelayout, null);
			tableLayoutArrayList.add(tl);
		}
		horizontalScrollView.addView(tableLayoutArrayList.get(0));
		athleteCompetitionNumber = (TextView) findViewById(R.id.athleteCompetitionNumber);

		addButton = (Button)findViewById(R.id.add_button);
		addButton.setOnClickListener(this);

		sp = (Spinner) findViewById(R.id.competitionSpinner);
		sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
									   int position, long id) {
				horizontalScrollView.removeViewAt(0);
				horizontalScrollView.addView(tableLayoutArrayList.get(position));
				String competition = competitionNamesList[sp.getSelectedItemPosition()];
				int athleteNumber = competitionList.getAthleteNumber(competition);
				int athleteMaxNumber = competitionList.getMaxAthleteNumber(competition);
				athleteCompetitionNumber.setText("Осталось: " + (athleteMaxNumber - athleteNumber) +
						"/" + athleteMaxNumber);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Пункт для отправки заявки на сервер.
		menu.add(0, 1, 0, "post application");
		return super.onCreateOptionsMenu(menu);
	}

	// обновление меню
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}

	// обработка нажатий
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case 1:// post application
				AuthorizationData data = AuthorizationData.getInstance();
				countryApplication = new CountryApplication(data.getLogin(), data.getPassword(), competitionList);
				//TODO: дописать передачу countryApplication через Тошин класс
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Adds dynamically a row in a table.
	 * @param index Is an index in witch new Row will be added in tableLayout.
	 */
	public void addRow(String name, String sex, String weight, String height, String competition, int index) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		TableRow ll = (TableRow) inflater.inflate(R.layout.tablerow, null);
		List<String> l = Arrays.asList(name, sex, weight, height, competition);
		for (int i = 0; i <= 4; i++) {
			((TextView) ll.getChildAt(i)).setText(l.get(i));
		}

		// Листенер долгого нажатия, для правки иформации о спортсмене.
		ll.setOnLongClickListener(this);

		// tableLayout.addView(ll, index);
		((TableLayout) horizontalScrollView.getChildAt(0)).addView(ll, index);
	}

	// Считаю, что long click есть только у tableRow.
	public boolean onLongClick(View v) {
		TableRow lr = (TableRow) v;

		text1.setText(((TextView)lr.getChildAt(0)).getText() + "");
		text2.setText(((TextView)lr.getChildAt(1)).getText() + "");
		text3.setText(((TextView)lr.getChildAt(2)).getText() + "");
		text4.setText(((TextView)lr.getChildAt(3)).getText() + "");

		String competition = ((TextView)lr.getChildAt(4)).getText() + "";
		int itemSelected = 0;
		String[] choose = getResources().getStringArray(R.array.sport_array);
		for (itemSelected = 0; itemSelected < choose.length; itemSelected++) {
			if (choose[itemSelected].equals(competition)) {
				break;
			}
		}
		sp.setSelection(itemSelected);

		forceEdit = true;
		oldAthleteName = ((TextView)lr.getChildAt(0)).getText() + "";

		return true;
	}

	public void onClick(View v) {
		switch (v.getId()){
			case R.id.add_button:
				// TODO проверить корректность и соответствие ограничениям введенных данных

				if (!forceEdit) {
					String competition = competitionNamesList[sp.getSelectedItemPosition()];
					if (competitionList.getAthleteNumber(competition) >= competitionList.getMaxAthleteNumber(competition)) {
						Toast.makeText(this, "Вы исчерпали количество заявок.", Toast.LENGTH_LONG).show();
						return;
					}

					// Имя спортсмена.
					String name = text1.getText() + "";
					int athleteIndex = this.competitionList.getAthleteListIndex(name, competitionNamesList[sp.getSelectedItemPosition()]);
					if (athleteIndex != -1) { // Т.е. спортсмен уже есть в таблице
						Intent tableCountryFilterIntent = new Intent(this, DialogActivity.class);
						// Далее вторым параметром стоит 2. Это requestCode, он может быть любым числом.
						// Выбрана 2, т.к. 1 уже использовалось в другом классе. requestCode может совпадать
						// в разных местах программы и даже в одном классе,
						// но рекомендуется ставить разные значения, во избежания неожиданных ошибок.
						startActivityForResult(tableCountryFilterIntent, 2);
						break;
					}
					// Т.е. если не нашли атлета в списке, то вставлять его будем в начало.
					if (addAthlete(0)) {
						Toast.makeText(this, "Новый спортсмен добавлен", Toast.LENGTH_SHORT).show();
						int athleteNumber = competitionList.getAthleteNumber(competition);
						int athleteMaxNumber = competitionList.getMaxAthleteNumber(competition);
						athleteCompetitionNumber.setText("Осталось: " + (athleteMaxNumber - athleteNumber) +
								"/" + athleteMaxNumber);
					}
				} else {
					// Имя спортсмена.
					String name = oldAthleteName;
					// Соревнование.
					String competition = competitionNamesList[sp.getSelectedItemPosition()];
					int athleteIndex = this.competitionList.getAthleteListIndex(name, competition);
					// Удаляем старые данные из таблицы пользователя.
					Log.d("DAN", "delete view index " + athleteIndex);
					((TableLayout) horizontalScrollView.getChildAt(0)).removeViewAt(athleteIndex + 1);
					// Удаляем старые данные о спортсмене
					Log.d("DAN", "delete list " +  name + " " + competition);
					competitionList.deleteAthlete(name, competition);
					Log.d("DAN", "deleted");
					if (addAthlete(athleteIndex)) {
						Toast.makeText(this, "Информация изменена", Toast.LENGTH_SHORT).show();
						forceEdit = false;
						oldAthleteName = "";
					}
				}
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data == null) return;
		if (requestCode == 2) {   // 2 соответствует параметру requestCode, передаваемому диалоговому окну при инициализации.
			if (resultCode == RESULT_OK) {
				boolean result = data.getBooleanExtra("dialogResult", false);
				if (result) {
					String name = text1.getText() + "";
					String competition = competitionNamesList[sp.getSelectedItemPosition()];
					int athleteIndex = this.competitionList.getAthleteListIndex(name, competitionNamesList[sp.getSelectedItemPosition()]);

					// Удаляем старые данные из таблицы пользователя.
					Log.d("DAN", "delete view index " + athleteIndex);
					((TableLayout) horizontalScrollView.getChildAt(0)).removeViewAt(athleteIndex + 1);

					// Удаляем старые данные о спортсмене
					competitionList.deleteAthlete(name, competition);

					if (addAthlete(athleteIndex)) {
						Toast.makeText(this, "Информация о спортсмене " + name + " изменёна", Toast.LENGTH_SHORT).show();
					}
				}
			}
		}
	}

	private Sex toSex(String str) {
		if (str.equals("Male") || str.equals("male") || str.equals("M") || str.equals("m")) {
			return Sex.Male;
		} else if (str.equals("Female") || str.equals("female") || str.equals("F") || str.equals("f")) {
			return Sex.Female;
		} else {
			return Sex.Undefined;
		}
	}

	/**
	 * Adds athlete in a athleteList and adds an row in the user table.
	 * If athlete is successfully added, returns true, returns false otherwise.
	 * @param athleteIndex Is an index in witch new athlete will be added in the list and user table.
	 */
	private boolean addAthlete(int athleteIndex) {
		// Получение данных из spinner-а(соревнование).
		//String[] choose = getResources().getStringArray(R.array.sport_array);
		// Добавляем данные в список, который будем передавать.
		try {
			String name = text1.getText() + "";
			Sex sex = toSex(text2.getText() + "");
			// TODO пока что weight и height обязательны для заполнения. Если надо - можно исправить.
			int weight = Integer.parseInt((text3.getText() + ""));
			int height = Integer.parseInt((text4.getText() + ""));
			String competition = competitionNamesList[sp.getSelectedItemPosition()];
			Log.d("DAN", "index " + athleteIndex);
			competitionList.addAthlete(athleteIndex, competition, new Athlete(name, sex, weight, height, competition));

			// Добавляем информацию в таблицу пользователя.
			addRow(name + "", sex + "", weight + "",
					height + "", competition, athleteIndex + 1);
		} catch (NumberFormatException e) {
			Toast.makeText(this, "Вес или рост введены некорректно.", Toast.LENGTH_SHORT).show();
			return false;
		}

		text1.setText(""); text2.setText("");
		text3.setText(""); text4.setText("");
		return true;
	}

	private boolean forceEdit; // при изменении информации об спортсмене, путём долгого нажатия,
								// становится истиной. Если она true, то диалога изменения не будет.
	private String oldAthleteName; // При изменении имени, надо запомнить старое. Считаю, что имя - ключ.
	private Button addButton;
	private EditText text1;
	private EditText text2;
	private EditText text3;
	private EditText text4;
	private Spinner sp;
	private HorizontalScrollView horizontalScrollView;
	private CountryApplication countryApplication;
	private ArrayList<TableLayout> tableLayoutArrayList;
	private TextView athleteCompetitionNumber;
	private int[] athleteNumberList; // Список, содржащий количество спортсменов на каждое соревнование, которое страна может подать.
	private String[] competitionNamesList; // Список названий соревнований.
	private CompetitionList competitionList; // Список соревнований и атлетов.
			// Этот список будет передаваться серверу.
			// Спортсмены, хранятся в этом списке, отображаются
			// в таблице на экране и в списве в одном и том же порядке.
}