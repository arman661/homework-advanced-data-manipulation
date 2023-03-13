package com.sample.hotel.listener;

import com.sample.hotel.entity.Booking;
import com.sample.hotel.entity.BookingStatus;
import io.jmix.core.DataManager;
import io.jmix.core.event.EntityChangedEvent;
import io.jmix.core.event.EntitySavingEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
public class BookingEventListener {

    @Autowired
    private DataManager dataManager;

    @EventListener
    public void onBookingSaving(EntitySavingEvent<Booking> event) {
        LocalDate arrivalDate = event.getEntity().getArrivalDate();
        Integer nightsOfStay = event.getEntity().getNightsOfStay();
        event.getEntity().setDepartureDate(arrivalDate.plusDays(nightsOfStay));
    }

    @EventListener
    @Transactional
    public void onBookingChangedBeforeCommit(EntityChangedEvent<Booking> event) {
        Booking booking = dataManager.load(event.getEntityId()).one();

        if (event.getType() == EntityChangedEvent.Type.UPDATED && booking.getRoomReservation() != null
                && booking.getStatus().equals(BookingStatus.CANCELLED) && event.getChanges().isChanged("status")) {
            dataManager.remove(booking.getRoomReservation());
        }
    }
}