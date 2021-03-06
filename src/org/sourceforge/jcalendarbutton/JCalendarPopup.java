/**
 * CalendarPopup.java
 *
 * Created on March 5, 2000, 5:07 AM
 * @author Don Corley <don@donandann.com>
 * @version 1.0.0
 */
 
package org.sourceforge.jcalendarbutton;

import java.util.Date;

/** 
 * Maintain backwards compatibility when I fixed the package name.
 */
@Deprecated
public class JCalendarPopup extends org.jbundle.thin.base.screen.jcalendarbutton.JCalendarPopup
{
    private static final long serialVersionUID = 1L;

    /**
     * Creates new form CalendarPopup.
     */
    public JCalendarPopup()
    {
        super();
    }
    /**
     * Creates new form CalendarPopup.
     * @param date The initial date for this button.
     */
    public JCalendarPopup(Date date)
    {
        super(date);
    }
    /**
     * Creates new form CalendarPopup.
     * @param strDateParam The name of the date property (defaults to "date").
     * @param date The initial date for this button.
     */
    public JCalendarPopup(String strDateParam, Date date)
    {
        super(strDateParam, date);
    }
    /**
     * Creates new form CalendarPopup.
     * @param strDateParam The name of the date property (defaults to "date").
     * @param date The initial date for this button.
     * @param strLanguage The language to use.
     */
    public JCalendarPopup(String strDateParam, Date date, String strLanguage)
    {
        super(strDateParam, date, strLanguage);
    }
}
