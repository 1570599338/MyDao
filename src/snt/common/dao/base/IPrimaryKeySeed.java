package snt.common.dao.base;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 数据库主键种子获取接口
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 *
 */
@Transactional(
		propagation=Propagation.REQUIRES_NEW
)
public interface IPrimaryKeySeed {

	public int genIntKeyInterval(int interval);

}
