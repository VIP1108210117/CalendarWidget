package cn.benguo.calendar.utils;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Months;
import org.joda.time.Weeks;

/**
 * Created by lzh on 2017/4/12 10:36.<br/>
 * month(月份)取值范围（1-12 ： 1月到12月）；<br/>
 * dayOfWeek(星期几)取值范围（1-7 ： 周一到周周末）；<br/>
 */

public class JODAUtils {

	private static boolean isDebug = false;

	/**
	 * 获取某月份的天数
	 *
	 * @param year
	 * @param month
	 * @return
	 */
	public static int getMonthDays(int year, int month) {
		return getMonthDays(new DateTime().withYear(year).withMonthOfYear(month));
	}

	/**
	 * 获取某月份的天数
	 *
	 * @param dateTime
	 * @return
	 */
	public static int getMonthDays(DateTime dateTime) {
		int days = dateTime.dayOfMonth().getMaximumValue();
		if(isDebug){
			System.out.printf("%d年%d月的天数：%d\n", dateTime.year().get(),
					dateTime.monthOfYear().get(), days);
		}
		return days;
	}

	/**
	 * 获取某月1号是周几
	 *
	 * @param year
	 * @param month
	 * @return
	 */
	public static int getFirstDayWeek(int year, int month) {
		return getFirstDayWeek(new DateTime().withDate(year, month, 1));
	}

	/**
	 * 获取某月1号是周几
	 * @param dateTime
	 * @return
	 */
	public static int getFirstDayWeek(DateTime dateTime){
		int dayOfWeek = (dateTime.getDayOfMonth() == 1 ? dateTime.getDayOfWeek() :
				dateTime.dayOfMonth().withMinimumValue().getDayOfWeek());
		if(isDebug){
			System.out.printf("%d年%d月的第一天为周%d\n", dateTime.year().get(),
					dateTime.monthOfYear().get(), dayOfWeek);
		}
		return dayOfWeek;
	}

	/**
	 * 获取当前月历的第一天对应的日期
	 * @param dateTime
	 * @return
	 */
	public static DateTime getCalendarFirstDay(DateTime dateTime) {
		if(dateTime == null){
			return null;
		}
		// dayOfWeek 本月第一天周几
		int dayOfWeek = dateTime.dayOfMonth().withMinimumValue().getDayOfWeek();
		DateTime currentPageFirstDay = dateTime.dayOfMonth().withMinimumValue()
				.minusDays(dayOfWeek%7);
		if(isDebug){
			System.out.printf("%d年%d月的月历第一天为：%s\n",
					dateTime.year().get(), dateTime.monthOfYear().get(),
					currentPageFirstDay.toLocalDate().toString());
		}
		return currentPageFirstDay;
	}

	/**
	 * 获取当前月历的最后一天对应的日期
	 * @param dateTime
	 * @return
	 */
	public static DateTime getCalendarLastDay(DateTime dateTime) {
		if(dateTime == null){
			return null;
		}
		// dayOfWeekForMinDayOfMonth ：月份第一天为周几
		int dayOfWeekForMinDayOfMonth = dateTime.dayOfMonth().withMinimumValue().getDayOfWeek();
		// nextMonthDays ：当前月历显示的下个月的天数
		int nextMonthDays = 42 - dayOfWeekForMinDayOfMonth%7 - dateTime.dayOfMonth().getMaximumValue();
		DateTime currentPageLastDay = dateTime.dayOfMonth().withMaximumValue().plusDays(nextMonthDays);
		if(isDebug){
			System.out.printf("%d年%d月的月历最后一天为：%s\n",
					dateTime.year().get(), dateTime.monthOfYear().get(),
					currentPageLastDay.toLocalDate().toString());
		}
		return currentPageLastDay;
	}


	/**
	 * 获取两个日期之间相差多少天，start在end之前返回正值，否则返回负值<br/>
	 * @param start
	 * @param end
	 * @return start=2017-04-10，end=2017-04-17时结果为6
	 */
	public static int daysBetween(DateTime start, DateTime end) {
		int days = Days.daysBetween(start, end).getDays();
		if(isDebug){
			System.out.printf("%s与%s相差%d天\n",start.toLocalDate().toString(),
					end.toLocalDate().toString(), days);
		}
		return days;
	}


	/**
	 * 获取两个日期之间相差多少月，start在end之前返回正值，否则返回负值<br/>
	 * @param start
	 * @param end
	 * @return
	 */
	public static int monthsBetween(DateTime start, DateTime end) {
		int months = Months.monthsBetween(start, end).getMonths();
		if(isDebug){
			System.out.printf("%s与%s相差%d月\n",start.toLocalDate().toString(),
					end.toLocalDate().toString(), months);
		}
		return months;
	}

	/**
	 * 获取两个日期之间相差多少周（每7天为一周）
	 * @param start
	 * @param end
	 * @return
	 */
	public static int weeksBetween(DateTime start, DateTime end) {
		int weeks = Weeks.weeksBetween(start, end).getWeeks();
		if(isDebug){
			System.out.printf("%s与%s相差%d周\n",start.toLocalDate().toString(),
					end.toLocalDate().toString(), weeks);
		}
		return weeks;
	}

	/**
	 * 比较两个日期时间对象的日期（年、月、日）是否相等
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static boolean isEqualDate(DateTime date1, DateTime date2) {
		if(date1 == null || date2 == null){
			return false;
		}
		return date1.getYear() == date2.getYear()
				&& date1.getMonthOfYear() == date2.getMonthOfYear()
				&& date1.getDayOfMonth() == date2.getDayOfMonth();
	}

	/**
	 * 获取指定日期在所属日历页的第几行
	 * @param year
	 * @param month
	 * @param day
	 * @return
	 */
	public static int getWeekRow(int year, int month, int day) {
		return getWeekRow(new DateTime().withDate(year, month, day));
	}

	/**
	 * 获取指定日期在所属日历页的第几行
	 * @param date
	 * @return
	 */
	public static int getWeekRow(DateTime date) {
		int firstDayWeek = getFirstDayWeek(date);
		return (firstDayWeek % 7 + date.getDayOfMonth() - 1)/7;
	}

	/**
	 * 根据国历获取假期
	 * @param date
	 * @return
	 */
	public static String getHolidayFromSolar(DateTime date) {
		return getHolidayFromSolar(date.getYear(),
				date.getMonthOfYear(), date.getDayOfMonth());
	}

	/**
	 * 根据国历获取假期
	 * @param year 年
	 * @param month 月
	 * @param day 日
	 * @return
	 */
	public static String getHolidayFromSolar(int year, int month, int day) {
		String message = "";
		if (month == 1 && day == 1) {
			message = "元旦";
		} else if (month == 2 && day == 14) {
			message = "情人节";
		} else if (month == 3) {
			switch (day){
				case 8:
					message = "妇女节";
					break;
				case 12:
					message = "植树节";
					break;
			}
		} else if (month == 4) {
			if (day == 1) {
				message = "愚人节";
			} else if (day >= 4 && day <= 6) {
				if (year <= 1999) {
					int compare = (int) (((year - 1900) * 0.2422 + 5.59) - ((year - 1900) / 4));
					if (compare == day) {
						message = "清明节";
					}
				} else {
					int compare = (int) (((year - 2000) * 0.2422 + 4.81) - ((year - 2000) / 4));
					if (compare == day) {
						message = "清明节";
					}
				}
			}
		} else if (month == 5) {
			switch (day){
				case 1:
					message = "劳动节";
					break;
				case 4:
					message = "青年节";
					break;
				case 12:
					message = "护士节";
					break;
			}
		} else if (month == 6 && day == 1) {
			message = "儿童节";
		} else if (month == 7 && day == 1) {
			message = "建党节";
		} else if (month == 8 && day == 1) {
			message = "建军节";
		} else if (month == 9 && day == 10) {
			message = "教师节";
		} else if (month == 10 && day == 1) {
			message = "国庆节";
		} else if (month == 11 && day == 11) {
			message = "光棍节";
		} else if (month == 12 && day == 25) {
			message = "圣诞节";
		}
		return message;
	}

	/**
	 * TODO 工具类测试
	 * @param args
	 */
	public static void main(String[]args){
		isDebug = true;
		DateTime dateTime = new DateTime();
		int testFunc = 4;
		switch (testFunc){
			case 1:
				//测试每月日历的开始日期和结束日期
				testCalendarFirstAndLastDay(dateTime);
				break;
			case 2:
				// 根据最小时间和最大时间计算日历的页数
				DateTime minDateTime = dateTime.plusMonths(-1);
				DateTime maxDateTime = dateTime.plusMonths(1);
				testDateBetween(JODAUtils.getCalendarFirstDay(minDateTime), dateTime);
				testDateBetween(dateTime, JODAUtils.getCalendarLastDay(maxDateTime));
				testDateBetween(JODAUtils.getCalendarFirstDay(minDateTime),
						JODAUtils.getCalendarLastDay(maxDateTime));
				break;
			case 3:
				// 测试相邻两周的周一之间相差几周
				testDateBetween(dateTime.dayOfWeek().withMinimumValue().minusDays(1).dayOfWeek().withMinimumValue(),
						dateTime.dayOfWeek().withMinimumValue());
				break;
			case 4:
				// 测试指定月份的日期在日历页的第几行
				testWeekRow(dateTime);
				break;

		}
	}

	private static void testWeekRow(DateTime dateTime) {
		int dayOfMonth = dateTime.getDayOfMonth();
		for(int i=1; i <= dateTime.dayOfMonth().getMaximumValue(); i++){
			DateTime date = dateTime.plusDays(i - dayOfMonth);
			System.out.printf("%s在第%d行\n",date.toLocalDate().toString(),
					JODAUtils.getWeekRow(date));
		}
	}

	/**
	 * 测试每月日历的开始日期和结束日期
	 * @param dateTime
	 */
	private static void testCalendarFirstAndLastDay(DateTime dateTime){
		DateTime date;
		for(int i = -12; i <= 12; i++){
			date = dateTime.plusMonths(i);
			JODAUtils.getMonthDays(date);
			JODAUtils.getCalendarFirstDay(date);
			JODAUtils.getCalendarLastDay(date);
		}
	}

	private static void testDateBetween(DateTime start, DateTime end){
		monthsBetween(start, end);
		weeksBetween(start, end);
		// 根据相差天数计算相差的月数和周数
		int days = Days.daysBetween(start, end).getDays();
		int months = days/42;
		int weeks = days/7;
		System.out.printf("days=%d, months=%d, weeks=%d\n\n", days, months, weeks);
	}

}
