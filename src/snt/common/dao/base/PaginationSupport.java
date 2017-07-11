package snt.common.dao.base;

import java.util.List;

import snt.common.rs.MemoryResultSet;

/**
 * 分页数据支持
 * @author <a href="mailto:yangxiong@sinotrust.cn">阳雄</a> 
 */
public class PaginationSupport {
	/**分页数据为内存结果集*/
	public static final int MRS = 1;
	/**表示分页数据为对象*/
	public static final int OBJ = 2;
	/**默认的页大小*/
	public static final int PAGESIZE = 20;	
	/**表示分页数据中是对象列表*/
	public static final int POJOLIST = 0;
	/**当前页，0代表第一页*/
	private int currentPage = 0;
	/**对象列表*/
	private List items;
	/**内存结果集*/
	private MemoryResultSet mrs;
	/**自定义对象*/
	private Object object;
	/**页数*/
	private int pageCount = 0;
	/**页大小*/
	private int pageSize = PAGESIZE;
	/**总行数*/
	private int totalCount;
	/**分页数据类型*/
	private int type;

	/**
	 * 
	 * @param paginationData
	 * @param totalCount
	 */
	public PaginationSupport(Object paginationData, int totalCount) {
		this(paginationData, totalCount, PAGESIZE, 0);		
	}

	/**
	 * 
	 * @param paginationData
	 * @param totalCount
	 * @param startPage
	 */
	public PaginationSupport(Object paginationData, int totalCount, int startPage) {
		this(paginationData, totalCount, PAGESIZE, startPage);
	}

	/**
	 * 
	 * @param paginationData
	 * @param totalCount
	 * @param pageSize
	 * @param startPage
	 */
	public PaginationSupport(Object paginationData, int totalCount, int pageSize,
			int startPage) {
		setPageSize(pageSize);
		setTotalCount(totalCount);
		if (paginationData instanceof List) {
			setItems((List)paginationData);
			setType(POJOLIST);
		}
		else if(paginationData instanceof MemoryResultSet){
			setMrs((MemoryResultSet)paginationData);
			setType(MRS);
		}
		else{
			setObject(paginationData);
			setType(OBJ);
		}
		setItems(items);
		setCurrentPage(startPage);		
	}

	/**
	 * 得到当前页（0代表第一页，依次类推）
	 * @return 当前页
	 */
	public int getCurrentPage() {
		return currentPage;
	}

	/**
	 * 得到对象列表
	 * @return 对象列表
	 */
	public List getItems() {
		if(type != POJOLIST)
			throw new IllegalStateException("分页结果的类型不是Pojo列表！");
		return items;
	}

	/**
	 * 得到内存结果集
	 * @return 内存结果集
	 */
	public MemoryResultSet getMrs() {
		if(type != MRS)
			throw new IllegalStateException("分页结果的类型不是内存结果集！");
		return mrs;
	}
	
	/**
	 * 得到下一页的页号，如果当前页已经是最后一页，则返回-1.
	 * @return 下一页的页号
	 */
	public int getNextPage() {
		return (currentPage>=getPageCount()-1)?-1:currentPage+1;
	}
	

	/**
	 * 得到自定义对象
	 * @return 自定义对象
	 */
	public Object getObject(){
		if(type != OBJ)
			throw new IllegalStateException("分页结果的类型不是普通对象！");
		return object;
	}

	/**
	 * 得到总页数
	 * @return 总页数
	 */
	public int getPageCount() {
		return pageCount;
	}

	/**
	 * 得到每页数据大小
	 * @return 每页数据大小
	 */
	public int getPageSize() {
		return pageSize;
	}
	
	/**
	 * 得到前一页的页码，如果当前页即为首页，返回-1。
	 * @return 前一页的页码
	 */
	public int getPreviousPage() {
		return (currentPage<=0)?-1:currentPage-1;
	}

	/**
	 * 得到数据总数
	 * @return 数据总数
	 */
	public int getTotalCount() {
		return totalCount;
	}

	/**
	 * 得到分页数据类型
	 * 返回值为PaginationSupport.OBJLIST或PaginationSupport.MRS
	 * @return 分页数据类型
	 */
	public int getType() {
		return type;
	}
	
	/**
	 * 判断当前页是否为首页
	 * @return boolean
	 */
	public boolean isFirstPage(){
		return currentPage == 0;
	}

	/**
	 * 判断当前页是否为末页
	 * @return boolean
	 */
	public boolean isLastPage(){
		return currentPage == pageCount-1;
	}
	
	private void setCurrentPage(int startPage) {
		if (startPage<-1){
			this.currentPage = -1;
		}else if(startPage > pageCount){
			this.currentPage = pageCount;
		}else{
			this.currentPage =startPage; 
		}
	}

	private void setItems(List items) {
		this.items = items;
	}
	
	private void setMrs(MemoryResultSet mrs) {
		this.mrs = mrs;
	}
	
	private void setObject(Object obj){
		this.object = obj;
	}

	private void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	private void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	
	private void setTotalCount(int totalCount) {
		if (totalCount > 0) {
			this.totalCount = totalCount;
			setPageCount((int)Math.ceil((double)totalCount / pageSize));			
		} else {
			this.totalCount = 0;
			setPageCount(0);
		}
	}

	private void setType(int type) {
		this.type = type;
	}
}
