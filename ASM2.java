import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private static final String FILE_NAME = "students.txt";
    private static final List<Student> students = new ArrayList<>();
    private static final Scanner scanner = new Scanner(System.in);

    // Student class
    static class Student {
        private String id;
        private String name;
        private double marks;

        public Student(String id, String name, double marks) {
            this.id = id;
            this.name = name;
            this.marks = validateMarks(marks);
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getMarks() {
            return marks;
        }

        public void setMarks(double marks) {
            this.marks = validateMarks(marks);
        }

        public String getRank() {
            if (marks < 5.0) return "Fail";
            if (marks < 6.5) return "Medium";
            if (marks < 7.5) return "Good";
            if (marks < 9.0) return "Very Good";
            return "Excellent";
        }

        @Override
        public String toString() {
            return id + "," + name + "," + marks;
        }

        public static Student fromString(String studentData) {
            String[] data = studentData.split(",");
            return new Student(data[0], data[1], Double.parseDouble(data[2]));
        }

        private double validateMarks(double marks) {
            while (marks < 0 || marks > 10) { // Nếu điểm không hợp lệ (nhỏ hơn 0 hoặc lớn hơn 10)
                System.out.println("Invalid marks. Please enter a number between 0 and 10.");
                marks = inputDouble("Enter valid marks (0-10): "); // Yêu cầu nhập lại điểm
            }
            return marks; // Trả về điểm hợp lệ
        }

    }

    // TimSort implementation
    static class TimSort {
        private static final int RUN = 32;

        public static void insertionSort(List<Student> students, int left, int right) {
            for (int i = left + 1; i <= right; i++) {
                Student temp = students.get(i);
                int j = i - 1;
                while (j >= left && students.get(j).getMarks() > temp.getMarks()) {
                    students.set(j + 1, students.get(j));
                    j--;
                }
                students.set(j + 1, temp);
            }
        }

        public static void merge(List<Student> students, int left, int mid, int right) {
            int len1 = mid - left + 1, len2 = right - mid;
            Student[] leftArray = new Student[len1];
            Student[] rightArray = new Student[len2];

            for (int i = 0; i < len1; i++) leftArray[i] = students.get(left + i);
            for (int i = 0; i < len2; i++) rightArray[i] = students.get(mid + 1 + i);

            int i = 0, j = 0, k = left;
            while (i < len1 && j < len2) {
                if (leftArray[i].getMarks() <= rightArray[j].getMarks()) {
                    students.set(k, leftArray[i]);
                    i++;
                } else {
                    students.set(k, rightArray[j]);
                    j++;
                }
                k++;
            }

            while (i < len1) students.set(k++, leftArray[i++]);
            while (j < len2) students.set(k++, rightArray[j++]);
        }

        public static void timSort(List<Student> students) {
            int n = students.size();
            for (int i = 0; i < n; i += RUN) {
                insertionSort(students, i, Math.min((i + RUN - 1), (n - 1)));
            }

            for (int size = RUN; size < n; size = 2 * size) {
                for (int left = 0; left < n; left += 2 * size) {
                    int mid = left + size - 1;
                    int right = Math.min((left + 2 * size - 1), (n - 1));

                    if (mid < right) {
                        merge(students, left, mid, right);
                    }
                }
            }
        }
    }

    // File handling methods
    public static void createFileIfNotExists() {
        try {
            File file = new File(FILE_NAME);
            if (file.createNewFile()) {
                System.out.println("File " + FILE_NAME + " created.");
            }
        } catch (IOException e) {
            System.out.println("Error creating the file: " + e.getMessage());
        }
    }

    public static void loadStudentsFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    students.add(Student.fromString(line));
                } catch (Exception e) {
                    System.out.println("Skipping invalid data: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    public static void saveStudentsToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Student student : students) {
                writer.write(student.toString() + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error writing file: " + e.getMessage());
        }
    }

    // Input validation methods
    private static String inputString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static double inputDouble(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }

    private static int inputInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }

    // Menu functionalities
    public static void addStudent() {
        String id = inputString("Enter Student ID: ");
        String name = inputString("Enter Student Name: ");
        double marks = inputDouble("Enter Student Marks (0-10): ");

        students.add(new Student(id, name, marks));
        saveStudentsToFile();
        System.out.println("Student added successfully!");
    }

    public static void editStudent() {
        String id = inputString("Enter Student ID to edit: ");
        students.stream().filter(student -> student.getId().equals(id)).findFirst().ifPresentOrElse(student -> {
            student.setName(inputString("Enter new Name: "));
            student.setMarks(inputDouble("Enter new Marks (0-10): "));
            saveStudentsToFile();
            System.out.println("Student updated successfully!");
        }, () -> System.out.println("Student not found!"));
    }

    public static void deleteStudent() {
        String id = inputString("Enter Student ID to delete: ");
        if (students.removeIf(student -> student.getId().equals(id))) {
            saveStudentsToFile();
            System.out.println("Student deleted successfully!");
        } else {
            System.out.println("Student not found!");
        }
    }

    public static void sortStudents() {
        if (students.isEmpty()) {
            System.out.println("No students to sort.");
            return;
        }
        TimSort.timSort(students);
        saveStudentsToFile();
        System.out.println("Students sorted by marks.");
        displayStudents();
    }

    public static void searchStudent() {
        String query = inputString("Enter Student ID or Name: ");
        List<Student> found = students.stream()
                .filter(s -> s.getId().equalsIgnoreCase(query) || s.getName().equalsIgnoreCase(query))
                .collect(Collectors.toList());
        if (found.isEmpty()) {
            System.out.println("No students found.");
        } else {
            found.forEach(System.out::println);
        }
    }

    public static void displayStudents() {
        if (students.isEmpty()) {
            System.out.println("No students to display.");
        } else {
            students.forEach(System.out::println);
        }
    }

    public static void displayStatistics() {
        if (students.isEmpty()) {
            System.out.println("No data for statistics.");
            return;
        }
        double avgMarks = students.stream().mapToDouble(Student::getMarks).average().orElse(0);
        System.out.println("Average Marks: " + avgMarks);
    }

    public static void main(String[] args) {
        createFileIfNotExists();
        loadStudentsFromFile();

        while (true) {
            System.out.println("\n1. Add Student\n" +
                    "2. Edit Student\n" +
                    "3. Delete Student\n" +
                    "4. Sort Students\n" +
                    "5. Search Student\n" +
                    "6. Display All\n" +
                    "7. Display Stats\n" +
                    "0. Exit");
            int choice = inputInt("Choose an option: ");
            switch (choice) {
                case 1 -> addStudent();
                case 2 -> editStudent();
                case 3 -> deleteStudent();
                case 4 -> sortStudents();
                case 5 -> searchStudent();
                case 6 -> displayStudents();
                case 7 -> displayStatistics();
                case 0 -> {
                    System.out.println("Exiting...");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }
}
