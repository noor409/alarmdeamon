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
// Description:
//  Parse NMEA-0183 records (currently only $GPRMC/$GPGGA supported)
// References:
//  http://www.scientificcomponent.com/nmea0183.htm
//  http://home.mira.net/~gnb/gps/nmea.html
// ----------------------------------------------------------------------------
// Change History:
//  2007/07/27  Martin D. Flynn
//     -Initial release
//  2007/09/16  Martin D. Flynn
//     -Added 'getExtraData' method to return data following checksum.
// ----------------------------------------------------------------------------
package com.dignity.alarmdeamon.dateutil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Nmea0183
{

    // ------------------------------------------------------------------------

    public static final double  KILOMETERS_PER_KNOT     = 1.85200000;
    
    // ------------------------------------------------------------------------

    public static final long    FIELD_RECORD_TYPE       = 0x0000000000000001L;
    public static final long    FIELD_VALID_FIX         = 0x0000000000000002L;
    public static final long    FIELD_DDMMYY            = 0x0000000000000004L;
    public static final long    FIELD_HHMMSS            = 0x0000000000000008L;
    public static final long    FIELD_LATITUDE          = 0x0000000000000010L;
    public static final long    FIELD_LONGITUDE         = 0x0000000000000020L;
    public static final long    FIELD_SPEED             = 0x0000000000000040L;
    public static final long    FIELD_HEADING           = 0x0000000000000080L;
    public static final long    FIELD_HDOP              = 0x0000000000000100L;
    public static final long    FIELD_NUMBER_SATS       = 0x0000000000000200L;
    public static final long    FIELD_ALTITUDE          = 0x0000000000000400L;
    public static final long    FIELD_FIX_TYPE          = 0x0000000000000800L;
    private static Logger logger = LogManager.getLogger(Nmea0183.class.getName());

    // ------------------------------------------------------------------------

    private boolean     validChecksum   = false;
    private String      rcdType         = "";
    private long        fieldMask       = 0L;
    
    private long        ddmmyy          = 0L;
    private long        hhmmss          = 0L;
    private long        fixtime         = 0L;
    
    private boolean     validGPS        = false;
    private double      latitude        = 0.0;
    private double      longitude       = 0.0;    
    private double      speedKnots      = 0.0;
    private double      heading         = 0.0;
    
    private double      hdop            = 0.0;
    private int         numSats         = 0;
    private double      altitudeM       = 0.0;
    private int         fixType         = 0;
    
    private String      extraData       = null;

    // ------------------------------------------------------------------------

    /* instantiate NMEA-0183 record */
    public Nmea0183(String rcd)
    {
        this._parse(rcd, false);
    }

    /* instantiate NMEA-0183 record */
    public Nmea0183(String rcd, boolean ignoreChecksum)
    {
        this._parse(rcd, ignoreChecksum);
    }

    // ------------------------------------------------------------------------

    /* return mask of available fields */
    public long getFieldMask()
    {
        return this.fieldMask;
    }
    
    /* return true if specified field is available */
    public boolean hasField(long fld)
    {
        return ((this.fieldMask & fld) != 0);
    }
    
    // ------------------------------------------------------------------------
    
    /* return record type */
    public String getRecordType()
    {
        return this.rcdType;
    }
    
    // ------------------------------------------------------------------------

    /* return true if checksum is valid */
    public boolean isValidChecksum()
    {
        return this.validChecksum;
    }
    
    // ------------------------------------------------------------------------

    /* set the day/month/year (for "$GPGGA" records) */
    public void setDDMMYY(long ddmmyy)
    {
        if ((ddmmyy >= 10100L) && (ddmmyy <= 311299L)) { // day/month must be specified
            this.ddmmyy = ddmmyy;
            this.fieldMask |= FIELD_DDMMYY;
        } else {
            this.ddmmyy = 0L;
            this.fieldMask &= ~FIELD_DDMMYY;
        }
    }

    /* return the day/month/year of the fix */
    public long getDDMMYY()
    {
        return this.ddmmyy;
    }

    /* return the hour/minute/seconds of the fix */
    public long getHHMMSS()
    {
        return this.hhmmss;
    }

    /* return the epoch fix time */
    public long getFixtime()
    {
        if (this.fixtime <= 0L) {
            this.fixtime = this._getUTCSeconds(this.ddmmyy, this.hhmmss);
        }
        return this.fixtime;
    }
    
    // ------------------------------------------------------------------------

    /* return true if the GPS fix is valid */
    public boolean isValidGPS()
    {
        return this.validGPS;
    }

    /* return the latitude */
    public double getLatitude()
    {
        return this.latitude;
    }

    /* return the longitude */
    public double getLongitude()
    {
        return this.longitude;
    }
    
    // ------------------------------------------------------------------------

    /* return the speed in knots */
    public double getSpeedKnots()
    {
        return this.speedKnots;
    }

    /* return the speed in KPH */
    public double getSpeedKPH()
    {
        return this.speedKnots * KILOMETERS_PER_KNOT;
    }

    /* return the heading/course in degrees */
    public double getHeading()
    {
        return this.heading;
    }

    // ------------------------------------------------------------------------

    /* return the "$GPGGA" fix type */
    // (0=no fix, 1=GPS, 2=DGPS, 3=PPS?, 6=dead-reckoning)
    public int getFixType()
    {
        return this.fixType;
    }

    // ------------------------------------------------------------------------

    /* return the number of satellites used in fix */
    public int getNumberOfSatellites()
    {
        return this.numSats;
    }

    // ------------------------------------------------------------------------

    /* return the horizontal-dilution-of-precision */
    public double getHDOP()
    {
        return this.hdop;
    }

    // ------------------------------------------------------------------------

    /* return the altitude in meters */
    public double getAltitudeMeters()
    {
        return this.altitudeM;
    }

    // ------------------------------------------------------------------------

    /* return any data that may follow the checksum */
    public String getExtraData()
    {
        return this.extraData;
    }
    
    // ------------------------------------------------------------------------

    /* return string representation */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("Type     : ").append(this.getRecordType()).append("\n");
        sb.append("Checksum : ").append(this.isValidChecksum()?"ok":"failed").append("\n");
        sb.append("Fixtime  : ").append(this.getFixtime()).append(" [").append(new DateTime(this.getFixtime()).toString()).append("]\n");
        sb.append("SpeedKPH : ").append(this.getSpeedKPH()).append(" kph, heading ").append(this.getHeading()).append("\n");
        sb.append("Altitude : ").append(this.getAltitudeMeters()).append(" meters\n");
        return sb.toString();
    }
    
    // ------------------------------------------------------------------------

    /* parse record */
    private boolean _parse(String rcd, boolean ignoreChecksum)
    {
        
        /* pre-validate */
        if ((rcd == null) || !rcd.startsWith("$")) {
            logger.error("Null/Invalid record: " + rcd);
            return false;
        }
        
        /* valid checksum? */
        if (ignoreChecksum) {
            this.validChecksum = true;
        } else {
            this.validChecksum = this._hasValidChecksum(rcd);
            if (!this.validChecksum) {
            	logger.error("Invalid Checksum: " + rcd);
                return false;
            }
        }
        
        /* parse into fields */
        String fld[] = StringTools.parseString(rcd, ',');
        if ((fld == null) || (fld.length < 1)) {
        	logger.error("Insufficient fields: " + rcd);
            return false;
        }
        
        /* parse record type */
        this.fieldMask = 0L;
        if (fld[0].equals("$GPRMC")) {
            this.rcdType = fld[0];
            this.fieldMask |= FIELD_RECORD_TYPE;
            return this._parse_GPRMC(fld);
        } else
        if (fld[0].equals("$GPGGA")) {
            this.rcdType = fld[0];
            this.fieldMask |= FIELD_RECORD_TYPE;
            return this._parse_GPGGA(fld);
        } else {
        	logger.error("Record not supported: " + rcd);
            return false;
        }
        
    }
    
    // ------------------------------------------------------------------------

    /* parse "$GPRMC" */
    private boolean _parse_GPRMC(String fld[])
    {
        /* valid number of fields? */
        if (fld.length < 10) {
            return false;
        }
        
        /* valid GPS? */
        this.validGPS = fld[2].equals("A");
        this.fieldMask |= FIELD_VALID_FIX;

        /* fixtime */
        this.hhmmss  = StringTools.parseLong(fld[1], 0L);
        this.ddmmyy  = StringTools.parseLong(fld[9], 0L);
        this.fieldMask |= FIELD_HHMMSS | FIELD_DDMMYY;
        this.fixtime = 0L; // calculated later

        /* latitude, longitude, speed, heading */
        if (this.validGPS) {
            this.latitude  = this._parseLatitude (fld[3], fld[4]);
            this.longitude = this._parseLongitude(fld[5], fld[6]);
            if ((this.latitude  >=  90.0) || (this.latitude  <=  -90.0) ||
                (this.longitude >= 180.0) || (this.longitude <= -180.0)   ) {
                this.validGPS   = false;
                this.latitude   = 0.0;
                this.longitude  = 0.0;
            } else {
                this.fieldMask |= FIELD_LATITUDE | FIELD_LONGITUDE;
                this.speedKnots = StringTools.parseDouble(fld[7], -1.0);
                this.heading    = StringTools.parseDouble(fld[8], -1.0);
                this.fieldMask |= FIELD_SPEED | FIELD_HEADING;
            }
        } else {
            this.latitude   = 0.0;
            this.longitude  = 0.0;
            this.speedKnots = 0.0;
            this.heading    = 0.0;
        }

        /* extra data? */
        this.extraData = (fld.length >= 13)? fld[12] : null;

        /* return valid GPS state */
        return this.validGPS;

    }
    
    // ----------------------------------------------------------------------------

    /* parse "$GPGGA" */
    private boolean _parse_GPGGA(String fld[])
    {
        /* valid number of fields? */
        if (fld.length < 14) {
            return false;
        }
        
        /* valid GPS? */
        this.validGPS = !fld[6].equals("0");
        this.fieldMask |= FIELD_VALID_FIX;

        /* fixtime */
        this.hhmmss  = StringTools.parseLong(fld[1], 0L);
        this.ddmmyy  = 0L;  // we don't know the day
        this.fieldMask |= FIELD_HHMMSS;
        this.fixtime = 0L; // calculated later

        /* latitude, longitude, altitude */
        if (this.validGPS) {
            this.latitude  = this._parseLatitude (fld[2], fld[3]);
            this.longitude = this._parseLongitude(fld[4], fld[5]);
            if ((this.latitude  >=  90.0) || (this.latitude  <=  -90.0) ||
                (this.longitude >= 180.0) || (this.longitude <= -180.0)   ) {
                this.validGPS   = false;
                this.latitude   = 0.0;
                this.longitude  = 0.0;
            } else {
                this.fieldMask |= FIELD_LATITUDE | FIELD_LONGITUDE;
                this.fixType    = StringTools.parseInt(fld[6], 1); // 1=GPS, 2=DGPS, 3=PPS?, ...
                this.numSats    = StringTools.parseInt(fld[7], 0);
                this.hdop       = StringTools.parseDouble(fld[8], 0.0);
                this.altitudeM  = StringTools.parseDouble(fld[9], 0.0); // meters
                this.fieldMask |= FIELD_FIX_TYPE | FIELD_NUMBER_SATS | FIELD_HDOP | FIELD_ALTITUDE;
            }
        } else {
            this.latitude   = 0.0;
            this.longitude  = 0.0;
            this.fixType    = 0;
            this.numSats    = 0;
            this.hdop       = 0.0;
            this.altitudeM  = 0.0;
        }

        /* extra data? */
        this.extraData = (fld.length >= 16)? fld[15] : null;

        /* return valid GPS state */
        return this.validGPS;
        
    }
    
    // ----------------------------------------------------------------------------

    /**
    * Computes seconds in UTC time given values from GPS device.
    * @param dmy Date received from GPS in DDMMYY format, where DD is day, MM is month,
    * YY is year.
    * @param hms Time received from GPS in HHMMSS format, where HH is hour, MM is minute,
    * and SS is second.
    * @return Time in UTC seconds.
    */
    private long _getUTCSeconds(long dmy, long hms)
    {
    
        /* time of day [TOD] */
        int    HH  = (int)((hms / 10000L) % 100L);
        int    MM  = (int)((hms / 100L) % 100L);
        int    SS  = (int)(hms % 100L);
        long   TOD = (HH * 3600L) + (MM * 60L) + SS;
    
        /* current UTC day */
        long DAY;
        if (dmy > 0L) {
            int    yy  = (int)(dmy % 100L) + 2000;
            int    mm  = (int)((dmy / 100L) % 100L);
            int    dd  = (int)((dmy / 10000L) % 100L);
            long   yr  = ((long)yy * 1000L) + (long)(((mm - 3) * 1000) / 12);
            DAY        = ((367L * yr + 625L) / 1000L) - (2L * (yr / 1000L))
                         + (yr / 4000L) - (yr / 100000L) + (yr / 400000L)
                         + (long)dd - 719469L;
        } else {
            // we don't have the day, so we need to figure out as close as we can what it should be.
            long   utc = DateTime.getCurrentTimeSec();
            long   tod = utc % DateTime.DaySeconds(1);
            DAY        = utc / DateTime.DaySeconds(1);
            long   dif = (tod >= TOD)? (tod - TOD) : (TOD - tod); // difference should be small (ie. < 1 hour)
            if (dif > DateTime.HourSeconds(12)) { // 12 to 18 hours
                // > 12 hour difference, assume we've crossed a day boundary
                if (tod > TOD) {
                    // tod > TOD likely represents the next day
                    DAY++;
                } else {
                    // tod < TOD likely represents the previous day
                    DAY--;
                }
            }
        }
        
        /* return UTC seconds */
        long sec = DateTime.DaySeconds(DAY) + TOD;
        return sec;
        
    }

    /**
    * Parses latitude given values from GPS device.
    * @param s Latitude String from GPS device in ddmm.mm format.
    * @param d Latitude hemisphere, "N" for northern, "S" for southern.
    * @return Latitude parsed from GPS data, with appropriate sign based on hemisphere or
    * 90.0 if invalid latitude provided.
    */
    private double _parseLatitude(String s, String d)
    {
        double _lat = StringTools.parseDouble(s, 99999.0);
        if (_lat < 99999.0) {
            double lat = (double)((long)_lat / 100L); // _lat is always positive here
            lat += (_lat - (lat * 100.0)) / 60.0;
            return d.equals("S")? -lat : lat;
        } else {
            return 90.0; // invalid latitude
        }
    }
    
    /**
    * Parses longitude given values from GPS device.
    * @param s Longitude String from GPS device in ddmm.mm format.
    * @param d Longitude hemisphere, "E" for eastern, "W" for western.
    * @return Longitude parsed from GPS data, with appropriate sign based on hemisphere or
    * 180.0 if invalid longitude provided.
    */
    private double _parseLongitude(String s, String d)
    {
        double _lon = StringTools.parseDouble(s, 99999.0);
        if (_lon < 99999.0) {
            double lon = (double)((long)_lon / 100L); // _lon is always positive here
            lon += (_lon - (lon * 100.0)) / 60.0;
            return d.equals("W")? -lon : lon;
        } else {
            return 180.0; // invalid longitude
        }
    }

    // ------------------------------------------------------------------------

    /**
    * Checks if NMEA-0183 formatted String has valid checksum by calculating the
    * checksum of the payload and comparing that to the received checksum.
    * @param str NMEA-0183 formatted String to be checked.
    * @return true if checksum is valid, false otherwise.
    */
    private boolean _hasValidChecksum(String str)
    {
        int c = str.indexOf("*");
        if (c < 0) {
            // does not contain a checksum char
            return false;
        }
        String chkSum = str.substring(c + 1);
        byte cs[] = StringTools.parseHex(chkSum,null);
        if ((cs == null) || (cs.length != 1)) {
            // invalid checksum hex length
            return false;
        }
        int calcSum = this._calcChecksum(str);
        boolean isValid = (calcSum == ((int)cs[0] & 0xFF));
        if (!isValid) { logger.warn("Expected checksum: 0x" + StringTools.toHexString(calcSum,8)); }
        return isValid;
    }
    
    /**
    * Calculates the checksum for a NMEA-0183 formatted String, to allow it to be
    * compared against the received checksum.
    * @param str NMEA-0183 formatted String to be checksummed.
    * @return Checksum computed from input.
    */
    private int _calcChecksum(String str)
    {
        byte b[] = StringTools.getBytes(str);
        if (b == null) {
            return -1;
        } else {
            int cksum = 0, s = 0;
            if ((b.length > 0) && (b[0] == '$')) { s++; }
            for (; s < b.length; s++) {
                if (b[s] ==  '*') { break; }
                if (b[s] == '\r') { break; }
                if (b[s] == '\n') { break; }
                cksum = (cksum ^ b[s]) & 0xFF;
            }
            return cksum;
        }
    }
    
    // ------------------------------------------------------------------------

    public static void main(String argv[])
    {
        RTConfig.setCommandLineArgs(argv);
        Nmea0183 n;

        n = new Nmea0183("$GPRMC,080701.00,A,3128.7540,N,14257.6714,W,000.0,000.0,180707,13.1,E,A*1C");
        Print.sysPrintln("NMEA-0183: \n" + n);

        n = new Nmea0183("$GPGGA,025425.494,3509.0743,N,14207.6314,W,1,04,2.3,530.3,M,-21.9,M,0.0,0000*45");
        logger.trace("NMEA-0183: \n" + n);

    }
    
}
