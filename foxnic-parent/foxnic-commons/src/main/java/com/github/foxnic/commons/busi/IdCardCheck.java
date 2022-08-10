package com.github.foxnic.commons.busi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

public class IdCardCheck {

    public static void main(String[] args) throws ParseException {
        IdCardCheck ic=new IdCardCheck();
        ic.test();

    }
    //441225200002312559 //检验码正确，但2月份没有31天
    //441225201012092558 //生成的正确格式的身份证号码
    public void test() throws ParseException
    {
        Scanner sc=new Scanner(System.in);
        String idNum;
        do
        {
            System.out.println("请输入身份证号码：");
            idNum=sc.nextLine();
        }while(!checkIdCardNum(idNum));

        System.out.println("身份证号码\""+idNum+"\"正确！");
        System.out.println("持卡人的年龄："+getYear(idNum));
        System.out.println("出生的所在那一周是出生那年的第 "+getBirthWeek(idNum)+" 周");
        System.out.println("从出生到现在过去了 "+getWeeks(idNum)+" 周");

    }

    //身份证号码的严格校验
    public boolean checkIdCardNum(String idNum) throws ParseException
    {
        idNum=idNum.toUpperCase(); //将末尾可能存在的x转成X

        String regex="";
        regex+="^[1-6]\\d{5}"; //前6位地址码。后面仍需打表校验

        regex+="(18|19|20)\\d{2}"; //年份。后面仍需校验
        regex+="((0[1-9])|(1[0-2]))"; //月份。后面仍需校验
        regex+="(([0-2][1-9])|10|20|30|31)"; //日期。后面仍需校验

        regex+="\\d{3}"; //3位顺序码

        regex+="[0-9X]"; //检验码。后面仍需验证

        if(!idNum.matches(regex))
            return false;

        //第1，2位(省)打表进一步校验
        int[] d={11,12,13,14,15,
                21,22,23,31,32,33,34,35,36,37,
                41,42,43,
                44,45,46,
                50,51,52,53,53,
                61,62,63,64,65,
                83,81,82};
        boolean flag=false;
        int prov=Integer.parseInt(idNum.substring(0, 2));
        for(int i=0;i<d.length;i++)
            if(d[i]==prov)
            {
                flag=true;
                break;
            }
        if(!flag)
            return false;

        //生日校验：生日的时间不能比当前时间（指程序检测用户输入的身份证号码的时候）晚
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
        Date birthDate=sdf.parse(idNum.substring(6, 14));
        Date curDate=new Date();
        if(birthDate.getTime()>curDate.getTime())
            return false;

        //生日校验：每个月的天数不一样（有的月份没有31），还要注意闰年的二月
        int year=Integer.parseInt(idNum.substring(6, 10));
        int leap=((year%4==0 && year%100!=0) || year%400==0)?1:0;
        final int[] month={0,31,28+leap,31,30,31,30,31,31,30,31,30,31};
        int mon=Integer.parseInt(idNum.substring(10, 12));
        int day=Integer.parseInt(idNum.substring(12, 14));
        if(day>month[mon])
        {
            //System.out.println(day+" "+month[mon]+"\n");
            //System.out.println("---");
            return false;
        }


        //检验码
        if(idNum.charAt(17)!=getLastChar(idNum))
            return false;

        return true;
    }

    //根据身份证号码的前17位计算校验码
    public char getLastChar(String idNum) //由于这个功能比较独立，就分离出来
    {
        final int[] w={0,7,9,10,5,8,4,2,1,6,3,7,9,10,5,8,4,2};
        final char[] ch={'1','0','X','9','8','7','6','5','4','3','2'}; //这就是为什么一开始将末尾可能存在的x转成X的原因
        int res=0;
        for(int i=0;i<17;i++)
        {
            int t=idNum.charAt(i)-'0';
            res+=(t*w[i+1]);
        }
        return ch[res%11];
    }

    //根据身份证号码的出生日期计算年龄
    public int getYear(String idNum)
    {
        int yearBirth=Integer.parseInt(idNum.substring(6, 10));
        int monBirth=Integer.parseInt(idNum.substring(10, 12));
        int dayBirth=Integer.parseInt(idNum.substring(12, 14));

        Calendar cur=Calendar.getInstance();
        int yearCur=cur.get(Calendar.YEAR);
        int monCur=cur.get(Calendar.MONTH)+1; //不要忘了+1
        int dayCur=cur.get(Calendar.DATE);


        //System.out.println(yearCur+" "+monCur+" "+dayCur);
        int age=yearCur-yearBirth;
        if(monCur<monBirth || (monCur==monBirth && dayCur<dayBirth))
            age--;

        return age;
    }

    //出生的所在那一周是出生那年的第几周
    public int getBirthWeek(String idNum) throws ParseException
    {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
        Date birthDate=sdf.parse(idNum.substring(6, 14));  //默认0时0分0秒
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(birthDate);
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }

    //从出生到现在过去了多少周
    public int getWeeks(String idNum) throws ParseException
    {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
        Date birthDate=sdf.parse(idNum.substring(6, 14));  //默认0时0分0秒
        Date curDate=new Date();
        return  (int)( (curDate.getTime()-birthDate.getTime())/(long)(7*24*60*60*1000) ) ; //不建议把分子转成int，可能会溢出
    }

}
