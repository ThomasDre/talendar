package at.ac.tuwien.sepm.groupphase.backend.tests.unit.service;

import at.ac.tuwien.sepm.groupphase.backend.Entity.Customer;
import at.ac.tuwien.sepm.groupphase.backend.rest.dto.CustomerDto;
import at.ac.tuwien.sepm.groupphase.backend.rest.dto.EventDto;
import at.ac.tuwien.sepm.groupphase.backend.schedule.ParticipantsList;
import at.ac.tuwien.sepm.groupphase.backend.testDataCreation.FakeData;
import at.ac.tuwien.sepm.groupphase.backend.util.mapper.CustomerMapper;
import at.ac.tuwien.sepm.groupphase.backend.util.mapper.CustomerMapperImpl;
import at.ac.tuwien.sepm.groupphase.backend.util.mapper.EventMapper;
import at.ac.tuwien.sepm.groupphase.backend.util.mapper.EventMapperImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ParticipantsListTest {

    @Autowired
    ParticipantsList participantsList;

    @Autowired
    private static FakeData faker = new FakeData();

    @Test
    public void createCustomer_thenCreateBill() {
        Customer newCustomer = faker.fakeNewCustomerEntity();
        System.out.println(newCustomer.toString());
        CustomerMapper mapper = new CustomerMapperImpl();
        CustomerDto newCustomerDto = mapper.entityToCustomerDto(newCustomer);

        EventMapper mapper2 = new EventMapperImpl();
        EventDto newEventDto = mapper2.entityToEventDto(faker.fakeNewCourseEntity());
        participantsList.createBill(newCustomerDto, newEventDto);
    }
}
