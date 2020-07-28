package restopass.dto;

import java.util.List;

public class RestaurantSlot {

    private List<List<DateTimeWithTables>> dateTimeWithTables;
    private Boolean isDayFull = false;

    public List<List<DateTimeWithTables>> getDateTime() {
        return dateTimeWithTables;
    }

    public void setDateTime(List<List<DateTimeWithTables>> dateTime) {
        this.dateTimeWithTables = dateTime;
    }

    public Boolean getDayFull() {
        return isDayFull;
    }

    public void setDayFull(Boolean dayFull) {
        isDayFull = dayFull;
    }

    public RestaurantSlot() {
    }


}
