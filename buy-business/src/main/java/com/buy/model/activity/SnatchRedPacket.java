package com.buy.model.activity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import com.alibaba.fastjson.serializer.IntegerCodec;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.buy.common.BaseConstants;
import com.buy.date.DateUtil;
import com.buy.string.StringUtil;
import com.google.gson.JsonObject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;

import redis.clients.jedis.Jedis;

/***
 * @author Jekay
 * @date 2016/8/18
 ***/
public class SnatchRedPacket extends Model<SnatchRedPacket> {
    private static final long serialVersionUID = 1L;

    public static final SnatchRedPacket dao = new SnatchRedPacket();
    /**
     * 红包队列
     */
    public static final String RED_PAKAGE_QUEUE = "red_pakage";
    /**
     * 已抢红包用户队列
     */
    public static final String RED_PAKAGE_USER_QUEUE = "red_pakage_user_map";
    /**
     * 红包总金额(元) 500
     * 2016-10-17改:分十段数据,每段=1000/10=100元
     *
     * 2016-11-10改:分十段数据,固定4个红包金额:28*3,88*1,每段=(500-28*3-88*1)=328+上一天剩余数/10=32.8+元
     */
    public BigDecimal REDPACKET_TOTAL_CASH = new BigDecimal("32.8");

    /**
     * 红包数量(个) 1000
     * 2016-10-17改:分十段数据,每段=3000/10=300个
     * 2016-11-10改:分十段数据,每段=1000/10=100个
     */
    public int REDPACKET_NUM = 100;

    /**
     * 每天活动开始时间 10:00
     */
    public static final int SNATCH_START_TIME = 10;

    /**
     * 每天活动结束时间 20:00
     */
    public static final int SNATCH_END_TIME = 20;

    /**
     * 红包最小金额 0.1
     * 2016-10-17改:0.1
     */
    public BigDecimal REDPACKET_MIN_CASH = new BigDecimal("0.1");

    /**
     * 最高金额倍数 10
     * 2016-10-17改:10倍
     */
    public static final int REDPACKET_MULTIPLE = 10;

    /**
     * 共计分成10段数据段,打乱顺序
     */
    public static final int REDPACKET_PART = 10;

    /**
     * 现金红包状态:(0: 未领取 ,1：已被领取)',
     */
    public static final int REDPACKET_STATUS_UNGET = 0;
    public static final int REDPACKET_STATUS_GETED = 1;




    /**
     * 红包生成
     * 金额分配公式：
     * 一、0.01 ≤抽中金额≤（剩余金额/剩余需发红包数*50）
     * 二、0.01≤max≤剩余金额-0.01x（剩余需发红包数-1）
     */
    public void createRedPacket() {
        String sql1 = "SELECT a.id FROM t_sys_param a WHERE a.`code` = ? ";
        int fixedId = Db.queryInt(sql1,"red_packet_fixed");
        String fixedSql = "SELECT b.`value` FROM t_sys_param b WHERE b.parent_id = ? ";
        List<String> fixedCashList = Db.query(fixedSql,fixedId);

        int dynamicId = Db.queryInt(sql1,"red_packet_dynamic");
        String dynamicSql = "SELECT b.`value` FROM t_sys_param b WHERE b.parent_id = ? AND b.`name`= ? ";
        //动态化固定金额
        String packetTotalCash = Db.queryStr(dynamicSql,dynamicId,"red_packet_total_cash");
        if(StringUtil.notNull(packetTotalCash)){
            BigDecimal fixedRedTotal = fixedRedTotal(fixedCashList);
            REDPACKET_TOTAL_CASH = new BigDecimal(packetTotalCash).subtract(fixedRedTotal).divide(new BigDecimal(10),2, BigDecimal.ROUND_FLOOR);
        }
        //动态化红包个数
        String packetSize = Db.queryStr(dynamicSql,dynamicId,"red_packet_size");
        if(StringUtil.notNull(packetSize)){
            REDPACKET_NUM = Integer.parseInt(packetSize)/10;
        }
        //动态化最小值
        String packetMin = Db.queryStr(dynamicSql,dynamicId,"red_packet_min");
        if(StringUtil.notNull(packetMin)){
            REDPACKET_MIN_CASH = new BigDecimal(packetMin);
        }

        Queue<Integer> ranPart = ranPartInsert(fixedCashList.size());
        Queue<BigDecimal> fixedRedPackets = fixedRedPacket(fixedCashList);
        Integer parts = REDPACKET_PART;
        BigDecimal unPreGetNum = unGetRedPaccket().divide(new BigDecimal(10),2, BigDecimal.ROUND_FLOOR);//上一天未领取的金额分成十份
        List<BigDecimal> cashList = new ArrayList<BigDecimal>();
        BigDecimal a = new BigDecimal(0);
        if (canCreate()) {//今天生成红包后不能再生成了,
            for(;parts > 0 ;parts--) {  //2016-10-17改:分十段乱序
                List<BigDecimal> cashPartList = new ArrayList<BigDecimal>();
                Integer redPacket = REDPACKET_NUM;//红包100
                //将四个固定红包随机分配
                if(!ranPart.isEmpty() && parts == ranPart.peek() && fixedCashList.size()>0){
                    cashPartList.add(fixedRedPackets.poll());
                    redPacket = REDPACKET_NUM -1;
                    ranPart.poll();
                }
                BigDecimal totalCash = REDPACKET_TOTAL_CASH.add(unPreGetNum); //红包32.8+上一天未领取的金额1/10
                for (; redPacket > 0; redPacket--) {
                    //随机生成的红包 //（剩余金额/剩余需发红包数*50）
                    BigDecimal nowCash = (totalCash.divide(new BigDecimal(redPacket), 2, BigDecimal.ROUND_FLOOR)).multiply(new BigDecimal(REDPACKET_MULTIPLE));
                    BigDecimal cash = null;
                    BigDecimal lasterPacket = REDPACKET_MIN_CASH.multiply(new BigDecimal(redPacket - 1));
                    BigDecimal lastCash = totalCash.subtract(lasterPacket);
                    //如果 剩余金额-0.1x（剩余需发红包数-1）小于总剩余金额
                    if (lastCash.compareTo(nowCash) == 1) {
                        cash = getRandom(REDPACKET_MIN_CASH, nowCash);
                    } else {
                        cash = getRandom(REDPACKET_MIN_CASH, lastCash);
                    }
                    totalCash = totalCash.subtract(cash);
                    a = a.add(cash);
                    cashPartList.add(cash);
                }
                //每一段数据打乱顺序
                Collections.shuffle(cashPartList);
                cashList.addAll(cashPartList);
            }
            //批量生成
            addRenPacketMany(cashList);
        }
    }

    //取0.01到最高金额的随机数
    private static BigDecimal getRandom(BigDecimal minCash, BigDecimal maxCash) {
        Random random = new Random();
        BigDecimal result = new BigDecimal((random.nextInt(maxCash.divide(minCash, 2, BigDecimal.ROUND_FLOOR).intValue()) + 1)).multiply(minCash);
        return result;
    }

    /**
     * 批量生成红包
     */
    private void addRenPacketMany(List<BigDecimal> cashList) {
        List<String> sqlList = new ArrayList<String>();
        //insert into `t_snatch_redPacket`(`create_time`, `snatch_time`, `cash`, `status`) values(?, ?, ?, ?)
        for (BigDecimal cash : cashList) {
            StringBuffer sql = new StringBuffer();
            sql.append(" INSERT INTO `t_snatch_redPacket`");
            sql.append("    (`snatch_time`, `create_time`, `cash`, `status`)");
            sql.append(" VALUES ( ");
            sql.append("NOW(), ");
            sql.append("NOW(), ");
            sql.append(cash + ", ");
            sql.append(REDPACKET_STATUS_UNGET);
            sql.append(" ) ");
            sqlList.add(sql.toString());
        }
       Db.batch(sqlList, 50);
        //获取红包id
       List<Record> reds=  Db.find("SELECT s.id ,s.cash FROM `t_snatch_redPacket` s where DATE(s.create_time) = CURRENT_DATE() ORDER BY s.id DESC ");
       //存放redis
       Cache cache = Redis.use(BaseConstants.Redis.CACHE_OTHER_DATA);
       //清楚红包队列
       cache.del(RED_PAKAGE_QUEUE);
       //清楚用户队列
       cache.del(RED_PAKAGE_USER_QUEUE);
       Jedis jedis = cache.getJedis();
       for(Record r : reds){
    	   JsonObject json  = new JsonObject();
    	   json.addProperty("redId", r.getInt("id"));
    	   json.addProperty("cash", r.getBigDecimal("cash"));
    	   System.out.println(json.toString());
    	   jedis.lpush(RED_PAKAGE_QUEUE, json.toString());
       }
       jedis.close();
    }

    /**
     * 判断现在是否可以生成红包:
     * 今天生成过就不能再生成了
     */
    private boolean canCreate() {
        String sql = "SELECT a.id,a.snatch_time FROM `t_snatch_redPacket` a ORDER BY a.id DESC ";
        SnatchRedPacket info = dao.findFirst(sql);
        if (StringUtil.isNull(info)) {
            return true;
        }
        String snatchTime1 = DateUtil.DateToString(info.getDate("snatch_time"), "yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        //时间解析
        DateTime snatchTime2 = DateTime.parse(snatchTime1, format);
        DateTime nowDate = DateTime.now();
        if (snatchTime2.getYear() == nowDate.getYear() && snatchTime2.getDayOfYear() == nowDate.getDayOfYear()) {
            return false;
        } else {
            return true;
        }

    }

    /***
     * 判断能不能抢.
     * 今天抢过就不能抢了,等明天
     * 活动范围: 每天上午10：00到晚上20：00
     * @Author: Jekay
     * @Date: 2016/8/18 20:57
     ***/
    public int canSnatch(String userId) {
        String sql = "SELECT a.id,a.snatch_time FROM `t_snatch_redPacket` a WHERE date(a.snatch_time) = curdate() AND a.`status` = ? AND user_id =? ";
        SnatchRedPacket redpacket = SnatchRedPacket.dao.findFirst(sql, new Object[]{REDPACKET_STATUS_GETED, userId});

        //今天已抢过
        if (!StringUtil.isNull(redpacket)) {
            return 2;
        }
        if(!haveRedPacket()){
            return 3;
        }

        return 0;
    }

    /***
     * 是否还有红包
     * true 有   可以抢
     * false 抢光了,明天再来吧!
     *
     * @Author: Jekay
     * @Date: 2016/8/19 15:12
     ***/
    public boolean haveRedPacket() {
        String sql = "SELECT a.id,a.snatch_time FROM `t_snatch_redPacket` a WHERE date(a.snatch_time) = curdate() AND a.`status` = ? ";
        List<SnatchRedPacket> redpacketList = dao.find(sql, REDPACKET_STATUS_UNGET);
        if (!redpacketList.isEmpty() && redpacketList.size() > 0) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 领取最新的这个红包
     */
    public SnatchRedPacket getSnatchRedPacket() {
        SnatchRedPacket redpacket = SnatchRedPacket.dao.findFirst("SELECT * FROM `t_snatch_redPacket` a WHERE date(a.snatch_time) = curdate() AND a.`status` = ? ", REDPACKET_STATUS_UNGET);
        return redpacket;
    }

    /**
     * 返回上一天未领取的红包金额
     * @return
     */
    public BigDecimal unGetRedPaccket(){
        String ungetsql = "SELECT IFNULL(SUM(a.cash),0) FROM t_snatch_redPacket a WHERE date(a.snatch_time) = (select date_sub(curdate(),interval 1 day)) AND a.`status` = ?";
        return Db.queryBigDecimal(ungetsql,REDPACKET_STATUS_UNGET);
    }



    //固定金额
    public Queue<BigDecimal> fixedRedPacket(List<String> list){
        Queue<BigDecimal> fixedRedPackets = new LinkedList<BigDecimal>();
        List<BigDecimal> tempCash = new ArrayList<>();
        for(String str : list){
            BigDecimal rmb = new BigDecimal(str);
            tempCash.add(rmb);
        }
        Collections.shuffle(tempCash);
        for(BigDecimal ranCash : tempCash){
            fixedRedPackets.offer(ranCash);
        }
        return fixedRedPackets;
    }
    //固定金额总和
    public BigDecimal fixedRedTotal(List<String> list){
       BigDecimal tempCash = new BigDecimal(0);
        for(String str : list){
            BigDecimal rmb = new BigDecimal(str);
            tempCash = tempCash.add(rmb);
        }
        return tempCash;
    }
    //返回4个固定的大红包随机插入的4个数据段
    public Queue<Integer> ranPartInsert(int len){
        Queue<Integer> ranPart = new LinkedList<Integer>();
        List<Integer> tempPart = new ArrayList<>();
        while (tempPart.size()<len){
            Random r = new Random();
            int ranInt = r.nextInt(10)+1;
            if(!tempPart.contains(ranInt)){
                tempPart.add(ranInt);
            }
        }
        Collections.sort(tempPart);
        Collections.reverse(tempPart);
        for(int ran : tempPart){
            ranPart.offer(ran);
        }
        return ranPart;
    }
}
