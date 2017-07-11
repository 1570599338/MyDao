package snt.common.rs.statistics;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 辅助项统计的统计值
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 *
 */
public class AsstSumValue implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Map sumMap = null;
    private String unknown = "UNKNOWN";
    public AsstSumValue(){
        sumMap = new LinkedHashMap();
    }

    public AsstSumValue(Map asstsumMap){
        sumMap = asstsumMap;
        if (sumMap == null) {
            sumMap = new LinkedHashMap();
        }
    }

    public void add(Object asst, double value){
        if (!sumMap.containsKey(asst)) {
            sumMap.put(asst, new Double(value));
        }
        else{
            Double dValue = (Double)sumMap.get(asst);
            if(dValue == null){
                dValue = new Double(0.0);
            }
            sumMap.put(asst, new Double(dValue.doubleValue() + value));
        }
    }
    
    public void add(AsstSumValue asstSumValue){
    	if (asstSumValue != null) {
			for (Iterator iter = asstSumValue.sumMap.keySet().iterator(); iter.hasNext();) {
				Object asst = iter.next();
				Number value = (Number)asstSumValue.sumMap.get(asst);
				if(value != null)
					add(asst, value.doubleValue());
			}
		}
    }

    public AsstSumValue setScale(int scale){
    	AsstSumValue scaledSumValue = new AsstSumValue();
        Iterator it = sumMap.keySet().iterator();
        while (it.hasNext()) {
            Object key = it.next();
            Double value = (Double)sumMap.get(key);
            if(value == null){
            	scaledSumValue.sumMap.put(key, null);
            }
            else{
            	BigDecimal d = new BigDecimal(value.doubleValue());
            	d = d.setScale(scale, BigDecimal.ROUND_HALF_UP);
            	scaledSumValue.sumMap.put(key, d);
            }
        }
        return scaledSumValue;
    }

    public String toString(){
        StringBuffer strbuf = new StringBuffer();
        Iterator it = sumMap.keySet().iterator();
        boolean first = true;
        while (it.hasNext()) {
            Object key = it.next();
            Object value = sumMap.get(key);
            if(value == null){
                continue;
            }
            if (first) {
                first = false;
            }
            else{
                strbuf.append(", ");
            }
            strbuf.append(value).append(" ");
            strbuf.append(key==null?unknown:key);
        }
        return strbuf.toString();
    }
}
