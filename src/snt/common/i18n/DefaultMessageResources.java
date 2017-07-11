  package snt.common.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

public class DefaultMessageResources implements IMessageResources {

	private static DefaultMessageResources defaultInstance = null;
	  private Properties bundleProps;
	  private boolean escape = true;
	  protected String escape(String string) {
		    if (!isEscape()) {
		      return string;
		    }

		    if ((string == null) || (string.indexOf('\'') < 0)) {
		      return string;
		    }

		    int n = string.length();
		    StringBuffer sb = new StringBuffer(n);

		    for (int i = 0; i < n; i++) {
		      char ch = string.charAt(i);

		      if (ch == '\'') {
		        sb.append('\'');
		      }

		      sb.append(ch);
		    }

		    return sb.toString();
		  }

		  protected String formatString(Locale locale, String formatString, Object[] args) {
		    if ((formatString == null) || (args == null) || (args.length == 0)) {
		      return formatString;
		    }
		    MessageFormat format = new MessageFormat(escape(formatString));
		    format.setLocale(locale);
		    return format.format(args);
		  }

		  public Properties getBundleProps() {
		    return this.bundleProps;
		  }
	
	public String getMessage(String key, Object[] args) {
		
		return formatString(Locale.getDefault(), getResourceBundle("", Locale.getDefault()).getString(key), args);
	}

	public String getMessage(String bundle, String key,Object[] args) {
		
		return formatString(Locale.getDefault(), getResourceBundle(bundle, Locale.getDefault()).getString(key), args);
	}

	public String getMessage(Locale locale, String key, Object[] args) {
		
	    return formatString(locale, getResourceBundle("", locale).getString(key), args);

	}

	public String getMessage(String bundle, Locale locale, String key, Object[] args) {
		
		return formatString(locale, getResourceBundle(bundle, locale).getString(key), args);
	}
	
	 private ResourceBundle getResourceBundle(String bundle, Locale locale) {
		    String baseName = this.bundleProps.getProperty(bundle, bundle);
		    return ResourceBundle.getBundle(baseName, locale);
		  }

		  public boolean isEscape() {
		    return this.escape;
		  }

		  public void setBundleProps(Properties bundleProps) {
		    this.bundleProps = bundleProps;
		  }

		  public void setEscape(boolean escape) {
		    this.escape = escape;
		  }

		  public DefaultMessageResources() {
		    if (defaultInstance == null)
		      defaultInstance = this;
		  }

		  public static DefaultMessageResources getInstance()
		  {
		    if (defaultInstance == null) {
		      new DefaultMessageResources();
		    }
		    return defaultInstance;
		  }

}
