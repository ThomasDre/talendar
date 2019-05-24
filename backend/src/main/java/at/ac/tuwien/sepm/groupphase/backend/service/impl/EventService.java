package at.ac.tuwien.sepm.groupphase.backend.service.impl;

import at.ac.tuwien.sepm.groupphase.backend.Entity.*;
import at.ac.tuwien.sepm.groupphase.backend.enums.EventType;
import at.ac.tuwien.sepm.groupphase.backend.exceptions.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exceptions.TimeNotAvailableException;
import at.ac.tuwien.sepm.groupphase.backend.exceptions.TrainerNotAvailableException;
import at.ac.tuwien.sepm.groupphase.backend.persistence.EventRepository;
import at.ac.tuwien.sepm.groupphase.backend.persistence.HolidayRepository;
import at.ac.tuwien.sepm.groupphase.backend.persistence.RoomUseRepository;
import at.ac.tuwien.sepm.groupphase.backend.persistence.TrainerRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.IEventService;
import at.ac.tuwien.sepm.groupphase.backend.service.exceptions.CancelationException;
import at.ac.tuwien.sepm.groupphase.backend.service.exceptions.EmailException;
import at.ac.tuwien.sepm.groupphase.backend.service.exceptions.ServiceException;
import at.ac.tuwien.sepm.groupphase.backend.service.exceptions.ValidationException;
import at.ac.tuwien.sepm.groupphase.backend.util.validator.Validator;
import at.ac.tuwien.sepm.groupphase.backend.util.validator.exceptions.InvalidEntityException;
import org.apache.tomcat.jni.Local;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.sql.rowset.serial.SerialException;
import javax.validation.Valid;
import javax.validation.Validation;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@Service
public class EventService implements IEventService {
    private final static Logger LOGGER = LoggerFactory.getLogger(EventService.class);
    private final EventRepository eventRepository;
    private final RoomUseRepository roomUseRepository;
    private final Validator validator;
    private final TrainerRepository trainerRepository;
    private final HolidayRepository holidayRepository;

    @Autowired
    public EventService(EventRepository eventRepository, Validator validator, RoomUseRepository roomUseRepository, TrainerRepository trainerRepository, HolidayRepository holidayRepository){
        this.eventRepository = eventRepository;
        this.validator = validator;
        this.roomUseRepository = roomUseRepository;
        this.trainerRepository = trainerRepository;
        this.holidayRepository = holidayRepository;
    }

    @Override
    public Event save (Event event) throws ValidationException, ServiceException {
        LOGGER.info("Prepare to save new Event");
        LocalDateTime now = LocalDateTime.now();
        event.setCreated(now);
        event.setUpdated(now);
        event.setDeleted(false);

        try {
            Thread.sleep(1);
        }
        catch(InterruptedException e) {
            e.printStackTrace();
        }

        switch(event.getEventType()) {
            case Birthday:
                  try {
                      validator.validateEvent(event);
                      event.setTrainer(findTrainerForBirthday(event.getRoomUses(), event.getBirthdayType()));
                      event = synchRoomUses(event);
                      for(Customer c: event.getCustomers()
                          ) {
                          sendCancelationMail(c.getEmail(), event);
                      }
                  }
                  catch(InvalidEntityException e) {
                      throw new ValidationException("Given Birthday is invalid: " + e.getMessage(), e);
                  }catch(TrainerNotAvailableException e){
                      throw new ServiceException("There are no Trainers available for this birthday " + e.getMessage(),e);
                  }catch(EmailException e){
                      throw new ValidationException("Something went wrong while attempting to send an email: " + e.getMessage(), e);
                  }

                  break;
            case Course:
                try {
                    validator.validateEvent(event);
                }
                catch(InvalidEntityException e) {
                    throw new ValidationException("Given Course is invalid: " + e.getMessage(), e);
                }
                break;
            case Rent:
                try {
                    validator.validateEvent(event);
                }
                catch(InvalidEntityException e) {
                    throw new ValidationException("Given Course is invalid: " + e.getMessage(), e);
                }
                break;
            case Consultation:
                //TODO
                break;
        }
        try{
            isAvailable(event.getRoomUses());
        }catch(TimeNotAvailableException e){
            throw new ValidationException("The event is attempting to be booked during a different event: " + e.getMessage(), e);
        }
        return eventRepository.save(event);
    }

    @Override
    public void deleteEvent(Long id){
        eventRepository.deleteThisEvent(id);
        roomUseRepository.deleteTheseRoomUses(id);
    }

    @Override
    public void cancelEvent(Long id) throws ValidationException {
        try{
            validateCancelation(id);
        }catch(CancelationException e){
            throw new ValidationException("The cancelation was ordered too late: " +  e.getMessage(), e);
        }
        eventRepository.deleteThisEvent(id);


    }

    public void validateCancelation(Long id) throws CancelationException {
        Event event = eventRepository.getOne(id);
        switch (event.getEventType()){
            case Birthday:
                for(RoomUse r: event.getRoomUses()
                    ) {
                    if(LocalDateTime.now().isAfter(r.getBegin().minusMonths(1))){
                        throw new CancelationException("Birthdays must be canceled 1 full month before the event");
                    }
                }
                break;
            case Consultation:
                for(RoomUse r: event.getRoomUses()
                    ) {
                    if(LocalDateTime.now().isAfter(r.getBegin().minusDays(1))){
                        throw new CancelationException("Consultations must be canceled 1 full day before the event");
                    }
                }
                break;
            case Rent:
                for(RoomUse r: event.getRoomUses()
                    ) {
                    if(LocalDateTime.now().isAfter(r.getBegin().minusDays(2))) {
                        throw new CancelationException("Rents must be canceled 2 full days before the event");
                    }
                }
                break;
            default:
                throw new CancelationException("Courses are not canceled this way");
        }
    }

    public Trainer findTrainerForBirthday(List<RoomUse> roomUses, String birthdayType) throws TrainerNotAvailableException{
        List<Trainer> appropriateTrainers = trainerRepository.findByBirthdayTypes(birthdayType);;
        Collections.shuffle(appropriateTrainers);
        for(Trainer t: appropriateTrainers
            ) {
            if(trainerAvailable(t, roomUses)){
                return t;
            }
        }
        throw new TrainerNotAvailableException("There are no trainers who can do a " + birthdayType + " birthday during the allotted time");
    }

    public Event synchRoomUses(Event event){
        for(RoomUse x: event.getRoomUses()
            ) {
            x.setEvent(event);
        }
        return event;
    }

    public boolean trainerAvailable(Trainer trainer, List<RoomUse> roomUses){
        List<RoomUse> trainersEvents = eventRepository.findByTrainer_IdAndRoomUses_BeginGreaterThanEqualAndDeletedIs(trainer.getId(), LocalDateTime.now(),false);
        List<Holiday> trainerHoliday = holidayRepository.findByTrainer_Id(trainer.getId());

        for(RoomUse x: roomUses
            ) {
            for(RoomUse db: trainersEvents
                ) {
                if(x.getBegin().isAfter(db.getBegin()) && x.getBegin().isBefore(db.getEnd()) || x.getEnd().isBefore(db.getEnd()) && x.getEnd().isAfter(db.getBegin()) || x.getBegin().isBefore(db.getBegin()) && x.getEnd().isAfter(db.getEnd())) {
                    return false;
                }
            }
            for(Holiday db: trainerHoliday
                ) {
                if(x.getBegin().isAfter(db.getHolidayStart()) && x.getBegin().isBefore(db.getHolidayEnd()) || x.getEnd().isBefore(db.getHolidayEnd()) && x.getEnd().isAfter(db.getHolidayStart()) || x.getBegin().isBefore(db.getHolidayStart()) && x.getEnd().isAfter(db.getHolidayEnd())) {
                    return false;
                }
            }
        }
        return true;
    }

    public void isAvailable(List<RoomUse> roomUseList) throws TimeNotAvailableException {
        LocalDateTime now = LocalDateTime.now();
        List<RoomUse> dbRooms = roomUseRepository.findByBeginGreaterThanEqualAndDeletedIs(now,false);
        for(RoomUse x: roomUseList
            ) {
            for(RoomUse db: roomUseList
                ) {
                if(x.getRoom() == db.getRoom()) {
                    if(x.getBegin().isAfter(db.getBegin()) && x.getBegin().isBefore(db.getEnd()) || x.getEnd().isBefore(db.getEnd()) && x.getEnd().isAfter(db.getBegin()) || x.getBegin().isBefore(db.getBegin()) && x.getEnd().isAfter(db.getEnd())) {
                        throw new TimeNotAvailableException("The Timeslot attempting to be booked is invalid. Attempted Booking: " + x.toString());
                    }
                }
            }
        }
    }

    public void sendCancelationMail(String email, Event event) throws EmailException {
        String to = email;
        String from = "testingsepmstuffqse25@outlook.com";
        String password = "This!is!a!password!";
        String host = "smtp-mail.outlook.com";
        Properties props = System.getProperties();
        props.put("mail.smtp.user", from);
        props.put("mail.smtp.pwd", password);
        props.put("mail.smtp.host", "smtp-mail.outlook.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.starttls.enable","true");
        props.put("mail.smtp.auth", "true");
        Session session = Session.getDefaultInstance(props);

        try{
            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(from));
            mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            mimeMessage.setSubject("Stornierungslink fur den Event am " + event.getRoomUses().get(0).getBegin());
            mimeMessage.setText(createCancelationMessage(event));
            Transport transport = session.getTransport("smtp");
            transport.connect(host, 587, from, password);
            transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
            transport.close();


        }catch(MessagingException e){
            throw new EmailException(" " + e.getMessage());
        }
    }
    private String createCancelationMessage(Event event){

        return "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam euismod convallis metus, ut ullamcorper mauris pharetra quis. Etiam ultricies, tortor at dictum varius, purus erat vulputate nisi, at efficitur ligula augue ac lectus. Integer ut accumsan urna. Fusce commodo odio libero, nec faucibus nunc dictum vel. Cras ut metus a velit hendrerit tincidunt sed sit amet magna. Vestibulum et lorem quis nunc fringilla pulvinar. In lectus lectus, sodales ac lacinia quis, aliquet ut nulla. Sed sem tellus, dapibus quis tristique non, facilisis vel quam. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Aliquam blandit ultrices elit, quis mattis quam bibendum ut. Nulla tempus felis laoreet, varius sapien in, ultrices libero. Aenean vel libero tincidunt, malesuada purus non, ornare nunc. Nunc mattis est maximus turpis bibendum aliquam.\n" +
            "\n" +
            "Donec pellentesque dolor eget ex lobortis, non condimentum enim egestas. Sed erat sapien, eleifend vitae neque eget, varius vulputate mauris. Sed urna ipsum, euismod et sapien eget, dignissim facilisis magna. Cras et velit enim. Nulla auctor porttitor dictum. Nam egestas neque nec diam vestibulum molestie. Etiam urna sapien, consequat eu posuere ut, eleifend maximus nibh. Suspendisse iaculis lectus ut vestibulum viverra. Aenean efficitur mi eget mattis lobortis. Donec id dapibus justo. Maecenas dapibus nisi ut elit laoreet suscipit. Aenean consequat porta neque quis tincidunt. Nulla vehicula ornare est et tincidunt. Maecenas gravida volutpat tellus ac dictum.\n" +
            "\n" +
            "Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Curabitur sodales pellentesque eleifend. In et ultricies enim. Suspendisse luctus sollicitudin imperdiet. Vestibulum vestibulum arcu at mollis fringilla. Ut venenatis ultricies lacus, sit amet tincidunt ipsum tristique id. Duis pulvinar pellentesque odio, quis ultrices nisi vestibulum et. Mauris mauris neque, mollis sodales pharetra sit amet, egestas sed ante. Proin eu nibh magna. Ut fringilla lacinia lorem semper tincidunt. Sed egestas lectus ut vulputate vestibulum. Nullam consequat rhoncus feugiat. Donec eget risus at leo efficitur euismod. Ut augue ex, lacinia vel magna sed, varius tristique nisl. Sed eget ante tincidunt, commodo orci quis, accumsan elit. Nullam mattis ante eleifend odio tristique, et mollis ante blandit.\n" +
            "\n" +
            "Fusce id vehicula nunc, eu venenatis massa. Praesent efficitur nisi nisl. Nulla id arcu in elit tincidunt cursus in et magna. Maecenas bibendum molestie risus eget congue. Nulla et efficitur odio. Sed euismod leo ac egestas pellentesque. Nam arcu mauris, congue quis elit nec, gravida viverra est.\n" +
            "\n" +
            "Sed placerat eros a pulvinar pulvinar. Etiam et odio nec tortor sagittis porttitor vitae sit amet metus. Quisque id nisl quis lacus tristique dignissim. Pellentesque quis dignissim sem. In ligula mi, iaculis in lorem ut, congue molestie libero. Curabitur sit amet leo ex. Vivamus finibus ex vel eleifend tristique. Nunc ornare nibh varius metus sodales mollis. Nam eu venenatis lacus. Mauris lobortis elit quis hendrerit gravida. Maecenas purus erat, volutpat ac sem sed, mattis fringilla ligula.";
    }
}
