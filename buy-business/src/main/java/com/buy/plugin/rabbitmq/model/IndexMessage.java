package com.buy.plugin.rabbitmq.model;

import java.util.List;

import com.buy.plugin.rabbitmq.wrapper.ExchangeType;
import com.buy.plugin.rabbitmq.wrapper.ModelType;

/**
 * RabbitMQ消息模型.
 * 
 * @author Chengyb
 */
public class IndexMessage {

	// 操作模型.
	ModelType modelType;

	// 模型Id.
	Object id;
	
	// 模型层级.
	Integer level;

	// 操作类型.
	ExchangeType exchangeType;

	public IndexMessage() {
	}

	public IndexMessage(ModelType modelType, Integer id, Integer level, ExchangeType exchangeType) {
		this.modelType = modelType;
		this.id = id;
		this.level = level;
		this.exchangeType = exchangeType;
	}
	
	public IndexMessage(ModelType modelType, Object id, ExchangeType exchangeType) {
		this.modelType = modelType;
		this.id = id;
		this.exchangeType = exchangeType;
	}
	
	public IndexMessage(ModelType modelType, ExchangeType exchangeType) {
		this.modelType = modelType;
		this.exchangeType = exchangeType;
	}
	
	public IndexMessage(ModelType modelType, List<Integer> idList, ExchangeType exchangeType) {
		this.modelType = modelType;
		this.id = idList;
		this.exchangeType = exchangeType;
	}

	public ModelType getModelType() {
		return modelType;
	}

	public void setModelType(ModelType modelType) {
		this.modelType = modelType;
	}

	public Object getId() {
		return id;
	}

	public void setId(Object id) {
		this.id = id;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public ExchangeType getExchangeType() {
		return exchangeType;
	}

	public void setExchangeType(ExchangeType exchangeType) {
		this.exchangeType = exchangeType;
	}

}