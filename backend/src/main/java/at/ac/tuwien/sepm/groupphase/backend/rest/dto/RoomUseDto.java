package at.ac.tuwien.sepm.groupphase.backend.rest.dto;


import at.ac.tuwien.sepm.groupphase.backend.Entity.enums.Room;

import java.time.LocalDateTime;
import java.util.Objects;

public class RoomUseDto {

    private Long id;
    private LocalDateTime begin;
    private LocalDateTime end;
    private Room room;
    private EventDto event;


    public RoomUseDto () {
    }


    public Long getId () {
        return id;
    }


    public void setId (Long id) {
        this.id = id;
    }


    public LocalDateTime getBegin () {
        return begin;
    }


    public void setBegin (LocalDateTime begin) {
        this.begin = begin;
    }


    public LocalDateTime getEnd () {
        return end;
    }


    public void setEnd (LocalDateTime end) {
        this.end = end;
    }


    public Room getRoom () {
        return room;
    }


    public void setRoom (Room room) {
        this.room = room;
    }


    public EventDto getEvent () {
        return event;
    }


    public void setEvent (EventDto event) {
        this.event = event;
    }


    @Override
    public boolean equals (Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        RoomUseDto that = (RoomUseDto) o;
        return Objects.equals(id, that.id) &&
            Objects.equals(begin, that.begin) &&
            Objects.equals(end, that.end) &&
            room == that.room &&
            Objects.equals(event, that.event);
    }


    @Override
    public int hashCode () {
        return Objects.hash(id, begin, end, room, event);
    }


    @Override
    public String toString () {
        return "RoomUseDto{" +
            "id=" + id +
            ", begin=" + begin +
            ", end=" + end +
            ", room=" + room +
            ", event=" + event +
            '}';
    }
}
