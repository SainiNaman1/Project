package com.railway.service.impl;

import com.railway.config.SessionContext;
import com.railway.dao.RailwayRuleDAO;
import com.railway.dao.RouteDAO;
import com.railway.dao.TrainDAO;
import com.railway.dao.impl.RailwayRuleDAOImpl;
import com.railway.dao.impl.RouteDAOImpl;
import com.railway.dao.impl.TrainDAOImpl;
import com.railway.exception.RailwayException;
import com.railway.exception.UnauthorizedException;
import com.railway.exception.ValidationException;
import com.railway.model.RailwayRule;
import com.railway.model.RouteStop;
import com.railway.model.Train;
import com.railway.service.AdminService;
import java.sql.SQLException;
import java.util.List;

public class AdminServiceImpl implements AdminService {

    private final TrainDAO trainDAO;
    private final RouteDAO routeDAO;
    private final RailwayRuleDAO ruleDAO;

    public AdminServiceImpl() {
        this.trainDAO = new TrainDAOImpl();
        this.routeDAO = new RouteDAOImpl();
        this.ruleDAO = new RailwayRuleDAOImpl();
    }

    public AdminServiceImpl(TrainDAO trainDAO, RouteDAO routeDAO, RailwayRuleDAO ruleDAO) {
        this.trainDAO = trainDAO;
        this.routeDAO = routeDAO;
        this.ruleDAO = ruleDAO;
    }

    private void enforceAdminRole() throws UnauthorizedException {
        if (!SessionContext.isAdmin()) {
            throw new UnauthorizedException("Administrative credentials required to perform this action.");
        }
    }

    @Override
    public Train addTrain(Train train, List<RouteStop> stops) throws RailwayException {
        enforceAdminRole();
        if (train == null || train.getTrainNumber() == null || train.getTrainNumber().isBlank()) {
            throw new ValidationException("Train number is required.");
        }
        if (train.getTrainName() == null || train.getTrainName().isBlank()) {
            throw new ValidationException("Train name is required.");
        }

        try {
            Train saved = trainDAO.save(train);
            if (stops != null && !stops.isEmpty()) {
                routeDAO.saveStops(saved.getId(), stops);
            }
            return saved;
        } catch (SQLException e) {
            throw new RailwayException("Failed to register train: " + e.getMessage(), e);
        }
    }

    @Override
    public void updatePlatform(long trainId, long stationId, int platformNumber) throws RailwayException {
        enforceAdminRole();
        if (platformNumber < 1 || platformNumber > 24) {
            throw new ValidationException("Platform number must be between 1 and 24.");
        }

        try {
            routeDAO.updatePlatform(trainId, stationId, platformNumber);
        } catch (SQLException e) {
            throw new RailwayException("Failed to update platform: " + e.getMessage(), e);
        }
    }

    @Override
    public void toggleTrainStatus(long trainId, boolean active) throws RailwayException {
        enforceAdminRole();
        try {
            trainDAO.updateStatus(trainId, active);
        } catch (SQLException e) {
            throw new RailwayException("Failed to toggle train operational status: " + e.getMessage(), e);
        }
    }

    @Override
    public RailwayRule addRule(RailwayRule rule) throws RailwayException {
        enforceAdminRole();
        if (rule == null || rule.getTitle() == null || rule.getTitle().isBlank()) {
            throw new ValidationException("Rule title is required.");
        }
        try {
            return ruleDAO.save(rule);
        } catch (SQLException e) {
            throw new RailwayException("Failed to post railway rule: " + e.getMessage(), e);
        }
    }
}
