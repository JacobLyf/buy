package com.buy.plugin.event.o2o;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import net.dreamlu.event.EventKit;
import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

import com.buy.common.JsonMessage;
import com.buy.common.Ret;
import com.buy.plugin.event.pos.product.PushPosProductEvent;
import com.buy.plugin.event.user.ProductSaleUpdateEvent;
import com.buy.plugin.rabbitmq.RabbitMQ;
import com.buy.plugin.rabbitmq.RabbitMQConstants;
import com.buy.service.pos.push.PushPosProduct;
import com.buy.service.product.BaseO2oProUpdateApplayService;
import com.buy.string.StringUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jfinal.aop.Duang;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;

/**
 * 
 * O2O商品更新发送
 *
 */
@Listener(enableAsync = true)
public class O2oUpdateReceiveListener implements ApplicationListener<O2oUpdateReceiveEvent> {
	private Logger L = Logger.getLogger(O2oUpdateReceiveListener.class);
		
	@Override
	public void onApplicationEvent(O2oUpdateReceiveEvent event) {
		JsonMessage jm = new JsonMessage();
		
		 	List<Integer> applyIdList = new ArrayList<Integer>();	// O2O商品申请改价ID
		 	
			try{			
				Connection connection = RabbitMQ.factory.newConnection();  
			    Channel channel = connection.createChannel();  
				 //声明队列，主要为了防止消息接收者先运行此程序，队列还不存在时创建队列。  
		        channel.queueDeclare(RabbitMQConstants.O2O_QUEUE, true, false, false, null);            
		        //创建队列消费者  
		        QueueingConsumer consumer = new QueueingConsumer(channel); 
		        //取消 autoAck  
		        boolean autoAck = false ; 
		        //指定消费队列  
		        channel.basicConsume(RabbitMQConstants.O2O_QUEUE, autoAck, consumer);  
		        while (true){
		        	//nextDelivery是一个阻塞方法（内部实现其实是阻塞队列的take方法）  
		        	QueueingConsumer.Delivery delivery = null;
		        	try{
		        		delivery = consumer.nextDelivery(); 
		        		//确认消息，已经收到  
			            channel.basicAck(delivery.getEnvelope().getDeliveryTag() , false);  
			            String message = new String(delivery.getBody());
			            System.out.println(" [x] Received '" + message + "'");
			            Gson gson = new GsonBuilder().setPrettyPrinting().create();
			            Record record = (Record) gson.fromJson(message, Record.class);
			            //申请id
			            Integer id = record.getDouble("id").intValue();
			            //自动改价
			            BaseO2oProUpdateApplayService service = Duang.duang(BaseO2oProUpdateApplayService.class);		
			            jm = service.allFinish(id);
			            applyIdList.add(id);
			            
			            /*
						 * 推送
						 */
						Record data = (Record) jm.getData();
						if (StringUtil.notNull(data)) {
							Integer proId = data.get("proId");
							String skuCode = data.get("skuCode");
							
							PushPosProduct source = new PushPosProduct()
								.setKey(PushPosProduct.OP_PRO_O2O_EDI)
								.setProId(proId)
								.setSku(skuCode);
							EventKit.postEvent(new PushPosProductEvent(source));
							
						 /*
						  * 发送消息 - 商品降价
						  */
						
						if (StringUtil.notNull(applyIdList) && applyIdList.size() > 0) {
							
							// 查询sku集合
							String applyIds = StringUtil.listToString(",", applyIdList);
							List<String> skuList = Db.query("SELECT sku_code FROM t_o2o_pro_update_apply WHERE id IN (" + applyIds + ")");
							
							// 事件驱动
							Ret sourceRet = new Ret().put("isO2o", "isO2o").put("skuList", skuList);
							EventKit.postEvent(new ProductSaleUpdateEvent(sourceRet));
						}
						}
		        	}catch(Exception e){
		        		e.printStackTrace();
		        		//确认消息，已经收到  
			            channel.basicAck(delivery.getEnvelope().getDeliveryTag() , false);  
			            
		        	}
		        }
		        
			}catch(Exception e){
				e.printStackTrace();
				L.error(e.getMessage());
			} finally {
				
				
				
				
			}
		
	}
	
}