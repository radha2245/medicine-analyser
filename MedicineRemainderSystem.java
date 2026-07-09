import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MedicineRemainderSystem {
    private static final String DATA_FILE = "medicine_data.ser";
    private static final Scanner scanner = new Scanner(System.in);
    private static List<Patient> patients = new ArrayList<>();

    public static void main(String[] args) {
        loadData();
        mainMenu();
        saveData();
    }

    private static void mainMenu() {
        while (true) {
            System.out.println("\n======================================");
            System.out.println("     MEDICINE REMINDER SYSTEM");
            System.out.println("======================================");
            System.out.println("1. Add Patient");
            System.out.println("2. View Patients");
            System.out.println("3. Add Medicine to Patient");
            System.out.println("4. View Reminder List");
            System.out.println("5. Mark Dose as Taken");
            System.out.println("6. Show Missed-Dose Report");
            System.out.println("7. View Patient Medicines");
            System.out.println("8. Save Data");
            System.out.println("0. Exit");
            System.out.print("Enter choice: ");

            int choice = readInt();

            switch (choice) {
                case 1 -> addPatient();
                case 2 -> viewPatients();
                case 3 -> addMedicineToPatient();
                case 4 -> viewReminderList();
                case 5 -> markDoseTaken();
                case 6 -> showMissedDoseReport();
                case 7 -> viewPatientMedicines();
                case 8 -> {
                    saveData();
                    System.out.println("Data saved successfully.");
                }
                case 0 -> {
                    System.out.println("Exiting... Data saved.");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void addPatient() {
        System.out.print("Enter patient ID: ");
        String id = scanner.nextLine().trim();

        if (findPatientById(id) != null) {
            System.out.println("Patient ID already exists.");
            return;
        }

        System.out.print("Enter patient name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter age: ");
        int age = readInt();

        System.out.print("Enter gender: ");
        String gender = scanner.nextLine().trim();

        patients.add(new Patient(id, name, age, gender));
        System.out.println("Patient added successfully.");
    }

    private static void viewPatients() {
        if (patients.isEmpty()) {
            System.out.println("No patients found.");
            return;
        }

        System.out.println("\n----- Patient Records -----");
        for (Patient p : patients) {
            System.out.println("ID: " + p.patientId + " | Name: " + p.name + " | Age: " + p.age + " | Gender: " + p.gender);
        }
    }

    private static void addMedicineToPatient() {
        System.out.print("Enter patient ID: ");
        String patientId = scanner.nextLine().trim();

        Patient patient = findPatientById(patientId);
        if (patient == null) {
            System.out.println("Patient not found.");
            return;
        }

        System.out.print("Enter medicine name: ");
        String medName = scanner.nextLine().trim();

        System.out.print("Enter dosage (e.g. 1 tablet, 5 ml): ");
        String dosage = scanner.nextLine().trim();

        System.out.print("Enter frequency per day (e.g. 1, 2, 3): ");
        int frequency = readInt();

        List<LocalTime> times = new ArrayList<>();
        for (int i = 1; i <= frequency; i++) {
            System.out.print("Enter dosage time " + i + " in HH:mm format: ");
            String timeInput = scanner.nextLine().trim();
            try {
                times.add(LocalTime.parse(timeInput));
            } catch (Exception e) {
                System.out.println("Invalid time format. Medicine not added.");
                return;
            }
        }

        Medicine medicine = new Medicine(medName, dosage, frequency, times);
        patient.medicines.add(medicine);
        System.out.println("Medicine added successfully to patient " + patient.name);
    }

    private static void viewReminderList() {
        if (patients.isEmpty()) {
            System.out.println("No data available.");
            return;
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        System.out.println("\n----- Reminder List for Today -----");
        boolean found = false;

        for (Patient patient : patients) {
            for (Medicine medicine : patient.medicines) {
                for (LocalTime time : medicine.times) {
                    boolean taken = medicine.isDoseTaken(today, time);
                    String status;

                    if (taken) {
                        status = "TAKEN";
                    } else if (time.isBefore(now)) {
                        status = "MISSED/PENDING";
                    } else {
                        status = "UPCOMING";
                    }

                    System.out.println("Patient: " + patient.name +
                            " | Medicine: " + medicine.name +
                            " | Dosage: " + medicine.dosage +
                            " | Time: " + time +
                            " | Status: " + status);
                    found = true;
                }
            }
        }

        if (!found) {
            System.out.println("No reminders found.");
        }
    }

    private static void markDoseTaken() {
        System.out.print("Enter patient ID: ");
        String patientId = scanner.nextLine().trim();

        Patient patient = findPatientById(patientId);
        if (patient == null) {
            System.out.println("Patient not found.");
            return;
        }

        if (patient.medicines.isEmpty()) {
            System.out.println("No medicines assigned to this patient.");
            return;
        }

        System.out.println("\nMedicines:");
        for (int i = 0; i < patient.medicines.size(); i++) {
            System.out.println((i + 1) + ". " + patient.medicines.get(i).name);
        }

        System.out.print("Select medicine number: ");
        int medChoice = readInt();

        if (medChoice < 1 || medChoice > patient.medicines.size()) {
            System.out.println("Invalid selection.");
            return;
        }

        Medicine medicine = patient.medicines.get(medChoice - 1);

        System.out.println("Scheduled times:");
        for (int i = 0; i < medicine.times.size(); i++) {
            System.out.println((i + 1) + ". " + medicine.times.get(i));
        }

        System.out.print("Select time number: ");
        int timeChoice = readInt();

        if (timeChoice < 1 || timeChoice > medicine.times.size()) {
            System.out.println("Invalid time selection.");
            return;
        }

        LocalTime selectedTime = medicine.times.get(timeChoice - 1);
        medicine.markDoseTaken(LocalDate.now(), selectedTime);
        System.out.println("Dose marked as taken.");
    }

    private static void showMissedDoseReport() {
        if (patients.isEmpty()) {
            System.out.println("No data available.");
            return;
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        System.out.println("\n----- Missed-Dose Report -----");
        boolean found = false;

        for (Patient patient : patients) {
            for (Medicine medicine : patient.medicines) {
                for (LocalTime time : medicine.times) {
                    if (time.isBefore(now) && !medicine.isDoseTaken(today, time)) {
                        System.out.println("Patient: " + patient.name +
                                " | Medicine: " + medicine.name +
                                " | Dosage: " + medicine.dosage +
                                " | Missed Time: " + time);
                        found = true;
                    }
                }
            }
        }

        if (!found) {
            System.out.println("No missed doses for today.");
        }
    }

    private static void viewPatientMedicines() {
        System.out.print("Enter patient ID: ");
        String patientId = scanner.nextLine().trim();

        Patient patient = findPatientById(patientId);
        if (patient == null) {
            System.out.println("Patient not found.");
            return;
        }

        if (patient.medicines.isEmpty()) {
            System.out.println("No medicines found for this patient.");
            return;
        }

        System.out.println("\n----- Medicines for " + patient.name + " -----");
        for (Medicine medicine : patient.medicines) {
            System.out.println("Medicine: " + medicine.name);
            System.out.println("Dosage: " + medicine.dosage);
            System.out.println("Frequency per day: " + medicine.frequency);
            System.out.println("Times: " + medicine.times);
            System.out.println("----------------------------------");
        }
    }

    private static Patient findPatientById(String id) {
        for (Patient p : patients) {
            if (p.patientId.equalsIgnoreCase(id)) {
                return p;
            }
        }
        return null;
    }

    private static int readInt() {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (Exception e) {
                System.out.print("Enter a valid number: ");
            }
        }
    }

    private static void saveData() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            out.writeObject(patients);
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return;

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            patients = (List<Patient>) in.readObject();
        } catch (Exception e) {
            System.out.println("Could not load previous data.");
        }
    }

    static class Patient implements Serializable {
        String patientId;
        String name;
        int age;
        String gender;
        List<Medicine> medicines = new ArrayList<>();

        Patient(String patientId, String name, int age, String gender) {
            this.patientId = patientId;
            this.name = name;
            this.age = age;
            this.gender = gender;
        }
    }

    static class Medicine implements Serializable {
        String name;
        String dosage;
        int frequency;
        List<LocalTime> times;
        Map<String, Set<String>> takenDoses = new HashMap<>();

        Medicine(String name, String dosage, int frequency, List<LocalTime> times) {
            this.name = name;
            this.dosage = dosage;
            this.frequency = frequency;
            this.times = times;
        }

        void markDoseTaken(LocalDate date, LocalTime time) {
            String dateKey = date.toString();
            takenDoses.putIfAbsent(dateKey, new HashSet<>());
            takenDoses.get(dateKey).add(time.toString());
        }

        boolean isDoseTaken(LocalDate date, LocalTime time) {
            String dateKey = date.toString();
            return takenDoses.containsKey(dateKey) && takenDoses.get(dateKey).contains(time.toString());
        }
    }
}