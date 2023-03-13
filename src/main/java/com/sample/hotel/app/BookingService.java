package com.sample.hotel.app;

import com.sample.hotel.entity.Booking;
import com.sample.hotel.entity.Room;
import com.sample.hotel.entity.RoomReservation;
import io.jmix.core.DataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.Optional;

@Component
public class BookingService {
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private DataManager dataManager;

    /**
     * Check if given room is suitable for the booking.
     * 1) Check that sleeping places is enough to fit numberOfGuests.
     * 2) Check that there are no reservations for this room at the same range of dates.
     * Use javax.persistence.EntityManager and JPQL query for querying database.
     *
     * @param booking booking
     * @param room room
     * @return true if checks are passed successfully
     */
    public boolean isSuitable(Booking booking, Room room) {
        if (room.getSleepingPlaces() < booking.getNumberOfGuests()) {
            return false;
        }

        Optional<RoomReservation> roomReservation = entityManager.createQuery(
                        "select e from RoomReservation e where e.room = :room", RoomReservation.class)
                .setParameter("room", room)
                .getResultList().stream().findFirst();
        if (roomReservation.isPresent()) {
            Booking earlyReserved = roomReservation.get().getBooking();
            LocalDate reservedArrivalDate = earlyReserved.getArrivalDate();
            LocalDate reservedDepartureDate = earlyReserved.getDepartureDate();
            if (!(reservedDepartureDate.isBefore(booking.getArrivalDate()) || booking.getDepartureDate().isBefore(reservedArrivalDate))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check that room is suitable for the booking, and create a reservation for this room.
     * @param room room to reserve
     * @param booking hotel booking
     * Wrap operation into a transaction (declarative or manual).
     *
     * @return created reservation object, or null if room is not suitable
     */
    @Transactional
    public RoomReservation reserveRoom(Booking booking, Room room) {
        if (!isSuitable(booking, room)) {
            return null;
        }

        RoomReservation roomReservation = dataManager.create(RoomReservation.class);
        roomReservation.setBooking(booking);
        roomReservation.setRoom(room);

        return dataManager.save(roomReservation);
    }
}