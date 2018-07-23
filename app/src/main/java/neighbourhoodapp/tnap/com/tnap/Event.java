package neighbourhoodapp.tnap.com.tnap;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Event {
    private String cc;
    private int capacity; // numbers are stored as long in Firestore
    private String description;
    private @ServerTimestamp Date enddate;
    private int eventid;
    private int headcount;
    private String name;
    private @ServerTimestamp Date registerby;
    private @ServerTimestamp Date startdate;
    private String venue;
    private boolean cancelled;

    public Event() { } // needed for Firebase

    public Event(String cc, int capacity, String description, Date enddate, int eventid,
                 int headcount, String name, Date registerby, Date startdate, String venue) {
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
        this.cancelled = false;
    }

    // JavaBeans naming convention for Firebase
    // getters
    public String getCC() { return cc; }
    public int getCapacity() { return capacity; }
    public String getDescription() { return description; }
    public Date getEnddate() { return enddate; }
    public int getEventid() { return eventid; }
    public int getHeadcount() { return headcount; }
    public String getName() { return name; }
    public Date getRegisterby() { return registerby; }
    public Date getStartdate() { return startdate; }
    public String getVenue() { return venue; }
    public boolean getCancelled() { return cancelled; }

    // setters
    public void setCC(String cc) { this.cc = cc; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setDescription(String description) { this.description = description; }
    public void setEnddate(Date enddate) { this.enddate = enddate; }
    public void setEventid(int eventid) { this.eventid = eventid; }
    public void setHeadcount(int headcount) { this.headcount = headcount; }
    public void setName(String name) { this.name = name; }
    public void setRegisterby(Date registerby) { this.registerby = registerby; }
    public void setStartdate(Date startdate) { this.startdate = startdate; }
    public void setVenue(String venue) { this.venue = venue; }
    public void setCancelled(boolean isCancelled) { this.cancelled = isCancelled; }

}
