package at.ac.tuwien.sepm.groupphase.backend.datagenerator;

import at.ac.tuwien.sepm.groupphase.backend.Entity.*;
import at.ac.tuwien.sepm.groupphase.backend.exceptions.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.persistence.EventRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.*;
import at.ac.tuwien.sepm.groupphase.backend.service.exceptions.EmailException;
import at.ac.tuwien.sepm.groupphase.backend.service.exceptions.ServiceException;
import at.ac.tuwien.sepm.groupphase.backend.service.exceptions.ValidationException;
import at.ac.tuwien.sepm.groupphase.backend.service.impl.CustomerService;
import at.ac.tuwien.sepm.groupphase.backend.service.impl.EventService;
import at.ac.tuwien.sepm.groupphase.backend.service.impl.InfoMail;
import at.ac.tuwien.sepm.groupphase.backend.service.impl.TrainerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

@Component
public class TestDataGenerator implements ApplicationRunner {

    private Logger LOGGER = LoggerFactory.getLogger(TestDataGenerator.class);

    @Mock
    private InfoMail infoMail;


    private FakeData faker;

    @InjectMocks
    private EventService eventService;

    private IHolidayService holidayService;

    @InjectMocks
    private TrainerService trainerService;

    @InjectMocks
    private CustomerService customerService;

    @Autowired
    private EventRepository eventRepository;
    private ITagService tagService;
    // ENVIRONMENT SETUP DEFAULT
    private int NO_TRAINERS = 10;
    private int NO_COURSES = 50;
    private int SIMULATED_DAYS = 100;
    private int NO_RENTS    = 10;
    private int NO_BIRTHDAYS = 10;
    private int NO_CONSULTATION = 20;



    @Autowired
    public TestDataGenerator(FakeData fakeData, EventService eventService, IHolidayService holidayService, TrainerService trainerService, CustomerService customerService, ITagService tagService) {
        this.faker = fakeData;
        this.eventService = eventService;
        this.holidayService = holidayService;
        this.trainerService = trainerService;
        this.customerService = customerService;
        this.tagService = tagService;
    }


    @Override
    public void run(ApplicationArguments args) throws Exception {

        // check command line argument
        if (args.containsOption("testData")) {
            if (args.getOptionValues("testData").get(0).equals("yes")) {
                LOGGER.debug("Spring-Boot Started With Option For Initial Test Data Set To True! Start Simulation With Random Data");

                if (args.containsOption("trainers")) {
                    NO_TRAINERS = Integer.parseInt(args.getOptionValues("trainers").get(0));
                }
                if (args.containsOption("courses")) {
                    NO_COURSES = Integer.parseInt(args.getOptionValues("courses").get(0));
                }
                if (args.containsOption("days")) {
                    SIMULATED_DAYS = Integer.parseInt(args.getOptionValues("days").get(0));
                }
                if (args.containsOption("rents")) {
                    NO_RENTS = Integer.parseInt(args.getOptionValues("rents").get(0));
                }
                if (args.containsOption("bdays")) {
                    NO_BIRTHDAYS = Integer.parseInt(args.getOptionValues("bdays").get(0));
                }
                if (args.containsOption("consultations")) {
                    NO_CONSULTATION = Integer.parseInt(args.getOptionValues("consultations").get(0));
                }

                startSimulation();

                System.out.println(
                    "##################################\n" +
                    "#  Test Data Creation Completed  #\n" +
                    "##################################\n"
                );

                return;
            }
        }

        LOGGER.debug("Spring Boot Started Without Test Data Initialization");
    }

    private void startSimulation() throws Exception {
        List<Trainer> trainers = new LinkedList();
        String password = "e9a75486736a550af4fea861e2378305c4a555a05094dee1dca2f68afea49cc3a50e8de6ea131ea521311f4d6fb054a146e8282f8e35ff2e6368c1a62e909716";

        // create initial trainer set
        for (int i = 0; i < NO_TRAINERS; i++) {
            try {
                Trainer trainer = faker.fakeNewTrainerEntity();
                trainer.setPassword(password);
                Trainer saved = trainerService.save(trainer);

                trainers.add(saved);
            } catch(Exception e) {


            }
        }

        for (Tag tag: faker.getFakedTags()) {
            try {
                tagService.save(tag);
            } catch(Exception e) {
                // its a simulation, some add courses will fail, but that is okay
            }
        }

        for (int i = 0; i < NO_COURSES; i++) {
            Event course = faker.fakeNewCourseEntity(trainers, SIMULATED_DAYS);

            try {
                eventService.save(course);
            } catch(Exception e) {
                // its a simulation, some add courses will fail, but that is okay
            }
        }

        for (int i = 0; i < NO_RENTS; i++) {
            Event rent = faker.fakeNewRent(SIMULATED_DAYS);

            try {
                eventService.save(rent);
            } catch(Exception e) {
                // its a simulation, some add courses will fail, but that is okay
            }
        }

        for (int i = 0; i < NO_BIRTHDAYS; i++) {
            Event birthday = faker.fakeNewBirthdayEntity(trainers, SIMULATED_DAYS);

            try {
                eventService.save(birthday);
            } catch(Exception e) {
                // its a simulation, some add courses will fail, but that is okay
            }
        }

        for (int i = 0; i < NO_CONSULTATION; i++) {
            Event consultation = faker.fakeConsultationEntity(trainers, SIMULATED_DAYS);

            try {
                eventService.save(consultation);
            } catch(Exception e) {
                // its a simulation, some add courses will fail, but that is okay
            }
        }
    }


    public void fillDatabase(int count){
        for(int i = 0; i< 10; i++){
            try {
                Trainer trainer = faker.fakeNewTrainerEntity();
                trainerService.save(trainer);
            }catch(ServiceException | ValidationException e){
                //failure is fine
            }
        }
        List<Trainer> trainers = new LinkedList<>();
        try {
            trainers = trainerService.getAll();
        }catch(ServiceException e){
            //
        }
        for(int i = count/5; i>0; i--){
            try {
                Event event = faker.fakeNewCourseEntity(trainers, 2);
                System.out.println(event.toString2());
                eventService.save(event);
            }catch(ValidationException | ServiceException |EmailException | NotFoundException e){
                //
            }
        }
        List<Event> events = new LinkedList<>();
        try {
            events = eventService.getAllEvents();
        }catch(ServiceException e){
            //
        }
        for(int n = count; n > 0; n--){
                Customer customer = faker.fakeNewCustomerEntity();
                boolean found = false;
                for(Event e: events
                ) {
                    if(faker.randomInt(1,100) < 50){
                        customer.setId(null);
                    }else{
                        customer.setId((long)(faker.randomInt(1,100)));
                    }
                    if(customer.getId() == null) {
                        if(e.getMinAge() != null && e.getMinAge() != null) {
                            if(customer.getBirthOfChild().isAfter(
                                LocalDateTime.now().minusYears(
                                    e.getMaxAge())) &&
                               customer.getBirthOfChild().isBefore(
                                   LocalDateTime.now().minusYears(
                                       e.getMinAge()))) {

                                e.setCustomers(new LinkedHashSet<>());
                                e.getCustomers().add(customer);
                                try {
                                    eventService.updateCustomers(e);
                                    found = true;
                                }
                                catch(ValidationException | NotFoundException | ServiceException exc) {

                                }
                            }
                        }
                    }
                }
                if(found = false){
                    n++;
                }
        }
    }

    public void addOldCustomer(String email){
        for(int i = 0; i< 10; i++){
            try {
                Trainer trainer = faker.fakeNewTrainerEntity();
                trainerService.save(trainer);
            }catch(ServiceException | ValidationException e){
                //failure is fine
            }
        }
        List<Trainer> trainers = new LinkedList<>();
        try {
            trainers = trainerService.getAll();
        }catch(ServiceException e){
            //
        }
        Customer customer = faker.fakeNewCustomerEntity();
        customer.setEmail(email);
        customer.setId(null);
        Event event = faker.fakeNewBirthdayEntity(trainers, 1);
        event.setCustomers(new LinkedHashSet<>());
        event.getCustomers().add(customer);
        for(RoomUse ru: event.getRoomUses()
        ) {
            ru.setBegin(LocalDateTime.now().minusYears(1));
            ru.setEnd(LocalDateTime.now().minusYears(1).plusHours(1));
        }

        try {
           eventService.save(event);
        }catch(ValidationException | ServiceException |EmailException | NotFoundException e){
            //
        }
    }
}
