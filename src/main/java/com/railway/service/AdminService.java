package com.railway.service;

import com.railway.exception.RailwayException;
import com.railway.model.RailwayRule;
import com.railway.model.RouteStop;
import com.railway.model.Train;
import java.util.List;

/**
 * Service interface governing high-privilege administrative operations.
 */
public interface AdminService {
    Train addTrain(Train train, List<RouteStop> stops) throws RailwayException;
    void updatePlatform(long trainId, long stationId, int platformNumber) throws RailwayException;
    void toggleTrainStatus(long trainId, boolean active) throws RailwayException;
    RailwayRule addRule(RailwayRule rule) throws RailwayException;
}
