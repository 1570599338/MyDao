/*package snt.common.expparser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lsmp.djep.xjep.XJep;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;
import org.nfunk.jep.SymbolTable;
import org.nfunk.jep.Variable;
import org.nfunk.jep.function.PostfixMathCommandI;

import snt.common.expparser.function.DateFunction;
import snt.common.expparser.function.IsNull;
import snt.common.expparser.function.StringFuction;
import snt.common.expparser.function.WeightedAverage;
import snt.common.expparser.util.NullAsZeroNumber;

*//**
 * 表达式解析器<br>
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 * 
 *//*
public class ExpressionParser1xxxxx {
	//内嵌表达式解析器
	private XJep xjep;
	//是否把空值当成零处理
	private boolean treatNullAsZero = false;
	//解析后表达式的根节点
	private List topNodeList = null;
	//求解后的值
	private List resultList = null;
	


	  public static void main(String[] args) {
	    ExpressionParser ep = new ExpressionParser();
	    try {
	      ep.setExpression("z=x+y;x=1;y=2;");
	      System.out.println(ep.calcVarValue("z"));
	    } catch (ExpressionParserException e) {
	      e.printStackTrace();
	    }
	  }


	
	public ExpressionParser() {
		
	}
	
	*//**
	 * 设置表达式
	 * @param exp
	 * @throws ExpressionParserException
	 * @see #setExpressionArray(String[])
	 *//*
	public void setExpression(String exp) throws ExpressionParserException{
		setExpressionArray(new String[]{exp});
	}
	
	*//**
	 * 设置表达式数组
	 * 数组中的字符串元素允许在一个字符串中含有多个表达式，表达式之间用分号分隔，如
	 * 允许设置"x=1; y=2; z=x+y;" 这样的表达式进行解析。
	 * 但是要注意的一点是假如表达式字符串如下：
	 * "x=1; ;y=2; z=x+y;"
	 * 表达式解析器在解析到第二个分号时得到的是一个空表达式，此时，解析器会停止解析剩
	 * 余部分，这样，解析得到的表达式只有一个。
	 * @param exps
	 * @throws ExpressionParserException
	 *//*
	@SuppressWarnings("unchecked")
	public void setExpressionArray(String[] exps) throws ExpressionParserException{
		topNodeList = new ArrayList();
		try {
			for (int i = 0; i < exps.length; i++) {
				String expression = exps[i];
				getXJep().restartParser(expression);
				List subNodeList = new ArrayList();
				Node node;
				while ((node = getXJep().continueParsing()) != null) {
					Node simp = getXJep().simplify(getXJep().preprocess(node));
					subNodeList.add(simp);
				}
				if (subNodeList.size() == 1) {
					topNodeList.add(subNodeList.get(0));
				} else {
					topNodeList.add(subNodeList);
				}
			}
			if (getXJep().hasError()) {
				throw new ExpressionParserException(getXJep().getErrorInfo());
			}
		} catch (ParseException e) {
			throw new ExpressionParserException("解析表达式出错！", e);
		}
	}
	
	*//**
	 * 设置变量的值
	 * @param varName 变量名
	 * @param varValue 变量值
	 *//*
	public void setVarValue(String varName, Object varValue){
		if (isTreatNullAsZero()) {
			getXJep().addVariable(varName, handleNullVarValue(varValue));
		} else {
			getXJep().addVariable(varName, varValue);
		}
	}
	
	*//**
	 * 返回参数列表(不含常量参数）
	 * @return List
	 *//*
	public List getVarNameList(){
		List varNameList = new ArrayList();
		SymbolTable symTab = getXJep().getSymbolTable();
		List variableList = symTab.getVariableList();
		for (Iterator nameIter = variableList.iterator(); nameIter.hasNext();) {
			String varName = (String) nameIter.next();
			Variable var = symTab.getVar(varName);
			if (var.isConstant()) {
				continue;
			}
			varNameList.add(varName);
		}
		return varNameList;
	}
	
	*//**
	 * 清除所有变量（不含常量参数）
	 *//*
	public void removeAllVariable(){
		List varNameList = getVarNameList();
		for (Object object : varNameList) {
			getXJep().removeVariable((String)object);
		}	
	}
	
	*//**
	 * 返回表达式求值的结果。
	 * 返回结果的组织方式是和setExpressionArray(String[])设置的表达式一一对应的。<br>
	 * 例如，设置表达式数组时数组长度为4，那么返回结果就是一个列表(java.util.List)，长度为4，列表中的<br>
	 * 每个元素分别对应表达式字符串的结果。如果设置的表达式字符串数组的长度为1，那么返回的结果就是<br>
	 * 该表达式字符串求值的结果。<br>
	 * 因为一个表达式字符串可以含有多个表达式（用分号分隔），所以实际上一个表达式字符串求值的结果<br>
	 * 本身也可能是一个列表(java.util.List)，列表中的元素对应单个表达式的结果。如果表达式字符串只包含<br>
	 * 一个表达式，那么该字符串求值的结果就是单个表达式的结果，而非长度为1的列表。<br>
	 * 综上，本方法返回的结果可能是：<br>
	 * a.一个列表，而列表中的元素对应设置的表达式数组里的表达式字符串，可能是一个列表（如果表达式<br>
	 * 字符串含有多个表达式）；也可能是单个表达式求值的结果；<br>
	 * b.一个列表，而列表中的元素对应单个表达式求值的结果，这种情况是设置的表达式数组长度为1，但是<br>
	 * 表达式字符串本身含有多个表达式；<br>
	 * c.单个表达式求值的结果，这种情况对应的是设置的表达式数组长度为1，且表达式字符串只包含一个表达式。<br>
	 * @return Object
	 * @throws ExpressionParserException
	 * @see #setExpressionArray(String[])
	 *//*
	@SuppressWarnings("unchecked")
	public Object getValueAsObject() throws ExpressionParserException{
		resultList = new ArrayList(topNodeList.size());
		for (int ni = 0,nn=topNodeList.size(); ni < nn; ni++) {
			Object topNode = topNodeList.get(ni);
			if (topNode instanceof Node) {
				resultList.add(evaluate((Node)topNode));
			}
			else if(topNode instanceof List){
				List subNodeList = (List)topNode;
				List subResultList = new ArrayList(subNodeList.size());
				for (Iterator iter = subNodeList.iterator(); iter.hasNext();) {
					Node node = (Node) iter.next();
					subResultList.add(evaluate(node));
				}
				if (subResultList.size() == 1) {
					resultList.add(subResultList.get(0));
				} else {
					resultList.add(subResultList);
				}
			}			
		}
		if (resultList.size() == 1) {
			return resultList.get(0);
		} else {
			return resultList;
		}
	}
	
	*//**
	 * 得到某个变量的值
	 * 如果有某个表达式被赋值给这个变量，这个值可能是经过计算的，也可能是没有经过完全计算的。
	 * 这个取决于表达式赋值时所处的位置。如下的表达式：
	 * "x=2;if(2<1,y=x*2,x)"
	 * x被赋值为2，而y被赋予了一个表达式x*2，但是是在一个if函数中。
	 * 通过本方法求取x的值时，得到2
	 * 求取y的值时，会得到null，因为在表达式求值时，if的条件永远为false，y不会得到计算。如果确实
	 * 需要得到计算y的赋值表达式之后y的值，可以调用方法calcVarValue(String)。
	 * @param varName
	 * @return Object
	 * @throws ExpressionParserException
	 * @see #calcVarValue(String)
	 *//*
	public Object getVarValue(String varName) throws ExpressionParserException{
		return getXJep().getVarValue(varName);
	}
	
	*//**
	 * 计算得到某个变量的值
	 * 如果该变量是被直接赋值的，则返回赋予的值；
	 * 如果该变量被赋予一个表达式，则计算该表达式，并返回表达式求值的结果。
	 * @param varName
	 * @return Object
	 * @throws ExpressionParserException
	 *//*
	public Object calcVarValue(String varName) throws ExpressionParserException{
		try {
			return getXJep().calcVarValue(varName);
		} catch (Exception e) {
			throw new ExpressionParserException("变量求值出错！", e);
		}
	}
	
	*//**
	 * 判断表达式解析器工作过程中是否产生了异常
	 * @return 表达式解析器工作过程中是否产生了异常
	 *//*
	public boolean hasError(){
		return getXJep().hasError();
	}
	
	*//**
	 * 取得错误信息
	 * @return 解析表达式和求值过程中产生的错误信息
	 *//*
	public String getErrorInfo(){
		return getXJep().getErrorInfo();
	}
	
	*//**
	 * @return 是否将Null作为零来处理
	 *//*
	public boolean isTreatNullAsZero() {
		return treatNullAsZero;
	}
	
	*//**
	 * 设置是否将Null作为零来处理
	 * @param treatNullAsZero
	 *//*

	public void setTreatNullAsZero(boolean treatNullAsZero) {
		this.treatNullAsZero = treatNullAsZero;
	}
	
	*//**
	 * 添加自定义的函数
	 * @param functionName 函数名
	 * @param function 函数实现
	 *//*
	public void addFunction(String functionName,
			PostfixMathCommandI function) {
		getXJep().addFunction(functionName, function);
	}

	*//**
	 * 对表达式节点求值
	 * @param node
	 * @return 表达式节点的值
	 * @throws ExpressionParserException
	 *//*
	private Object evaluate(Node node) throws ExpressionParserException{
		try {
			return getXJep().evaluate(node);
		} catch (Exception e) {
			throw new ExpressionParserException("表达式求值出错！", e);
		}
	}
	
	*//**
	 * 得到内嵌的表达式解析器
	 * @return XJep实例
	 *//*
	private synchronized XJep getXJep() {
		if (xjep == null) {
			xjep = new XJep();
			xjep.addStandardConstants();
			xjep.addStandardFunctions();
			addExtendedFunctions(xjep);
			xjep.addComplex();
			xjep.setAllowUndeclared(true);
			xjep.setAllowAssignment(true);
			xjep.setImplicitMul(true);
		}

		return xjep;
	}
	
	*//**
	 * 添加扩展的函数
	 * @param xjep
	 *//*
	private void addExtendedFunctions(XJep xjep){
		//日期函数
		xjep.addFunction("Date", new DateFunction());
		xjep.addFunction("Year", new DateFunction(DateFunction.YEAR));
		xjep.addFunction("Mon", new DateFunction(DateFunction.MONTH));
		xjep.addFunction("Day", new DateFunction(DateFunction.DAY));
		xjep.addFunction("DayOfYear", new DateFunction(DateFunction.DAY_OF_YEAR));
		xjep.addFunction("Hour", new DateFunction(DateFunction.HOUR));
		xjep.addFunction("HourOfDay", new DateFunction(DateFunction.HOUR_OF_DAY));
		xjep.addFunction("Minute", new DateFunction(DateFunction.MINUTE));
		xjep.addFunction("Sec", new DateFunction(DateFunction.SECOND));
		xjep.addFunction("Quarter", new DateFunction(DateFunction.QUARTER));
		
		//字符串函数
		xjep.addFunction("Upp", new StringFuction("Upp", StringFuction.UPPER));
		xjep.addFunction("Low", new StringFuction("Low", StringFuction.LOWER));
		xjep.addFunction("Len", new StringFuction("Len", StringFuction.LENGTH));
		xjep.addFunction("ToNum", new StringFuction("ToNum", StringFuction.TONUMBER));
		xjep.addFunction("Mid", new StringFuction("Mid", StringFuction.MID));
		xjep.addFunction("Left", new StringFuction("Left", StringFuction.LEFT));
		xjep.addFunction("Right", new StringFuction("Right", StringFuction.RIGHT));
		xjep.addFunction("CharAt", new StringFuction("CharAt", StringFuction.CHARAT));
		xjep.addFunction("StartW", new StringFuction("StartW", StringFuction.STARTWITH));
		xjep.addFunction("EndW", new StringFuction("Upper", StringFuction.ENDWITH));
		xjep.addFunction("IndexOf", new StringFuction("Upper", StringFuction.INDEXOF));
		xjep.addFunction("LIndexOf", new StringFuction("Upper", StringFuction.LASTINDEXOF));
		
		//补充逻辑操作
		xjep.addFunction("IsNull", new IsNull());
		
		//统计函数
		//加权平均，忽略空值
		xjep.addFunction("WAvg0", new WeightedAverage(WeightedAverage.IGNORE_NULL));
		//加权平均，空值当成零来处理
		xjep.addFunction("WAvg1", new WeightedAverage(WeightedAverage.TREAT_NULL_AS_ZERO));
	}
	
	@SuppressWarnings("unchecked")
	private Object handleNullVarValue(Object varValue){
		if (varValue instanceof List) {
			List varValueList = (List)varValue;
			for (int vi = 0, vn=varValueList.size(); vi < vn; vi++) {
				varValueList.set(vi, handleNullVarValue(varValueList.get(vi)));				
			}
			return varValueList;
		} else if(varValue == null){
			return new NullAsZeroNumber();
		} else {
			return varValue;
		}
	}
}
*/