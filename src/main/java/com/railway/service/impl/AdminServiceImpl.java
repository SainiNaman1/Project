package com.railway.service.impl;

import com.railway.audit.AuditService;
import com.railway.audit.AuditServiceImpl;
import com.railway.dao.RailwayRuleDAO;
import com.railway.dao.RouteDAO;
import com.railway.dao.TrainDAO;
import com.railway.dao.impl.RailwayRuleDAOImpl;
import com.railway.dao.impl.RouteDAOImpl;
import com.railway.dao.impl.TrainDAOImpl;
import com.railway.exception.RailwayException;
import com.railway.exception.ValidationException;
import com.railway.model.RailwayRule;
import com.railway.model.RouteStop;
import com.railway.model.Train;
import com.railway.model.User;
import com.railway.security.AuthorizationService;
import com.railway.service.AdminService;
import java.sql.SQLException;
import java.util.List;

public class AdminServiceImpl implements AdminService {

    private final TrainDAO trainDAO;
    private final RouteDAO routeDAO;
    private final RailwayRuleDAO ruleDAO;
    private final AuditService auditService;

    public AdminServiceImpl() {
        this(new TrainDAOImpl(), new RouteDAOImpl(), new RailwayRuleDAOImpl(), new AuditServiceImpl());
    }

    public AdminServiceImpl(TrainDAO trainDAO, RouteDAO routeDAO, RailwayRuleDAO ruleDAO) {
        this(trainDAO, routeDAO, ruleDAO, new AuditServiceImpl());
    }

    public AdminServiceImpl(TrainDAO trainDAO, RouteDAO routeDAO, RailwayRuleDAO ruleDAO, AuditService auditService) {
        this.trainDAO = trainDAO;
        this.routeDAO = routeDAO;
        this.ruleDAO = ruleDAO;
        this.auditService = auditService != null ? auditService : new AuditServiceImpl();
    }

    @Override
    public Train addTrain(Train train, List<RouteStop> stops) throws RailwayException {
        User admin = AuthorizationService.requireAdmin();
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
            auditService.recordAction(admin.getId(), "ADD_TRAIN", "TRAIN", saved.getTrainNumber(), "Registered new train: " + saved.getTrainName());
            return saved;
        } catch (SQLException e) {
            throw new RailwayException("Failed to register train: " + e.getMessage(), e);
        }
    }

    @Override
    public void updatePlatform(long trainId, long stationId, int platformNumber) throws RailwayException {
        User admin = AuthorizationService.requireAdmin();
        if (platformNumber < 1 || platformNumber > 24) {
            throw new ValidationException("Platform number must be between 1 and 24.");
        }

        try {
            routeDAO.updatePlatform(trainId, stationId, platformNumber);
            auditService.recordAction(admin.getId(), "UPDATE_PLATFORM", "ROUTE_STOP",
                    "Train:" + trainId + "/Stn:" + stationId,
                    "Reassigned scheduled platform to Platform " + platformNumber);
        } catch (SQLException e) {
            throw new RailwayException("Failed to update platform: " + e.getMessage(), e);
        }
    }

    @Override
    public void toggleTrainStatus(long trainId, boolean active) throws RailwayException {
        User admin = AuthorizationService.requireAdmin();
        try {
            trainDAO.updateStatus(trainId, active);
            auditService.recordAction(admin.getId(), "TOGGLE_TRAIN_STATUS", "TRAIN",
                    String.valueOf(trainId),
                    "Train operational status set to: " + (active ? "ACTIVE" : "INACTIVE"));
        } catch (SQLException e) {
            throw new RailwayException("Failed to toggle train operational status: " + e.getMessage(), e);
        }
    }

    @Override
    public RailwayRule addRule(RailwayRule rule) throws RailwayException {
        User admin = AuthorizationService.requireAdmin();
        if (rule == null || rule.getTitle() == null || rule.getTitle().isBlank()) {
            throw new ValidationException("Rule title is required.");
        }
        try {
            RailwayRule saved = ruleDAO.save(rule);
            auditService.recordAction(admin.getId(), "ADD_RULE", "RULE",
                    String.valueOf(saved.getId()),
                    "Posted guideline: " + saved.getTitle());
            return saved;
        } catch (SQLException e) {
            throw new RailwayException("Failed to post railway rule: " + e.getMessage(), e);
        }
    }
}
