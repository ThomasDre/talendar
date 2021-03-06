package at.ac.tuwien.sepm.groupphase.backend.testObjects;


import java.time.LocalDateTime;
import java.util.Objects;

public class HolidayDto {


    private Long id;
    private TrainerDto trainer;
    private String title;
    private String description;
    private LocalDateTime holidayStart;
    private LocalDateTime holidayEnd;


    public HolidayDto() {

    }


    public HolidayDto (Long id, TrainerDto trainer, String title, String description, LocalDateTime holidayStart, LocalDateTime holidayEnd) {
        this.id = id;
        this.trainer = trainer;
        this.title = title;
        this.description = description;
        this.holidayStart = holidayStart;
        this.holidayEnd = holidayEnd;
    }


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public TrainerDto getTrainer() {
        return trainer;
    }


    public void setTrainer(TrainerDto trainer) {
        this.trainer = trainer;
    }


    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }


    public LocalDateTime getHolidayStart() {
        return holidayStart;
    }


    public void setHolidayStart(LocalDateTime holidayStart) {
        this.holidayStart = holidayStart;
    }


    public LocalDateTime getHolidayEnd() {
        return holidayEnd;
    }


    public void setHolidayEnd(LocalDateTime holidayEnd) {
        this.holidayEnd = holidayEnd;
    }


    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        HolidayDto that = (HolidayDto) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(trainer, that.trainer) &&
               Objects.equals(title, that.title) &&
               Objects.equals(description, that.description) &&
               Objects.equals(holidayStart, that.holidayStart) &&
               Objects.equals(holidayEnd, that.holidayEnd);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, trainer, title, description, holidayStart, holidayEnd);
    }


    @Override
    public String toString() {
        return "HolidayDto{" +
               "id=" + id +
               ", trainer=" + trainer +
               ", title='" + title + '\'' +
               ", description='" + description + '\'' +
               ", holidayStart=" + holidayStart +
               ", holidayEnd=" + holidayEnd +
               '}';
    }
}
