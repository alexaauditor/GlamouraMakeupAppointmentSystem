
package com.scc.ui;

import com.scc.dao.AppointmentDao;
import com.scc.dao.ArtistAvailabilityDao;
import com.scc.dao.ServiceDao;
import com.scc.dao.UserDao;
import com.scc.model.ArtistAvailability;
import com.scc.util.InputValidator;

import java.util.List;
import java.util.Scanner;

public class MakeupArtistMenu {

    private final Scanner sc;
    private final UserDao userDao;
    private final String username;

    private final ArtistAvailabilityDao availabilityDao =
            new ArtistAvailabilityDao();

    private static final String[] DAYS = {
            "MONDAY", "TUESDAY", "WEDNESDAY",
            "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"
    };

    public MakeupArtistMenu(Scanner sc, UserDao userDao, String username) {
        this.sc = sc;
        this.userDao = userDao;
        this.username = username;
    }

    public void show() {
        boolean running = true;
        AppointmentDao appointmentDao = new AppointmentDao();
        ServiceDao serviceDao = new ServiceDao();

        while (running) {
            System.out.println("\n================================");
            System.out.println("   GLAMOURA MAKEUP ARTIST");
            System.out.println("================================");
            System.out.println("Welcome, " + username + "!");
            System.out.println("1. View My Appointments");
            System.out.println("2. View Services");
            System.out.println("3. Manage My Availability");
            System.out.println("4. View My Profile");
            System.out.println("5. Logout");
            System.out.print("Choose an option: ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    int artistId = userDao.getUserIdByUsername(username);

                    if (artistId != -1) {
                        appointmentDao.viewAssignedAppointments(artistId);

                        if (readYesNo(
                                "\nWould you like to update an appointment? (y/n): ")) {

                            int apptId = InputValidator.readInt(
                                    sc,
                                    "Enter Appointment ID: ",
                                    1,
                                    Integer.MAX_VALUE
                            );

                            System.out.print(
                                    "Enter new status (ACCEPTED/REJECTED): "
                            );
                            String status = sc.nextLine().trim();

                            appointmentDao.updateAppointmentStatus(
                                    apptId, artistId, status
                            );
                        }

                        if (readYesNo(
                                "\nWould you like to view customer information? (y/n): ")) {

                            boolean hasCustomers =
                                    userDao.viewArtistCustomers(artistId);

                            if (hasCustomers) {
                                int custId = InputValidator.readInt(
                                        sc,
                                        "Enter Customer User ID: ",
                                        1,
                                        Integer.MAX_VALUE
                                );

                                userDao.viewArtistCustomerInfo(custId, artistId);
                            }
                        }
                    }
                    break;

                case "2":
                    serviceDao.viewServices();
                    break;

                case "3":
                    manageAvailability();
                    break;

                case "4":
                    artistProfileMenu();
                    break;

                case "5":
                    System.out.println("Logging out...");
                    running = false;
                    break;

                default:
                    System.out.println("Invalid option. Please choose 1-5.");
            }
        }
    }

    // Validates Y/N inputs and keeps asking until a valid answer is entered.
    private boolean readYesNo(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();

            if (input.equalsIgnoreCase("y")) {
                return true;
            }

            if (input.equalsIgnoreCase("n")) {
                return false;
            }

            System.out.println("Invalid input. Please enter Y or N.");
        }
    }

    private void manageAvailability() {
        int artistId = userDao.getUserIdByUsername(username);

        if (artistId == -1) {
            System.out.println("Unable to identify your artist account.");
            return;
        }

        availabilityDao.initializeSchedule(artistId);

        boolean running = true;

        while (running) {
            System.out.println("\n================================");
            System.out.println("     MY WEEKLY AVAILABILITY");
            System.out.println("================================");
            System.out.println("1. View My Weekly Schedule");
            System.out.println("2. Set Working Day & Hours");
            System.out.println("3. Set Day Off");
            System.out.println("4. Back");
            System.out.print("Choose an option: ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    viewWeeklySchedule(artistId);
                    break;

                case "2":
                    setWorkingHours(artistId);
                    break;

                case "3":
                    setDayOff(artistId);
                    break;

                case "4":
                    running = false;
                    break;

                default:
                    System.out.println("Invalid option. Please choose 1-4.");
            }
        }
    }

    private void viewWeeklySchedule(int artistId) {
        List<ArtistAvailability> schedule =
                availabilityDao.getSchedule(artistId);

        System.out.println("\n========== MY WEEKLY SCHEDULE ==========");

        if (schedule.isEmpty()) {
            System.out.println("No schedule found.");
            return;
        }

        for (ArtistAvailability day : schedule) {
            System.out.print(day.dayOfWeek() + " : ");

            if (day.available()) {
                System.out.println(
                        day.startTime() + " - " + day.endTime()
                );
            } else {
                System.out.println("DAY OFF");
            }
        }

        System.out.println("====================================");
    }

    private void setWorkingHours(int artistId) {
        System.out.println("\n========== SET WORKING HOURS ==========");

        displayDays();

        int dayNumber = InputValidator.readInt(
                sc, "Select day (1-7): ", 1, 7
        );

        String selectedDay = DAYS[dayNumber - 1];

        String startTime = readValidTime(
                "Enter start time (HH:MM, 24-hour format): "
        );

        String endTime = readValidTime(
                "Enter end time (HH:MM, 24-hour format): "
        );

        while (startTime.compareTo(endTime) >= 0) {
            System.out.println(
                    "End time must be later than start time."
            );

            endTime = readValidTime(
                    "Enter a valid end time (HH:MM): "
            );
        }

        boolean saved = availabilityDao.saveSchedule(
                artistId,
                selectedDay,
                startTime,
                endTime,
                true
        );

        if (saved) {
            System.out.println(
                    selectedDay + " working hours saved successfully!"
            );
        } else {
            System.out.println("Failed to save working hours.");
        }
    }

    private void setDayOff(int artistId) {
        System.out.println("\n========== SET DAY OFF ==========");

        displayDays();

        int dayNumber = InputValidator.readInt(
                sc, "Select day to mark as OFF (1-7): ", 1, 7
        );

        String selectedDay = DAYS[dayNumber - 1];

        boolean saved = availabilityDao.saveSchedule(
                artistId,
                selectedDay,
                null,
                null,
                false
        );

        if (saved) {
            System.out.println(selectedDay + " marked as DAY OFF.");
        } else {
            System.out.println("Failed to update day off.");
        }
    }

    private String readValidTime(String prompt) {
        while (true) {
            System.out.print(prompt);
            String time = sc.nextLine().trim();

            if (isValidTime(time)) {
                return time;
            }

            System.out.println(
                    "Invalid time. Please use HH:MM (example: 09:00 or 17:30)."
            );
        }
    }

    private void displayDays() {
        System.out.println("\nAvailable Days:");

        for (int i = 0; i < DAYS.length; i++) {
            System.out.println((i + 1) + ". " + DAYS[i]);
        }
    }

    private boolean isValidTime(String time) {
        if (time == null || !time.matches("\\d{2}:\\d{2}")) {
            return false;
        }

        String[] parts = time.split(":");

        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        return hour >= 0 && hour <= 23
                && minute >= 0 && minute <= 59;
    }

    private void artistProfileMenu() {
        int artistId = userDao.getUserIdByUsername(username);

        if (artistId == -1) {
            System.out.println("Unable to identify your artist account.");
            return;
        }

        boolean running = true;
        AppointmentDao appointmentDao = new AppointmentDao();

        while (running) {
            System.out.println("\n================================");
            System.out.println("       MY ARTIST PROFILE");
            System.out.println("================================");
            System.out.println("1. View Profile & Statistics");
            System.out.println("2. View Customer History");
            System.out.println("3. Back");
            System.out.print("Choose an option: ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    appointmentDao.viewArtistProfile(artistId);
                    break;

                case "2":
                    appointmentDao.viewArtistCustomerHistory(artistId);
                    break;

                case "3":
                    running = false;
                    break;

                default:
                    System.out.println("Invalid option. Please choose 1-3.");
            }
        }
    }
}
