package snt.common.i18n;

import java.util.Locale;

public interface IMessageResources {
	  public abstract String getMessage(String paramString, Object[] paramArrayOfObject);

	  public abstract String getMessage(String paramString1, String paramString2, Object[] paramArrayOfObject);

	  public abstract String getMessage(Locale paramLocale, String paramString, Object[] paramArrayOfObject);

	  public abstract String getMessage(String paramString1, Locale paramLocale, String paramString2, Object[] paramArrayOfObject);
}
