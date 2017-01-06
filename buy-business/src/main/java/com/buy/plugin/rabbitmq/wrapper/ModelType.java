package com.buy.plugin.rabbitmq.wrapper;

public enum ModelType {
	SORT("sort"), BRAND("brand"), SHOP("shop"), PRODUCT("product"),
	SUPPLIER("supplier"), EFUN("efun"),O2O("o2o");

	private String value;

	private ModelType(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return this.value;
	}

	public static ModelType fromValue(String value) {
		for (ModelType type : ModelType.values()) {
			if (type.toString().equals(value)) {
				return type;
			}
		}
		return null;
	}
}