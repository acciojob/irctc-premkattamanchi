package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.lang.StringBuilder;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library
        List<Station> stationList=trainEntryDto.getStationRoute();
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<stationList.size()-1;i++){
            sb.append(stationList.get(i));
            sb.append(",");
        }
        sb.append(stationList.get(stationList.size()-1));
        Train train=new Train();
        train.setRoute(sb.toString());
        train.setDepartureTime(trainEntryDto.getDepartureTime());
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());
        trainRepository.save(train);
        return train.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto)throws Exception{

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.
       Train train=trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();
       String stations[]=train.getRoute().split(",");
       HashMap<String,Integer> routeMap=new HashMap<>();
       int i=1;
       for(String s:stations){
           routeMap.put(s,i);
           i++;
       }
       List<Ticket> bookedTickets=train.getBookedTickets();
       int totalSeats=train.getNoOfSeats();
       String fromStation=seatAvailabilityEntryDto.getFromStation().toString();
       String toStation=seatAvailabilityEntryDto.getToStation().toString();
       if(routeMap.containsKey(fromStation) && routeMap.containsKey(toStation) && routeMap.get(fromStation)< routeMap.get(toStation)){
           for(Ticket ticket:bookedTickets){
                   if(routeMap.get(fromStation)<=routeMap.get(ticket.getToStation().toString()) && routeMap.get(toStation)>=routeMap.get(ticket.getFromStation().toString())){
                       totalSeats-=ticket.getPassengersList().size();
                   }
           }
       }
       return totalSeats;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.
        Train train=trainRepository.findById(trainId).get();
        String stations[]=train.getRoute().split(",");
        boolean isFound=false;
        for(String s:stations){
            if(s.equals(station.toString()))
                isFound=true;
        }
        if(isFound==false)
            throw new Exception("Train is not passing from this station");
        //station is present
        int ans=0;
        List<Ticket> ticketList=train.getBookedTickets();
        for(Ticket ticket:ticketList){
            if(station.equals(ticket.getFromStation()))
                ans+=ticket.getPassengersList().size();
        }
        return ans;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId) throws Exception{

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0
        Train train=trainRepository.findById(trainId).get();
        int age=0;
        List<Ticket> ticketList=train.getBookedTickets();
        for(Ticket ticket:ticketList){
            List<Passenger> passengerList=ticket.getPassengersList();
            for(Passenger passenger:passengerList){
                if(passenger.getAge()>age)
                    age= passenger.getAge();
            }
        }
        return age;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime) throws Exception{

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.
        List<Integer> ans=new ArrayList<>();
        List<Train> trainList=trainRepository.findAll();
        int startTimeInMinutes= startTime.getHour()*60+startTime.getMinute();
        int endTimeInMinutes=endTime.getHour()*60+endTime.getMinute();
        for(Train train:trainList){
            int hoursToReachStation=0;
            String route=train.getRoute();
            String stations[]=route.split(",");
            if(route.contains(station.toString())){
                for(int i=0;i<stations.length;i++){
                    if(stations[i].equals(station.toString())) {
                        hoursToReachStation=i;
                        break;
                    }
                }
            }
            LocalTime departureTime=train.getDepartureTime();
            int arrivalTimeInMin=departureTime.getHour()*60+departureTime.getMinute()+hoursToReachStation*60;
            if(arrivalTimeInMin>=startTimeInMinutes && arrivalTimeInMin<=endTimeInMinutes)
                ans.add(train.getTrainId());

        }
        return ans;
    }

}
