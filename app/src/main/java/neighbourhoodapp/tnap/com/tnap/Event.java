package neighbourhoodapp.tnap.com.tnap;

import java.util.Date;

public class Event {
    private String cc;
    private long capacity; // numbers are stored as long in Firestore
    private String description;
    private Date enddate;
    private long eventid;
    private long headcount;
    private String name;
    private Date registerby;
    private Date startdate;
    private String venue;

    public Event() { } // needed for Firebase

    public Event(String cc, long capacity, String description, Date enddate, long eventid,
                 long headcount, String name, Date registerby, Date startdate, String venue) {
        this.cc = cc;
        this.capacity = capacity;
        this.description = description;
        this.enddate = enddate;
        this.eventid = eventid;
        this.headcount = headcount;
        this.name = name;
        this.registerby = registerby;
        this.startdate = startdate;
        this.venue = venue;
    }

    // JavaBeans naming convention for Firebase
    public String getCC() { return cc; }
    public long getCapacity() { return capacity; }
    public String getDescription() { return description; }
    public Date getEnddate() { return enddate; }
    public long getEventid() { return eventid; }
    public long getHeadcount() { return headcount; }
    public String getName() { return name; }
    public Date getRegisterby() { return registerby; }
    public Date getStartdate() { return startdate; }
    public String getVenue() { return venue; }

}
