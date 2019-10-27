// ----------------------------------------------------------------------------
// Copyright 2006-2008, Martin D. Flynn
// All rights reserved
// ----------------------------------------------------------------------------
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
// http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// ----------------------------------------------------------------------------
// Change History:
//  2007/05/25  Martin D. Flynn
//      Initial release
// ----------------------------------------------------------------------------
package com.digitrinity.alarmdeamon.dateutil;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class I18N
{

    // ------------------------------------------------------------------------
    // English: LocalStrings_en_US.properties
    // Mexican: LocalStrings_sp_MX.properties

    private static final String LOCAL_STRINGS   = "LocalStrings";
    private static final String _LOCAL_STRINGS  = "." + LOCAL_STRINGS;
    private static Logger logger = LogManager.getLogger(I18N.class.getName());

    // ------------------------------------------------------------------------
    
    private static HashMap localeMap = new HashMap();

    public static I18N getI18N(Class pkgClz, Locale loc)
    {
        return getI18N(pkgClz.getPackage().getName(), loc);
    }

    public static I18N getI18N(Package pkg, Locale loc)
    {
        return I18N.getI18N(pkg.getName(), loc);
    }

    public static I18N getI18N(String pkgName, Locale loc)
    {
        if (pkgName != null) {
            loc = I18N.getLocale(loc);

            /* get package map for specific Locale */
            HashMap packageMap = (HashMap)localeMap.get(loc);
            if (packageMap == null) {
                packageMap = new HashMap();
                localeMap.put(loc, packageMap);
            }

            /* get I18N instance for package */
            I18N i18n = (I18N)packageMap.get(pkgName);
            if (i18n == null) {
                i18n = new I18N(pkgName, loc);
                packageMap.put(pkgName, i18n);
            }
            return i18n;

        } else {

            /* no package specified */
            return null;

        }
    }

    // ------------------------------------------------------------------------   
    public static Locale getLocale(String loc)
    {
        String locale = ((loc!=null)&&!loc.equals(""))? loc : RTConfig.getString(RTKey.LOCALE,"");
        if ((locale == null) || locale.equals("")) {
            return I18N.getDefaultLocale();
        } else {
            int p = locale.indexOf("_");
            try {
                if (p < 0) {
                    String language = locale;
                    return new Locale(language);
                } else {
                    String language = locale.substring(0,p);
                    String country  = locale.substring(p+1);
                    return new Locale(language,country);
                }
            } catch (Throwable th) {
                return I18N.getDefaultLocale();
            }
        }
    }
    
    public static Locale getLocale(Locale loc)
    {
        if (loc != null) {
            return loc;
        } else {
            return I18N.getDefaultLocale();
        }
    }
    
    public static Locale getDefaultLocale()
    {
        return Locale.getDefault(); // System default
    }

    // ------------------------------------------------------------------------

    private ResourceBundle resBundle = null;
    private Locale locale = null;
    
    private I18N(String pkgName, Locale loc)
    {
        String bundleName = null;
        try {
            this.locale = I18N.getLocale(loc);
            bundleName = ((pkgName == null) || pkgName.equals(""))? LOCAL_STRINGS : (pkgName + _LOCAL_STRINGS);
            this.resBundle = ResourceBundle.getBundle(bundleName, this.locale);
     
        } catch (Throwable th) { 
            // MissingResourceException
            if (loc != null) {
                logger.info("Bundle not found: " + bundleName + " [" + th);
            }
            this.resBundle = null;
        }
    }
    
    private I18N(String pkgName)
    {
        this(pkgName, null);
    }
    
    // ------------------------------------------------------------------------

    public Locale getLocale()
    {
        return this.locale;
    }
    
    // ------------------------------------------------------------------------

    public Enumeration getKeys()
    {
        return (this.resBundle != null)? this.resBundle.getKeys() : null;
    }
    
    public void printKeyValues()
    {
        Enumeration e = this.getKeys();
        if (e != null) {
            for (; e.hasMoreElements();) {
                String k = (String)e.nextElement();
                String v = this.getString(k,"?");
                logger.debug("Key:" + k + " Value:" + v);
            }
        }
    }   
    // ------------------------------------------------------------------------

    public String getString(String key, String dft)
    {
        if ((key != null) && !key.equals("") && (this.resBundle != null)) {
            try {
                String s = this.resBundle.getString(key);
                if (s != null) {
                    return I18N.decodeNewLine(s);
                }
            } catch (Throwable th) { 
                // MissingResourceException - if no object for the given key can be found 
                // ClassCastException - if the object found for the given key is not a string
            }
        }
        return I18N.decodeNewLine(dft);
    }
    
    public String getString(String key, String dft, Object args[])
    {
        String val = this.getString(key, dft);
        if ((args != null) && (args.length > 0) && (val != null)) {
            try {
                MessageFormat mf = new MessageFormat(val);
                mf.setLocale(this.locale);
                StringBuffer sb = mf.format(args, new StringBuffer(), null);
                return I18N.decodeNewLine(sb).toString();
            } catch (Throwable th) {
                logger.error("Exception: " + key + " ==> " + val);
            }
        }
        return I18N.decodeNewLine(val);
    }
    
    public String getString(String key, String dft, Object arg)
    {
        return this.getString(key, dft, new Object[] { NonNull(arg) });
    }
    
    public String getString(String key, String dft, Object arg0, Object arg1)
    {
        return this.getString(key, dft, new Object[] { NonNull(arg0), NonNull(arg1) });
    }

    // ------------------------------------------------------------------------

    protected static Object NonNull(Object obj)
    {
        return (obj != null)? obj : "";
    }
    
    protected static String decodeNewLine(String s)
    {
        return StringTools.replace(s, "\\n", "\n");
    }

    protected static StringBuffer decodeNewLine(StringBuffer s)
    {
        return StringTools.replace(s, "\\n", "\n");
    }

    // ------------------------------------------------------------------------

    protected static final String I18N_KEY_STARTE  = "[$I18N=";
    protected static final String I18N_KEY_STARTC  = "[$I18N:";
    protected static final String I18N_KEY_END     = "]";

    public static class Text
    {
        private String pkg      = null;
        private String key      = null;
        private String dft      = null;
        public Text() {
            this((String)null,null,null);
        }
        public Text(String pkg, String key, String dft) {
            this.pkg = ((pkg != null) && !pkg.equals(""))? pkg : null; // may be null
            this.key = (key != null)? key : "";
            this.dft = (dft != null)? dft : "";           
        }
        public Text(Class clazz, String key, String dft) {
            this.pkg = (clazz != null)? clazz.getPackage().getName() : null;
            this.key = (key != null)? key : "";
            this.dft = (dft != null)? dft : "";            
        }
        public String getPackage() {
            return this.pkg;
        }
        public String getKey() {
            return this.key;
        }
        public String getDefault() {
            return this.dft;
        }
        public String toString() {
            return this.dft;
        }
        public String toString(I18N i18n) {
            return i18n.getString(this.getKey(), this.getDefault());
        }
        public String toString(Locale loc) {
            return (this.pkg != null)? this.toString(I18N.getI18N(this.pkg,loc)) : this.getDefault();
        }
    }

    public static I18N.Text parseText(Class clazz, String key, String str)
    {
        // i18n 'key' is separately specified
        String pkg = (clazz != null)? clazz.getPackage().getName() : null;
        return I18N.parseText(pkg, key, str);
    }

    public static I18N.Text parseText(Class clazz, String dft)
    {
        // i18n 'key' is part of the default string
        String pkg = (clazz != null)? clazz.getPackage().getName() : null;
        return I18N.parseText(pkg, null, dft);
    }

    public static I18N.Text parseText(String pkg, String key, String dft)
    {
        if (dft == null) {
        	logger.trace("Default value is null!");
            return new I18N.Text();
        } else
        if ((key != null) && !key.equals("")) {
            return new I18N.Text(pkg, key, dft);
        } else
        if (!StringTools.startsWithIgnoreCase(dft,I18N_KEY_STARTE) &&
            !StringTools.startsWithIgnoreCase(dft,I18N_KEY_STARTC)   ) {
            logger.trace("Invalid key definition! " + dft);
            return new I18N.Text(pkg, null, dft);
        } else {
            int ks = I18N_KEY_STARTE.length();
            int ke = dft.indexOf(I18N_KEY_END, ks);
            if (ke < ks) {
                return new I18N.Text(pkg, null, dft); // ']' is missing, return string as-is
            }
            String k = dft.substring(ks, ke).trim();
            String v = dft.substring(ke + I18N_KEY_END.length()).trim();
            return new I18N.Text(pkg, k, v);
        }
    }

    // ------------------------------------------------------------------------
    
    // DEBUG: test I18N
    private static String mainStr = "Cow";
    public static void main(String argv[])
    {
        I18N i18n = getI18N(I18N.class,null);
        i18n.printKeyValues();
        String m3 = i18n.getString("m.m3","{0}", new Object() {public String toString(){return mainStr;}});
        String m2 = i18n.getString("m.m2","How Now Brown {0}", m3);
        String m1 = i18n.getString("m.m1","Message: \\n{0}", m2);
        Print.sysPrintln(m1);
        mainStr = "Horse";
        Print.sysPrintln(m1);
    }

}