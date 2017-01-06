package com.buy.model.message;


import com.buy.date.DateUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

import java.util.Date;
import java.util.List;

/**
 * Model - 消息（备份）
 */
public class MessageBak extends Model<MessageBak> {

    private static final long serialVersionUID = 1L;
    public static final MessageBak dao = new MessageBak();

    public void addByMessage() {
        long total = getCanDelCount();
        if (total > 0) {



        }
    }

    /**
     * 可删除消息条数
     */
    public long getCanDelCount() {
        Date now = new Date();
        Date limitDate = DateUtil.addDay(now, -30);
        limitDate = DateUtil.getMinDate(limitDate);
        int limitNum = 1000;

        return Db.queryLong(new StringBuffer(" SELECT COUNT(1) FROM t_message ")
                        .append(" WHERE create_time < ? ")
                        .append(" ORDER BY id ASC ")
                        .append(" LIMIT ? ")
                        .toString(),
                limitDate, limitNum
        );
    }

    public List<Message> findCanDel() {
        Date now = new Date();
        Date limitDate = DateUtil.addDay(now, -30);
        limitDate = DateUtil.getMinDate(limitDate);
        int limitNum = 1000;

        return Message.dao.find(new StringBuffer(" SELECT * FROM t_message ")
                        .append(" WHERE create_time < ? ")
                        .append(" ORDER BY id ASC ")
                        .append(" LIMIT ? ")
                        .toString(),
                limitDate, limitNum
        );
    }

}
