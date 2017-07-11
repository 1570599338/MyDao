package snt.common.web.util;

/**
 * 一些常量的定义
 * 
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 */
public interface Constants { 
	/** 加密参数的参数名 */
	String ENCODEDPARAMNAME = "sntep";

	/** 校验码在Session中的key名 */
	String ENCRYPTIMGCODE = "ENIMGCODE";

	/** 校验码生成的时间 */
	String ENCRYPTIMGGENTIME = "ENIMGGENTIME";

	/** 业务异常在Request中的key名 */
	String ERRORKEY = "SNTERROR";

	/** 弱引用保存的会话数据key值 */
	String WEAKREFDATA = "WEAKREFDATA";

	/** 每页显示记录条数 */
	int PERPAGESIZE = 20;
}
