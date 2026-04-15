import java.io.*;
import java.util.*;

// Booking class
class Booking implements Serializable {
    String bookingId;
    String roomType;
    String roomId;

    public Booking(String bookingId, String roomType, String roomId) {
        this.bookingId = bookingId;
        this.roomType = roomType;
        this.roomId = roomId;
    }

    @Override
    public String toString() {
        return bookingId + " -> " + roomType + " (" + roomId + ")";
    }
}

// Wrapper class for persistence
class SystemState implements Serializable {
    Map<String, Integer> inventory;
    Map<String, Booking> bookings;

    public SystemState(Map<String, Integer> inventory,
                       Map<String, Booking> bookings) {
        this.inventory = inventory;
        this.bookings = bookings;
    }
}

// Persistence Service
class PersistenceService {

    private static final String FILE_NAME = "system_state.ser";

    // Save state to file
    public static void save(SystemState state) {
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {

            oos.writeObject(state);
            System.out.println("System state saved successfully.");

        } catch (IOException e) {
            System.out.println("Error saving system state: " + e.getMessage());
        }
    }

    // Load state from file
    public static SystemState load() {
        try (ObjectInputStream ois =
                     new ObjectInputStream(new FileInputStream(FILE_NAME))) {

            SystemState state = (SystemState) ois.readObject();
            System.out.println("System state loaded successfully.");
            return state;

        } catch (FileNotFoundException e) {
            System.out.println("No previous state found. Starting fresh.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Corrupted state file. Starting fresh.");
        }
        return null;
    }
}

// Main system
public class BookMyStayApp {

    private static Map<String, Integer> inventory = new HashMap<>();
    private static Map<String, Booking> bookings = new HashMap<>();

    public static void main(String[] args) {

        // STEP 1: Load previous state (Recovery)
        SystemState loadedState = PersistenceService.load();

        if (loadedState != null) {
            inventory = loadedState.inventory;
            bookings = loadedState.bookings;
        } else {
            initializeFreshState();
        }

        // Display current state
        displayState();

        // Simulate new booking
        createBooking("B201", "Deluxe");

        // Display updated state
        displayState();

        // STEP 2: Save state before shutdown
        PersistenceService.save(new SystemState(inventory, bookings));

        System.out.println("\nSystem shutdown complete.");
    }

    // Initialize fresh system
    private static void initializeFreshState() {
        System.out.println("Initializing new system state...");

        inventory.put("Deluxe", 2);
    }

    // Booking logic
    private static void createBooking(String bookingId, String roomType) {

        int available = inventory.getOrDefault(roomType, 0);

        if (available <= 0) {
            System.out.println("No rooms available for " + roomType);
            return;
        }

        String roomId = "R" + (bookings.size() + 1);

        Booking booking = new Booking(bookingId, roomType, roomId);
        bookings.put(bookingId, booking);

        inventory.put(roomType, available - 1);

        System.out.println("Booking created: " + booking);
    }

    // Display system state
    private static void displayState() {
        System.out.println("\n--- CURRENT SYSTEM STATE ---");

        System.out.println("Inventory:");
        for (String type : inventory.keySet()) {
            System.out.println(type + " -> " + inventory.get(type));
        }

        System.out.println("\nBookings:");
        if (bookings.isEmpty()) {
            System.out.println("No bookings found.");
        } else {
            for (Booking b : bookings.values()) {
                System.out.println(b);
            }
        }
    }
}