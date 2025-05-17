        package com.birthcertify.services;

        import com.birthcertify.utils.DatabaseConnection;
        import org.slf4j.Logger;
        import org.slf4j.LoggerFactory;

        import java.sql.*;
        import java.util.*;

        public class ApplicationDashboardService {

            private static final Logger logger = LoggerFactory.getLogger(ApplicationDashboardService.class);  // Logger initialization

            public DashboardStats getDashboardStats(String timeRange) throws SQLException {
                String query = """
                    SELECT 
                        (SELECT COUNT(*) FROM users WHERE role = 'REGISTRANT') AS total_registrants,
                        (SELECT COUNT(*) FROM birth_applications WHERE %s) AS total_applications,
                        (SELECT COUNT(*) FROM birth_applications WHERE status = 'APPROVED' AND %s) AS approved,
                        (SELECT COUNT(*) FROM birth_applications WHERE status = 'REJECTED' AND %s) AS rejected
                """;

                String timeFilter = getTimeFilter("birth_applications", timeRange);
                query = query.formatted(timeFilter, timeFilter, timeFilter);

                logger.info("Executing query to fetch dashboard stats with time range: {}", timeRange);  // Log the query execution

                try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(query)) {

                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        DashboardStats stats = new DashboardStats(
                            rs.getInt("total_registrants"),
                            rs.getInt("total_applications"),
                            rs.getInt("approved"),
                            rs.getInt("rejected")
                        );
                        logger.info("Dashboard stats fetched successfully: {}", stats);  // Log the fetched stats
                        return stats;
                    }
                } catch (SQLException e) {
                    logger.error("Error fetching dashboard stats", e);  // Log any error
                    throw e;  // Rethrow the exception to be handled elsewhere
                }

                return new DashboardStats(0, 0, 0, 0);
            }

            public List<CityStat> getApplicationDistributionByCity(String timeRange) throws SQLException {
                String query = """
                    SELECT city_of_birth, COUNT(*) AS count
                    FROM birth_applications
                    WHERE %s
                    GROUP BY city_of_birth
                    ORDER BY count DESC
                """;

                query = query.formatted(getTimeFilter("birth_applications", timeRange));

                logger.info("Executing query to fetch application distribution by city with time range: {}", timeRange);  // Log query execution

                List<CityStat> stats = new ArrayList<>();
                try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(query)) {

                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        stats.add(new CityStat(rs.getString("city_of_birth"), rs.getInt("count")));
                    }
                    logger.info("Fetched {} city stats", stats.size());  // Log the number of city stats fetched
                } catch (SQLException e) {
                    logger.error("Error fetching application distribution by city", e);  // Log error
                    throw e;
                }

                return stats;
            }

            public List<TopRequesterStat> getTopRequesters(int limit, String timeRange) throws SQLException {
                String query = """
                    SELECT u.first_name, u.last_name, COUNT(*) AS request_count
                    FROM birth_applications a
                    JOIN users u ON a.applicant_id = u.user_id
                    WHERE %s
                    GROUP BY u.user_id, u.first_name, u.last_name
                    ORDER BY request_count DESC
                    LIMIT ?
                """;

                query = query.formatted(getTimeFilter("a", timeRange));

                logger.info("Executing query to fetch top requesters with time range: {}", timeRange);  // Log query execution

                List<TopRequesterStat> stats = new ArrayList<>();
                try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(query)) {

                    stmt.setInt(1, limit);
                    ResultSet rs = stmt.executeQuery();

                    while (rs.next()) {
                        String name = rs.getString("first_name") + " " + rs.getString("last_name");
                        int count = rs.getInt("request_count");
                        stats.add(new TopRequesterStat(name, count));
                    }

                    logger.info("Fetched top {} requesters", stats.size());  // Log the number of requesters fetched
                } catch (SQLException e) {
                    logger.error("Error fetching top requesters", e);  // Log error
                    throw e;
                }

                return stats;
            }

            private String getTimeFilter(String tableAlias, String range) {
                return switch (range) {
                    case "Past Week" -> tableAlias + ".submitted_at >= NOW() - INTERVAL '7 days'";
                    case "Past Month" -> tableAlias + ".submitted_at >= NOW() - INTERVAL '1 month'";
                    case "Past Year" -> tableAlias + ".submitted_at >= NOW() - INTERVAL '1 year'";
                    default -> "TRUE"; // No filter for All Time
                };
            }

            // Data Transfer Classes
            public static class DashboardStats {
                public final int totalRegistrants;
                public final int totalApplications;
                public final int approved;
                public final int rejected;

                public DashboardStats(int totalRegistrants, int totalApplications, int approved, int rejected) {
                    this.totalRegistrants = totalRegistrants;
                    this.totalApplications = totalApplications;
                    this.approved = approved;
                    this.rejected = rejected;
                }

                @Override
                public String toString() {
                    return String.format("DashboardStats{totalRegistrants=%d, totalApplications=%d, approved=%d, rejected=%d}",
                            totalRegistrants, totalApplications, approved, rejected);
                }
            }

            public static class CityStat {
                public final String city;
                public final int count;

                public CityStat(String city, int count) {
                    this.city = city;
                    this.count = count;
                }

                @Override
                public String toString() {
                    return String.format("CityStat{city='%s', count=%d}", city, count);
                }
            }

            public static class TopRequesterStat {
                public final String fullName;
                public final int requestCount;

                public TopRequesterStat(String fullName, int requestCount) {
                    this.fullName = fullName;
                    this.requestCount = requestCount;
                }

                @Override
                public String toString() {
                    return String.format("TopRequesterStat{fullName='%s', requestCount=%d}", fullName, requestCount);
                }
            }
        }
