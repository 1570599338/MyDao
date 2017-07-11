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
 * ���ʽ������<br>
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 * 
 *//*
public class ExpressionParser1xxxxx {
	//��Ƕ���ʽ������
	private XJep xjep;
	//�Ƿ�ѿ�ֵ�����㴦��
	private boolean treatNullAsZero = false;
	//��������ʽ�ĸ��ڵ�
	private List topNodeList = null;
	//�����ֵ
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
	 * ���ñ��ʽ
	 * @param exp
	 * @throws ExpressionParserException
	 * @see #setExpressionArray(String[])
	 *//*
	public void setExpression(String exp) throws ExpressionParserException{
		setExpressionArray(new String[]{exp});
	}
	
	*//**
	 * ���ñ��ʽ����
	 * �����е��ַ���Ԫ��������һ���ַ����к��ж�����ʽ�����ʽ֮���÷ֺŷָ�����
	 * ��������"x=1; y=2; z=x+y;" �����ı��ʽ���н�����
	 * ����Ҫע���һ���Ǽ�����ʽ�ַ������£�
	 * "x=1; ;y=2; z=x+y;"
	 * ���ʽ�������ڽ������ڶ����ֺ�ʱ�õ�����һ���ձ��ʽ����ʱ����������ֹͣ����ʣ
	 * �ಿ�֣������������õ��ı��ʽֻ��һ����
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
			throw new ExpressionParserException("�������ʽ����", e);
		}
	}
	
	*//**
	 * ���ñ�����ֵ
	 * @param varName ������
	 * @param varValue ����ֵ
	 *//*
	public void setVarValue(String varName, Object varValue){
		if (isTreatNullAsZero()) {
			getXJep().addVariable(varName, handleNullVarValue(varValue));
		} else {
			getXJep().addVariable(varName, varValue);
		}
	}
	
	*//**
	 * ���ز����б�(��������������
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
	 * ������б�������������������
	 *//*
	public void removeAllVariable(){
		List varNameList = getVarNameList();
		for (Object object : varNameList) {
			getXJep().removeVariable((String)object);
		}	
	}
	
	*//**
	 * ���ر��ʽ��ֵ�Ľ����
	 * ���ؽ������֯��ʽ�Ǻ�setExpressionArray(String[])���õı��ʽһһ��Ӧ�ġ�<br>
	 * ���磬���ñ��ʽ����ʱ���鳤��Ϊ4����ô���ؽ������һ���б�(java.util.List)������Ϊ4���б��е�<br>
	 * ÿ��Ԫ�طֱ��Ӧ���ʽ�ַ����Ľ����������õı��ʽ�ַ�������ĳ���Ϊ1����ô���صĽ������<br>
	 * �ñ��ʽ�ַ�����ֵ�Ľ����<br>
	 * ��Ϊһ�����ʽ�ַ������Ժ��ж�����ʽ���÷ֺŷָ���������ʵ����һ�����ʽ�ַ�����ֵ�Ľ��<br>
	 * ����Ҳ������һ���б�(java.util.List)���б��е�Ԫ�ض�Ӧ�������ʽ�Ľ����������ʽ�ַ���ֻ����<br>
	 * һ�����ʽ����ô���ַ�����ֵ�Ľ�����ǵ������ʽ�Ľ�������ǳ���Ϊ1���б�<br>
	 * ���ϣ����������صĽ�������ǣ�<br>
	 * a.һ���б����б��е�Ԫ�ض�Ӧ���õı��ʽ������ı��ʽ�ַ�����������һ���б�������ʽ<br>
	 * �ַ������ж�����ʽ����Ҳ�����ǵ������ʽ��ֵ�Ľ����<br>
	 * b.һ���б����б��е�Ԫ�ض�Ӧ�������ʽ��ֵ�Ľ����������������õı��ʽ���鳤��Ϊ1������<br>
	 * ���ʽ�ַ��������ж�����ʽ��<br>
	 * c.�������ʽ��ֵ�Ľ�������������Ӧ�������õı��ʽ���鳤��Ϊ1���ұ��ʽ�ַ���ֻ����һ�����ʽ��<br>
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
	 * �õ�ĳ��������ֵ
	 * �����ĳ�����ʽ����ֵ��������������ֵ�����Ǿ�������ģ�Ҳ������û�о�����ȫ����ġ�
	 * ���ȡ���ڱ��ʽ��ֵʱ������λ�á����µı��ʽ��
	 * "x=2;if(2<1,y=x*2,x)"
	 * x����ֵΪ2����y��������һ�����ʽx*2����������һ��if�����С�
	 * ͨ����������ȡx��ֵʱ���õ�2
	 * ��ȡy��ֵʱ����õ�null����Ϊ�ڱ��ʽ��ֵʱ��if��������ԶΪfalse��y����õ����㡣���ȷʵ
	 * ��Ҫ�õ�����y�ĸ�ֵ���ʽ֮��y��ֵ�����Ե��÷���calcVarValue(String)��
	 * @param varName
	 * @return Object
	 * @throws ExpressionParserException
	 * @see #calcVarValue(String)
	 *//*
	public Object getVarValue(String varName) throws ExpressionParserException{
		return getXJep().getVarValue(varName);
	}
	
	*//**
	 * ����õ�ĳ��������ֵ
	 * ����ñ����Ǳ�ֱ�Ӹ�ֵ�ģ��򷵻ظ����ֵ��
	 * ����ñ���������һ�����ʽ�������ñ��ʽ�������ر��ʽ��ֵ�Ľ����
	 * @param varName
	 * @return Object
	 * @throws ExpressionParserException
	 *//*
	public Object calcVarValue(String varName) throws ExpressionParserException{
		try {
			return getXJep().calcVarValue(varName);
		} catch (Exception e) {
			throw new ExpressionParserException("������ֵ����", e);
		}
	}
	
	*//**
	 * �жϱ��ʽ�����������������Ƿ�������쳣
	 * @return ���ʽ�����������������Ƿ�������쳣
	 *//*
	public boolean hasError(){
		return getXJep().hasError();
	}
	
	*//**
	 * ȡ�ô�����Ϣ
	 * @return �������ʽ����ֵ�����в����Ĵ�����Ϣ
	 *//*
	public String getErrorInfo(){
		return getXJep().getErrorInfo();
	}
	
	*//**
	 * @return �Ƿ�Null��Ϊ��������
	 *//*
	public boolean isTreatNullAsZero() {
		return treatNullAsZero;
	}
	
	*//**
	 * �����Ƿ�Null��Ϊ��������
	 * @param treatNullAsZero
	 *//*

	public void setTreatNullAsZero(boolean treatNullAsZero) {
		this.treatNullAsZero = treatNullAsZero;
	}
	
	*//**
	 * ����Զ���ĺ���
	 * @param functionName ������
	 * @param function ����ʵ��
	 *//*
	public void addFunction(String functionName,
			PostfixMathCommandI function) {
		getXJep().addFunction(functionName, function);
	}

	*//**
	 * �Ա��ʽ�ڵ���ֵ
	 * @param node
	 * @return ���ʽ�ڵ��ֵ
	 * @throws ExpressionParserException
	 *//*
	private Object evaluate(Node node) throws ExpressionParserException{
		try {
			return getXJep().evaluate(node);
		} catch (Exception e) {
			throw new ExpressionParserException("���ʽ��ֵ����", e);
		}
	}
	
	*//**
	 * �õ���Ƕ�ı��ʽ������
	 * @return XJepʵ��
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
	 * �����չ�ĺ���
	 * @param xjep
	 *//*
	private void addExtendedFunctions(XJep xjep){
		//���ں���
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
		
		//�ַ�������
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
		
		//�����߼�����
		xjep.addFunction("IsNull", new IsNull());
		
		//ͳ�ƺ���
		//��Ȩƽ�������Կ�ֵ
		xjep.addFunction("WAvg0", new WeightedAverage(WeightedAverage.IGNORE_NULL));
		//��Ȩƽ������ֵ������������
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