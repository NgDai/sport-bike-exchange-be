package com.bicycle.marketplace.repository;

import com.bicycle.marketplace.entities.EventBicycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IEventBicycleRepository extends JpaRepository<EventBicycle, Integer> {
    boolean existsByListing_ListingId(int listingId);

    boolean existsByEvent_EventId(int eventId);

    void deleteByEvent_EventId(int eventId);

    boolean existsByBicycle_BikeId(int bikeId);
}
