package com.railway.config;

import com.railway.audit.AuditLogDAO;
import com.railway.audit.AuditLogDAOImpl;
import com.railway.audit.AuditService;
import com.railway.audit.AuditServiceImpl;
import com.railway.dao.RailwayRuleDAO;
import com.railway.dao.ReservationDAO;
import com.railway.dao.RouteDAO;
import com.railway.dao.SeatDAO;
import com.railway.dao.StationDAO;
import com.railway.dao.TrainDAO;
import com.railway.dao.UserDAO;
import com.railway.dao.impl.RailwayRuleDAOImpl;
import com.railway.dao.impl.ReservationDAOImpl;
import com.railway.dao.impl.RouteDAOImpl;
import com.railway.dao.impl.SeatDAOImpl;
import com.railway.dao.impl.StationDAOImpl;
import com.railway.dao.impl.TrainDAOImpl;
import com.railway.dao.impl.UserDAOImpl;
import com.railway.service.AdminService;
import com.railway.service.AuthService;
import com.railway.service.FareCalculationService;
import com.railway.service.ReservationService;
import com.railway.service.TrainService;
import com.railway.service.impl.AdminServiceImpl;
import com.railway.service.impl.AuthServiceImpl;
import com.railway.service.impl.FareCalculationServiceImpl;
import com.railway.service.impl.ReservationServiceImpl;
import com.railway.service.impl.TrainServiceImpl;

public final class ApplicationContext {

    private static volatile ApplicationContext instance;

    private final UserDAO userDAO;
    private final StationDAO stationDAO;
    private final TrainDAO trainDAO;
    private final RouteDAO routeDAO;
    private final SeatDAO seatDAO;
    private final ReservationDAO reservationDAO;
    private final RailwayRuleDAO ruleDAO;
    private final AuditLogDAO auditLogDAO;

    private final AuthService authService;
    private final TrainService trainService;
    private final FareCalculationService fareCalculationService;
    private final ReservationService reservationService;
    private final AuditService auditService;
    private final AdminService adminService;

    private ApplicationContext() {

        this.userDAO = new UserDAOImpl();
        this.stationDAO = new StationDAOImpl();
        this.trainDAO = new TrainDAOImpl();
        this.routeDAO = new RouteDAOImpl();
        this.seatDAO = new SeatDAOImpl();
        this.reservationDAO = new ReservationDAOImpl();
        this.ruleDAO = new RailwayRuleDAOImpl();
        this.auditLogDAO = new AuditLogDAOImpl();

        this.authService = new AuthServiceImpl(this.userDAO);
        this.trainService = new TrainServiceImpl(this.trainDAO, this.routeDAO, this.seatDAO, this.stationDAO, this.ruleDAO);
        this.fareCalculationService = new FareCalculationServiceImpl();
        this.reservationService = new ReservationServiceImpl(this.reservationDAO, this.seatDAO, this.trainDAO, this.routeDAO, this.stationDAO, this.fareCalculationService);
        this.auditService = new AuditServiceImpl(this.auditLogDAO);
        this.adminService = new AdminServiceImpl(this.trainDAO, this.routeDAO, this.ruleDAO, this.auditService);
    }

    public static ApplicationContext getInstance() {
        if (instance == null) {
            synchronized (ApplicationContext.class) {
                if (instance == null) {
                    instance = new ApplicationContext();
                }
            }
        }
        return instance;
    }

    public UserDAO getUserDAO() { return userDAO; }
    public StationDAO getStationDAO() { return stationDAO; }
    public TrainDAO getTrainDAO() { return trainDAO; }
    public RouteDAO getRouteDAO() { return routeDAO; }
    public SeatDAO getSeatDAO() { return seatDAO; }
    public ReservationDAO getReservationDAO() { return reservationDAO; }
    public RailwayRuleDAO getRuleDAO() { return ruleDAO; }
    public AuditLogDAO getAuditLogDAO() { return auditLogDAO; }

    public AuthService getAuthService() { return authService; }
    public TrainService getTrainService() { return trainService; }
    public FareCalculationService getFareCalculationService() { return fareCalculationService; }
    public ReservationService getReservationService() { return reservationService; }
    public AuditService getAuditService() { return auditService; }
    public AdminService getAdminService() { return adminService; }
}
