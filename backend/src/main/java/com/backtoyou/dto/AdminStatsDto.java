package com.backtoyou.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
    "totalUsers",
    "activeUsers",
    "inactiveUsers",
    "students",
    "admins",
    "total_users",
    "active_users",
    "inactive_users",
    "students_count",
    "admins_count",
    "totalItems",
    "totalLost",
    "totalFound",
    "totalResolved",
    "total_items",
    "total_lost",
    "total_found",
    "total_resolved"
})
public class AdminStatsDto {

    @JsonProperty("totalUsers")
    private long totalUsers;

    @JsonProperty("activeUsers")
    private long activeUsers;

    @JsonProperty("inactiveUsers")
    private long inactiveUsers;

    @JsonProperty("students")
    private long students;

    @JsonProperty("admins")
    private long admins;

    @JsonProperty("totalItems")
    private long totalItems;

    @JsonProperty("totalLost")
    private long totalLost;

    @JsonProperty("totalFound")
    private long totalFound;

    @JsonProperty("totalResolved")
    private long totalResolved;

    public AdminStatsDto() {
    }

    public AdminStatsDto(long totalUsers, long activeUsers, long inactiveUsers, long students,
                         long admins, long totalItems, long totalLost, long totalFound, long totalResolved) {
        this.totalUsers = totalUsers;
        this.activeUsers = activeUsers;
        this.inactiveUsers = inactiveUsers;
        this.students = students;
        this.admins = admins;
        this.totalItems = totalItems;
        this.totalLost = totalLost;
        this.totalFound = totalFound;
        this.totalResolved = totalResolved;
    }

    // Required camelCase getters and setters
    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public long getInactiveUsers() {
        return inactiveUsers;
    }

    public void setInactiveUsers(long inactiveUsers) {
        this.inactiveUsers = inactiveUsers;
    }

    public long getStudents() {
        return students;
    }

    public void setStudents(long students) {
        this.students = students;
    }

    public long getAdmins() {
        return admins;
    }

    public void setAdmins(long admins) {
        this.admins = admins;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }

    public long getTotalLost() {
        return totalLost;
    }

    public void setTotalLost(long totalLost) {
        this.totalLost = totalLost;
    }

    public long getTotalFound() {
        return totalFound;
    }

    public void setTotalFound(long totalFound) {
        this.totalFound = totalFound;
    }

    public long getTotalResolved() {
        return totalResolved;
    }

    public void setTotalResolved(long totalResolved) {
        this.totalResolved = totalResolved;
    }

    // Backward-compatibility snake_case JSON aliases
    @JsonProperty("total_users")
    public long getTotalUsersSnake() {
        return totalUsers;
    }

    @JsonProperty("active_users")
    public long getActiveUsersSnake() {
        return activeUsers;
    }

    @JsonProperty("inactive_users")
    public long getInactiveUsersSnake() {
        return inactiveUsers;
    }

    @JsonProperty("students_count")
    public long getStudentsCountSnake() {
        return students;
    }

    @JsonProperty("admins_count")
    public long getAdminsCountSnake() {
        return admins;
    }

    @JsonProperty("total_items")
    public long getTotalItemsSnake() {
        return totalItems;
    }

    @JsonProperty("total_lost")
    public long getTotalLostSnake() {
        return totalLost;
    }

    @JsonProperty("total_found")
    public long getTotalFoundSnake() {
        return totalFound;
    }

    @JsonProperty("total_resolved")
    public long getTotalResolvedSnake() {
        return totalResolved;
    }

    // Helper compatibility getters for tests/code
    @JsonIgnore
    public long getStudentsCount() {
        return students;
    }

    @JsonIgnore
    public long getAdminsCount() {
        return admins;
    }
}
