package at.ac.tuwien.sepm.groupphase.backend.schedule;

import at.ac.tuwien.sepm.groupphase.backend.Entity.Customer;
import at.ac.tuwien.sepm.groupphase.backend.exceptions.BackendException;
import at.ac.tuwien.sepm.groupphase.backend.persistence.CustomerRepository;
import at.ac.tuwien.sepm.groupphase.backend.rest.EventEndpoint;
import at.ac.tuwien.sepm.groupphase.backend.rest.dto.EventDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@Component
public class RedactInactiveUsers {


    private static final Logger LOGGER = LoggerFactory.getLogger(RedactInactiveUsers.class);

    private final CustomerRepository customerRepository;
    private final EventEndpoint eventEndpoint;

    @Autowired
    public RedactInactiveUsers (CustomerRepository customerRepository, EventEndpoint eventEndpoint) {
        this.customerRepository = customerRepository;
        this.eventEndpoint = eventEndpoint;
    }


    //Cron needs to be changed for everyday at 23:00

    @Transactional
    public void redact() throws BackendException {
        LOGGER.info("Initiated redaction of inactive customers...");
        List<Customer> customers = customerRepository.findAll();
        List<EventDto> events = eventEndpoint.getAllEvents();
        List<EventDto> eventsNotOlderThan6Months = new LinkedList<>();

        LOGGER.info("Creating list of events not older than 6 months.");
        for(int i = 0; i < events.size(); i++){
            if(events.get(i).getRoomUses().get(0).getBegin().isAfter(LocalDateTime.now().minusMonths(6))) {
                eventsNotOlderThan6Months.add(events.get(i));
            }
        }
        LOGGER.info("Creating list of all customer ids that were active.");
        List<Long> activeIDs = new LinkedList<>();
        for(int i = 0; i < eventsNotOlderThan6Months.size(); i++){
            for(int j = 0; j < eventsNotOlderThan6Months.get(i).getCustomerDtos().size(); j++){
                if(!activeIDs.contains(eventsNotOlderThan6Months.get(i).getCustomerDtos().get(j).getId())){
                    activeIDs.add(eventsNotOlderThan6Months.get(i).getCustomerDtos().get(j).getId());
                }
            }
        }
        LOGGER.info("Removing all customers active in the last 6 months from customers list.");
        List<Customer> redactThese = new LinkedList<>();
        for(int i = 0; i < customers.size(); i++){
            if(!activeIDs.contains(customers.get(i).getId())){
                redactThese.add(customers.get(i));
            }
        }
        LOGGER.info("Redacting all inactive customers.");
        redactThese = changeValuesToRedact(redactThese);
        LOGGER.info("Updating the list of customers in repository.");
        try {
            for(int i = 0; i < redactThese.size(); i++){
                customerRepository.save(redactThese.get(i));
            }
            LOGGER.info("Censoring successful. Putin gibs me medal of honor now!");
        }catch(Exception e){
            LOGGER.error("Error while updating redacted customers", e);
        }

    }

    public List<Customer> changeValuesToRedact (List<Customer> redactThese) {
        for(int i = 0; i < redactThese.size(); i++){
            redactThese.get(i).setFirstName("redacted");
            redactThese.get(i).setLastName("redacted");
            redactThese.get(i).setEmail("redacted");
            redactThese.get(i).setPhone("redacted");
        }

        return redactThese;
    }
}
