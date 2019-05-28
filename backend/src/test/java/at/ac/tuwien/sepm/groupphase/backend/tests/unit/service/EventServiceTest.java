package at.ac.tuwien.sepm.groupphase.backend.tests.unit.service;



import at.ac.tuwien.sepm.groupphase.backend.Entity.Customer;
import at.ac.tuwien.sepm.groupphase.backend.Entity.Event;
import at.ac.tuwien.sepm.groupphase.backend.Entity.RoomUse;
import at.ac.tuwien.sepm.groupphase.backend.exceptions.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exceptions.TrainerNotAvailableException;
import at.ac.tuwien.sepm.groupphase.backend.persistence.CustomerRepository;
import at.ac.tuwien.sepm.groupphase.backend.persistence.EventRepository;
import at.ac.tuwien.sepm.groupphase.backend.persistence.RoomUseRepository;
import at.ac.tuwien.sepm.groupphase.backend.persistence.TrainerRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.IEventService;
import at.ac.tuwien.sepm.groupphase.backend.service.ITrainerService;
import at.ac.tuwien.sepm.groupphase.backend.testDataCreation.FakeData;
import at.ac.tuwien.sepm.groupphase.backend.service.exceptions.ServiceException;
import at.ac.tuwien.sepm.groupphase.backend.service.exceptions.ValidationException;
import at.ac.tuwien.sepm.groupphase.backend.util.validator.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import at.ac.tuwien.sepm.groupphase.backend.Entity.Trainer;


import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class EventServiceTest {

    @Autowired
    private IEventService eventService;

    @Autowired
    private Validator validator;
    @Autowired
    private static FakeData faker = new FakeData();

    @Autowired
    private ITrainerService trainerService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RoomUseRepository roomUseRepository;

    @Autowired
    private CustomerRepository  customerRepository;



    private static Event VALID_INCOMING_BIRTHDAY = faker.fakeNewBirthdayEntity();
    private static Event VALID_INCOMING_COURSE = faker.fakeNewCourseEntity();
    private static Event VALID_INCOMING_BIRTHDAY_B = faker.fakeNewBirthdayEntity();
    private static Event PERSISTED_BIRHDAY = faker.fakeBirthdayEntity();
    private static Event VALID_INCOMING_RENT = faker.fakeNewRent();
    private static List<Trainer> savedTrainers = new LinkedList<>();
    private static Trainer trainer = faker.fakeNewTrainerEntity();



    @BeforeEach
    public void init(){
        VALID_INCOMING_BIRTHDAY = faker.fakeNewBirthdayEntity();
        VALID_INCOMING_BIRTHDAY_B = faker.fakeNewBirthdayEntity();
        VALID_INCOMING_COURSE = faker.fakeNewCourseEntity();
        VALID_INCOMING_RENT = faker.fakeNewRent();
        trainer = postTrainer();
    }


    @Test
    public void test_saveValidEvent_EventShouldBeAccepted() throws Exception{
        //when(eventRepository.save(any(Event.class))).thenReturn(PERSISTED_BIRHDAY);
        eventService.save(VALID_INCOMING_BIRTHDAY);
        System.out.println(VALID_INCOMING_BIRTHDAY.getId());
        assertNotNull(VALID_INCOMING_BIRTHDAY.getCreated());
        assertNotNull(VALID_INCOMING_BIRTHDAY.getUpdated());
        assertFalse(VALID_INCOMING_BIRTHDAY.getCreated().isAfter(VALID_INCOMING_BIRTHDAY.getUpdated()));
    }

    @Test
    public void postEvent_CheckRoomUseHasEventId() throws Exception{

        Event event = eventService.save(VALID_INCOMING_BIRTHDAY);
        List<RoomUse> rooms = roomUseRepository.findByEvent_IdAndEvent_DeletedFalse(event.getId());

        for(RoomUse r: rooms
            ) {
            assertEquals(event.getId(), r.getEvent().getId());
        }

    }

    @Test
    public void updateEvent_changedName() throws NotFoundException, ServiceException, ValidationException {
        String newName = "newNameGuaranteed";

        Event savedEvent = eventService.save(VALID_INCOMING_COURSE);

        savedEvent.setName(newName);
        Event updatedEvent = this.eventService.update(savedEvent);
        assertEquals(newName, updatedEvent.getName());
    }

    @Test
    public void updateEvent_changedPrice() throws NotFoundException, ServiceException, ValidationException {
        Double newPrice = 6.2;

        Event savedEvent = eventService.save(VALID_INCOMING_COURSE);

        savedEvent.setPrice(newPrice);
        Event updatedEvent = this.eventService.update(savedEvent);
        assertEquals(newPrice, updatedEvent.getPrice());
    }

    @Test
    public void updateEvent_changedMaxParticipant() throws NotFoundException, ServiceException, ValidationException {
        Integer newMaxParticipant = 5;

        Event savedEvent = eventService.save(VALID_INCOMING_COURSE);

        savedEvent.setMaxParticipants(newMaxParticipant);
        this.eventService.update(savedEvent);

        Event checkEvent = eventService.getEventById(VALID_INCOMING_COURSE.getId());
        assertEquals(newMaxParticipant, checkEvent.getMaxParticipants());
    }

    @Test
    public void updateEvent_changedMinAgeAndMaxAge() throws NotFoundException, ServiceException, ValidationException {
        Integer newMinAge = VALID_INCOMING_COURSE.getMinAge()+1;
        Integer newMaxAge = VALID_INCOMING_COURSE.getMaxAge()+1;

        Event savedEvent = eventService.save(VALID_INCOMING_COURSE);

        savedEvent.setMinAge(newMinAge);
        savedEvent.setMaxAge(newMaxAge);
        Event updatedEvent = this.eventService.update(savedEvent);
        assertEquals(newMinAge, updatedEvent.getMinAge());
        assertEquals(newMaxAge, updatedEvent.getMaxAge());
    }


    @Test
    public void postEvent_CheckCustomerHasEventId() throws Exception{
        Event event = eventService.save(VALID_INCOMING_BIRTHDAY);
        List<Customer> customers = customerRepository.findByEvents_Id(event.getId());
        assertNotNull(customers);
    }

    @Test
    public void postRent_MissingCustomer(){
        VALID_INCOMING_RENT.setCustomers(null);
        assertThrows(ValidationException.class, () -> eventService.save(VALID_INCOMING_RENT));
    }

    @Test
    public void postRent_MissingRoomUse(){
        VALID_INCOMING_RENT.setRoomUses(null);
        assertThrows(ValidationException.class, () -> eventService.save(VALID_INCOMING_RENT));
    }


    @Test
    public void postRent_MissingEmailInCustomer(){
        for(Customer x : VALID_INCOMING_RENT.getCustomers()) {
            x.setEmail(null);
        }
        assertThrows(ValidationException.class, () -> eventService.save(VALID_INCOMING_RENT));
    }

    @Test
    public void postRent_MissingPhoneNumberInCustomer(){
        for(Customer x : VALID_INCOMING_RENT.getCustomers()) {
            x.setPhone(null);
        }
        assertThrows(ValidationException.class, () -> eventService.save(VALID_INCOMING_RENT));
    }

    @Test
    public void postRent_MissingLastNameInCustomer(){
        for(Customer x : VALID_INCOMING_RENT.getCustomers()) {
            x.setLastName(null);
        }
        assertThrows(ValidationException.class, () -> eventService.save(VALID_INCOMING_RENT));
    }


    @Test
    public void postCourse_MissingName(){
        VALID_INCOMING_COURSE.setName(null);
        assertThrows(ValidationException.class, () -> eventService.save(VALID_INCOMING_COURSE));
    }

    @Test
    public void postCourse_MissingRoomUse(){
        VALID_INCOMING_COURSE.setRoomUses(null);
        assertThrows(ValidationException.class, () -> eventService.save(VALID_INCOMING_COURSE));
    }

    @Test
    public void postCourse_MissingMinAge(){
        VALID_INCOMING_COURSE.setMinAge(null);
        assertThrows(ValidationException.class, () -> eventService.save(VALID_INCOMING_COURSE));
    }

    @Test
    public void postCourse_MissingMaxAge(){
        VALID_INCOMING_COURSE.setMaxAge(null);
        assertThrows(ValidationException.class, () -> eventService.save(VALID_INCOMING_COURSE));
    }

    @Test
    public void postCourse_MissingEndOfApplication(){
        VALID_INCOMING_COURSE.setEndOfApplication(null);
        assertThrows(ValidationException.class, () -> eventService.save(VALID_INCOMING_COURSE));
    }

    @Test
    public void postCourse_MissingDescription(){
        VALID_INCOMING_COURSE.setDescription(null);
        assertThrows(ValidationException.class, () -> eventService.save(VALID_INCOMING_COURSE));
    }

    @Test
    public void postCourse_TrainerNotFound(){
        VALID_INCOMING_COURSE.getTrainer().setId(200L);
        assertThrows(ValidationException.class, () -> eventService.save(VALID_INCOMING_COURSE));
    }


    @Test
    public void postCourse_CustomerIsSet(){
        RoomUse roomUse = faker.fakeRoomUseDto();
        List<RoomUse> roomUses = new LinkedList<>();
        roomUses.add(roomUse);
        VALID_INCOMING_COURSE.setRoomUses(roomUses);
        assertThrows(ValidationException.class, () -> eventService.save(VALID_INCOMING_COURSE));
    }


    @Test
    public void postBirthday_MissingCustomer(){
        VALID_INCOMING_BIRTHDAY.setCustomers(null);
        assertThrows(ValidationException.class, () -> eventService.save(VALID_INCOMING_BIRTHDAY));
    }

    @Test
    public void postBirthday_MissingRoomUse(){
        VALID_INCOMING_BIRTHDAY.setRoomUses(null);
        assertThrows(ValidationException.class, () -> eventService.save(VALID_INCOMING_BIRTHDAY));
    }

    @Test
    public void postBirthday_MissingName(){
        VALID_INCOMING_BIRTHDAY.setName(null);
        assertThrows(ValidationException.class, () -> eventService.save(VALID_INCOMING_BIRTHDAY));
    }

    @Test
    public void postBirthday_MissingType(){
        VALID_INCOMING_BIRTHDAY.setBirthdayType(null);
        assertThrows(ValidationException.class, () -> eventService.save(VALID_INCOMING_BIRTHDAY));
    }

    @Test
    public void postBirthday_ZeroHeadCount(){
        VALID_INCOMING_BIRTHDAY.setHeadcount(0);
        assertThrows(ValidationException.class, () -> eventService.save(VALID_INCOMING_BIRTHDAY));
    }


    @Test
    public void setupTrainers(){
        savedTrainers = postTrainers();
    }



    public List<Trainer> postTrainers(){
        List<Trainer> trainers = new LinkedList<>();
        for(int i = 0; i <= 100; i++){
            Trainer t = faker.fakeNewTrainerEntity();
            try {
                trainerService.save(t);
                trainers.add(t);
            }catch(ValidationException e){
                System.out.println("System Validation Error creating Test Data");
            }catch(ServiceException e){
                System.out.println("System Error Creating Test Data");
            }
        }
        return trainers;
    }

    public Trainer postTrainer(){
        Trainer trainer = faker.fakeNewTrainerEntity();
        List<String> birthdayTypes = new LinkedList<>();//Rocket", "Dryice", "Superhero", "Photography", "Painting"
        birthdayTypes.add("Rocket");
        birthdayTypes.add("Dryice");
        birthdayTypes.add("Superhero");
        birthdayTypes.add("Photography");
        birthdayTypes.add("Painting");
        trainer.setBirthdayTypes(birthdayTypes);
        try {
            trainerService.save(trainer);
        }catch(ValidationException e){
            System.out.println("System Validation Error Creating Test Trainer");
        }catch(ServiceException e){
            System.out.println("System Error Creating Test Trainer");
        }
        return trainer;
    }
}
