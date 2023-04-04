package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db
        Train train=trainRepository.findById(bookTicketEntryDto.getTrainId()).get();
        List<Ticket> bookedTickets=train.getBookedTickets();
        int availableTickets=train.getNoOfSeats();
        for(Ticket ticket:bookedTickets){
            availableTickets-=ticket.getPassengersList().size();
        }
        if(availableTickets < bookTicketEntryDto.getNoOfSeats())
            throw new Exception("Less tickets are available");
        String stations[]=train.getRoute().split(",");
        HashMap<String,Integer> routeMap=new HashMap<>();
        int i=1;
        for(String s:stations){
            routeMap.put(s,i);
            i++;
        }
        String fromStation=bookTicketEntryDto.getFromStation().toString();
        String toStation=bookTicketEntryDto.getToStation().toString();
        if(!routeMap.containsKey(fromStation) || !routeMap.containsKey(toStation))
            throw new Exception("Invalid stations");
        // add attributes
        Ticket ticket=new Ticket();
        List<Passenger> passengerList=new ArrayList<>();
        List<Integer> passengerIdList=bookTicketEntryDto.getPassengerIds();
        for(Integer in:passengerIdList){
            Passenger passenger=passengerRepository.findById(in).get();
            passengerList.add(passenger);
        }
        ticket.setPassengersList(passengerList);
        //finding fare
        int fare=(routeMap.get(toStation)- routeMap.get(fromStation))*300;
        ticket.setTotalFare(fare);
        ticket.setTrain(train);
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());

        ticketRepository.save(ticket);
        Passenger passenger=passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get();
        passenger.getBookedTickets().add(ticket);
        passengerRepository.save(passenger);
        train.getBookedTickets().add(ticket);
        trainRepository.save(train);

        return ticket.getTicketId();

    }
}
