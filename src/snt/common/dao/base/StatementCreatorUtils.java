package snt.common.dao.base;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import org.springframework.jdbc.core.DisposableSqlTypeValue;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlTypeValue;
import snt.common.rs.ByteBlob;

abstract class StatementCreatorUtils
{
  public static void setParameterValue(PreparedStatement ps, int paramIndex, SqlParameter declaredParam, Object inValue)
    throws SQLException
  {
    setParameterValue(ps, paramIndex, declaredParam.getSqlType(), declaredParam.getTypeName(), inValue);
  }

  public static void setParameterValue(PreparedStatement ps, int paramIndex, int sqlType, String typeName, Object inValue)
    throws SQLException
  {
    if (inValue == null) {
      if (sqlType == -2147483648)
        ps.setNull(paramIndex, 0);
      else if (typeName != null)
        ps.setNull(paramIndex, sqlType, typeName);
      else {
        ps.setNull(paramIndex, sqlType);
      }
    }
    else if ((inValue instanceof SqlTypeValue)) {
      ((SqlTypeValue)inValue).setTypeValue(ps, paramIndex, sqlType, typeName);
    }
    else if (sqlType == 12)
      ps.setString(paramIndex, inValue.toString());
    else if ((sqlType == 3) || (sqlType == 2)) {
      if ((inValue instanceof BigDecimal))
        ps.setBigDecimal(paramIndex, (BigDecimal)inValue);
      else
        ps.setObject(paramIndex, inValue, sqlType);
    }
    else if (sqlType == 91) {
      if ((inValue instanceof java.util.Date)) {
        if ((inValue instanceof java.sql.Date))
          ps.setDate(paramIndex, (java.sql.Date)inValue);
        else {
          ps.setDate(paramIndex, new java.sql.Date(((java.util.Date)inValue).getTime()));
        }
      }
      else if ((inValue instanceof Calendar)) {
        Calendar cal = (Calendar)inValue;
        ps.setDate(paramIndex, new java.sql.Date(cal.getTime().getTime()), cal);
      }
      else {
        ps.setObject(paramIndex, inValue, 91);
      }
    } else if (sqlType == 92) {
      if ((inValue instanceof java.util.Date)) {
        if ((inValue instanceof Time))
          ps.setTime(paramIndex, (Time)inValue);
        else {
          ps.setTime(paramIndex, new Time(((java.util.Date)inValue).getTime()));
        }
      }
      else if ((inValue instanceof Calendar)) {
        Calendar cal = (Calendar)inValue;
        ps.setTime(paramIndex, new Time(cal.getTime().getTime()), cal);
      }
      else {
        ps.setObject(paramIndex, inValue, 92);
      }
    } else if (sqlType == 93) {
      if ((inValue instanceof java.util.Date)) {
        if ((inValue instanceof Timestamp)) {
          ps.setTimestamp(paramIndex, (Timestamp)inValue);
        }
        else {
          ps.setTimestamp(paramIndex, new Timestamp(((java.util.Date)inValue).getTime()));
        }
      }
      else if ((inValue instanceof Calendar)) {
        Calendar cal = (Calendar)inValue;
        ps.setTimestamp(paramIndex, new Timestamp(cal.getTime().getTime()), cal);
      }
      else {
        ps.setObject(paramIndex, inValue, 93);
      }
    } else if (sqlType == 2004) {
      if ((inValue instanceof byte[]))
        try {
          ps.setBlob(paramIndex, new ByteBlob((byte[])inValue));
        } catch (Throwable e) {
          ps.setBinaryStream(paramIndex, new ByteArrayInputStream((byte[])inValue), ((byte[])inValue).length);
        }
      else
        ps.setObject(paramIndex, inValue, sqlType);
    }
    else if (sqlType == -2147483648) {
      if (((inValue instanceof StringBuffer)) || ((inValue instanceof StringWriter)))
      {
        ps.setString(paramIndex, inValue.toString());
      } else if (((inValue instanceof java.util.Date)) && (!(inValue instanceof java.sql.Date)) && (!(inValue instanceof Time)) && (!(inValue instanceof Timestamp)))
      {
        ps.setTimestamp(paramIndex, new Timestamp(((java.util.Date)inValue).getTime()));
      }
      else if ((inValue instanceof Calendar)) {
        Calendar cal = (Calendar)inValue;
        ps.setTimestamp(paramIndex, new Timestamp(cal.getTime().getTime()));
      }
      else
      {
        ps.setObject(paramIndex, inValue);
      }
    }
    else
      ps.setObject(paramIndex, inValue, sqlType);
  }

  public static void cleanupParameters(Object[] paramValues)
  {
    if (paramValues != null)
      cleanupParameters(Arrays.asList(paramValues));
  }

  public static void cleanupParameters(Collection paramValues)
  {
    Iterator it;
    if (paramValues != null)
      for (it = paramValues.iterator(); it.hasNext(); ) {
        Object inValue = it.next();
        if ((inValue instanceof DisposableSqlTypeValue))
          ((DisposableSqlTypeValue)inValue).cleanup();
      }
  }
}