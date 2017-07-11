/**
 * 
 */
package snt.common.dao.base;

import java.sql.Types;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;

/**
 * 数据库主键种子获取实现
 * 
 * @author yangxiong
 * 
 */
public class PrimaryKeySeedImpl implements IPrimaryKeySeed {
	private static Log log = LogFactory.getLog(PrimaryKeySeedImpl.class);
	/**
	 * 数据访问接口
	 */
	private CommonDAO commonDAO;

	/**
	 * 种子表
	 */
	private String seedTable;

	/**
	 * 种子字段
	 */
	private String seedFld = "seed";

	/**
	 * 版本字段
	 */
	private String verFld = "version";

	/**
	 * 主键字段
	 */
	private String pkFld = "pk_id";
	
	/**
	 * 是否初始化的标志
	 */
	private boolean init;

	/**
	 * 进行数据库操作的几个sql
	 */
	private String querySeedSql;

	private String updateSeedSql;

	private String insertSeedSql;

	public PrimaryKeySeedImpl() {
		super();
	}

	public int genIntKeyInterval(int interval) {
		if (log.isDebugEnabled()) {
			log.debug("获取主键区间，区间大小为"+interval);
		}
		initialize();
		int keyBase = 1;
		int version = 0;
		int count = 0;
		do {
			List<Map<String, Object>> mapList = getCommonDAO().queryForMapList(querySeedSql);
			if (mapList.size() > 0) {// 如果种子表里已经有记录
				Map map = mapList.get(0);
				keyBase = (Integer) map.get("seed");
				version = (Integer) map.get("ver");
				count = getCommonDAO()
						.update(updateSeedSql,
								new Object[] { interval, version },
								new int[] { Types.INTEGER, Types.INTEGER });
			} else {// 对于这种情况没有考虑严格意义上的同步，全部依赖数据库操作本身的同步——即不可能插入两条相同主键的记录
				try {
					count = getCommonDAO()
							.update(insertSeedSql,
									new Object[] { interval + keyBase });
				} catch (DataAccessException e) {// 不需要处理这个异常，只需要让while循环继续即可
				}
			}
		} while (count != 1);
		return keyBase;
	}

	public CommonDAO getCommonDAO() {
		return commonDAO;
	}

	public String getPkFld() {
		return pkFld;
	}

	public String getSeedFld() {
		return seedFld;
	}

	public String getSeedTable() {
		return seedTable;
	}

	public String getVerFld() {
		return verFld;
	}

	private synchronized void initialize() {
		if (!init) {
			querySeedSql = MessageFormat.format(
					"select {0}, {1} from {2} where {3}=0", new Object[] {
							getSeedFld(), getVerFld(), getSeedTable(),
							getPkFld() });
			updateSeedSql = MessageFormat
					.format(
							"update {0} set {1}={1}+?, {2}={2}+1 where {3}=0 and {2}=?",
							new Object[] { getSeedTable(), getSeedFld(),
									getVerFld(), getPkFld() });
			insertSeedSql = MessageFormat.format(
					"insert into {0} ({1}, {2}, {3}) values (0, ?, 0)",
					new Object[]{getSeedTable(), getPkFld(), getSeedFld(), getVerFld()});
			init = true;
		}
	}

	public void setCommonDAO(CommonDAO commonDAO) {
		this.commonDAO = commonDAO;
	}

	public void setPkFld(String pkFld) {
		this.pkFld = pkFld;
	}

	public void setSeedFld(String seedFld) {
		this.seedFld = seedFld;
	}

	public void setSeedTable(String seedTable) {
		this.seedTable = seedTable;
	}

	public void setVerFld(String verFld) {
		this.verFld = verFld;
	}
}
