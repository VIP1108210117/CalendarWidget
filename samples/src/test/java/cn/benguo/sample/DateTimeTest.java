package cn.benguo.sample;

import org.joda.time.DateTime;
import org.junit.Test;

/**
 * Created by lzh on 2017/4/5 14:12.
 */

public class DateTimeTest {

	@Test
	public void canlendar(){
		DateTime mStartDate = new DateTime();
		System.out.println("init---"+mStartDate);
		System.out.println("getDayOfWeek---"+mStartDate.getDayOfWeek());
		mStartDate = mStartDate.plusDays(-mStartDate.getDayOfWeek() % 7);
		System.out.println("plusDays---"+mStartDate);

		int position = 0;
		int mWeekCount = 440;
		System.out.printf("plusWeeks-%d--%s\n", position, mStartDate.plusWeeks(position - mWeekCount / 2));

		position=1;
		System.out.printf("plusWeeks-%d--%s\n", position, mStartDate.plusWeeks(position - mWeekCount / 2));
	}
}
