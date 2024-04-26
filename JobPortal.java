import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class User {
    private String username;
    private String password;
    private boolean isJobSeeker;

    public User(String username, String password, boolean isJobSeeker) {
        this.username = username;
        this.password = password;
        this.isJobSeeker = isJobSeeker;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isJobSeeker() {
        return isJobSeeker;
    }
}

class Job {
    private String title;
    private String description;
    private String postedBy;

    public Job(String title, String description, String postedBy) {
        this.title = title;
        this.description = description;
        this.postedBy = postedBy;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPostedBy() {
        return postedBy;
    }
}

public class JobPortal {
    private static final String USERS_FILE = "users.txt";
    private static final String JOBS_FILE = "jobs.txt";
    private static final String APPLICATIONS_FILE = "applications.txt";
    private static final String APPROVED_JOBS_FILE = "approved_jobs.txt";
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        boolean loggedIn = false;
        User currentUser = null;

        while (true) {
            if (!loggedIn) {
                System.out.println("1. Register");
                System.out.println("2. Login");
                System.out.println("3. Exit");
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        register();
                        break;
                    case 2:
                        currentUser = login();
                        loggedIn = (currentUser != null);
                        break;
                    case 3:
                        System.out.println("Exiting...");
                        System.exit(0);
                    default:
                        System.out.println("Invalid option");
                }
            } else {
                if (currentUser.isJobSeeker()) {
                    System.out.println("1. View Jobs");
                    System.out.println("2. Logout");
                    System.out.print("Choose an option: ");
                    int choice = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    switch (choice) {
                        case 1:
                            viewJobs();
                            break;
                        case 2:
                            loggedIn = false;
                            currentUser = null;
                            break;
                        default:
                            System.out.println("Invalid option");
                    }
                } else {
                    System.out.println("1. Post Job");
                    System.out.println("2. Approve or Reject Applications");
                    System.out.println("3. Logout");
                    System.out.print("Choose an option: ");
                    int choice = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    switch (choice) {
                        case 1:
                            postJob(currentUser);
                            break;
                        case 2:
                            approveOrRejectApplications(currentUser);
                            break;
                        case 3:
                            loggedIn = false;
                            currentUser = null;
                            break;
                        default:
                            System.out.println("Invalid option");
                    }
                }
            }
        }
    }

    private static void register() {
        System.out.println("Enter username:");
        String username = scanner.nextLine();
        System.out.println("Enter password:");
        String password = scanner.nextLine();
        System.out.println("Are you a job seeker? (y/n)");
        String input = scanner.nextLine();
        boolean isJobSeeker = input.equalsIgnoreCase("y");

        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE, true))) {
            writer.println(username + "," + password + "," + isJobSeeker);
            System.out.println("Registration successful!");
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static User login() {
        System.out.println("Enter username:");
        String username = scanner.nextLine();
        System.out.println("Enter password:");
        String password = scanner.nextLine();

        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(username) && parts[1].equals(password)) {
                    System.out.println("Login successful!");
                    return new User(username, password, Boolean.parseBoolean(parts[2]));
                }
            }
            System.out.println("Invalid username or password");
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return null;
    }

    private static void postJob(User user) {
        System.out.println("Enter job title:");
        String title = scanner.nextLine();
        System.out.println("Enter job description:");
        String description = scanner.nextLine();

        try (PrintWriter writer = new PrintWriter(new FileWriter(JOBS_FILE, true))) {
            writer.println(user.getUsername() + "," + title + "," + description);
            System.out.println("Job posted successfully!");
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void viewJobs() {
        List<Job> jobs = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(JOBS_FILE))) {
            String line;
            int i = 1;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                jobs.add(new Job(parts[1], parts[2], parts[0]));
                System.out.println(i + ". Title: " + parts[1]);
                System.out.println("   Description: " + parts[2]);
                System.out.println("   Posted By: " + parts[0]);
                i++;
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }

        System.out.println("Choose a job to apply (0 to go back):");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        if (choice > 0 && choice <= jobs.size()) {
            applyForJob(jobs.get(choice - 1));
        }
    }

    private static void applyForJob(Job job) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(APPLICATIONS_FILE, true))) {
            writer.println(job.getTitle() + "," + job.getPostedBy());
            System.out.println("Applied for job successfully!");
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void approveOrRejectApplications(User user) {
        List<Job> pendingApplications = new ArrayList<>();
        List<Job> approvedJobs = new ArrayList<>();

        // Read applications from the applications file
        try (BufferedReader reader = new BufferedReader(new FileReader(APPLICATIONS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3 && parts[1].equals(user.getUsername())) {
                    // Display pending applications for the current user
                    pendingApplications.add(new Job(parts[0], "", parts[2]));
                }
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }

        // Display pending applications
        if (!pendingApplications.isEmpty()) {
            System.out.println("Pending applications:");
            for (int i = 0; i < pendingApplications.size(); i++) {
                Job job = pendingApplications.get(i);
                System.out.println((i + 1) + ". Job Title: " + job.getTitle());
                System.out.println("   Applied By: " + job.getPostedBy());
            }

            System.out.println("Choose an application to approve or reject (0 to go back):");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            if (choice > 0 && choice <= pendingApplications.size()) {
                Job selectedJob = pendingApplications.get(choice - 1);
                System.out.println("Do you want to approve or reject the application for job '" + selectedJob.getTitle() + "'?");
                System.out.println("1. Approve");
                System.out.println("2. Reject");
                System.out.print("Enter your choice: ");
                int decision = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                if (decision == 1) {
                    // Move the approved job to the approved jobs file
                    approvedJobs.add(selectedJob);
                    removeJob(selectedJob.getTitle());
                    saveApprovedJobs(approvedJobs);
                    System.out.println("Application approved successfully!");
                } else if (decision == 2) {
                    System.out.println("Application rejected successfully!");
                } else {
                    System.out.println("Invalid choice.");
                }
            }
        } else {
            System.out.println("No pending applications to review.");
        }
    }

    private static void removeJob(String title) {
        // Remove the job from the jobs file
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(JOBS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (!parts[1].equals(title)) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(JOBS_FILE))) {
            for (String line : lines) {
                writer.println(line);
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void saveApprovedJobs(List<Job> approvedJobs) {
        // Save approved jobs to the approved jobs file
        try (PrintWriter writer = new PrintWriter(new FileWriter(APPROVED_JOBS_FILE, true))) {
            for (Job job : approvedJobs) {
                writer.println(job.getPostedBy() + "," + job.getTitle() + "," + job.getDescription());
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
