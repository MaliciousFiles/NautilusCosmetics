package org.apache.commons.dbcp2;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.FieldPosition;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

public final class Utils {
//    private static final ResourceBundle messages = ResourceBundle.getBundle(Utils.class.getPackage().getName() + ".LocalStrings");
    /** @deprecated */
    @Deprecated
    public static final boolean IS_SECURITY_ENABLED = isSecurityEnabled();
    public static final String DISCONNECTION_SQL_CODE_PREFIX = "08";
    public static final Set<String> DISCONNECTION_SQL_CODES = new HashSet();
    static final ResultSet[] EMPTY_RESULT_SET_ARRAY = new ResultSet[0];
    static final String[] EMPTY_STRING_ARRAY = new String[0];

    public static char[] clone(char[] value) {
        return value == null ? null : (char[])value.clone();
    }

    public static Properties cloneWithoutCredentials(Properties properties) {
        if (properties != null) {
            Properties temp = (Properties)properties.clone();
            temp.remove("user");
            temp.remove("password");
            return temp;
        } else {
            return properties;
        }
    }

    public static void closeQuietly(AutoCloseable autoCloseable) {
        if (autoCloseable != null) {
            try {
                autoCloseable.close();
            } catch (Exception var2) {
            }
        }

    }

    /** @deprecated */
    @Deprecated
    public static void closeQuietly(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception var2) {
            }
        }

    }

    /** @deprecated */
    @Deprecated
    public static void closeQuietly(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (Exception var2) {
            }
        }

    }

    /** @deprecated */
    @Deprecated
    public static void closeQuietly(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (Exception var2) {
            }
        }

    }

    public static String getMessage(String key) {
        return getMessage(key, (Object[])null);
    }

    public static String getMessage(String key, Object... args) {
        String msg = "TRIED TO GET MESSAGE!";//messages.getString(key);
        if (args != null && args.length != 0) {
            MessageFormat mf = new MessageFormat(msg);
            return mf.format(args, new StringBuffer(), (FieldPosition)null).toString();
        } else {
            return msg;
        }
    }

    static boolean isSecurityEnabled() {
        return System.getSecurityManager() != null;
    }

    public static char[] toCharArray(String value) {
        return value != null ? value.toCharArray() : null;
    }

    public static String toString(char[] value) {
        return value == null ? null : String.valueOf(value);
    }

    private Utils() {
    }

    static {
        DISCONNECTION_SQL_CODES.add("57P01");
        DISCONNECTION_SQL_CODES.add("57P02");
        DISCONNECTION_SQL_CODES.add("57P03");
        DISCONNECTION_SQL_CODES.add("01002");
        DISCONNECTION_SQL_CODES.add("JZ0C0");
        DISCONNECTION_SQL_CODES.add("JZ0C1");
    }
}