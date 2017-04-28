package cn.benguo.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import cn.benguo.calendar.CalendarWidget;
import cn.benguo.calendar.OnCalendarClickListener;

public class MainActivity extends AppCompatActivity {

	private CalendarWidget bgcvCalendar;

	public static final String CURRENT_DATE_FORMAT = "current date is %4d-%02d-%02d";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		bgcvCalendar = (CalendarWidget) findViewById(R.id.bgcv_calendar);
		bgcvCalendar.setOnCalendarClickListener(new OnCalendarClickListener() {

			@Override
			public void onClickDate(int year, int month, int day) {
				Toast.makeText(MainActivity.this,
						String.format(CURRENT_DATE_FORMAT, year,month,day),
						Toast.LENGTH_SHORT).show();
				if(bgcvCalendar.isOpen()){
					bgcvCalendar.switchOpenStatus();
				}
			}
		});
		findViewById(R.id.btn_today).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				bgcvCalendar.showTodayPage(true);
			}
		});
	}

}
