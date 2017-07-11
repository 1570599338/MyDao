package snt.common.web.util;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

public class PropertyConfigurer extends PropertyPlaceholderConfigurer {
	
	public PropertyConfigurer(){
		super();
	}

	@Override
	protected Properties mergeProperties() throws IOException {
	      Properties props = super.mergeProperties();
	      try {
	        //Set placeHolderSet = new HashSet();
	        Enumeration<Object> enumeration = props.keys();
	        while (enumeration.hasMoreElements()) {
	          String key = (String)enumeration.nextElement();
	          String val = props.getProperty(key);
	          String newVal = parseStringValue(val, props, null);
	          if (val != newVal)
	            props.setProperty(key, newVal);
	        }
	      }
	      catch (Exception e) {
	      }
	      WebUtils.moduleProperties = props;
		return props;
	}
	

}
