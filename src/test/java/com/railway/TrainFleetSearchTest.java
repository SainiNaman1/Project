package com.railway;

import com.railway.database.DatabaseConnection;
import com.railway.model.dto.TrainSearchResult;
import com.railway.service.TrainService;
import com.railway.service.impl.TrainServiceImpl;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class TrainFleetSearchTest {

    @Test
    @DisplayName("Verify Fleet Listing and Route Search Integration")
    void testFleetListingAndRouteSearch() throws Exception {
        DatabaseConnection db = DatabaseConnection.getInstance();
        assumeTrue(db.testConnection(), "Database connection is required for live fleet integration test");

        TrainService trainService = new TrainServiceImpl();
        LocalDate journeyDate = LocalDate.now().plusDays(2);

        // 1. All available trains should return the complete fleet (12 trains)
        List<TrainSearchResult> allTrains = trainService.getAllAvailableTrains(journeyDate);
        assertNotNull(allTrains);
        assertFalse(allTrains.isEmpty(), "Fleet list should not be empty");
        assertTrue(allTrains.size() >= 12, "Should contain at least 12 seeded trains");

        // Verify each train has valid route endpoints and estimated fares
        for (TrainSearchResult res : allTrains) {
            assertNotNull(res.getTrain());
            assertNotNull(res.getSourceStop());
            assertNotNull(res.getDestStop());
            assertTrue(res.getDistanceKm() > 0);
            assertFalse(res.getEstimatedFares().isEmpty());
        }

        // 2. Filtered search between NDLS (1) and CNB (4)
        List<TrainSearchResult> filteredTrains = trainService.searchTrains(1L, 4L, journeyDate);
        assertNotNull(filteredTrains);
        assertFalse(filteredTrains.isEmpty(), "Should find trains from NDLS to CNB");
        for (TrainSearchResult res : filteredTrains) {
            assertEqualsIgnoreCaseOrContains("NDLS", res.getSourceStop().getStation().getStationCode());
            assertEqualsIgnoreCaseOrContains("CNB", res.getDestStop().getStation().getStationCode());
        }
    }

    private void assertEqualsIgnoreCaseOrContains(String expected, String actual) {
        assertNotNull(actual);
        assertTrue(actual.toUpperCase().contains(expected.toUpperCase()));
    }
}
