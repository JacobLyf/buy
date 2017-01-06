package com.buy.solr;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.RangeFacet;

import com.buy.string.StringUtil;

public class SolrUtil {

	public static String transformMetachar(String input) throws Exception {
		if(!StringUtil.isBlank(input)) {
			Pattern pattern = Pattern.compile("[+&|!()（）{}\\[\\【\\】\\]^\"~*?:(\\)\\s]"); 
			Matcher matcher = pattern.matcher(input); 
			return matcher.replaceAll(" ").replaceAll(" +"," ").trim(); 
		}
		return input;
	}
	
//	/**
//	 * 计算价格区间间隔.
//	 * 
//	 * @param max 最大值.
//	 * @param min 最小值.
//	 * @return
//	 */
//	public static Integer priceRangeGap(double max, double min) {
//		Integer gap = null;
//		if(max - min >= 10000) {
//			gap = 1000;
//		} else if(9999 >= max - min && max - min >= 1000) {
//			int segments = 15;
////			if(((max - min) / 100) < segments) {
//				gap = (int) (Math.ceil((int) Math.ceil((max - min) / segments) / 100)) * 100;
////			} else {
////				if(((max - min) / 100) >= segments) {
////					gap = 100;
////				}
////				if(((max - min) / 200) >= segments) {
////					gap = 200;
////				}
////				if(((max - min) / 300) >= segments) {
////					gap = 300;
////				}
////				if(((max - min) / 400) >= segments) {
////					gap = 400;
////				}
////				if(((max - min) / 500) >= segments) {
////					gap = 500;
////				}
////				if(((max - min) / 600) >= segments) {
////					gap = 600;
////				}
////			}
//		} else if(999 >= max - min && max - min >= 100) {
//			int segments = 15;
//			if(((max - min) / 10) >= segments) {
//				gap = 10;
//			}
//			if(((max - min) / 20) >= segments) {
//				gap = 20;
//			}
//			if(((max - min) / 30) >= segments) {
//				gap = 30;
//			}
//			if(((max - min) / 40) >= segments) {
//				gap = 40;
//			}
//			if(((max - min) / 50) >= segments) {
//				gap = 50;
//			}
//			if(((max - min) / 60) >= segments) {
//				gap = 60;
//			}
//		} else if(99 >= max - min && max - min >= 0) {
//			gap = 1;
//		}
//		return gap;
//	}
	
	/**
	 * 计算价格区间间隔.
	 * 
	 * @param max 最大值.
	 * @param min 最小值.
	 * @return
	 */
//	public static Integer priceRangeGap(double max, double min) {
//		Integer gap = null;
//		int segments = 15;
//		
//		if(max - min >= 1000000*segments) {
//			gap = (int) (Math.ceil((int) Math.ceil((max - min) / segments) / 1000000)) * 1000000;
//		} else if(1000000*segments > max - min && max - min >= 100000*segments) {
//			gap = (int) (Math.ceil((int) Math.ceil((max - min) / segments) / 100000)) * 100000;
//		} else if(100000*segments > max - min && max - min >= 10000*segments) {
//			gap = (int) (Math.ceil((int) Math.ceil((max - min) / segments) / 10000)) * 10000;
//		} else if(10000*segments > max - min && max - min >= 1000*segments) {
//			gap = (int) (Math.ceil((int) Math.ceil((max - min) / segments) / 1000)) * 1000;
//		} else if(1000*segments > max - min && max - min >= 100*segments) {
//			gap = (int) (Math.ceil((int) Math.ceil((max - min) / segments) / 100)) * 100;
//		} else if(100*segments > max - min && max - min >= 10*segments) {
//			gap = (int) (Math.ceil((int) Math.ceil((max - min) / segments) / 10)) * 10;
//		} else if(10*segments > max - min && max - min >= 0) {
//			gap = (int) (Math.ceil((int) Math.ceil((max - min) / segments) / 1)) * 1;
//		}
//		return gap;
//	}
	
	public static Integer priceRangeGap(double max, double min, double mean) {
		Integer gap = null;
		int segments = 15;
		
		if(mean >= 1000000*segments) {
			gap = (int) (Math.ceil((int) Math.ceil(mean / segments) / 1000000)) * 1000000;
		} else if(1000000*segments > mean && mean >= 100000*segments) {
			gap = (int) (Math.ceil((int) Math.ceil(mean / segments) / 100000)) * 100000;
		} else if(100000*segments > mean && mean >= 10000*segments) {
			gap = (int) (Math.ceil((int) Math.ceil(mean / segments) / 10000)) * 10000;
		} else if(10000*segments > mean && mean >= 1000*segments) {
			gap = (int) (Math.ceil((int) Math.ceil(mean / segments) / 1000)) * 1000;
		} else if(1000*segments > mean && mean >= 100*segments) {
			gap = (int) (Math.ceil((int) Math.ceil(mean / segments) / 100)) * 100;
		} else if(100*segments > mean && mean >= 10*segments) {
			gap = (int) (Math.ceil((int) Math.ceil(mean / segments) / 10)) * 10;
		} else if(10*segments > mean && mean >= 0) {
			gap = (int) (Math.ceil((int) Math.ceil(mean / segments) / 1)) * 1;
		}
		return gap;
	}
	
	/**
	 * 计算价格区间点.
	 * 
	 * @param total 总记录数.
	 * @param response RangeQuery.
	 * @param first
	 * @param second
	 * @param third
	 * @param fourth
	 * @param fifth
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List<String> calculatePriceRange(Long total, QueryResponse response, List<String> priceRange, int first, int second, int third, int fourth, int fifth) {
		List<RangeFacet> list = response.getFacetRanges();
		if(null != list && list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				RangeFacet rangeFacet = list.get(i);
				
				List<RangeFacet.Count> countList = rangeFacet.getCounts();
				
				if(null != countList && countList.size() > 1) {
					// 中间段开始位置index.
					Integer middleStartIndex = 0;
					// 中间段结束位置index.
					Integer middleEndIndex = 0;
					
					// 正向确定first.
					// 第一段记录数.
					Integer verseOneNo = 0;
					for (int j = 0; j < countList.size(); j++) {
						RangeFacet.Count count = countList.get(j);
						
						verseOneNo += count.getCount();
						
						if(verseOneNo > 0 && new BigDecimal(verseOneNo).divide(new BigDecimal(total), 2, BigDecimal.ROUND_HALF_UP).doubleValue() > 0.05) {
							first = (int) Double.parseDouble(countList.get(j + 1).getValue());
							middleStartIndex = j + 2;
							break;
						}
					}
					
					priceRange.add("0-" + first);
					
					// 逆向确定fourth.
					Collections.reverse(countList);
					// 最后一段记录数.
					Integer tlynsNo = 0;
					for (int j = 0; j < countList.size(); j++) {
						RangeFacet.Count count = countList.get(j);
						tlynsNo += count.getCount();
							
						if(tlynsNo > 0 && new BigDecimal(tlynsNo).divide(new BigDecimal(total), 2, BigDecimal.ROUND_HALF_UP).doubleValue() > 0.05) {
							fifth = (int) Double.parseDouble(count.getValue());	
							middleEndIndex = countList.size() - j - 1;
							break;
						}
					}
					
					Collections.reverse(countList);
					
					for (int j = 0; j < countList.size(); j++) {
						RangeFacet.Count count = countList.get(j);
						System.out.println(count.getValue() + " " + count.getCount());
					}
					
					System.out.println("一段 0~" + first + " 五段 " + fifth + "以上");
					System.out.println(middleStartIndex + " " + middleEndIndex + "\r\t");
					// 处理中间段数,合并相邻空白区间.
					List<RangeFacet.Count> middleCountList = new ArrayList<RangeFacet.Count>();
					for (int j = middleStartIndex; j < middleEndIndex; j++) {
						RangeFacet.Count count = countList.get(j);
						if(count.getCount() > 0) {
							middleCountList.add(count);
						}
					}
					
					if(middleCountList.size() <=3) { // 三段内不做合并,直接显示.
						evaluationPriceRange(middleCountList, priceRange, first, second, third, fourth, fifth);
					} else { // 超出三段,合并至三段.
						System.out.println("合并空白区间段后:\r\t");
						
						// N段合并为一段.
						Integer section = middleCountList.size()/3;
						List<RangeFacet.Count> middlMmergeCountList = new ArrayList<RangeFacet.Count>();
						for (int j = 0; j < middleCountList.size(); j++) {
							RangeFacet.Count count = middleCountList.get(j);
							if(j > 0 && (j + 1)%section == 0) {
								middlMmergeCountList.add(count);
							}
//							else if(j == middleCountList.size() - 1) {
//								middlMmergeCountList.set(middlMmergeCountList.size() - 1, count);
//							}
						}
						
						for (int j = 0; j < middlMmergeCountList.size(); j++) {
							RangeFacet.Count count = countList.get(j);
							System.out.println(count.getValue() + " " + count.getCount());
						}
						
						evaluationPriceRange(middlMmergeCountList, priceRange, first, second, third, fourth, fifth);
					}
				}
			}
			
//			priceRange.add("0-" + first);
//			priceRange.add((first + 1) + "-" + second);
//			priceRange.add((second + 1) + "-" + third);
//			priceRange.add((third + 1) + "-" + fourth);
			if(fifth > 0) {
				priceRange.add((fifth + 1) + "以上");
			} else {
				priceRange.add((fourth + 1) + "以上");
			}
		}
		return priceRange;
	}
	
	/**
	 * 对价格区间点赋值.
	 * 
	 * @param countList
	 * @param first
	 * @param second
	 * @param third
	 * @param fourth
	 * @param fifth
	 */
	private static void evaluationPriceRange(List<RangeFacet.Count> countList, List<String> priceRange, int first, int second, int third, int fourth, int fifth) {
		for (int i = 0; i < countList.size(); i++) {
			RangeFacet.Count count = countList.get(i);
			
			if(i == 0) {
				second = (int) Double.parseDouble(count.getValue());
				priceRange.add((first + 1) + "-" + second);
			} else if(i == 1) {
				third = (int) Double.parseDouble(count.getValue());
				priceRange.add((second + 1) + "-" + third);
			} else if(i == 2) {
				fourth = (int) Double.parseDouble(count.getValue());
				priceRange.add((third + 1) + "-" + fourth);
			}
		}
		
		if(countList.size() == 1) {
			priceRange.add((second + 1) + "-" + fifth);
		} else if(countList.size() == 2) {
			priceRange.add((third + 1) + "-" + fifth);
		} else if(countList.size() == 3) {
			priceRange.add((fourth + 1) + "-" + fifth);
		}
	}
}