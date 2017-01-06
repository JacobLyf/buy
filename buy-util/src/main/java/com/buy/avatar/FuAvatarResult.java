package com.buy.avatar;

import java.util.ArrayList;
import java.util.List;

/**
 * 富头像插件结果
 * 由富头像插件API来设计
 */
public class FuAvatarResult
{
	private Boolean success;								// 表示图片是否已上传成功
	private String msg;										// 自定义的附加消息
	private String sourceUrl;								// 表示原始图片的保存地址
	private List<String> avatarUrls = new ArrayList<>();	// 表示所有头像图片的保存地址，该变量为一个数组
	private Integer imgId;

	public Boolean getSuccess() { return success; }
	public FuAvatarResult setSuccess(Boolean success)
	{
		this.success = success;
		return this;
	}

	public String getMsg() { return msg; }
	public FuAvatarResult setMsg(String msg)
	{
		this.msg = msg;
		return this;
	}

	public String getSourceUrl() { return sourceUrl; }
	public FuAvatarResult setSourceUrl(String sourceUrl)
	{
		this.sourceUrl = sourceUrl;
		return this;
	}

	public List<String> getAvatarUrls() { return avatarUrls; }
	public FuAvatarResult setAvatarUrls(List<String> avatarUrls)
	{
		this.avatarUrls = avatarUrls;
		return this;
	}

	public Integer getImgId() { return imgId; }
	public FuAvatarResult setImgId(Integer imgId)
	{
		this.imgId = imgId;
		return this;
	}

}
